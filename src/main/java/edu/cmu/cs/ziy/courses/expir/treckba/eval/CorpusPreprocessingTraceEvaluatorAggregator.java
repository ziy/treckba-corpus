package edu.cmu.cs.ziy.courses.expir.treckba.eval;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;

import edu.cmu.lti.oaqa.framework.ViewManager;
import edu.cmu.lti.oaqa.framework.ViewManager.ViewType;
import edu.cmu.lti.oaqa.framework.eval.retrieval.RetrievalEvalConsumer;
import edu.cmu.lti.oaqa.framework.types.OutputElement;

public class CorpusPreprocessingTraceEvaluatorAggregator extends RetrievalEvalConsumer<OutputElement> {

  @Override
  protected Ordering<OutputElement> getOrdering() {
    return new Ordering<OutputElement>() {

      @Override
      public int compare(OutputElement left, OutputElement right) {
        return left.getSequenceId().compareTo(right.getSequenceId());
      }

    }.reverse();
  }

  @Override
  protected Function<OutputElement, String> getToIdStringFct() {
    return new Function<OutputElement, String>() {

      @Override
      public String apply(OutputElement input) {
        return input.getSequenceId();
      }
    };
  }

  @Override
  protected List<OutputElement> getGoldStandard(JCas jcas) throws CASException {
    return getAnnotations(ViewManager.getOrCreateView(jcas, ViewType.DOCUMENT_GS),
            OutputElement.type);
  }

  @Override
  protected List<OutputElement> getResults(JCas jcas) throws CASException {
    return getAnnotations(ViewManager.getOrCreateView(jcas, ViewType.DOCUMENT), OutputElement.type);
  }

  @SuppressWarnings("unchecked")
  private static <T extends Annotation> List<T> getAnnotations(JCas jcas, int type) {
    List<T> annotations = new ArrayList<T>();
    for (Annotation annotation : jcas.getAnnotationIndex(type)) {
      if (annotation.getTypeIndexID() != type) {
        continue;
      }
      annotations.add((T) annotation);
    }
    return annotations;
  }

}
