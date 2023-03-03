package chav1961.csce.swing;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Locale;

import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import chav1961.csce.Application;
import chav1961.csce.project.ProjectContainer;
import chav1961.csce.project.ProjectNavigator.ItemType;
import chav1961.csce.project.ProjectNavigator.ProjectNavigatorItem;
import chav1961.purelib.basic.SimpleTimerTask;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.useful.JBackgroundComponent;
import chav1961.purelib.ui.swing.useful.JBackgroundComponent.FillMode;
import chav1961.purelib.ui.swing.useful.JCloseableTab;
import chav1961.purelib.ui.swing.useful.JCreoleEditor;
import chav1961.purelib.ui.swing.useful.JLocalizedOptionPane;
import chav1961.purelib.ui.swing.useful.LocalizedFormatter;

public class ProjectTabbedPane extends JTabbedPane implements LocaleChangeListener {
	private static final long 		serialVersionUID = 1L;

	private static final Icon		SAVE_ICON = new ImageIcon(ProjectTabbedPane.class.getResource("icon_save_16.png"));
	private static final Icon		GRAY_SAVE_ICON = new ImageIcon(GrayFilter.createDisabledImage(((ImageIcon)SAVE_ICON).getImage()));
	
	private final Application		parent;
	private final ProjectContainer	project;
	
	public ProjectTabbedPane(final Application parent, final ProjectContainer project) {
		if (parent == null) {
			throw new NullPointerException("Parent can't be null");
		}
		else if (project == null) {
			throw new NullPointerException("Project container can't be null");
		}
		else {
			this.parent = parent;
			this.project = project;
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		for (int index = 0; index < getTabCount(); index++) {
			SwingUtils.refreshLocale(getComponentAt(index), oldLocale, newLocale);
		}
	}
	
	public void openCreoleTab(final ProjectNavigatorItem item) {
		if (item == null) {
			throw new NullPointerException("Project item can't be null");
		}
		else if (item.type != ItemType.CreoleRef) {
			throw new IllegalArgumentException("Project item type ["+item.type+"] is not a Creole item");
		}
		else {
			final String	projectPartName = project.getPartNameById(item.id);
			
			for(int index = 0; index < getTabCount(); index++) {
				final JPanel	component = (JPanel)getComponentAt(index);
				
				if (component instanceof CreoleTab) {
					if (((CreoleTab)component).projectPartName.equals(projectPartName)) {
						setSelectedIndex(index);
						return;
					}
				}
			}
			
			final CreoleTab	tab = new CreoleTab(project.getLocalizer(), item.name, projectPartName, item.titleId);
			
			tab.getTabLabel().associate(this, tab);
			addTab("", tab);
			setTabComponentAt(getTabCount()-1, tab.getTabLabel());
			setSelectedIndex(getTabCount()-1);
			tab.setText(project.getProjectPartContent(projectPartName));
			tab.getTabLabel().setToolTipText(item.desc);
			tab.setModified(false);
		}
	}

	public void openImageTab(final ProjectNavigatorItem item) {
		if (item == null) {
			throw new NullPointerException("Project item can't be null");
		}
		else if (item.type != ItemType.ImageRef) {
			throw new IllegalArgumentException("Project item type ["+item.type+"] is not a Image item");
		}
		else {
			final String	projectPartName = project.getPartNameById(item.id);
			
			for(int index = 0; index < getTabCount(); index++) {
				final JPanel	component = (JPanel)getComponentAt(index);
				
				if (component instanceof ImageTab) {
					if (((ImageTab)component).projectPartName.equals(projectPartName)) {
						setSelectedIndex(index);
						return;
					}
				}
			}
			final ImageTab	tab = new ImageTab(project.getLocalizer(), item.name, projectPartName, item.titleId);
			
			tab.getTabLabel().associate(this, tab);
			addTab("", tab);
			setTabComponentAt(getTabCount()-1, tab.getTabLabel());
			setSelectedIndex(getTabCount()-1);
			tab.setImage(project.getProjectPartContent(projectPartName));
			tab.getTabLabel().setToolTipText(item.desc);
			tab.setModified(false);
		}
	}
	
	public void fixProject() {
		for(int index = 0; index < getTabCount(); index++) {
			final JPanelWithLabel	component = (JPanelWithLabel)getComponentAt(index);
			
			component.saveContent();
		}
	}
	
	private abstract class JPanelWithLabel extends JPanel implements LocaleChangeListener {
		private static final long serialVersionUID = 8580069110763696367L;

		private static final String	KEY_ASK_SAVE_TITLE = "chav1961.csce.swing.ProjectTabbedPane.CreoleTab.save.title";
		private static final String	KEY_ASK_SAVE_MESSAGE = "chav1961.csce.swing.ProjectTabbedPane.CreoleTab.save.message";	
		
		protected final String		partName;
		protected final Localizer	localizer;
		
		private final JCloseableTab	tab;
		protected final String		titleId;
		private boolean				isModified = false;

		protected JPanelWithLabel(final Localizer localizer, final String partName, final String titleId) {
			this.localizer = localizer;
			this.partName = partName;
			this.titleId = titleId;
			this.tab = new JCloseableTab(localizer) {
				private static final long serialVersionUID = 4321309725141806561L;

				@Override
				public boolean closeTab() {
					if (isModified()) {
						switch (new JLocalizedOptionPane(localizer).confirm(ProjectTabbedPane.this, new LocalizedFormatter(KEY_ASK_SAVE_MESSAGE, partName), KEY_ASK_SAVE_TITLE, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION)) {
							case JOptionPane.YES_OPTION		:
								saveContent();
							case JOptionPane.NO_OPTION		:
								return super.closeTab();
							case JOptionPane.CANCEL_OPTION	:
								return false;
							default :
								throw new UnsupportedOperationException("Unknown option returned from JLocalizedOptionPane.confirm(...)");
						}
					}
					else {
						return super.closeTab();
					}
				}
				
				@Override
				protected void onClickIcon() {
					if (isModified()) {
						saveContent();
					}
					else {
						super.onClickIcon();
					}
				}
			};
			this.tab.setIcon(GRAY_SAVE_ICON);
			fillLocalizedStrings();
		}

		protected abstract void saveContent();
		
		@Override
		public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
			fillLocalizedStrings();
		}
		
		public JCloseableTab getTabLabel() {
			return tab;
		}
		
		public boolean isModified() {
			return isModified;
		}
		
		public void setModified(final boolean modified) {
			if (modified != isModified()) {
				this.tab.setIcon(modified ? SAVE_ICON : GRAY_SAVE_ICON);
			}
			isModified = modified; 
		}
		
		private void fillLocalizedStrings() {
			this.tab.setText(localizer.getValue(titleId));
		}
	}
	
