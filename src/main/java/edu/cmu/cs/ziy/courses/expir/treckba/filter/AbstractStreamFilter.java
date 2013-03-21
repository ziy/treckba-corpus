package edu.cmu.cs.ziy.courses.expir.treckba.filter;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.cs.ziy.courses.expir.treckba.log.PreprocessingIndexingLogEntry;
import edu.cmu.lti.oaqa.ecd.log.AbstractLoggedComponent;
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
    AnnotationIndex<Annotation> annotations = jcas.getAnnotationIndex(InputElement.type);
    assert annotations.size() == 1;
    InputElement input = (InputElement) annotations.iterator().next();
    String body = input.getQuestion();
    if (body != null && isKept(body)) {
      outputSize++;
      OutputElement output = new OutputElement(jcas);
      output.setSequenceId(input.getSequenceId());
      output.setAnswer(body);
      output.addToIndexes(jcas);
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
