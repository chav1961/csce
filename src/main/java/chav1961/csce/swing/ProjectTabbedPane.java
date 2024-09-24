package chav1961.csce.swing;




import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.datatransfer.FlavorListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Hashtable;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.RuntimeErrorException;
import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoManager;

import chav1961.csce.Application;
import chav1961.csce.project.ProjectContainer;
import chav1961.csce.project.ProjectNavigator.ItemType;
import chav1961.csce.project.ProjectNavigator.ProjectNavigatorItem;
import chav1961.csce.utils.SearchUtils;
import chav1961.purelib.basic.MimeType;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SimpleTimerTask;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.enumerations.MarkupOutputFormat;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.streams.char2char.CreoleWriter;
import chav1961.purelib.ui.swing.JToolBarWithMeta;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JBackgroundComponent;
import chav1961.purelib.ui.swing.useful.JBackgroundComponent.FillMode;
import chav1961.purelib.ui.swing.useful.JFileSelectionDialog.FilterCallback;
import chav1961.purelib.ui.swing.useful.JCloseableTab;
import chav1961.purelib.ui.swing.useful.JCreoleEditor;
import chav1961.purelib.ui.swing.useful.JEnableMaskManipulator;
import chav1961.purelib.ui.swing.useful.JFileSelectionDialog;
import chav1961.purelib.ui.swing.useful.JLocalizedOptionPane;
import chav1961.purelib.ui.swing.useful.LocalizedFormatter;

import chav1961.bt.paint.control.ImageEditPanel;
import chav1961.bt.paint.interfaces.PaintScriptException;

public class ProjectTabbedPane extends JTabbedPane implements LocaleChangeListener {
	private static final long 		serialVersionUID = 1L;

	
	private static final Icon		SAVE_ICON = new ImageIcon(ProjectTabbedPane.class.getResource("icon_save_16.png"));
	private static final Icon		GRAY_SAVE_ICON = new ImageIcon(GrayFilter.createDisabledImage(((ImageIcon)SAVE_ICON).getImage()));
	private static final String		MENU_EDIT_UNDO = "menu.main.edit.undo";
	private static final String		MENU_EDIT_REDO = "menu.main.edit.redo";
	private static final String		MENU_EDIT_CUT = "menu.main.edit.cut";
	private static final String		MENU_EDIT_COPY = "menu.main.edit.copy";
	private static final String		MENU_EDIT_PASTE = "menu.main.edit.paste";
	private static final String		MENU_EDIT_PASTE_LINK = "menu.main.edit.pasteLink";
	private static final String		MENU_EDIT_PASTE_IMAGE = "menu.main.edit.pasteImage";
	private static final String		MENU_EDIT_FIND = "menu.main.edit.find";
	private static final String		MENU_EDIT_FIND_REPLACE = "menu.main.edit.findreplace";
	private static final String		MENU_EDIT_CAPTION_UP = "menu.main.edit.captionUp";
	private static final String		MENU_EDIT_CAPTION_DOWN = "menu.main.edit.captionDown";
	private static final String		MENU_EDIT_LIST_UP = "menu.main.edit.listUp";
	private static final String		MENU_EDIT_LIST_DOWN = "menu.main.edit.listDown";
	private static final String		MENU_EDIT_ORDERED_LIST_UP = "menu.main.edit.orderedListUp";
	private static final String		MENU_EDIT_ORDERED_LIST_DOWN = "menu.main.edit.orderedListDown";
	private static final String		MENU_EDIT_ORDERED_BOLD = "menu.main.edit.bold";
	private static final String		MENU_EDIT_ORDERED_ITALIC = "menu.main.edit.italic";
	private static final String		MENU_TOOLS_PREVIEW = "menu.main.tools.preview";

