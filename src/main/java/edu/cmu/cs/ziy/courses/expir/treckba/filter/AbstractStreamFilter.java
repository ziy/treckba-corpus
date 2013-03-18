package edu.cmu.cs.ziy.courses.expir.treckba.filter;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import edu.cmu.lti.oaqa.ecd.log.AbstractLoggedComponent;
import edu.cmu.lti.oaqa.framework.types.InputElement;

public abstract class AbstractStreamFilter extends AbstractLoggedComponent {

  private int inputSize;

  private int outputSize;

  protected abstract boolean isKept(String body);

  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException {
    inputSize++;
    AnnotationIndex<Annotation> annotations = jcas.getAnnotationIndex(InputElement.type);
    assert annotations.size() == 1;
    InputElement input = (InputElement) annotations.iterator().next();
    String body = input.getQuestion();
    if (body != null && isKept(body)) {
      outputSize++;
    } else {
      input.removeFromIndexes();
    }
    System.out.println(inputSize + " -> " + outputSize);
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    log(null, inputSize + " -> " + outputSize + " have been selected.");
  }
}
