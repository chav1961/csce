package chav1961.csce.swing;

import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;

@LocaleResourceLocation("i18n:xml:root://chav1961.csce.swing.PageLinkEditor/chav1961/csce/localization.xml")
@LocaleResource(value="PageLinkEditor.caption",tooltip="PageLinkEditor.caption.tt",help="PageLinkEditor.caption.help")
public class PageLinkEditor implements FormManager<Object, PageLinkEditor>, ModuleAccessor {
	private final LoggerFacade	logger;

	@LocaleResource(value="PageLinkEditor.ref",tooltip="PageLinkEditor.ref.tt")
	@Format("20m")
	public String		ref = "";

	@LocaleResource(value="PageLinkEditor.caption",tooltip="PageLinkEditor.caption.tt")
	@Format("20m")
	public String		caption = "";
	
	public PageLinkEditor(final LoggerFacade logger) {
		if (logger == null) {
			throw new NullPointerException("Loccaeg can't be null");
		}
		else {
			this.logger = logger;
		}
	}

	@Override
	public RefreshMode onField(PageLinkEditor inst, Object id, String fieldName, Object oldValue, boolean beforeCommit) throws FlowException, LocalizationException {
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