	private static final String[]	MENUS = {
										MENU_EDIT_UNDO,
										MENU_EDIT_REDO,
										MENU_EDIT_CUT,
										MENU_EDIT_COPY,
										MENU_EDIT_PASTE,
										MENU_EDIT_PASTE_LINK,
										MENU_EDIT_PASTE_IMAGE,
										MENU_EDIT_FIND,
										MENU_EDIT_FIND_REPLACE,
										MENU_EDIT_CAPTION_UP,
										MENU_EDIT_CAPTION_DOWN,
										MENU_EDIT_LIST_UP,
										MENU_EDIT_LIST_DOWN,
										MENU_EDIT_ORDERED_LIST_UP,
										MENU_EDIT_ORDERED_LIST_DOWN,
										MENU_EDIT_ORDERED_BOLD,
										MENU_EDIT_ORDERED_ITALIC,
										MENU_TOOLS_PREVIEW
									};

	private static final long 		EDIT_UNDO = 1L << 0;
	private static final long 		EDIT_REDO = 1L << 1;
	private static final long 		EDIT_CUT = 1L << 2;
	private static final long 		EDIT_COPY = 1L << 3;
	private static final long 		EDIT_PASTE = 1L << 4;
	private static final long 		EDIT_PASTE_LINK = 1L << 5;
	private static final long 		EDIT_PASTE_IMAGE = 1L << 6;
	private static final long 		EDIT_FIND = 1L << 7;
	private static final long 		EDIT_FIND_REPLACE = 1L << 8;
	private static final long 		EDIT_CAPTION_UP = 1L << 9;
	private static final long 		EDIT_CAPTION_DOWN = 1L << 10;
	private static final long 		EDIT_LIST_UP = 1L << 11;
	private static final long 		EDIT_LIST_DOWN = 1L << 12;
	private static final long 		EDIT_ORDERED_LIST_UP = 1L << 13;
	private static final long 		EDIT_ORDERED_LIST_DOWN = 1L << 14;
	private static final long 		EDIT_ORDERED_BOLD = 1L << 15;
	private static final long 		EDIT_ORDERED_ITALIC = 1L << 16;	
	private static final long 		TOOLS_PREVIEW = 1L << 17;	
	private static final long 		TOTAL_EDIT = EDIT_PASTE_LINK | EDIT_PASTE_IMAGE | EDIT_FIND | EDIT_FIND_REPLACE | TOOLS_PREVIEW;	
	private static final long 		TOTAL_EDIT_SELECTION = EDIT_CUT | EDIT_COPY | EDIT_CAPTION_UP | EDIT_CAPTION_DOWN | EDIT_LIST_UP | EDIT_LIST_DOWN | EDIT_ORDERED_LIST_UP | EDIT_ORDERED_LIST_DOWN | EDIT_ORDERED_BOLD | EDIT_ORDERED_ITALIC;	
	
	private static final FilterCallback	IMAGE_FILTER = FilterCallback.of("Image files", "*.png", "*.jpg");
	private static final FilterCallback	CREOLE_FILTER = FilterCallback.of("Creole files", "*.cre");
	private static final FilterCallback	DOCUMENT_FILTER = FilterCallback.of("Documents", "*.pdf");
	private static final Pattern	CLIPBOARD_REF = Pattern.compile("\\[\\[.*\\#(.*)\\|(.*)\\]\\]");
	
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
			SwingUtils.assignActionKey(tab, WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, SwingUtils.KS_EXIT, (e)->tab.setSouthPanelState(CreoleSouthPanel.NONE), SwingUtils.ACTION_EXIT);
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

		static final String	KEY_ASK_SAVE_TITLE = "chav1961.csce.swing.ProjectTabbedPane.CreoleTab.save.title";
		static final String	KEY_ASK_SAVE_MESSAGE = "chav1961.csce.swing.ProjectTabbedPane.CreoleTab.save.message";	
		static final String	KEY_SELECT_LINK = "chav1961.csce.swing.ProjectTabbedPane.CreoleTab.selectLink.title";
		
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

	private enum CreoleSouthPanel {
		NONE,
		FIND,
		FIND_REPLACE
	}
	
	private class CreoleTab extends JPanelWithLabel implements ModuleAccessor {
		private static final long 		serialVersionUID = 7675426768332709976L;
		private static final String		CARD_EDIT = "edit";
		private static final String		CARD_PREVIEW = "preview";
		
