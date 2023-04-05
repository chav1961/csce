package chav1961.csce.project;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import chav1961.csce.Application;
import chav1961.csce.project.ProjectChangeEvent.ProjectChangeType;
import chav1961.csce.project.ProjectNavigator.ItemType;
import chav1961.csce.project.ProjectNavigator.ProjectNavigatorItem;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.i18n.interfaces.MutableLocalizedString;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;

public class ProjectContainerTest {
	private static ContentMetadataInterface	mdi;

	@BeforeClass
	public static void prepare() throws IOException {
		try(final InputStream		is = Application.class.getResourceAsStream("application.xml")) {
			
			mdi = ContentModelFactory.forXmlDescription(is);
		}
	}
	
	@Test
	public void basicTest() {
		final ProjectContainer	pc = new ProjectContainer(()->PureLibSettings.PURELIB_LOCALIZER, mdi);
		
		Assert.assertEquals("", pc.getProjectFileName());
		pc.setProjectFileName("new");
		Assert.assertEquals("new", pc.getProjectFileName());

		try{pc.setProjectFileName(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{pc.setProjectFileName("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{new ProjectContainer(null, mdi);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new ProjectContainer(()->PureLibSettings.PURELIB_LOCALIZER, null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Test
	public void newProjectTest() throws IOException, ContentException {
		final ProjectContainer	pc = new ProjectContainer(()->PureLibSettings.PURELIB_LOCALIZER, mdi);

		try{pc.getLocalizer();
			Assert.fail("Mandatory exception was not detected (project is not prepared)");
		} catch (IllegalStateException exc) {
		}
		
		try(final OutputStream	os = pc.fromOutputStream()) {
			Utils.copyStream(new ByteArrayInputStream(new byte[0]), os);
		}
		Assert.assertNotNull(pc.getLocalizer());
		Assert.assertArrayEquals(new String[0], pc.getPartNames());

		// Test project properties
		Assert.assertEquals(ProjectContainer.DEFAULT_PROJECT_VERSION, pc.getProperties().getProperty(ProjectContainer.PROJECT_VERSION));
		
		// Test localizer
		final String localizedId = pc.createUniqueLocalizationString();

		Assert.assertNotNull(localizedId);

		final MutableLocalizedString	mls = pc.getLocalizationString(localizedId);

		Assert.assertNotNull(mls);
 
		try{pc.getLocalizationString(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{pc.getLocalizationString("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		// Test project navigator and project parts
		final ProjectNavigator		nav = pc.getProjectNavigator();
		final ProjectNavigatorItem	root = nav.getRoot();

		Assert.assertEquals(-1, nav.getUniqueId()-nav.getUniqueId());

		Assert.assertEquals(ItemType.Root, root.type);
		Assert.assertEquals(-1, root.parent);
		Assert.assertEquals(0, nav.getChildren(root.id).length);
		
		final long					unique = nav.getUniqueId();
		final String				partName = "Creole"+unique+".cre";
		final ProjectNavigatorItem	pni = new ProjectNavigatorItem(unique, root.id, partName, ItemType.CreoleRef, "test", localizedId, partName);

		try{pc.getPartNameById(unique);
			Assert.fail("Mandatory exception was not detected (unknown 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{pc.getIdByPartName(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{pc.getIdByPartName("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{pc.getIdByPartName("unknown");
			Assert.fail("Mandatory exception was not detected (unsupported template of 1-st argument)");
		} catch (IllegalArgumentException exc) {
		} 
		try{pc.getIdByPartName("Creole"+unique+".cre");
			Assert.fail("Mandatory exception was not detected (non-existent 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		nav.addItem(pni);
		Assert.assertEquals("Creole"+unique+".cre", pc.getPartNameById(unique));
		Assert.assertEquals(unique, pc.getIdByPartName("Creole"+unique+".cre"));
		
		try{nav.addItem(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{nav.addItem(pni); 
			Assert.fail("Mandatory exception was not detected (duplicated 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{pc.addProjectPartContent(partName, null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}

		Assert.assertFalse(pc.hasProjectPart(partName));
		
		try{pc.hasProjectPart(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{pc.hasProjectPart("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}

		pc.addProjectPartContent(partName, "content");

		Assert.assertTrue(pc.hasProjectPart(partName));
		
		try{pc.addProjectPartContent(null, "content");
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{pc.addProjectPartContent("", "content");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{pc.addProjectPartContent(partName, "content");
			Assert.fail("Mandatory exception was not detected (duplicated 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		Assert.assertArrayEquals(new String[] {partName}, pc.getPartNames());
		Assert.assertEquals("content", pc.getProjectPartContent(partName));

		try{pc.getProjectPartContent(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{pc.getProjectPartContent("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{pc.getProjectPartContent("unknown");
			Assert.fail("Mandatory exception was not detected (non-existent 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		pc.setProjectPartContent(partName, "content 2");
		Assert.assertEquals("content 2", pc.getProjectPartContent(partName));
		
		try{pc.setProjectPartContent(null, "content 2");
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{pc.setProjectPartContent("", "content 2");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{pc.setProjectPartContent("unknown", "content 2");
			Assert.fail("Mandatory exception was not detected (non-existent 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{pc.setProjectPartContent(partName, null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		
		pc.removeProjectPartContent(partName);
		Assert.assertArrayEquals(new String[0], pc.getPartNames());

		try{pc.removeProjectPartContent(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{pc.removeProjectPartContent("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{pc.removeProjectPartContent(partName);
			Assert.fail("Mandatory exception was not detected (non-existent 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		int[]	count = {0};
		
		nav.walkDown((m,n)->{count[0]++; return ContinueMode.CONTINUE;});
		Assert.assertEquals(4, count[0]);
		
		nav.removeItem(unique);
		
		try{nav.removeItem(unique);
			Assert.fail("Mandatory exception was not detected (item is missing)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void serializationTest() throws IOException, ContentException {
		final ProjectContainer	pc = new ProjectContainer(()->PureLibSettings.PURELIB_LOCALIZER, mdi);

		try(final OutputStream	os = pc.fromOutputStream()) {
			Utils.copyStream(new ByteArrayInputStream(new byte[0]), os);
		}
		
		final String localizedId = pc.createUniqueLocalizationString();
		
		final ProjectNavigator		nav = pc.getProjectNavigator();
		final ProjectNavigatorItem	root = nav.getRoot();
		final long					unique = nav.getUniqueId();
		final String				partName = "Creole"+unique+".cre";
		final ProjectNavigatorItem	pni = new ProjectNavigatorItem(unique, root.id, partName, ItemType.CreoleRef, "test", localizedId, partName);
		
		nav.addItem(pni);
		pc.addProjectPartContent(partName, "content");
		
		final ProjectContainer	pcNew = new ProjectContainer(()->PureLibSettings.PURELIB_LOCALIZER, mdi);
		
		try(final InputStream	is = pc.toInputStream(); 
			final OutputStream	os = pcNew.fromOutputStream()) {
			
			Utils.copyStream(is, os);
		}

		final ProjectNavigator			navNew = pcNew.getProjectNavigator();
		final MutableLocalizedString	mls = pcNew.getLocalizationString(localizedId);

		int[]	count = {0};
		
		navNew.walkDown((m,n)->{count[0]++; return ContinueMode.CONTINUE;});
		Assert.assertEquals(4, count[0]);
		Assert.assertArrayEquals(new String[] {partName}, pcNew.getPartNames());
		Assert.assertEquals("content", pcNew.getProjectPartContent(partName));
	}

	@Test
	public void contentTypeTest() throws IOException, ContentException {
		final ProjectContainer			pc = new ProjectContainer(()->PureLibSettings.PURELIB_LOCALIZER, mdi);
		final File						temp = File.createTempFile("test", ".pdf");
		final List<ProjectChangeEvent>	list = new ArrayList<>();
		final ProjectChangeListener		pcl = (e)->list.add(e); 

		try(final OutputStream	os = pc.fromOutputStream()) {
			Utils.copyStream(new ByteArrayInputStream(new byte[0]), os);
		}
		
		final ProjectNavigator		nav = pc.getProjectNavigator();
		final ProjectNavigatorItem	root = nav.getRoot();

		pc.addProjectChangeListener(pcl);
		
		try {pc.addProjectChangeListener(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		try(final InputStream	is = this.getClass().getResourceAsStream("creole.cre")) {
			pc.addProjectPart(root.id, ItemType.CreoleRef, "creole.cre", is);
		}

		try(final InputStream	is = this.getClass().getResourceAsStream("image.png")) {
			pc.addProjectPart(root.id, ItemType.ImageRef, "image.png", is);
		}

		try {pc.addProjectPart(9999, ItemType.CreoleRef, "creole.cre", new ByteArrayInputStream(new byte[0]));
			Assert.fail("Mandatory exception was not detected (unknown 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try {pc.addProjectPart(root.id, null, "creole.cre", new ByteArrayInputStream(new byte[0]));
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try {pc.addProjectPart(root.id, ItemType.Root, "creole.cre", new ByteArrayInputStream(new byte[0]));
			Assert.fail("Mandatory exception was not detected (illegal 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try {pc.addProjectPart(root.id, ItemType.CreoleRef, null, new ByteArrayInputStream(new byte[0]));
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try {pc.addProjectPart(root.id, ItemType.CreoleRef, "", new ByteArrayInputStream(new byte[0]));
			Assert.fail("Mandatory exception was not detected (empty 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try {pc.addProjectPart(root.id, ItemType.CreoleRef, "creole.cre", null);
			Assert.fail("Mandatory exception was not detected (null 4-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{try(final OutputStream	os = new FileOutputStream(temp);
				final InputStream	is = this.getClass().getResourceAsStream("sample.pdf")) {
				
				Utils.copyStream(is, os);
			}
		
			pc.addProjectPart(root.id, ItemType.DocumentRef, temp);
		} finally {
			temp.delete();
		}

		try{pc.addProjectPart(9999, ItemType.DocumentRef, temp);
			Assert.fail("Mandatory exception was not detected (unknown 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{pc.addProjectPart(root.id, null, temp);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{pc.addProjectPart(root.id, ItemType.Root, temp);
			Assert.fail("Mandatory exception was not detected (illegal 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{pc.addProjectPart(root.id, ItemType.DocumentRef, null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		Assert.assertEquals(3, pc.getPartNames().length);
		Assert.assertArrayEquals(new ProjectChangeType[] {ProjectChangeType.ITEM_INSERTED, ProjectChangeType.ITEM_CONTENT_CHANGED, ProjectChangeType.ITEM_INSERTED, ProjectChangeType.ITEM_CONTENT_CHANGED, ProjectChangeType.ITEM_INSERTED, ProjectChangeType.ITEM_CONTENT_CHANGED}, 
							getChangesAndClear(list));

		pc.removeProjectChangeListener(pcl);

		try {pc.removeProjectChangeListener(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}	
	
	private static ProjectChangeType[] getChangesAndClear(final List<ProjectChangeEvent> list) {
		final ProjectChangeType[]	result = new ProjectChangeType[list.size()];
		int	count = 0;
		
		for(ProjectChangeEvent item : list) {
			result[count++] = item.getChangeType();
		}
		list.clear();
		return result;
	}
}
