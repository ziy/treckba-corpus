package edu.cmu.cs.ziy.courses.expir.treckba.filter;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.cs.ziy.courses.expir.treckba.log.PreprocessingIndexingLogEntry;
import edu.cmu.lti.oaqa.ecd.log.AbstractLoggedComponent;
import edu.cmu.lti.oaqa.framework.CasUtils;
import edu.cmu.lti.oaqa.framework.ViewManager;
import edu.cmu.lti.oaqa.framework.ViewManager.ViewType;
import edu.cmu.lti.oaqa.framework.types.InputElement;
import edu.cmu.lti.oaqa.framework.types.OutputElement;

public abstract class AbstractStreamFilter extends AbstractLoggedComponent {

  private int inputSize;

  private int outputSize;

  protected abstract boolean isKept(String body);

  @Override
  public void initialize(UimaContext c) throws ResourceInitializationException {
    super.initialize(c);
  }

  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException {
    inputSize++;
    JCas documentView;
    try {
      documentView = ViewManager.getOrCreateView(jcas, ViewType.DOCUMENT);
    } catch (CASException e) {
      throw new AnalysisEngineProcessException(e);
    }
    InputElement input = (InputElement) CasUtils.getFirst(jcas, InputElement.class.getName());
    String body = input.getQuestion();
    if (body != null && isKept(body)) {
      outputSize++;
      OutputElement output = new OutputElement(documentView);
      output.setSequenceId(input.getSequenceId());
      output.setAnswer(body);
      output.addToIndexes(documentView);
    }
    // System.out.println(inputSize + " -> " + outputSize);
  }

  @Override
  public void collectionProcessComplete() throws AnalysisEngineProcessException {
    super.collectionProcessComplete();
    log(PreprocessingIndexingLogEntry.FILTER, inputSize + " -> " + outputSize
            + " have been selected.");
  }
}
