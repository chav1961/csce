package chav1961.csce.swing.renderers;

import java.awt.Component;
import java.net.MalformedURLException;
import java.util.Set;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import chav1961.csce.project.ProjectNavigator.ProjectNavigatorItem;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.LocalizerOwner;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.SwingItemRenderer;

public class ProjectNavigatorItemRenderer<T, R> implements SwingItemRenderer<ProjectNavigatorItem, R> {
	private static final Set<Class<?>>	SUPPORTED_RENDERERDS = Set.of(TreeCellRenderer.class, ListCellRenderer.class);
	
	public ProjectNavigatorItemRenderer() {
	}
	
	@Override
	public boolean canServe(Class<ProjectNavigatorItem> class2Render, Class<R> rendererType, Object... options) {
		if (class2Render == null) {
			throw new NullPointerException("Class to render descriptor can't be null"); 
		}
		else if (rendererType == null) {
			throw new NullPointerException("Renderer type can't be null"); 
		}
		else if (class2Render.isArray()) {
			return canServe((Class<ProjectNavigatorItem>) class2Render.getComponentType(), rendererType, options);
		}
		else {
			return ProjectNavigatorItem.class.isAssignableFrom(class2Render) && SUPPORTED_RENDERERDS.contains(rendererType); 
		}
	}

	@Override
	public R getRenderer(final Class<R> rendererType, final Object... options) {
		if (rendererType == null) {
			throw new NullPointerException("Renderer type can't be null"); 
		}
		else if (ListCellRenderer.class.isAssignableFrom(rendererType)) {
			return (R)new DefaultListCellRenderer() {
				@Override
				public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
					final JLabel				label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					final ContentNodeMetadata	meta = ((ProjectNavigatorItem)value).getNodeMetadata();
					
					try{label.setIcon(new ImageIcon(meta.getIcon().toURL()));
						label.setText(LocalizerFactory.getLocalizer(meta.getLocalizerAssociated()).getValue(meta.getLabelId()));
					} catch (MalformedURLException | LocalizationException e) {
						label.setText(meta.getLabelId());
					}
					if (meta.getTooltipId() != null) {
						label.setToolTipText(LocalizerFactory.getLocalizer(meta.getLocalizerAssociated()).getValue(meta.getTooltipId()));
					}
					return label;
				}
			};
		}
		else if (TreeCellRenderer.class.isAssignableFrom(rendererType)) {
			return (R)new DefaultTreeCellRenderer() {
				@Override
				public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean sel, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
					if (value == null) {
						return new JLabel("");
					}
					else {
						final JLabel				label = (JLabel)super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
						final ContentNodeMetadata	meta = ((ProjectNavigatorItem)((DefaultMutableTreeNode)value).getUserObject()).getNodeMetadata();
						final Localizer				localizer = ((LocalizerOwner)SwingUtils.getNearestOwner(tree, LocalizerOwner.class)).getLocalizer();
						
						try{label.setIcon(new ImageIcon(meta.getIcon().toURL()));
							label.setText(localizer.getValue(meta.getLabelId()));
						} catch (MalformedURLException | LocalizationException e) {
							label.setText(meta.getLabelId());
						}
						if (meta.getTooltipId() != null) {
							label.setToolTipText(localizer.getValue(meta.getTooltipId()));
						}
						return label;
					}
				}
			};
		}
		else {
			throw new UnsupportedOperationException("Required cell renderer ["+rendererType+"] is not supported yet");
		}
	}
}