		private final JCreoleEditor		editor = new JCreoleEditor();
		private final JEditorPane		pane = new JEditorPane(MimeType.MIME_HTML_TEXT.toString(),"");
		private final CardLayout		card = new CardLayout();
		private final JPanel			inside = new JPanel(card);
		private final UndoManager		undoMgr = new UndoManager();
		private final JToolBar			toolbar;
		private final String			projectPartName;
		private final JEnableMaskManipulator	emm;
		private final FindPanel			findPanel;
		private final ReplacePanel		replacePanel;
		private final FlavorListener	fl = (e)->clipboardChanged();
		
		private CreoleSouthPanel		southPanel = CreoleSouthPanel.NONE;
		private boolean					inPreview = false;
		
		private CreoleTab(final Localizer localizer, final String partName, final String projectPartName, final String titleId) {
			super(localizer, partName, titleId);
			setLayout(new BorderLayout());

			this.projectPartName = projectPartName;
			this.editor.setText(project.getProjectPartContent(projectPartName));
			this.toolbar = SwingUtils.toJComponent(parent.getNodeMetadata().getOwner().byUIPath(URI.create("ui:/model/navigation.top.toolbarmenu")), JToolBar.class);
			this.findPanel = new FindPanel(localizer, editor, (p)->setSouthPanelState(CreoleSouthPanel.NONE));
			this.replacePanel = new ReplacePanel(localizer, editor, (p)->setSouthPanelState(CreoleSouthPanel.NONE));
			this.emm = new JEnableMaskManipulator(MENUS, toolbar);
			
			this.toolbar.setFloatable(false);
			SwingUtils.assignActionKey(editor, SwingUtils.KS_SAVE, (e)->saveContent(), SwingUtils.ACTION_SAVE);
			SwingUtils.assignActionKey(pane, SwingUtils.KS_EXIT, (e)->previewOff(), SwingUtils.ACTION_EXIT);
			SwingUtils.assignActionKey(this, WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, SwingUtils.KS_FORWARD, (e)->searchForward(), SwingUtils.ACTION_FORWARD);
			SwingUtils.assignActionKey(this, WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, SwingUtils.KS_BACKWARD, (e)->searchBackward(), SwingUtils.ACTION_BACKWARD);
			
			SimpleTimerTask.start(()->{
				this.editor.getDocument().addDocumentListener(new DocumentListener() {
					@Override public void removeUpdate(DocumentEvent e) {setModified(true);}
					@Override public void insertUpdate(DocumentEvent e) {setModified(true);}
					@Override public void changedUpdate(DocumentEvent e) {setModified(true);}
				});
			}, 200);
			SwingUtils.assignActionListeners(toolbar, this);
			((JToolBarWithMeta)toolbar).assignAccelerators(editor);

			editor.addCaretListener((e)->refreshSelection());
			editor.getDocument().addUndoableEditListener((e)->processUndo(e));
			pane.setEditable(false);
			addComponentListener(new ComponentListener() {
				@Override public void componentResized(ComponentEvent e) {}
				@Override public void componentMoved(ComponentEvent e) {}
				
				@Override
				public void componentShown(final ComponentEvent e) {
					Toolkit.getDefaultToolkit().getSystemClipboard().addFlavorListener(fl);
				}
				
				@Override
				public void componentHidden(final ComponentEvent e) {
					Toolkit.getDefaultToolkit().getSystemClipboard().removeFlavorListener(fl);
				}
			});

			
			inside.add(new JScrollPane(editor), CARD_EDIT);
			inside.add(new JScrollPane(pane), CARD_PREVIEW);
			
			add(toolbar, BorderLayout.NORTH);
			add(inside, BorderLayout.CENTER);
			emm.setEnableMaskOn(TOTAL_EDIT);
			refreshUndoMenu();
			clipboardChanged();
			card.show(inside, CARD_EDIT);
		}
		
		private void clipboardChanged() {
			emm.setEnableMaskTo(EDIT_PASTE, Toolkit.getDefaultToolkit().getSystemClipboard().isDataFlavorAvailable(DataFlavor.stringFlavor));
		}

		@Override
		public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
			super.localeChanged(oldLocale, newLocale);
			findPanel.localeChanged(oldLocale, newLocale);
			replacePanel.localeChanged(oldLocale, newLocale);
		}
			
