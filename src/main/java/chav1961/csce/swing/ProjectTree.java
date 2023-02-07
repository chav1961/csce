package chav1961.csce.swing;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.Locale;

import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

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
	private final DefaultMutableTreeNode	rootNode;
	
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
	
	private void walkAndRefresh(final DefaultMutableTreeNode node) {
		for (int index = 0; index < node.getChildCount(); index++) {
			walkAndRefresh((DefaultMutableTreeNode)node.getChildAt(index));
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
	
	private static DefaultMutableTreeNode buildTree(final ProjectNavigator navigator) {
		return buildTree(navigator, navigator.getRoot().id);
	}	
	
	private static DefaultMutableTreeNode buildTree(final ProjectNavigator navigator, final long id) {
		final DefaultMutableTreeNode	result = new DefaultMutableTreeNode(navigator.getItem(id));
		
		for (ProjectNavigatorItem item : navigator.getChildren(id)) {
			result.add(buildTree(navigator, item.id));
		}
		return result;
	}

}
