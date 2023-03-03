package chav1961.csce.swing;

import chav1961.csce.project.ProjectContainer;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.i18n.interfaces.SupportedLanguages;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;

@LocaleResourceLocation("i18n:xml:root://chav1961.csce.swing.Scorm2004SettingsEditor/chav1961/csce/localization.xml")
@LocaleResource(value="Scorm2004SettingsEditor.caption",tooltip="Scorm2004SettingsEditor.caption.tt",help="Scorm2004SettingsEditor.caption.help")
public class Scorm2004SettingsEditor implements FormManager<Object, Scorm2004SettingsEditor>, ModuleAccessor {
	private final LoggerFacade		logger;

	@LocaleResource(value="Scorm2004SettingsEditor.name",tooltip="Scorm2004SettingsEditor.name.tt")
	@Format("20m")
	public String				name;

	@LocaleResource(value="Scorm2004SettingsEditor.description",tooltip="Scorm2004SettingsEditor.description.tt")
	@Format("20m")
	public String				description;

	@LocaleResource(value="Scorm2004SettingsEditor.servletPath",tooltip="Scorm2004SettingsEditor.servletPath.tt")
	@Format("20m")
	public String				servletPath;

	@LocaleResource(value="Scorm2004SettingsEditor.preferredLang",tooltip="Scorm2004SettingsEditor.preferredLang.tt")
	@Format("20m")
	public SupportedLanguages	lang;
	
	public Scorm2004SettingsEditor(final LoggerFacade logger, final SubstitutableProperties props) {
		if (logger == null) {
			throw new NullPointerException("Logger can't be null");
		}
		else if (props == null) {
			throw new NullPointerException("Project properties can't be null");
		}
		else {
			this.logger = logger;
			this.name = props.getProperty(ProjectContainer.SCORM2004_NAME, "servlet"); 			
			this.description = props.getProperty(ProjectContainer.SCORM2004_DESCRIPTOR,"???").replace("\\n", "\n"); 			
			this.servletPath = props.getProperty(ProjectContainer.SCORM2004_PATH,"/"); 			
			this.lang = props.getProperty(ProjectContainer.SCORM2004_LANGUAGE, SupportedLanguages.class, "ru"); 			
		}
	}
	
	@Override
	public RefreshMode onField(final Scorm2004SettingsEditor inst, final Object id, final String fieldName, final Object oldValue, final boolean beforeCommit) throws FlowException, LocalizationException {
		return RefreshMode.DEFAULT;
	}

	@Override
	public LoggerFacade getLogger() {
		return logger;
	}

	@Override
	public void allowUnnamedModuleAccess(Module... unnamedModules) {
		for (Module item : unnamedModules) {
			this.getClass().getModule().addExports(this.getClass().getPackageName(),item);
		}
	}

	public void storeProperties(final SubstitutableProperties settings) throws PrintingException {
		settings.setProperty(ProjectContainer.SCORM2004_NAME, name); 			
		settings.setProperty(ProjectContainer.SCORM2004_DESCRIPTOR,description.replace("\n","\\n")); 			
		settings.setProperty(ProjectContainer.SCORM2004_PATH, servletPath); 			
		settings.setProperty(ProjectContainer.SCORM2004_LANGUAGE, lang.name()); 			
	}
}
