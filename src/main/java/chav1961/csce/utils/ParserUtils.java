package chav1961.csce.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import chav1961.csce.project.ProjectContainer;
import chav1961.purelib.i18n.interfaces.SupportedLanguages;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;

/*
package chav1961.bt.lucenewrapper;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.fsys.FileSystemOnFile;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
 
public class LuceneHelloWorld {
 
 public static void main(String[] args) throws IOException, ParseException {
	 final File 	f = new File(new File(System.getProperty("java.io.tmpdir")),"inside");
	 
	 f.mkdirs(); 
	 
	 try(final FileSystemInterface	fsi = new FileSystemOnFile(f.toURI())) {
		 try(final Directory directory = new LuceneFileSystemWrapperDirectory(fsi)) {
			 
			 StandardAnalyzer standardAnalyzer = new StandardAnalyzer();
			 
			 IndexWriterConfig config = new IndexWriterConfig(standardAnalyzer); 
			 //Create a writer
			 try(IndexWriter writer = new IndexWriter(directory, config)) {
				 Document document = new Document ();
				 //In a real world example, content would be the actual content that needs to be indexed.
				 //Setting content to Hello World as an example.
				 document.add(new TextField("content", "Hello World", Field.Store.YES));
				 writer.addDocument(document);
				 document.add(new TextField("content", "Hello people", Field.Store.YES));
				 writer.addDocument(document); 
			 }
			  
			 //Now let's try to search for Hello
			 try(IndexReader reader = DirectoryReader.open(directory)) {
				 IndexSearcher searcher = new IndexSearcher (reader);
				 QueryParser parser = new QueryParser ("content", standardAnalyzer);
				 Query query = parser.parse("Hello");
				 TopDocs results = searcher.search(query, 5);
				 System.out.println("Hits for Hello -->" + results.totalHits);
				 
				 //case insensitive search
				 query = parser.parse("hello");
				 results = searcher.search(query, 5);
				 System.out.println("Hits for hello -->" + results.totalHits);
				 
				 //search for a value not indexed
				 query = parser.parse("Hi there");
				 results = searcher.search(query, 5);
				 System.out.println("Hits for Hi there -->" + results.totalHits);
			 }
		 }
	 	fsi.deleteAll();
	 }
	 }
}

*/
public class ParserUtils {
	public static void parseProjectContent(final ProjectContainer container, final SupportedLanguages lang, final ZipOutputStream zos) throws IOException {
		if (container == null) {
			throw new NullPointerException("Project container can't be null");
		}
		else if (lang == null) {
			throw new NullPointerException("Supported language can't be null");
		}
		else if (zos == null) {
			throw new NullPointerException("ZIP output stream can't be null");
		}
		else {
			for (String item : container.getPartNames()) {
				final ZipEntry	ze = new ZipEntry(item);
				
				ze.setMethod(ZipEntry.DEFLATED);
				zos.putNextEntry(ze);
				switch (lang) {
					case en		:
						parseProjectContent(item, container.getProjectPartContent(item), new EnglishAnalyzer(), zos);
						break;
					case ru		:
						parseProjectContent(item, container.getProjectPartContent(item), new RussianAnalyzer(), zos);
						break;
					default	:
						throw new UnsupportedOperationException("Language ["+lang+"] is not supported yet");
				}
				zos.closeEntry();
			}
		}
	}

	private static <T> void parseProjectContent(final String part, final T content, final Analyzer analyzer, final OutputStream os) throws IOException {
		// TODO Auto-generated method stub
		try(final TokenStream ts = analyzer.tokenStream(part, content.toString())) {
			
		}
	}
}
