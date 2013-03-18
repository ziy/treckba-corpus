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

  private Set<String> keyterms;

  private Boolean lowerCase;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    lowerCase = (Boolean) context.getConfigParameterValue("lowercase");
    try {
      String file = (String) context.getConfigParameterValue("file");
      BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(
              file)));
      List<String> lines = CharStreams.readLines(br);
      keyterms = Sets.newHashSet();
      for (String line : lines) {
        String keyterm = line.split("\t", 2)[0];
        keyterm = keyterm.replaceAll("Category:", "");
        keyterm = keyterm.replaceAll("\\s*\\(.*?\\)\\s*", "");
        if (lowerCase) {
          keyterm = keyterm.toLowerCase();
        }
        keyterms.add(keyterm);
      }
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  protected boolean isKept(String body) {
    Set<String> tokens = Sets.newHashSet(body.split("\\s+"));
    if (Sets.intersection(keyterms, tokens).size() > 0) {
      return true;
    } else {
      return false;
    }
  }

}
