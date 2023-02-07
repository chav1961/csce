package chav1961.csce.swing;

import java.util.Locale;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import chav1961.csce.Application;
import chav1961.csce.project.ProjectContainer;
import chav1961.csce.project.ProjectNavigator;
import chav1961.csce.project.ProjectNavigator.ProjectNavigatorItem;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.swing.SwingUtils;

public class ProjectViewer extends JSplitPane implements LocaleChangeListener {
	private static final long serialVersionUID = 5507071556068623361L;

	private final Application				parent;
	private final ProjectContainer 			project;
	private final ContentMetadataInterface	mdi;
	private final ProjectTabbedPane			tabs = new ProjectTabbedPane();
	private final ScreenLogger				screenLogger = new ScreenLogger();
	private final ProjectTree				tree;
	
	public ProjectViewer(final Application parent, final ProjectContainer project, final ContentMetadataInterface mdi) {
		super(JSplitPane.HORIZONTAL_SPLIT);
		this.parent = parent;
		this.project = project;
		this.mdi = mdi;
		this.tree = new ProjectTree(project, mdi);

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
	
	public boolean isProjectNavigatorItemSelected() {
		return !tree.isSelectionEmpty();
	}
	
	public ProjectNavigatorItem getProjectNavigatorItemSelected() {
		if (isProjectNavigatorItemSelected()) {
			final TreePath	path = tree.getSelectionPath();
			
			return (ProjectNavigatorItem)(((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject());
		}
		else {
			return null;
		}
	}
}
