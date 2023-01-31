package chav1961.csce.swing;

import java.util.Locale;

import javax.swing.JTree;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;

import chav1961.csce.project.ProjectContainer;
import chav1961.csce.project.ProjectNavigator;
import chav1961.csce.project.ProjectNavigator.ProjectNavigatorItem;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.i18n.interfaces.LocalizerOwner;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.ui.swing.SwingUtils;

public class ProjectTree extends JTree implements LocalizerOwner, LocaleChangeListener {
	private static final long serialVersionUID = 1L;

	private final ProjectContainer			project;
	private final DefaultMutableTreeNode	rootNode;
	
	public ProjectTree(final ProjectContainer project) {
		super();
		if (project == null) {
			throw new NullPointerException("Project can't be null"); 
		}
		else {
			this.project = project;
			this.rootNode = buildTree(project.getProjectNavigator());
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
		walkAndRefresh(rootNode);
	}
	
	private void walkAndRefresh(final DefaultMutableTreeNode node) {
		for (int index = 0; index < node.getChildCount(); index++) {
			walkAndRefresh((DefaultMutableTreeNode)node.getChildAt(index));
		}
		((DefaultTreeModel)getModel()).nodeChanged(node);
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