		@OnAction("action:/undo")
		public void undo() {
			if (undoMgr.canUndo()) {
				undoMgr.undo();
				refreshUndoMenu();
			}
		}
		
		@OnAction("action:/redo")
		public void redo() {
			if (undoMgr.canRedo()) {
				undoMgr.redo();
				refreshUndoMenu();
			}
		}

		
		@OnAction("action:/cut")
		public void cut() {
			editor.cut();
		}
		
		@OnAction("action:/copy")
		public void copy() {
			editor.copy();
		}

		@OnAction("action:/paste")
		public void paste() {
			editor.paste();
		}

		@OnAction("action:/pasteLinkPage")
		public void pasteLinkPage() {
			try{final PageLinkEditor	ple = new PageLinkEditor(SwingUtils.getNearestLogger(this));
				final Clipboard			cb = Toolkit.getDefaultToolkit().getSystemClipboard();
				final String			cbString = cb.isDataFlavorAvailable(DataFlavor.stringFlavor) ? cb.getData(DataFlavor.stringFlavor).toString() : "";
				final Matcher			m = CLIPBOARD_REF.matcher(cbString);
				final String			caption = editor.getSelectedText(); 
			
				if (m.find()) {
					ple.ref = m.group(1);
					ple.caption = caption.isEmpty() ? m.group(2) : caption; 
				}
				else {
					ple.ref = "";
					ple.caption = caption; 
				}
			
				if (Application.ask(ple, parent.getLocalizer(), 400, 100)) {
					editor.replaceSelection(" [[#"+ple.ref.replace(' ', '_')+'|'+ple.caption+"]] ");
				}
			} catch (ContentException | UnsupportedFlavorException | IOException e) {
				SwingUtils.getNearestLogger(this).message(Severity.error, e, e.getLocalizedMessage());
			}
		}
		
