package chav1961.csce.project;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import chav1961.csce.Application;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
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
	}

	@Test
	public void newProjectTest() throws IOException {
		final ProjectContainer	pc = new ProjectContainer(()->PureLibSettings.PURELIB_LOCALIZER, mdi);

		try{pc.getLocalizer();
			Assert.fail("Mandatory exception was not detected (project is not prepared)");
		} catch (IllegalStateException exc) {
		}
		
		try(final OutputStream	os = pc.fromOutputStream()) {
			Utils.copyStream(new ByteArrayInputStream(new byte[0]), os);
		}
		Assert.assertNotNull(pc.getLocalizer());

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
		
		// Test project parts
		Assert.assertArrayEquals(new String[0], pc.getPartNames());
		
		pc.addProjectPartContent("Creole1.cre", "test");
		Assert.assertEquals("test", pc.getProjectPartContent("Creole1.cre"));
	}
}
