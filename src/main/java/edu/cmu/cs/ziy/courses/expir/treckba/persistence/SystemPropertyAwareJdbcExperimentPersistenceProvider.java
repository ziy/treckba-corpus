package edu.cmu.cs.ziy.courses.expir.treckba.persistence;

import java.util.Map;

import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import edu.cmu.lti.oaqa.cse.driver.impl.JdbcExperimentPersistenceProvider;

public class SystemPropertyAwareJdbcExperimentPersistenceProvider extends
        JdbcExperimentPersistenceProvider {

  private static final String URL_PROPERTY = "treckba-corpus.persistence-provider.url";

  private static final String USERNAME_PROPERTY = "treckba-corpus.persistence-provider.username";

  private static final String PASSWORD_PROPERTY = "treckba-corpus.persistence-provider.password";

  private static final String DRIVER_PROPERTY = "treckba-corpus.persistence-provider.drive";

  @Override
  public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> tuples)
          throws ResourceInitializationException {
    String url = System.getProperty(URL_PROPERTY);
    if (url != null) {
      tuples.put("url", url);
    }
    String username = System.getProperty(USERNAME_PROPERTY);
    if (username != null) {
      tuples.put("username", username);
    }
    String password = System.getProperty(PASSWORD_PROPERTY);
    if (password != null) {
      tuples.put("password", password);
    }
    String driver = System.getProperty(DRIVER_PROPERTY);
    if (driver != null) {
      tuples.put("driver", driver);
    }
    return super.initialize(aSpecifier, tuples);
  }

}