		@OnAction("action:/pasteLinkInner")
		public void pasteLinkInner() {
			try{for(String item : JFileSelectionDialog.select(parent, localizer, project.getFileSystem(), JFileSelectionDialog.OPTIONS_FOR_OPEN | JFileSelectionDialog.OPTIONS_CAN_SELECT_FILE | JFileSelectionDialog.OPTIONS_FILE_MUST_EXISTS, CREOLE_FILTER, DOCUMENT_FILTER)) {
					if (item.endsWith(".cre")) {
						final String	content;
						
						try(final FileSystemInterface	fsi = project.getFileSystem().clone().open(item);
							final Reader				rdr = fsi.charRead();
							final Writer				wr = new StringWriter()) {
							
							Utils.copyStream(rdr, wr);
							content = wr.toString();
						}
						
						if (!Utils.checkEmptyOrNullString(content)) {
							final String[]		refs = SearchUtils.extractCreoleAnchors(content);
							final JList<String>	listRefs = new JList<>(refs);
							
							if (new JLocalizedOptionPane(parent.getLocalizer()).confirm(this, new JScrollPane(listRefs), KEY_SELECT_LINK, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION && listRefs.getSelectedIndex() >= 0) {
								editor.replaceSelection(" [["+item+"#"+listRefs.getSelectedValue()+"|здесь]] ");
							}
							else {
								editor.replaceSelection(" [["+item+"|здесь]] ");
							}
						}
						else {
							editor.replaceSelection(" [["+item+"|здесь]] ");
						}
					}
					else {
						editor.replaceSelection(" [["+item+"|здесь]] ");
					}
				}
			} catch (IOException e) {
				SwingUtils.getNearestLogger(this).message(Severity.error, e, e.getLocalizedMessage());
			}		
		}
		
		@OnAction("action:/pasteLinkExt")
		public void pasteLinkExt() {
			try{final ExternalLinkEditor	ele = new ExternalLinkEditor(SwingUtils.getNearestLogger(this));
			
				ele.caption = editor.getSelectedText();
				ele.ref = URI.create("https://./");
			
				if (Application.ask(ele, parent.getLocalizer(), 400, 100)) {
					editor.replaceSelection(" [["+ele.ref+"|"+ele.caption+"]] ");
				}
			} catch (ContentException e) {
				SwingUtils.getNearestLogger(this).message(Severity.error, e, e.getLocalizedMessage());
			}
		}

		@OnAction("action:/pasteImage")
		public void pasteImage() {
			try{for(String item : JFileSelectionDialog.select(parent, localizer, project.getFileSystem(), JFileSelectionDialog.OPTIONS_FOR_OPEN | JFileSelectionDialog.OPTIONS_CAN_SELECT_FILE | JFileSelectionDialog.OPTIONS_FILE_MUST_EXISTS, IMAGE_FILTER)) {
					final String	lastComponent = item.substring(item.lastIndexOf('/')+1);
					
					editor.replaceSelection(" {{file:"+item+"|"+lastComponent+"}} ");
				}
			} catch (IOException e) {
				SwingUtils.getNearestLogger(this).message(Severity.error, e, e.getLocalizedMessage());
			}		
		}
		
		@OnAction("action:/find")
		public void find() {
			setSouthPanelState(CreoleSouthPanel.FIND);
		}
		
		@OnAction("action:/findreplace")
		public void findReplace() {
			setSouthPanelState(CreoleSouthPanel.FIND_REPLACE);
		}

		@OnAction("action:/paragraphCaptionUp")
		public void paragraphCaptionUp() {
			final int	pos = Math.min(editor.getCaret().getDot(),editor.getCaret().getMark());
			String		text = editor.getSelectedText();
			
			if (text.length() > 0) {
				if (text.startsWith("=")) {
					text = text.substring(1);
				}
				else {
					text = "======"+text;
				}
				editor.replaceSelection(text);
				editor.setSelectionStart(pos);
				editor.setSelectionEnd(pos + text.length());
			}
		}
		
		@OnAction("action:/paragraphCaptionDown")
		public void paragraphCaptionDown() {
			final int	pos = Math.min(editor.getCaret().getDot(),editor.getCaret().getMark());
			String		text = editor.getSelectedText();
			
			if (text.length() > 0) {
				if (text.startsWith("======")) {
					text = text.substring(6);
				}
				else {
					text = '=' + text;
				}
				editor.replaceSelection(text);
				editor.setSelectionStart(pos);
				editor.setSelectionEnd(pos + text.length());
			}
		}
		
		@OnAction("action:/paragraphListUp")
		public void paragraphListUp() {
			final int		pos = Math.min(editor.getCaret().getDot(),editor.getCaret().getMark());
			final String	text = editor.getSelectedText();
			
			if (text.length() > 0) {
				final StringBuilder	sb = new StringBuilder();
				
				for (String line : text.split("\n")) {
					if (line.startsWith("*")) {
						sb.append(line.substring(1)).append('\n');
					}
					else {
						sb.append(line).append('\n');
					}
				}
				editor.replaceSelection(sb.toString());
				editor.setCaretPosition(pos);
				editor.setSelectionStart(pos);
				editor.setSelectionEnd(pos + sb.length());
			}
		}
		
		@OnAction("action:/paragraphListDown")
		public void paragraphListDown() {
			final int		pos = Math.min(editor.getCaret().getDot(),editor.getCaret().getMark());
			final String	text = editor.getSelectedText();
			int				mark;
			
			if (text.length() > 0) {
				final StringBuilder	sb = new StringBuilder("\n");
				
				for (String line : text.split("\n")) {
					sb.append('*').append(line).append('\n');
				}
				editor.replaceSelection(sb.toString());
				mark = pos + sb.length();
				editor.setSelectionStart(pos);
				editor.setSelectionEnd(mark);
			}
		}
		
		@OnAction("action:/paragraphOrderedListUp")
		public void paragraphOrderedListUp() {
			final int		pos = Math.min(editor.getCaret().getDot(),editor.getCaret().getMark());
			final String	text = editor.getSelectedText();
			int				mark;
			
			if (text.length() > 0) {
				final StringBuilder	sb = new StringBuilder();
				
				for (String line : text.split("\n")) {
					if (line.startsWith("#")) {
						sb.append(line.substring(1)).append('\n');
					}
					else {
						sb.append(line).append('\n');
					}
				}
				editor.replaceSelection(sb.toString());
				mark = pos + sb.length();
				editor.setSelectionStart(pos);
				editor.setSelectionEnd(mark);
			}
		}
		
		@OnAction("action:/paragraphOrderedListDown")
		public void paragraphOrderedListDown() {
			final int		pos = Math.min(editor.getCaret().getDot(),editor.getCaret().getMark());
			final String	text = editor.getSelectedText();
			int				mark;
			
			if (text.length() > 0) {
				final StringBuilder	sb = new StringBuilder("\n");
				
				for (String line : text.split("\n")) {
					sb.append('#').append(line).append('\n');
				}
				editor.replaceSelection(sb.toString());
				mark = pos + sb.length();
				editor.setSelectionStart(pos);
				editor.setSelectionEnd(mark);
			}
		}

		@OnAction("action:/paragraphInsertTable")
		public void paragraphInsertTable() {
			try{final InsertTableDialog itd = new InsertTableDialog(SwingUtils.getNearestLogger(this));
			
				if (Application.ask(itd, parent.getLocalizer(), 300, 100)) {
					final StringBuilder	sb = new StringBuilder("\n");
					
					if (itd.captionRequired) {
						for (int col = 0; col < itd.numberOfColumns; col++) {
							sb.append("|=caption").append(col+1);
						}
						sb.append("|\n");
					}
					for (int row = 0; row < itd.numberOfRows; row++) {
						for (int col = 0; col < itd.numberOfColumns; col++) {
							sb.append("|value").append(row+1).append("-").append(col+1);
						}
						sb.append("|\n");
					}
					sb.append('\n');
					editor.replaceSelection(sb.toString());
				}
			} catch (ContentException e) {
				SwingUtils.getNearestLogger(this).message(Severity.error, e, e.getLocalizedMessage());
			}
		}
		
		@OnAction("action:/fontBold")
		public void fontBold() {
			final int	pos = Math.min(editor.getCaret().getDot(),editor.getCaret().getMark());
			int			mark;
			String		text = editor.getSelectedText();
			
			if (text.length() > 0) {
				if (text.startsWith("**") && text.endsWith("**")) {
					if (text.length() > 4) {
						editor.replaceSelection(text.substring(2, text.length()-2));
						mark = pos + text.length() - 4; 
					}
					else {
						editor.replaceSelection("");
						mark = 0;
					}
				}
				else {
					editor.replaceSelection("**"+text+"**");
					mark = pos + text.length() + 4;
				}
				editor.setSelectionStart(pos);
				editor.setSelectionEnd(mark);
			}
		}
		
		@OnAction("action:/fontItalic")
		public void fontItalic() {
			final int	pos = Math.min(editor.getCaret().getDot(),editor.getCaret().getMark());
			int			mark;
			String		text = editor.getSelectedText();

			if (text.length() > 0) {
				if (text.startsWith("//") && text.endsWith("//")) {
					if (text.length() > 4) {
						editor.replaceSelection(text.substring(2, text.length()-2));
						mark = pos + text.length() - 4; 
					}
					else {
						editor.replaceSelection("");
						mark = 0;
					}
				}
				else {
					editor.replaceSelection("//"+text+"//");
					mark = pos + text.length() + 4;
				}
				editor.setSelectionStart(pos);
				editor.setSelectionEnd(mark);
			}
		}
		
		@OnAction("action:/previewProject")
		public void previewProject(final Hashtable<String,String[]> modes) {
			if (!inPreview) {
				previewOn();
			}
			else {
				previewOff();
			}
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
			undoMgr.discardAllEdits();
			refreshUndoMenu();
			setModified(false);
		}

		@Override
		public void allowUnnamedModuleAccess(Module... unnamedModules) {
			for (Module item : unnamedModules) {
				this.getClass().getModule().addExports(this.getClass().getPackageName(),item);
			}
		}

		private void searchForward() {
			switch (southPanel) {
				case FIND			:
					findPanel.searchForward();
					break;
				case FIND_REPLACE	:
					replacePanel.searchForward();
					break;
				case NONE			:
					break;
				default:
					throw new UnsupportedOperationException("Panel state ["+southPanel+"] is not supported yet");
			}
		}

		private void searchBackward() {
			switch (southPanel) {
				case FIND			:
					findPanel.searchBackward();
					break;
				case FIND_REPLACE	:
					replacePanel.searchBackward();
					break;
				case NONE			:
					break;
				default:
					throw new UnsupportedOperationException("Panel state ["+southPanel+"] is not supported yet");
			}
		}
		
		private void previewOn() {
			try(final Writer			wr = new StringWriter()) {
				
				try(final CreoleWriter	cwr = new CreoleWriter(wr, MarkupOutputFormat.XML2HTML)) {
					cwr.write(editor.getText());
				}
				
				pane.setText(wr.toString());
				inPreview = true;
				emm.setCheckMaskOn(TOOLS_PREVIEW);
				card.show(inside, CARD_PREVIEW);
				pane.requestFocusInWindow();
			} catch (IOException e) {
				SwingUtils.getNearestLogger(this).message(Severity.error, e, e.getLocalizedMessage());
			}
		}

		private void previewOff() {
			inPreview = false;
			emm.setCheckMaskOff(TOOLS_PREVIEW);
			card.show(inside, CARD_EDIT);
			editor.requestFocusInWindow();
		}
		
		private void processUndo(final UndoableEditEvent e) {
			if (!editor.isHighlightingLocked()) {
				undoMgr.addEdit(e.getEdit());
				refreshUndoMenu();
			}
		}
		
		private void refreshUndoMenu() {
			emm.setEnableMaskTo(EDIT_UNDO, undoMgr.canUndo());
			emm.setEnableMaskTo(EDIT_REDO, undoMgr.canRedo());
		}
		
		private void refreshSelection() {
			if (editor.getSelectedText() == null || editor.getSelectedText().isEmpty()) {
				emm.setEnableMaskOff(TOTAL_EDIT_SELECTION);
			}
			else {
				emm.setEnableMaskOn(TOTAL_EDIT_SELECTION);
			}
		}
		
		private void setSouthPanelState(final CreoleSouthPanel newState) {
			if (southPanel != newState) {
				switch (southPanel) {
					case FIND			:
						remove(findPanel);
						break;
					case FIND_REPLACE	:
						remove(replacePanel);
						break;
					case NONE			:
						break;
					default :
						throw new UnsupportedOperationException("Panel state ["+southPanel+"] is not supported yet"); 
				}
				southPanel = newState;
				switch (southPanel) {
					case FIND			:
						add(findPanel, BorderLayout.SOUTH);
						findPanel.requestFocusInWindow();
						break;
					case FIND_REPLACE	:
						add(replacePanel, BorderLayout.SOUTH);
						replacePanel.requestFocusInWindow();
						break;
					case NONE			:
						editor.requestFocusInWindow();
						break;
					default :
						throw new UnsupportedOperationException("Panel state ["+southPanel+"] is not supported yet"); 
				}
				revalidate();
				repaint();
			}
		}
	}

