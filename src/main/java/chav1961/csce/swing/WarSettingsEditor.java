package chav1961.csce.swing;

import javax.security.auth.callback.LanguageCallback;

import org.w3c.dom.Document;

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

@LocaleResourceLocation("i18n:xml:root://chav1961.csce.swing.WarSettingsEditor/chav1961/csce/localization.xml")
@LocaleResource(value="WarSettingsEditor.caption",tooltip="WarSettingsEditor.caption.tt",help="WarSettingsEditor.caption.help")
public class WarSettingsEditor implements FormManager<Object, WarSettingsEditor>, ModuleAccessor {
	private final LoggerFacade		logger;

	@LocaleResource(value="WarSettingsEditor.name",tooltip="WarSettingsEditor.name.tt")
	@Format("20m")
	public String				name;

	@LocaleResource(value="WarSettingsEditor.description",tooltip="WarSettingsEditor.description.tt")
	@Format("20m")
	public String				description;

	@LocaleResource(value="WarSettingsEditor.servletPath",tooltip="WarSettingsEditor.servletPath.tt")
	@Format("20m")
	public String				servletPath;

	@LocaleResource(value="WarSettingsEditor.preferredLang",tooltip="WarSettingsEditor.preferredLang.tt")
	@Format("20m")
	public SupportedLanguages	lang;
	
	public WarSettingsEditor(final LoggerFacade logger, final SubstitutableProperties props) {
		if (logger == null) {
			throw new NullPointerException("Logger can't be null");
		}
		else if (props == null) {
			throw new NullPointerException("Project properties can't be null");
		}
		else {
			this.logger = logger;
			this.name = props.getProperty(ProjectContainer.WAR_NAME, "servlet"); 			
			this.description = props.getProperty(ProjectContainer.WAR_DESCRIPTOR,"???").replace("\\n", "\n"); 			
			this.servletPath = props.getProperty(ProjectContainer.WAR_PATH,"/"); 			
			this.lang = props.getProperty(ProjectContainer.WAR_LANGUAGE, SupportedLanguages.class, "ru"); 			
		}
	}
	
	@Override
	public RefreshMode onField(final WarSettingsEditor inst, final Object id, final String fieldName, final Object oldValue, final boolean beforeCommit) throws FlowException, LocalizationException {
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
		settings.setProperty(ProjectContainer.WAR_NAME, name); 			
		settings.setProperty(ProjectContainer.WAR_DESCRIPTOR,description.replace("\n","\\n")); 			
		settings.setProperty(ProjectContainer.WAR_PATH, servletPath); 			
		settings.setProperty(ProjectContainer.WAR_LANGUAGE, lang.name()); 			
	}
}