	private class CreoleTab extends JPanelWithLabel {
		private static final long 	serialVersionUID = 7675426768332709976L;
		private final JCreoleEditor	editor = new JCreoleEditor();

		private final String				projectPartName;
		
		private CreoleTab(final Localizer localizer, final String partName, final String projectPartName, final String titleId) {
			super(localizer, partName, titleId);
			setLayout(new BorderLayout());

			this.projectPartName = projectPartName;
			this.editor.setText(project.getProjectPartContent(projectPartName));
			SwingUtils.assignActionKey(editor, SwingUtils.KS_SAVE, (e)->saveContent(), SwingUtils.ACTION_SAVE);
			
			SimpleTimerTask.start(()->{
				this.editor.getDocument().addDocumentListener(new DocumentListener() {
					@Override public void removeUpdate(DocumentEvent e) {setModified(true);}
					@Override public void insertUpdate(DocumentEvent e) {setModified(true);}
					@Override public void changedUpdate(DocumentEvent e) {setModified(true);}
				});
			}, 200);			

			add(new JScrollPane(editor));
		}
		
		public String getText() {
			return editor.getText();
		}

		public void setText(final String text) {
			if (text == null) {
				throw new NullPointerException("Text to set can't be null"); 
			}
			else {
				editor.setText(text);
			}
		}
		
		@Override
		public void saveContent() {
			project.setProjectPartContent(projectPartName, getText());
			setModified(false);
		}
	}

	private class ImageTab extends JPanelWithLabel {
		private static final long 	serialVersionUID = 7675426768332709976L;

		private final JBackgroundComponent	bc;
		private final String				projectPartName;
		
		private ImageTab(final Localizer localizer, final String partName, final String projectPartName, final String titleId) {
			super(localizer, partName, titleId);
			setLayout(new BorderLayout());

			this.projectPartName = projectPartName;
			this.bc = new JBackgroundComponent(localizer);
			this.bc.setFocusable(true);
			this.bc.setFillMode(FillMode.ORIGINAL);
			this.bc.setBackgroundImage((Image)project.getProjectPartContent(projectPartName));
			SwingUtilities.invokeLater(()->bc.requestFocusInWindow());
			SwingUtils.assignActionKey(bc, SwingUtils.KS_SAVE, (e)->saveContent(), SwingUtils.ACTION_SAVE);
			SwingUtils.assignActionKey(bc, SwingUtils.KS_PASTE, (e)->paste(), SwingUtils.ACTION_PASTE);

			add(new JScrollPane(bc));
		}
		
		public Image getImage() {
			return bc.getBackgroundImage();
		}

		public void setImage(final Image image) {
			if (image == null) {
				throw new NullPointerException("Image to set can't be null"); 
			}
			else {
				bc.setBackgroundImage(image);
			}
		}
		
		@Override
		public void saveContent() {
			project.setProjectPartContent(projectPartName, getImage());
			setModified(false);
		}
		
		private void paste() {
			final Transferable 	transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
			
			if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
			    try{setImage((Image) transferable.getTransferData(DataFlavor.imageFlavor));
			    	setModified(true);
				} catch (UnsupportedFlavorException | IOException e) {
					SwingUtils.getNearestLogger(this).message(Severity.error, e, e.getLocalizedMessage());
				}
			}
		}
	}
}
