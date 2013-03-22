package edu.cmu.cs.ziy.courses.expir.treckba.collection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;

import edu.cmu.lti.oaqa.framework.CasUtils;
import edu.cmu.lti.oaqa.framework.ViewManager;
import edu.cmu.lti.oaqa.framework.ViewManager.ViewType;
import edu.cmu.lti.oaqa.framework.types.InputElement;
import edu.cmu.lti.oaqa.framework.types.OutputElement;

public class KbaGoldStandardDecorator extends JCasAnnotator_ImplBase {

  private static final String GSPATH_PROPERTY = "treckba-corpus.collection.gspath";

  public enum Relevance {
    RELEVANT, CENTRAL
  };

  private Map<String, Relevance> streamId2relevance = Maps.newHashMap();

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    String gsPath = System.getProperty(GSPATH_PROPERTY);
    if (gsPath == null) {
      System.err.printf("%s property not specified, using 'gspath' parameter from configuration\n",
              GSPATH_PROPERTY);
      gsPath = (String) context.getConfigParameterValue("gspath");
    }
    BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(
            gsPath)));
    try {
      for (String line : Collections2.filter(CharStreams.readLines(br),
              Predicates.containsPattern("^[^#]"))) {
        String[] segs = line.split("\t");
        String relevance = segs[5];
        if (!relevance.equals("1") && !relevance.equals("2")) {
          continue;
        }
        streamId2relevance.put(segs[2], relevance.equals("1") ? Relevance.RELEVANT
                : Relevance.CENTRAL);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException {
    JCas gsView;
    try {
      gsView = ViewManager.getOrCreateView(jcas, ViewType.DOCUMENT_GS);
    } catch (CASException e) {
      throw new AnalysisEngineProcessException(e);
    }
    InputElement input = (InputElement) CasUtils.getFirst(jcas, InputElement.class.getName());
    String sequenceId = input.getSequenceId();
    if (streamId2relevance.containsKey(sequenceId)) {
      OutputElement output = new OutputElement(gsView);
      output.setSequenceId(sequenceId);
      output.setAnswer(streamId2relevance.get(sequenceId).name());
      output.addToIndexes(gsView);
    }
  }

}
