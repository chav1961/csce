package chav1961.csce.project;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import chav1961.csce.Application;
import chav1961.csce.project.ProjectNavigator.ItemType;
import chav1961.csce.project.ProjectNavigator.ProjectNavigatorItem;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;

public class ProjectFileSystemTest {
	private static ContentMetadataInterface	mdi;

	@BeforeClass
	public static void prepare() throws IOException {
		try(final InputStream		is = Application.class.getResourceAsStream("application.xml")) {
			
			mdi = ContentModelFactory.forXmlDescription(is);
		}
	}
	
	@Test
	public void basicTest() throws IOException {
		final ProjectContainer	pc = new ProjectContainer(()->PureLibSettings.PURELIB_LOCALIZER, mdi);

		try(final OutputStream	os = pc.fromOutputStream()) {
			Utils.copyStream(new ByteArrayInputStream(new byte[0]), os);
		}
		
		try(final ProjectFileSystem	pfs = new ProjectFileSystem(pc)) {
			Assert.assertEquals("/", pfs.getPath());
			Assert.assertEquals("/", pfs.getName());
			
			pfs.open("test");
			
			Assert.assertEquals("/test", pfs.getPath());
			Assert.assertEquals("test", pfs.getName());
			Assert.assertFalse(pfs.exists());
			
			pfs.mkDir();
			Assert.assertTrue(pfs.exists());
			Assert.assertTrue(pfs.isDirectory());

			try(final FileSystemInterface	child = pfs.clone()) {
				final long		unique = pc.getProjectNavigator().getUniqueId();
				final String	fileName = ItemType.CreoleRef.getPartNamePrefix()+unique+".cre";
				
				child.open("/"+fileName);
				
				Assert.assertFalse(child.exists());
				child.create();
				Assert.assertTrue(child.exists());
				Assert.assertFalse(child.isDirectory());
				Assert.assertEquals(0, child.size());
				
				try(final Reader	rdr = new StringReader("content");
					final Writer	wr = child.charWrite()) {
				
					Utils.copyStream(rdr, wr);
				}
				Assert.assertEquals("content".length(), child.size());
	
				Assert.assertArrayEquals(new String[] {fileName}, pfs.list());
				
				try(final Reader	rdr = child.charRead();
					final Writer	wr = new StringWriter()) {
				
					Utils.copyStream(rdr, wr);
					Assert.assertEquals("content", wr.toString());
				}
				Assert.assertEquals(fileName, ((ProjectNavigatorItem)child.getAttributes().get(ProjectFileSystem.NAVIGATION_NODE)).name);
				
				child.delete();
				Assert.assertFalse(child.exists());

				Assert.assertArrayEquals(new String[0], pfs.list());
			}
		}
	}
}
