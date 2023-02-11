package chav1961.csce.swing;

import java.util.Locale;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import chav1961.csce.Application;
import chav1961.csce.project.ProjectChangeEvent;
import chav1961.csce.project.ProjectChangeListener;
import chav1961.csce.project.ProjectContainer;
import chav1961.csce.project.ProjectNavigator;
import chav1961.csce.project.ProjectNavigator.ProjectNavigatorItem;
import chav1961.csce.swing.ProjectTree.ProjectItemTreeNode;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.swing.SwingUtils;

public class ProjectViewer extends JSplitPane implements LocaleChangeListener {
	private static final long serialVersionUID = 5507071556068623361L;

	private final Application				parent;
	private final ProjectContainer 			project;
	private final ContentMetadataInterface	mdi;
	private final LightWeightListenerList<ProjectViewerChangeListener>	listeners = new LightWeightListenerList<>(ProjectViewerChangeListener.class);  
	private final ProjectTabbedPane			tabs = new ProjectTabbedPane();
	private final ScreenLogger				screenLogger = new ScreenLogger();
	private final ProjectTree				tree;
	private boolean							recursionProtector = false;
	
	public ProjectViewer(final Application parent, final ProjectContainer project, final ContentMetadataInterface mdi) {
		super(JSplitPane.HORIZONTAL_SPLIT);
		this.parent = parent;
		this.project = project;
		this.mdi = mdi;
		this.tree = new ProjectTree(project, mdi);

		tree.getSelectionModel().addTreeSelectionListener((e)->treeSelectionChanged(e));
		
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
	}

	public void addProjectViewerChangeListener(final ProjectViewerChangeListener l) {
		if (l == null) {
			throw new NullPointerException("Project change listener to add can't be null"); 
		}
		else {
			listeners.addListener(l);
		}
	}

	public void removeProjectViewerChangeListener(final ProjectViewerChangeListener l) {
		if (l == null) {
			throw new NullPointerException("Project change listener to remove can't be null"); 
		}
		else {
			listeners.removeListener(l);
		}
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

	public void refreshProject(final ProjectChangeEvent event) {
		tree.refreshTree(event);
	}

	private void treeSelectionChanged(final TreeSelectionEvent e) {
		if (!recursionProtector) {
			try{recursionProtector = true;
				final TreePath				newPath = e.getNewLeadSelectionPath();
				final TreePath				oldPath = e.getOldLeadSelectionPath();
				final ProjectItemTreeNode	newNode = newPath != null ? (ProjectItemTreeNode) newPath.getLastPathComponent() : null; 
				final ProjectItemTreeNode	oldNode = oldPath != null ? (ProjectItemTreeNode) oldPath.getLastPathComponent() : null;
				
				if (oldNode != null) {
					final ProjectViewerChangeEvent	pvce = new ProjectViewerChangeEvent(this, 
																		ProjectViewerChangeEvent.ProjectChangeType.NAVIGATOR_ITEM_DESELECTED, 
																		oldNode.getUserObject().id);
					
					listeners.fireEvent((l)->l.processEvent(pvce));
				}
				if (newNode != null) {
					final ProjectViewerChangeEvent	pvce = new ProjectViewerChangeEvent(this, 
																		ProjectViewerChangeEvent.ProjectChangeType.NAVIGATOR_ITEM_SELECTED, 
																		newNode.getUserObject().id);
					
					listeners.fireEvent((l)->l.processEvent(pvce));
				}
			} finally {
				recursionProtector = false;
			}
		}
	}

}
