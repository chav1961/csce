package chav1961.csce.swing;

import java.util.Locale;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import chav1961.csce.Application;
import chav1961.csce.project.ProjectContainer;
import chav1961.csce.project.ProjectNavigator;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.ui.swing.SwingUtils;

public class ProjectViewer extends JSplitPane implements LocaleChangeListener {
	private static final long serialVersionUID = 5507071556068623361L;

	private final Application		parent;
	private final ProjectContainer 	project;
	private final ProjectTabbedPane	tabs = new ProjectTabbedPane();
	private final ScreenLogger		screenLogger = new ScreenLogger();
	private final ProjectTree		tree;
	
	public ProjectViewer(final Application parent, final ProjectContainer project) {
		super(JSplitPane.HORIZONTAL_SPLIT);
		this.parent = parent;
		this.project = project;
		this.tree = new ProjectTree(project);

		final JSplitPane	rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabs, new JScrollPane(screenLogger));

		rightSplit.setDividerLocation(600);
		
		setLeftComponent(new JScrollPane(tree));
		setRightComponent(rightSplit);
		
		setDividerLocation(300);
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		// TODO Auto-generated method stub
		SwingUtils.refreshLocale(tree, oldLocale, newLocale);
		refreshProject();
	}
	
	public void refreshProject() {
		
	}

}
