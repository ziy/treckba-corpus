package edu.cmu.cs.ziy.courses.expir.treckba.filter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;

import edu.cmu.cs.ziy.util.PhraseCollectionMentionChecker;

public class GoogleDistanceExpandedKeytermStreamFilter extends AbstractStreamFilter {

  private Boolean lowerCase;

  private PhraseCollectionMentionChecker checker;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    lowerCase = (Boolean) context.getConfigParameterValue("lowercase");
    HashSet<String> categories = Sets.newHashSet((String[]) context
            .getConfigParameterValue("categories"));
    String expandedKeytermFile = (String) context.getConfigParameterValue("expanded-keyterm-file");
    String accumulatedBingDistanceFile = (String) context
            .getConfigParameterValue("accumulated-google-distance-file");
    int column = (Integer) context.getConfigParameterValue("column");
    float threshold = (Float) context.getConfigParameterValue("threshold");

    Set<String> keyterms = Sets.newHashSet();
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(
              expandedKeytermFile)));
      List<String> lines = CharStreams.readLines(br);
      for (String line : lines) {
        String[] fields = line.split("\t");
        if (fields.length < 4) {
          continue;
        }
        String category = fields[2];
        if (!categories.contains(category)) {
          continue;
        }
        String keyterm = fields[0];
        keyterm = keyterm.replaceAll("Category:", "");
        keyterm = keyterm.replaceAll("List of ", "");
        keyterm = keyterm.replaceAll("\\s*\\(.*?\\)\\s*", "");
        keyterms.add(lowerCase ? keyterm.toLowerCase() : keyterm);
      }
      br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(
              accumulatedBingDistanceFile)));
      lines = CharStreams.readLines(br);
      for (String line : lines) {
        String[] fields = line.split("\t");
        if (Float.parseFloat(fields[column]) > threshold) {
          keyterms.remove(lowerCase ? fields[0].toLowerCase() : fields[0]);
        }
      }
      checker = new PhraseCollectionMentionChecker(keyterms, Splitter
              .on(CharMatcher.anyOf("\" ();,.'[]{}!?:”“…\n\r\t]")).trimResults().omitEmptyStrings());
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  protected boolean isKept(String body) {
    Set<String> phrases = checker.checkDocument(lowerCase ? body.toLowerCase() : body);
    if (phrases != null && !phrases.isEmpty()) {
      System.out.println("{{Target}} " + phrases);
      return true;
    } else {
      return false;
    }
  }

}
