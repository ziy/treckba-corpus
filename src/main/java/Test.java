import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.thrift.TException;

public class Test {

  public static void main(String[] args) throws TException, IOException {
    Pattern pattern = Pattern.compile("^[^#]");
    Matcher matcher = pattern.matcher("#ab");
    System.out.println(matcher.find());
    matcher = pattern.matcher("ab");
    System.out.println(matcher.find());
  }
}
