package chav1961.csce.swing;

import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;

@LocaleResourceLocation("i18n:xml:root://chav1961.csce.swing.InsertTableDialog/chav1961/csce/localization.xml")
@LocaleResource(value="InsertTableDialog.caption",tooltip="InsertTableDialog.caption.tt",help="InsertTableDialog.caption.help")
public class InsertTableDialog implements FormManager<Object, InsertTableDialog>, ModuleAccessor {
	private final LoggerFacade	logger;

	@LocaleResource(value="InsertTableDialog.numberOfRows",tooltip="InsertTableDialog.numberOfRows.tt")
	@Format("20mp")
	public int		numberOfRows = 2;
	
	@LocaleResource(value="InsertTableDialog.numberOfColumns",tooltip="InsertTableDialog.numberOfColumns.tt")
	@Format("20mp")
	public int		numberOfColumns = 2;

	@LocaleResource(value="InsertTableDialog.captionRequired",tooltip="InsertTableDialog.captionRequired.tt")
	@Format("1m")
	public boolean	captionRequired = true;
	
	public InsertTableDialog(final LoggerFacade logger) throws SyntaxException {
		if (logger == null) {
			throw new NullPointerException("Loccaeg can't be null");
		}
		else {
			this.logger = logger;
		}
	}

	@Override
	public RefreshMode onField(final InsertTableDialog inst, final Object id, final String fieldName, final Object oldValue, final boolean beforeCommit) throws FlowException, LocalizationException {
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
