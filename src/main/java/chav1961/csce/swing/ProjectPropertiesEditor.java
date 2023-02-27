package chav1961.csce.swing;

import chav1961.csce.project.ProjectContainer;
import chav1961.csce.project.ProjectNavigator.ProjectNavigatorItem;
import chav1961.csce.utils.SimpleLocalizedString;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.i18n.interfaces.LocalizedString;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;

@LocaleResourceLocation("i18n:xml:root://chav1961.csce.swing.ProjectPropertiesEditor/chav1961/csce/localization.xml")
@LocaleResource(value="ProjectPropertiesEditor.caption",tooltip="ProjectPropertiesEditor.caption.tt",help="ProjectPropertiesEditor.caption.help")
public class ProjectPropertiesEditor implements FormManager<Object, ProjectPropertiesEditor>, ModuleAccessor {
	private final LoggerFacade		logger;
	private final Localizer			localizer;

	@LocaleResource(value="ProjectPropertiesEditor.name",tooltip="ProjectPropertiesEditor.name.tt")
	@Format("20m")
	public SimpleLocalizedString	name;

	@LocaleResource(value="ProjectPropertiesEditor.author",tooltip="ProjectPropertiesEditor.author.tt")
	@Format("20m")
	public SimpleLocalizedString	author;

	@LocaleResource(value="ProjectPropertiesEditor.desc",tooltip="ProjectPropertiesEditor.desc.tt")
	@Format("20*5m")
	public String					descriptor;


	public ProjectPropertiesEditor(final LoggerFacade logger, final Localizer localizer, final SubstitutableProperties props) throws SyntaxException {
		if (logger == null) {
			throw new NullPointerException("Logger can't be null"); 
		}
		else if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else if (props == null) {
			throw new NullPointerException("Project properties can't be null"); 
		}
		else {
			this.logger = logger;
			this.localizer = localizer;
			this.name = new SimpleLocalizedString(ProjectContainer.PROJECT_NAME, props.getProperty(ProjectContainer.PROJECT_NAME));
			this.author = new SimpleLocalizedString(ProjectContainer.PROJECT_AUTHOR, props.getProperty(ProjectContainer.PROJECT_AUTHOR));
			this.descriptor = props.getProperty(ProjectContainer.PROJECT_DESCRIPTOR);
		}
	}

	@Override
	public RefreshMode onField(final ProjectPropertiesEditor inst, final Object id, final String fieldName, final Object oldValue, final boolean beforeCommit) throws FlowException, LocalizationException {
		return RefreshMode.DEFAULT;
	}

	@Override
	public LoggerFacade getLogger() {
		return logger;
	}

	@Override
	public void allowUnnamedModuleAccess(final Module... unnamedModules) {
		for (Module item : unnamedModules) {
			this.getClass().getModule().addExports(this.getClass().getPackageName(),item);
		}
	}
	
	public void storeProperties(final SubstitutableProperties props) throws PrintingException {
		props.setProperty(ProjectContainer.PROJECT_NAME, name.toStringValue());
		props.setProperty(ProjectContainer.PROJECT_AUTHOR, author.toStringValue());
		props.setProperty(ProjectContainer.PROJECT_DESCRIPTOR, descriptor);
	}
}
