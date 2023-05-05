package chav1961.csce.swing;

import java.net.URI;

import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.i18n.interfaces.SupportedLanguages;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;

@LocaleResourceLocation("i18n:xml:root://chav1961.csce.swing.ExternalLinkEditor/chav1961/csce/localization.xml")
@LocaleResource(value="ExternalLinkEditor.caption",tooltip="ExternalLinkEditor.caption.tt",help="ExternalLinkEditor.caption.help")
public class ExternalLinkEditor implements FormManager<Object, ExternalLinkEditor>, ModuleAccessor {
	private final LoggerFacade	logger;

	@LocaleResource(value="ExternalLinkEditor.ref",tooltip="ExternalLinkEditor.ref.tt")
	@Format("20m")
	public URI			ref = URI.create("./");

	@LocaleResource(value="ExternalLinkEditor.caption",tooltip="ExternalLinkEditor.caption.tt")
	@Format("20m")
	public String		caption = "";
	
	public ExternalLinkEditor(final LoggerFacade logger) {
		if (logger == null) {
			throw new NullPointerException("Loccaeg can't be null");
		}
		else {
			this.logger = logger;
		}
	}
	
	@Override
	public RefreshMode onField(final ExternalLinkEditor inst, final Object id, final String fieldName, final Object oldValue, final boolean beforeCommit) throws FlowException, LocalizationException {
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
}
