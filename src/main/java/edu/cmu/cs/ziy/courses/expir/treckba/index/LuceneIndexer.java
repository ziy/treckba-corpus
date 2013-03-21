package edu.cmu.cs.ziy.courses.expir.treckba.index;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasConsumer_ImplBase;

import edu.cmu.lti.oaqa.framework.types.OutputElement;

public class LuceneIndexer extends JCasConsumer_ImplBase {

  private static final String ROOT_PROPERTY = "treckba-corpus.index.root";

  private static final String DIR_PROPERTY = "treckba-corpus.index.dir";
  
  private File fullDir;

  private IndexWriter writer;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    String root = System.getProperty(ROOT_PROPERTY);
    if (root == null) {
      System.err.printf(
              "%s property not specified, using 'root' parameter from configuration\n",
              ROOT_PROPERTY);
      root = (String) context.getConfigParameterValue("root");
    }
    String dir = System.getProperty(DIR_PROPERTY);
    if (dir == null) {
      System.err.printf("%s property not specified, using 'dir' parameter from configuration\n",
              DIR_PROPERTY);
      dir = (String) context.getConfigParameterValue("dir");
    }
    fullDir = new File(new File(root), dir);
    try {
      if (fullDir.exists()) {
        FileUtils.deleteDirectory(fullDir);
      } else {
        fullDir.mkdirs();
      }
      Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_42);
      IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_42, analyzer);
      iwc.setOpenMode(OpenMode.CREATE);
      writer = new IndexWriter(FSDirectory.open(fullDir), iwc);
    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException {
    AnnotationIndex<Annotation> annotations = jcas.getAnnotationIndex(OutputElement.type);
    if (annotations.size() < 1) {
      return;
    }
    OutputElement input = (OutputElement) annotations.iterator().next();
    Document doc = new Document();
    doc.add(new StringField("stream-id", input.getSequenceId(), Field.Store.YES));
    doc.add(new TextField("body", input.getAnswer(), Field.Store.YES));
    // doc.add(new TextField("body-index", input.getQuestion(), Field.Store.NO));
    // doc.add(new StoredField("body-store", CompressionTools.compressString(input.getQuestion(),
    // Deflater.BEST_COMPRESSION)));
    try {
      writer.addDocument(doc);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  @Override
  public void collectionProcessComplete() throws AnalysisEngineProcessException {
    super.collectionProcessComplete();
    try {
      writer.close();
      System.out.println("Index folder size: " + FileUtils.sizeOfDirectory(fullDir));
    } catch (IOException e) {
      throw new AnalysisEngineProcessException(e);
    }
  }

}
