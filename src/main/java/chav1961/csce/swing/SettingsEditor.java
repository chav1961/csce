package chav1961.csce.swing;

import chav1961.csce.Application;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.i18n.interfaces.SupportedLanguages;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;

@LocaleResourceLocation("i18n:xml:root://chav1961.csce.swing.SettingsEditor/chav1961/csce/localization.xml")
@LocaleResource(value="SettingsEditor.caption",tooltip="SettingsEditor.caption.tt",help="SettingsEditor.caption.help")
public class SettingsEditor implements FormManager<Object, SettingsEditor>, ModuleAccessor {
	private final LoggerFacade	logger;

	@LocaleResource(value="SettingsEditor.preferredLang",tooltip="SettingsEditor.preferredLang.tt")
	@Format("20m")
	public SupportedLanguages	lang;
	
	@LocaleResource(value="SettingsEditor.automaticPaste",tooltip="SettingsEditor.automaticPaste.tt")
	@Format("20m")
	public boolean		automaticPaste;

	@LocaleResource(value="SettingsEditor.checkExternalLinks",tooltip="SettingsEditor.checkExternalLinks.tt")
	@Format("20m")
	public boolean		checkExternalLinks;

	public SettingsEditor(final LoggerFacade logger, final SubstitutableProperties props) throws SyntaxException {
		if (logger == null) {
			throw new NullPointerException("Logger can't be null"); 
		}
		else if (props == null) {
			throw new NullPointerException("Project properties can't be null"); 
		}
		else {
			this.logger = logger;
			loadPropertiesInternal(props);
		}
	}
	
	@Override
	public RefreshMode onField(SettingsEditor inst, Object id, String fieldName, Object oldValue, boolean beforeCommit) throws FlowException, LocalizationException {
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

	public void loadProperties(final SubstitutableProperties props) throws PrintingException {
		loadPropertiesInternal(props);
	}
	
	public void storeProperties(final SubstitutableProperties props) throws PrintingException {
		props.setProperty(Application.PROP_AUTOMATIC_PASTE, String.valueOf(automaticPaste));
		props.setProperty(Application.PROP_CHECK_EXTERNAL_LINKS, String.valueOf(checkExternalLinks));
		props.setProperty(Application.PROP_PREFERRED_LANG, lang.name());
	}
	
	private void loadPropertiesInternal(final SubstitutableProperties props) {
		this.automaticPaste = props.getProperty(Application.PROP_AUTOMATIC_PASTE, boolean.class, "false"); 
		this.checkExternalLinks = props.getProperty(Application.PROP_CHECK_EXTERNAL_LINKS, boolean.class, "false"); 
		this.lang = props.getProperty(Application.PROP_PREFERRED_LANG, SupportedLanguages.class, "ru"); 
	}
}
