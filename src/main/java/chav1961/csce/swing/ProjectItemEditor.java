package chav1961.csce.swing;

import chav1961.csce.project.ProjectContainer;
import chav1961.csce.project.ProjectNavigator.ProjectNavigatorItem;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.i18n.interfaces.LocalizedString;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;

@LocaleResourceLocation("i18n:xml:root://chav1961.csce.swing.ProjectItemEditor/chav1961/csce/localization.xml")
@LocaleResource(value="ProjectItemEditor.caption",tooltip="ProjectItemEditor.caption.tt",help="ProjectItemEditor.caption.help")
public class ProjectItemEditor implements FormManager<Object, ProjectItemEditor>, ModuleAccessor {
	private final LoggerFacade			logger;
	private final ProjectNavigatorItem	pni;

	public String			name;

	@LocaleResource(value="ProjectItemEditor.title",tooltip="ProjectItemEditor.title.tt")
	@Format("20m")
	public LocalizedString	titleId;

	@LocaleResource(value="ProjectItemEditor.desc",tooltip="ProjectItemEditor.desc.tt")
	@Format("20*5m")
	public String			desc;
	

	public ProjectItemEditor(final LoggerFacade logger, final ProjectContainer project, final ProjectNavigatorItem pni) {
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
			this.desc = pni.desc;
			this.pni = pni;
			this.titleId = project.getLocalizationString(pni.titleId);
		}
	}
	
	@Override
	public RefreshMode onField(final ProjectItemEditor inst, final Object id, final String fieldName, final Object oldValue, final boolean beforeCommit) throws FlowException, LocalizationException {
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
		return new ProjectNavigatorItem(pni.id, pni.parent, name, pni.type, desc, titleId.getId(), pni.subtreeRef);
	}
}
