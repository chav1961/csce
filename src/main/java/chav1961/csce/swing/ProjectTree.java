package chav1961.csce.swing;


import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.Locale;
import java.util.function.Consumer;

import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import chav1961.csce.project.ProjectChangeEvent;
import chav1961.csce.project.ProjectContainer;
import chav1961.csce.project.ProjectNavigator;
import chav1961.csce.project.ProjectNavigator.ProjectNavigatorItem;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.i18n.interfaces.LocalizerOwner;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.swing.SwingUtils;

public class ProjectTree extends JTree implements LocalizerOwner, LocaleChangeListener {
	private static final long serialVersionUID = 1L;

	private final ProjectContainer			project;
	private final ContentMetadataInterface	mdi;
	private final JPopupMenu				popup; 
	private ProjectItemTreeNode				rootNode;
	
	public ProjectTree(final ProjectContainer project, final ContentMetadataInterface mdi) {
		super();
		if (project == null) {
			throw new NullPointerException("Project can't be null"); 
		}
		else {
			this.project = project;
			this.mdi = mdi;
			this.popup = SwingUtils.toJComponent(mdi.byUIPath(URI.create("ui:/model/navigation.top.treemenu")), JPopupMenu.class);
			this.rootNode = buildTree(project.getProjectNavigator());
			
			SwingUtils.assignActionListeners(popup, project.getApplication());
			SwingUtils.assignActionKey(this, SwingUtils.KS_CONTEXTMENU, (e)->{
				if (!isSelectionEmpty()) {
					showMenu(getSelectionPoint());
				}
			}, SwingUtils.ACTION_CONTEXTMENU);
			SwingUtils.assignActionKey(this, SwingUtils.KS_DELETE, (e)->{
				if (!isSelectionEmpty()) {
					project.getApplication().deleteItem();
				}
			}, SwingUtils.ACTION_DELETE);
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON3) {
						showMenu(e.getPoint());
					}
					else {
						super.mouseClicked(e);
					}
				}
			});
			getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			
			setModel(new DefaultTreeModel(this.rootNode));
			
			setCellRenderer(SwingUtils.getCellRenderer(ProjectNavigatorItem.class, new FieldFormat(ProjectNavigatorItem.class), TreeCellRenderer.class));
		}
	}

	@Override
	public Localizer getLocalizer() {
		return project.getLocalizer();
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		SwingUtils.refreshLocale(popup, oldLocale, newLocale);
		walkAndRefresh(rootNode);
	}

	
	public void refreshTree(final ProjectChangeEvent event) {
		switch (event.getChangeType()) {
			case PART_INSERTED				:
				final TreePath 				parentPath = long2TreePath((long)event.getParameters()[0]);
				final ProjectItemTreeNode	parentNode = ((ProjectItemTreeNode)parentPath.getLastPathComponent());
				final ProjectItemTreeNode	child = new ProjectItemTreeNode(project.getProjectNavigator().getItem((long)event.getParameters()[1]));

				((DefaultTreeModel)getModel()).insertNodeInto(child, parentNode, parentNode.getChildCount());
				getSelectionModel().setSelectionPath(parentPath.pathByAddingChild(child));
				break;
			case PROJECT_FILENAME_CHANGED	:
				break;
			default :
				throw new UnsupportedOperationException("Change type ["+event.getChangeType()+"] is not supprted yet");
		}
	}	
	
	private TreePath long2TreePath(final long id) {
		final TreePath[]	path = new TreePath[] {null};
		
		walkAndProcess((ProjectItemTreeNode)getModel().getRoot(), (node)->{
			if (node.getUserObject().id == id) {
				path[0] = new TreePath(node.getPath());
			}
		});
		return path[0];
	}

	private void walkAndProcess(final ProjectItemTreeNode node, final Consumer<ProjectItemTreeNode> consumer) {
		for (int index = 0; index < node.getChildCount(); index++) {
			walkAndProcess((ProjectItemTreeNode)node.getChildAt(index), consumer);
		}
		consumer.accept(node);
	}
	
	
	private void walkAndRefresh(final ProjectItemTreeNode node) {
		for (int index = 0; index < node.getChildCount(); index++) {
			walkAndRefresh((ProjectItemTreeNode)node.getChildAt(index));
		}
		((DefaultTreeModel)getModel()).nodeChanged(node);
	}

	private Point getSelectionPoint() {
		final Rectangle	rect = getRowBounds(getRowForPath(getSelectionPath()));
		
		return new Point((int)rect.getCenterX(), (int)rect.getCenterX());
	}
	
	private void showMenu(final Point p) {
		if (!isSelectionEmpty()) {
			final TreePath	path = getClosestPathForLocation(p.x, p.y);
			
			if (path != null) {
				final int	row = getRowForPath(path);
				
				SwingUtils.findComponentByName(popup, "treemenu.properties").setEnabled(row != 0);
				SwingUtils.findComponentByName(popup, "treemenu.delete").setEnabled(row != 0);
				popup.show(this, p.x, p.y);
			}
		}
	}
	
	private static ProjectItemTreeNode buildTree(final ProjectNavigator navigator) {
		return buildTree(navigator, navigator.getRoot().id);
	}	
	
	private static ProjectItemTreeNode buildTree(final ProjectNavigator navigator, final long id) {
		final ProjectItemTreeNode	result = new ProjectItemTreeNode(navigator.getItem(id));
		
		for (ProjectNavigatorItem item : navigator.getChildren(id)) {
			result.add(buildTree(navigator, item.id));
		}
		return result;
	}
	
	private static class ProjectItemTreeNode extends DefaultMutableTreeNode {
		private static final long serialVersionUID = 1L;

		public ProjectItemTreeNode(final ProjectNavigatorItem userObject) {
			this(userObject, !userObject.type.isLeafItem());
		}
		
		public ProjectItemTreeNode(final ProjectNavigatorItem userObject, boolean allowsChildren) {
			super(userObject, allowsChildren);
			if (userObject == null) {
				throw new NullPointerException("User object can't be null");
			}
		}

		@Override
		public ProjectNavigatorItem getUserObject() {
			return (ProjectNavigatorItem)super.getUserObject();
		}
		
		@Override
		public void setUserObject(final Object userObject) {
			if (!(userObject instanceof ProjectNavigatorItem)) {
				throw new IllegalArgumentException("User object no set can't be null and must be ProjectNavigatorItem instance"); 
			}
			else {
				super.setUserObject(userObject);
			}
		}
		
		@Override
		public boolean isLeaf() {
			return getUserObject().type.isLeafItem();
		}
	}

}
