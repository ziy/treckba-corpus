import java.io.IOException;

import org.apache.thrift.TException;

public class Test {

  public static void main(String[] args) throws TException, IOException {

    String keyterm = "Aharon (given name)";
    keyterm = keyterm.replaceAll("Category:", "");
    keyterm = keyterm.replaceAll("\\s*\\(.*?\\)\\s*", "");
    System.out.println(keyterm);
  }
}