	private class ImageTab extends JPanelWithLabel {
		private static final long 	serialVersionUID = 7675426768332709976L;

		private final ImageEditPanel		bc;
		private final String				projectPartName;
		
		private ImageTab(final Localizer localizer, final String partName, final String projectPartName, final String titleId) {
			super(localizer, partName, titleId);
			setLayout(new BorderLayout());

			this.projectPartName = projectPartName;
			this.bc = new ImageEditPanel(localizer);
			this.bc.setFocusable(true);
			this.bc.setImage((Image)project.getProjectPartContent(projectPartName));
			SwingUtilities.invokeLater(()->bc.requestFocusInWindow());
			SwingUtils.assignActionKey(bc, SwingUtils.KS_SAVE, (e)->saveContent(), SwingUtils.ACTION_SAVE);
			SwingUtils.assignActionKey(bc, SwingUtils.KS_PASTE, (e)->paste(), SwingUtils.ACTION_PASTE);

			add(new JScrollPane(bc));
		}
		
		public Image getImage() {
			try{return bc.getImage().getImage();
			} catch (PaintScriptException e) {
				throw new RuntimeException(e);
			}
		}

		public void setImage(final Image image) {
			if (image == null) {
				throw new NullPointerException("Image to set can't be null"); 
			}
			else {
				bc.setImage(image);
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
