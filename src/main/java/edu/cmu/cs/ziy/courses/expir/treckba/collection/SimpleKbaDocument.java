package edu.cmu.cs.ziy.courses.expir.treckba.collection;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

import kba.StreamItem;

public class SimpleKbaDocument {

  private String streamId;

  private String source;

  private String body;

  public SimpleKbaDocument(StreamItem doc) {
    streamId = doc.stream_id;
    source = doc.source;
    // source = doc.source;
    if (doc.body.cleansed != null && doc.body.encoding != null) {
      Charset charset = null;
      try {
        charset = Charset.forName(doc.body.encoding);
      } catch (UnsupportedCharsetException e) {
        charset = Charset.defaultCharset();
      } catch (IllegalCharsetNameException e) {
        charset = Charset.defaultCharset();
      }
      body = charset.decode(doc.body.cleansed).toString();
      body = stripNonValidXMLCharacters(body);
    }
  }

  public static String stripNonValidXMLCharacters(String in) {
    StringBuffer out = new StringBuffer(); // Used to hold the output.
    char current; // Used to reference the current character.

    if (in == null || ("".equals(in)))
      return ""; // vacancy test.
    for (int i = 0; i < in.length(); i++) {
      current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not
                              // happen.
      if ((current == 0x9) || (current == 0xA) || (current == 0xD)
              || ((current >= 0x20) && (current <= 0xD7FF))
              || ((current >= 0xE000) && (current <= 0xFFFD))
              || ((current >= 0x10000) && (current <= 0x10FFFF)))
        out.append(current);
    }
    return out.toString();
  }

  public String getStreamId() {
    return streamId;
  }

  public String getSource() {
    return source;
  }

  public String getBody() {
    return body;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((streamId == null) ? 0 : streamId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SimpleKbaDocument other = (SimpleKbaDocument) obj;
    if (streamId == null) {
      if (other.streamId != null)
        return false;
    } else if (!streamId.equals(other.streamId))
      return false;
    return true;
  }

}
