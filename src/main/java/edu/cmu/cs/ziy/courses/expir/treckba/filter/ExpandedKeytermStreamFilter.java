package edu.cmu.cs.ziy.courses.expir.treckba.filter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;

public class ExpandedKeytermStreamFilter extends AbstractStreamFilter {

  private Set<String> keyterms = Sets.newHashSet();

  private Boolean lowerCase;

  private Set<String> categories;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    lowerCase = (Boolean) context.getConfigParameterValue("lowercase");
    categories = Sets.newHashSet((String[]) context.getConfigParameterValue("categories"));
    try {
      String file = (String) context.getConfigParameterValue("file");
      BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(
              file)));
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
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  protected boolean isKept(String body) {
    Set<String> tokens = Sets.newHashSet((lowerCase ? body.toLowerCase() : body).split("\\s+"));
    if (Sets.intersection(keyterms, tokens).size() > 0) {
      System.out.println("{{Target}} " + Sets.intersection(keyterms, tokens));
      return true;
    } else {
      return false;
    }
  }

}
