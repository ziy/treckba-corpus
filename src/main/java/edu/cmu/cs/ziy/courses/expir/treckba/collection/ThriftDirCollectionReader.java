package edu.cmu.cs.ziy.courses.expir.treckba.collection;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

import kba.StreamItem;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.Objects;
import com.google.common.io.PatternFilenameFilter;

import edu.cmu.lti.oaqa.framework.DataElement;
import edu.cmu.lti.oaqa.framework.collection.IterableCollectionReader;

public final class ThriftDirCollectionReader extends IterableCollectionReader {

  private static final String ROOT_PROPERTY = "treckba-corpus.collection.root";

  private static final String DIR_PROPERTY = "treckba-corpus.collection.dir";

  @Override
  protected Iterator<DataElement> getInputSet() throws ResourceInitializationException {
    String root = Objects.firstNonNull(System.getProperty(ROOT_PROPERTY),
            (String) getConfigParameterValue("root"));
    String dir = Objects.firstNonNull(System.getProperty(DIR_PROPERTY),
            dir = (String) getConfigParameterValue("dir"));
    return new KbaThriftElementIterator(new File(new File(root), dir));
  }

  private final class KbaThriftElementIterator implements Iterator<DataElement> {

    private int ptr;

    private boolean nextCalled = true;

    private File[] files;

    private StreamItem item = new StreamItem();

    private TProtocol protocol;

    public KbaThriftElementIterator(File dir) {
      this.ptr = -1;
      this.files = dir.listFiles(new PatternFilenameFilter(".*\\.gz"));
    }

    @Override
    public boolean hasNext() {
      // avoid multiple calls of hasNext() without calling next() so that the pointer is keeping
      // forwarding.
      if (!nextCalled) {
        return item != null;
      }
      nextCalled = false;
      do {
        try {
          item.read(protocol);
          return true;
        } catch (Exception e) {
          ptr++;
          if (ptr >= files.length) {
            item = null;
            return false;
          }
          TIOStreamTransport transport;
          try {
            transport = new TIOStreamTransport(new BufferedInputStream(new GZIPInputStream(
                    new FileInputStream(files[ptr]))));
            transport.open();
            protocol = new TBinaryProtocol(transport);
          } catch (Exception e1) {
          }
        }
      } while (true);
    }

    @Override
    public DataElement next() {
      if (nextCalled) {
        hasNext();
      }
      nextCalled = true;
      SimpleKbaDocument doc = new SimpleKbaDocument(item);
      return new DataElement(null, doc.getStreamId(), doc.getBody(), doc.getSource());
    }

    @Override
    public void remove() {
    }

  }
}
