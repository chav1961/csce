package chav1961.csce.swing;

import java.util.Locale;

import chav1961.csce.project.ProjectContainer;
import chav1961.csce.project.ProjectNavigator.ItemType;
import chav1961.csce.project.ProjectNavigator.ProjectNavigatorItem;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.i18n.interfaces.LocalizedString;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;

@LocaleResourceLocation("i18n:xml:root://chav1961.csce.swing.ProjectPartEditor/chav1961/csce/localization.xml")
@LocaleResource(value="ProjectPartEditor.caption",tooltip="ProjectPartEditor.caption.tt",help="ProjectPartEditor.caption.help")
public class ProjectPartEditor implements FormManager<Object, ProjectPartEditor>, ModuleAccessor {
	private final LoggerFacade			logger;
	private final ProjectNavigatorItem	pni;

	@LocaleResource(value="ProjectPartEditor.name",tooltip="ProjectPartEditor.name.tt")
	@Format("20m")
	public String			name;

	@LocaleResource(value="ProjectPartEditor.title",tooltip="ProjectPartEditor.title.tt")
	@Format("20m")
	public LocalizedString	titleId;

	@LocaleResource(value="ProjectPartEditor.desc",tooltip="ProjectPartEditor.desc.tt")
	@Format("20*5m")
	public String			desc;
	

	public ProjectPartEditor(final LoggerFacade logger, final ProjectContainer project, final ProjectNavigatorItem pni) {
		if (logger == null) {
			throw new NullPointerException("Logger can't be null"); 
		}
		else if (project == null) {
			throw new NullPointerException("Project can't be null"); 
		}
		else if (pni == null) {
			throw new NullPointerException("Logger can't be null"); 
		}
		else {
			this.logger = logger;
			this.name = pni.name;
			this.desc = pni.desc;
			this.pni = pni;
			this.titleId = project.getLocalizationString(pni.titleId);
		}
	}
	
	@Override
	public RefreshMode onField(final ProjectPartEditor inst, final Object id, final String fieldName, final Object oldValue, final boolean beforeCommit) throws FlowException, LocalizationException {
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
	
	public ProjectNavigatorItem getNavigatorItem() {
		return new ProjectNavigatorItem(pni.id, pni.parent, name, pni.type, desc, titleId.getId(), pni.subtreeRef, pni.partRef);
	}
}
