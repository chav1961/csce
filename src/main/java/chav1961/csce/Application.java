package chav1961.csce;


import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.border.EtchedBorder;

import chav1961.csce.builders.HTMLBuilder;
import chav1961.csce.project.ProjectChangeEvent;
import chav1961.csce.project.ProjectChangeEvent.ProjectChangeType;
import chav1961.csce.project.ProjectContainer;
import chav1961.csce.project.ProjectNavigator.ItemType;
import chav1961.csce.project.ProjectNavigator.ProjectNavigatorItem;
import chav1961.csce.swing.ProjectItemEditor;
import chav1961.csce.swing.ProjectPartEditor;
import chav1961.csce.swing.ProjectPropertiesEditor;
import chav1961.csce.swing.ProjectVariablesEditor;
import chav1961.csce.swing.ProjectViewer;
import chav1961.csce.swing.ProjectViewerChangeEvent;
import chav1961.csce.swing.Scorm2004SettingsEditor;
import chav1961.csce.swing.SettingsEditor;
import chav1961.csce.swing.WarSettingsEditor;
import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.fsys.FileSystemFactory;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.i18n.interfaces.LocalizerOwner;
import chav1961.purelib.i18n.interfaces.SupportedLanguages;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.nanoservice.NanoServiceFactory;
import chav1961.purelib.nanoservice.StaticHelp;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.LRUPersistence;
import chav1961.purelib.ui.swing.AutoBuiltForm;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JDialogContainer;
import chav1961.purelib.ui.swing.useful.JDialogContainer.JDialogContainerOption;
import chav1961.purelib.ui.swing.useful.JEnableMaskManipulator;
import chav1961.purelib.ui.swing.useful.JFileContentManipulator;
import chav1961.purelib.ui.swing.useful.JFileSelectionDialog;
import chav1961.purelib.ui.swing.useful.JFileSelectionDialog.FilterCallback;
import chav1961.purelib.ui.swing.useful.JLocalizedOptionPane;
import chav1961.purelib.ui.swing.useful.JSimpleSplash;
import chav1961.purelib.ui.swing.useful.JStateString;
import chav1961.purelib.ui.swing.useful.JTabbedEditor;
import chav1961.purelib.ui.swing.useful.LocalizedFormatter;
import chav1961.purelib.ui.swing.useful.interfaces.FileContentChangedEvent;

// https://burakkanber.com/blog/machine-learning-full-text-search-in-javascript-relevance-scoring/
// https://stackoverflow.com/questions/4437910/java-pdf-viewer
// https://www.lucenetutorial.com/lucene-query-syntax.html
public class Application  extends JFrame implements AutoCloseable, NodeMetadataOwner, LocaleChangeListener, LoggerFacadeOwner, LocalizerOwner  {
	private static final long 		serialVersionUID = 8855923580582029585L;
	
	public static final String		ARG_HELP_PORT = "helpPort";
	public static final String		ARG_PROPFILE_LOCATION = "prop";
	public static final String		DEFAULT_ARG_PROPFILE_LOCATION = "./.csce.properties";
	public static final String		PROP_AUTOMATIC_PASTE = "automaticPaste";
	public static final String		PROP_CHECK_EXTERNAL_LINKS = "checkExternalLinks";
	public static final String		PROP_PREFERRED_LANG = "preferredLanguage";

	public static final String		KEY_FILTER_CREOLE_FILE = "chav1961.csce.Application.filter.creole.file";
	public static final String		KEY_FILTER_PDF_FILE = "chav1961.csce.Application.filter.pdf.file";
	public static final String		KEY_FILTER_DJVU_FILE = "chav1961.csce.Application.filter.djvu.file";
	public static final String		KEY_FILTER_IMAGE_FILE = "chav1961.csce.Application.filter.image.file";
	public static final String		KEY_FILTER_ZIP_FILE = "chav1961.csce.Application.filter.zip.file";
	
	public static final FilterCallback	CREOLE_FILTER = FilterCallback.ofWithExtension(KEY_FILTER_CREOLE_FILE, "cre", "*.cre"); 	
	public static final FilterCallback	PDF_FILTER = FilterCallback.ofWithExtension(KEY_FILTER_PDF_FILE, "pdf", "*.pdf"); 	
	public static final FilterCallback	DJVU_FILTER = FilterCallback.ofWithExtension(KEY_FILTER_DJVU_FILE, "djvu", "*.djv", "*.djvu"); 	
	public static final FilterCallback	IMAGE_FILTER = FilterCallback.ofWithExtension(KEY_FILTER_IMAGE_FILE, "png", "*.png", "*.jpg"); 	
	public static final FilterCallback	ZIP_FILTER = FilterCallback.ofWithExtension(KEY_FILTER_ZIP_FILE, "zip", "*.zip"); 	
	
	static final String				PROJECT_SUFFIX = "csc";
	
	private static final String		LRU_PREFIX = "lru";
	private static final String		EXPORT_LRU_PREFIX = "export_lru";
	private static final String		PREVIEW_DIR = "preview";
	
	private static final FilterCallback	FILE_FILTER = FilterCallback.ofWithExtension("CSC project", PROJECT_SUFFIX, "*."+PROJECT_SUFFIX);
	private static final FilterCallback	EXPORT_WAR_FILTER = FilterCallback.ofWithExtension("WAR plugin", "war", "*.war");
	private static final FilterCallback	EXPORT_SCORM2004_FILTER = FilterCallback.ofWithExtension("Scorm 2004 plugin", "scorm", "*.scorm");
	private static final FilterCallback	EXPORT_DIRECTORY_FILTER = FilterCallback.ofWithExtension("Packed directory content", "zip", "*.zip");
	
	public static final String		KEY_APPLICATION_FRAME_TITLE = "chav1961.csce.Application.frame.title";
	public static final String		KEY_APPLICATION_HELP_TITLE = "chav1961.csce.Application.help.title";
	public static final String		KEY_APPLICATION_HELP_CONTENT = "chav1961.csce.Application.help.content";

	public static final String		KEY_APPLICATION_CONFIRM_DELETE_TITLE = "chav1961.csce.Application.confirm.delete.title";
	public static final String		KEY_APPLICATION_CONFIRM_DELETE_MESSAGE = "chav1961.csce.Application.confirm.delete.message";

	public static final String		KEY_APPLICATION_CONFIRM_REPLACE_TITLE = "chav1961.csce.Application.confirm.replace.title";
	public static final String		KEY_APPLICATION_CONFIRM_REPLACE_MESSAGE = "chav1961.csce.Application.confirm.replace.message";

	public static final String		KEY_APPLICATION_PROJECT_PROPERTIES_TITLE = "chav1961.csce.Application.project.properties.title";
	public static final String		KEY_APPLICATION_PROJECT_VARIABLES_TITLE = "chav1961.csce.Application.project.variables.title";
	
	public static final String		KEY_APPLICATION_MESSAGE_READY = "chav1961.csce.Application.message.ready";
	public static final String		KEY_APPLICATION_MESSAGE_FILE_NOT_EXISTS = "chav1961.csce.Application.message.file.not.exists";
	public static final String		KEY_APPLICATION_MESSAGE_VALIDATION_SUCCESSFUL = "chav1961.csce.Application.message.validation.successful";
	public static final String		KEY_APPLICATION_MESSAGE_VALIDATION_FAILED = "chav1961.csce.Application.message.validation.failed";
	public static final String		KEY_APPLICATION_MESSAGE_DESKTOP_IS_NOT_SUPPORTED = "chav1961.csce.Application.message.desktop.is.not.supported";

	
	private static final String		MENU_FILE_LRU = "menu.main.file.lru";
	private static final String		MENU_FILE_SAVE = "menu.main.file.save";
	private static final String		MENU_FILE_SAVEAS = "menu.main.file.saveAs";
	private static final String		MENU_FILE_EXPORT = "menu.main.file.export";
	private static final String		MENU_EDIT = "menu.main.edit";
	private static final String		MENU_EDIT_UNDO = "menu.main.edit.undo";
	private static final String		MENU_EDIT_REDO = "menu.main.edit.redo";
	private static final String		MENU_INSERT = "menu.main.insert";
	private static final String		MENU_TOOLS_VALIDATE = "menu.main.tools.validate";
	private static final String		MENU_TOOLS_PREVIEW = "menu.main.tools.preview";
	private static final String		MENU_TOOLS_BUILD_INDEX = "menu.main.tools.buildIndex";
	private static final String		MENU_FILE_EXPORT_LRU = "menu.main.file.export.lru";
	private static final String		MENU_INSERT_IMAGE_FROM_CLIPBOARD = "menu.main.insert.image.from.clipboard";
	

	private static final String[]	MENUS = {
										MENU_FILE_LRU,
										MENU_FILE_SAVE,
										MENU_FILE_SAVEAS,
										MENU_FILE_EXPORT,
										MENU_EDIT,
										MENU_EDIT_UNDO,
										MENU_EDIT_REDO,
										MENU_INSERT,
										MENU_TOOLS_VALIDATE,
										MENU_TOOLS_PREVIEW,
										MENU_TOOLS_BUILD_INDEX,
										MENU_FILE_EXPORT_LRU,
										MENU_INSERT_IMAGE_FROM_CLIPBOARD
									};
	
	private static final long 		FILE_LRU = 1L << 0;
	private static final long 		FILE_SAVE = 1L << 1;
	private static final long 		FILE_SAVEAS = 1L << 2;
	private static final long 		FILE_EXPORT = 1L << 3;
	private static final long 		EDIT = 1L << 4;
	private static final long 		EDIT_UNDO = 1L << 5;
	private static final long 		EDIT_REDO = 1L << 6;
	private static final long 		INSERT = 1L << 7;
	private static final long 		TOOLS_VALIDATE = 1L << 8;
	private static final long 		TOOLS_PREVIEW = 1L << 9;
	private static final long 		TOOLS_BUILD_INDEX = 1L << 10;
	private static final long 		FILE_EXPORT_LRU = 1L << 11;
	private static final long 		INSERT_IMAGE_FROM_CLIPBOARD = 1L << 12;

	private static enum ExportFormat {
		AS_WAR(EXPORT_WAR_FILTER),
		AS_SCORM_2004(EXPORT_SCORM2004_FILTER),
		AS_DIRECTORY(EXPORT_DIRECTORY_FILTER);
		
		private final FilterCallback	callback;
		
		private ExportFormat(final FilterCallback callback) {
			this.callback = callback;
		}
		
		public FilterCallback getCallbackFilter() {
			return callback;
		}
	}

	private final File						propFile;
	private final URI						helpServerURI;
	private final ContentMetadataInterface	mdi;
	private final Localizer					localizer;
	private final FileSystemInterface		repo = FileSystemFactory.createFileSystem(URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":file:/"));
	private final JMenuBar					menuBar;
	private final JStateString				state;
	private final LRUPersistence			lru;
	private final JFileContentManipulator	fcm;
	private final LRUPersistence			exportLru;
	private final JFileContentManipulator	exportFcm;
	private final ProjectContainer			project;
	private final CountDownLatch			latch = new CountDownLatch(1);
	private final JEnableMaskManipulator	emm;
	private final SettingsEditor			se;

	private FirstScreen						firstScreen = null; 
	private ProjectViewer					viewer = null;
	private ExportFormat					exportFormat = null;
	
	public Application(final File propFile, final URI helpServerURI) throws ContentException, IOException {
		try(final InputStream		is = this.getClass().getResourceAsStream("application.xml");) {
			
			this.mdi = ContentModelFactory.forXmlDescription(is);
			this.project = new ProjectContainer(this, mdi);
			this.localizer = Localizer.Factory.newInstance(mdi.getRoot().getLocalizerAssociated());
	        this.localizer.addLocaleChangeListener(this);
			
			PureLibSettings.PURELIB_LOCALIZER.push(localizer);
		}

		this.propFile = propFile;
		this.helpServerURI = helpServerURI;
		this.menuBar = SwingUtils.toJComponent(mdi.byUIPath(URI.create("ui:/model/navigation.top.mainmenu")), JMenuBar.class); 
		this.emm = new JEnableMaskManipulator(MENUS, this.menuBar);
		this.state = new JStateString(localizer, 30);
		this.se = new SettingsEditor(state, SubstitutableProperties.of(propFile));		
		this.lru = LRUPersistence.of(propFile, LRU_PREFIX); 
		this.fcm = new JFileContentManipulator(repo, localizer, 
				()->project.toInputStream(), 
				()->project.fromOutputStream(), 
				lru);
		this.fcm.appendNewFileSupport();
		this.fcm.setFilters(FILE_FILTER);
		this.fcm.addFileContentChangeListener((e)->processLRU(e));
		this.exportLru = LRUPersistence.of(propFile, EXPORT_LRU_PREFIX); 
		this.exportFcm = new JFileContentManipulator(repo, localizer, 
				()->toInputStream(exportFormat), 
				()->new OutputStream() {@Override public void write(int b) throws IOException {}}, 
				exportLru);
		this.exportFcm.appendNewFileSupport();
		this.exportFcm.setFilters(EXPORT_DIRECTORY_FILTER);
		this.exportFcm.addFileContentChangeListener((e)->processExportLRU(e));
		
		state.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		setJMenuBar(menuBar);		
        getContentPane().add(firstScreen = new FirstScreen(this), BorderLayout.CENTER);
        getContentPane().add(state, BorderLayout.SOUTH);

        Toolkit.getDefaultToolkit().getSystemClipboard().addFlavorListener((e)->refreshPasteMenu());
        refreshPasteMenu();
        
        SwingUtils.assignActionListeners(menuBar, this);
		SwingUtils.assignExitMethod4MainWindow(this,()->exit());
        fillLRU(fcm.getLastUsed());
        fillExportLRU(exportFcm.getLastUsed());
		
		SwingUtils.centerMainWindow(this, 0.85f);
		fillLocalizationStrings();
	}

	@Override
	public Localizer getLocalizer() {
		return localizer;
	}

	@Override
	public LoggerFacade getLogger() {
		return state;
	}

	public URI getHelpServerURI() {
		return helpServerURI;
	}
	
	public FileSystemInterface getFileSystem() {
		return repo;
	}
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		SwingUtils.refreshLocale(getContentPane(), oldLocale, newLocale);
		SwingUtils.refreshLocale(menuBar, oldLocale, newLocale);
		SwingUtils.refreshLocale(state, oldLocale, newLocale);
		fillLocalizationStrings();
	}

	@Override
	public ContentNodeMetadata getNodeMetadata() {
		return mdi.getRoot();
	}

	@Override
	public void close() throws EnvironmentException {
		try{latch.await();

			repo.close();
			if (localizer != null) {
				getLocalizer().pop();
				getLocalizer().removeLocaleChangeListener(this);
			}
		} catch (InterruptedException | IOException e) {
			throw new ThreadDeath();
		} finally {
			dispose();
		}
	}
	
	@OnAction("action:/newProject")
	public void newProject() {
		try{fcm.newFile();
		} catch (IOException e) {
			getLogger().message(Severity.error, e, e.getLocalizedMessage());
		}
	}
	
	@OnAction("action:/openProject")
	public void openProject() {
		try{fcm.openFile();
		} catch (IOException e) {
			getLogger().message(Severity.error, e, e.getLocalizedMessage());
		}
	}
	
	@OnAction("action:/saveProject")
	public void saveProject() {
		try{fixProject();
			fcm.saveFile();
		} catch (IOException e) {
			getLogger().message(Severity.error, e, e.getLocalizedMessage());
		}
	}
	
	@OnAction("action:/saveProjectAs")
	public void saveProjectAs() {
		try{fixProject();
			fcm.saveFileAs();
		} catch (IOException e) {
			getLogger().message(Severity.error, e, e.getLocalizedMessage());
		}
	}
	
	@OnAction("action:/exportProjectAsWar")
	public void exportProjectAsWar() {
		try{exportFormat = ExportFormat.AS_WAR;
		
			exportFcm.setFilters(exportFormat.getCallbackFilter());
			exportFcm.saveFileAs();
		} catch (IOException e) {
			getLogger().message(Severity.error, e, e.getLocalizedMessage());
		}
	}
	
	@OnAction("action:/exportProjectAsScorm2004")
	public void exportProjectAsScorm2004() {
		try{exportFormat = ExportFormat.AS_SCORM_2004;
		
			exportFcm.setFilters(EXPORT_SCORM2004_FILTER);
			exportFcm.saveFileAs();
		} catch (IOException e) {
			getLogger().message(Severity.error, e, e.getLocalizedMessage());
		}
	}
	
	@OnAction("action:/exportProjectAsSubdir")
	public void exportProjectAsSubdir() {
		try{exportFormat = ExportFormat.AS_DIRECTORY;
		
			exportFcm.setFilters(EXPORT_DIRECTORY_FILTER);
			exportFcm.saveFileAs();
		} catch (IOException e) {
			getLogger().message(Severity.error, e, e.getLocalizedMessage());
		}
	}
	
	@OnAction("action:/exit")
	public void exit() throws IOException {
		if (fcm.commit()) {
			exportFcm.close();
			latch.countDown();
		}
	}
	
	@OnAction("action:/undo")
	public void undo() {
	}
	
	@OnAction("action:/redo")
	public void redo() {
	}
	
	@OnAction("action:/find")
	public void find() {
	}
	
	@OnAction("action:/findAndGo")
	public void findAndGo() {
	}
	
	@OnAction("action:/properties")
	public void properties() throws ContentException {
		final SubstitutableProperties	props = project.getProperties();
		final ProjectPropertiesEditor	ppe = new ProjectPropertiesEditor(getLogger(), props);
		final WarSettingsEditor			wse = new WarSettingsEditor(getLogger(), props);
		final Scorm2004SettingsEditor	s2004se = new Scorm2004SettingsEditor(getLogger(), props);

		try (final JTabbedEditor	jte = new JTabbedEditor(localizer, ppe, wse, s2004se)) {
			jte.setPreferredSize(new Dimension(500,300));
			jte.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			
			if (new JDialogContainer<>(getLocalizer(), this, KEY_APPLICATION_PROJECT_PROPERTIES_TITLE, jte).showDialog(JDialogContainerOption.DONT_USE_ENTER_AS_OK)) {
				ppe.storeProperties(props);
				wse.storeProperties(props);
				s2004se.storeProperties(props);
				fcm.setModificationFlag();
			}			
		}
	}

	@OnAction("action:/variables")
	public void variables() throws ContentException {
		final ProjectVariablesEditor	pve = new ProjectVariablesEditor(getLocalizer(), project.getProperties());
		
		if (new JDialogContainer<>(getLocalizer(), this, KEY_APPLICATION_PROJECT_VARIABLES_TITLE, pve).showDialog()) {
			pve.storeProperties(project.getProperties());
			fcm.setModificationFlag();
		}
	}
	
	@OnAction("action:/insertPart")
	public void insertPart() {
		if (viewer.isProjectNavigatorItemSelected()) {
			final ProjectNavigatorItem		pni = viewer.getProjectNavigatorItemSelected();
			final long						unique = project.getProjectNavigator().getUniqueId();
			final ProjectNavigatorItem		toAdd = new ProjectNavigatorItem(unique
																	, pni.id
																	, "part"+unique
																	, ItemType.Subtree
																	, "New part"
																	, project.createUniqueLocalizationString()
																	, -1);
			final ProjectPartEditor		ppe = new ProjectPartEditor(getLogger(), project, toAdd);
			
			try{if (ask(ppe, getLocalizer(), 400, 180)) {
					project.getProjectNavigator().addItem(ppe.getNavigatorItem());
				}
			} catch (ContentException e) {
				getLogger().message(Severity.error, e, e.getLocalizedMessage());
			}
		}
	}
	
	@OnAction("action:/insertBlankPage")
	public void insertBlankPage() {
		if (viewer.isProjectNavigatorItemSelected()) {
			final ProjectNavigatorItem	pni = viewer.getProjectNavigatorItemSelected();
			final long					unique = project.getProjectNavigator().getUniqueId();
			final ProjectNavigatorItem	toAdd = new ProjectNavigatorItem(unique
																	, pni.id
																	, ItemType.CreoleRef.getPartNamePrefix()+unique+".cre"
																	, ItemType.CreoleRef
																	, "Creole content"
																	, project.createUniqueLocalizationString()
																	, pni.id);
			final ProjectItemEditor		pie = new ProjectItemEditor(getLogger(), project, toAdd);
			
			try{if (ask(pie, getLocalizer(), 400, 180)) {
					project.getProjectNavigator().addItem(pie.getNavigatorItem());
					project.addProjectPartContent(project.getPartNameById(toAdd.id), "");
					viewer.getProjectTabbedPane().openCreoleTab(pie.getNavigatorItem());
				}
			} catch (ContentException e) {
				getLogger().message(Severity.error, e, e.getLocalizedMessage());
			}
		}
	}

	@OnAction("action:/insertPage")
	public void insertPage() {
		insertSomething(ItemType.CreoleRef, CREOLE_FILTER);
	}	
	
	@OnAction("action:/insertDocument")
	public void insertDocument() {
		insertSomething(ItemType.DocumentRef, PDF_FILTER, DJVU_FILTER);
	}
	
	@OnAction("action:/insertImage")
	public void insertImage() {
		insertSomething(ItemType.ImageRef, IMAGE_FILTER);
	}
	
	@OnAction("action:/insertImageFromClipboard")	
	public void insertImageFromClipboard() {
		if (viewer.isProjectNavigatorItemSelected()) {
			try{final Image					image = (Image)Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.imageFlavor);
				final ProjectNavigatorItem	pni = viewer.getProjectNavigatorItemSelected();
				final long					unique = project.getProjectNavigator().getUniqueId();
				final ProjectNavigatorItem	toAdd = new ProjectNavigatorItem(unique
																	, pni.id
																	, ItemType.ImageRef.getPartNamePrefix()+unique+".png"
																	, ItemType.ImageRef
																	, "Image content"
																	, project.createUniqueLocalizationString()
																	, pni.id);
				final ProjectItemEditor		pie = new ProjectItemEditor(getLogger(), project, toAdd);
			
				if (ask(pie, getLocalizer(), 400, 180)) {
					project.getProjectNavigator().addItem(pie.getNavigatorItem());
					project.addProjectPartContent(project.getPartNameById(toAdd.id), image);
				}
			} catch (ContentException | HeadlessException | UnsupportedFlavorException | IOException e) {
				getLogger().message(Severity.error, e, e.getLocalizedMessage());
			}
		}
	}
	
	public void copyCreoleLink2Clipboard(final String partName, final String link) {
		final Clipboard 	clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		final String		trimmedLink = link.replace('=', ' ').trim(); 
		
		clipboard.setContents(new StringSelection(" [["+partName+"#"+trimmedLink+"|"+trimmedLink+"]] "), null);
	}	

	@OnAction("action:/copyLink")
	public void copyLink2Clipboard() {
		if (viewer.isProjectNavigatorItemSelected()) {
			final ProjectNavigatorItem	pni = viewer.getProjectNavigatorItemSelected();
			final Clipboard 			clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			
			switch (pni.type) {
				case ImageRef		:
					clipboard.setContents(new StringSelection(" {{"+project.getPartNameById(pni.id)+"|"+pni.name+"}} "), null);
					break;
				case DocumentRef	:
					clipboard.setContents(new StringSelection(" [["+project.getPartNameById(pni.id)+"|"+pni.name+"]] "), null);
					break;
				default:
					throw new UnsupportedOperationException("Item type ["+pni.type+"] is not supported yet"); 
			}
		}
	}	
	
	@OnAction("action:/editItem")
	public void editItem() {
		if (viewer.isProjectNavigatorItemSelected()) {
			try{final ProjectNavigatorItem	pni = viewer.getProjectNavigatorItemSelected();
				
				switch (pni.type) {
					case CreoleRef : case ImageRef : case DocumentRef :
						final ProjectItemEditor	pie = new ProjectItemEditor(getLogger(), project, pni);
						
						if (ask(pie, getLocalizer(), 400, 180)) {
							project.getProjectNavigator().setItem(pni.id, pie.getNavigatorItem());
						}
						break;
					case Root		:
						break;
					case Subtree	:
						final ProjectPartEditor	ppe = new ProjectPartEditor(getLogger(), project, pni);
						
						if (ask(ppe, getLocalizer(), 400, 180)) {
							project.getProjectNavigator().setItem(pni.id, ppe.getNavigatorItem());
						}
						break;
					default:
						throw new UnsupportedOperationException("Item type ["+pni.type+"] is not supported yet"); 
				}
			} catch (ContentException e) {
				getLogger().message(Severity.error, e, e.getLocalizedMessage());
			}
		}
	}
	
	@OnAction("action:/deleteItem")
	public void deleteItem() {
		if (viewer.isProjectNavigatorItemSelected()) {
			final ProjectNavigatorItem	pni = viewer.getProjectNavigatorItemSelected();
			
			if (pni.type != ItemType.Root && new JLocalizedOptionPane(getLocalizer()).confirm(this
							, new LocalizedFormatter(KEY_APPLICATION_CONFIRM_DELETE_MESSAGE, pni.getNodeMetadata().getLabelId())
							, KEY_APPLICATION_CONFIRM_DELETE_TITLE
							, JOptionPane.QUESTION_MESSAGE
							, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				project.getProjectNavigator().removeItem(pni.id);
			}
		}
	}
	
	@OnAction("action:/validateProject")
	public void validateProject() {
		viewer.getScreenLogger().clear();
		if (project.validateProject(viewer.getScreenLogger())) {
			getLogger().message(Severity.info, KEY_APPLICATION_MESSAGE_VALIDATION_SUCCESSFUL);
		}
		else {
			getLogger().message(Severity.warning, KEY_APPLICATION_MESSAGE_VALIDATION_FAILED);
		}
	}
	
	@OnAction("action:/previewProject")
	public void previewProject() {
		if (Desktop.isDesktopSupported()) {
			final File	tempDir = new File(new File(System.getProperty("java.io.tmpdir")), PREVIEW_DIR);
			final File	startPage = new File(tempDir, "index_"+project.getLocalizer().currentLocale().getLanguage()+".html");
			
			if (tempDir.exists() && tempDir.isDirectory()) {
				Utils.deleteDir(tempDir);
			}
			try(final ZipInputStream	zis = new ZipInputStream(toInputStream(ExportFormat.AS_DIRECTORY))) {
				ZipEntry	ze;
				
				while ((ze = zis.getNextEntry()) != null) {
					final File	f = new File(tempDir, ze.getName());
					
					f.getParentFile().mkdirs();
					
					try(final OutputStream	os = new FileOutputStream(f)) {
						Utils.copyStream(zis, os);						
					}
				}
				Desktop.getDesktop().browse(startPage.toURI());
			} catch (IOException e) {
				getLogger().message(Severity.error, e, e.getLocalizedMessage());
			}
		}
		else {
			getLogger().message(Severity.error, KEY_APPLICATION_MESSAGE_DESKTOP_IS_NOT_SUPPORTED);
		}
	}
	
	@OnAction("action:/buildIndex")
	public void buildIndex() {
	}
	
	@OnAction("action:/settings")
	public void settings() {
		try{final SubstitutableProperties	props = SubstitutableProperties.of(propFile);
		
			se.loadProperties(props);
			if (ask(se, getLocalizer(), 500, 100)) {
				se.storeProperties(props);
				props.store(propFile);
				getLogger().message(Severity.info, KEY_APPLICATION_MESSAGE_READY);
			}
		} catch (ContentException | IOException exc) {
			getLogger().message(Severity.error, exc, exc.getLocalizedMessage());
		}
	}
	
	@OnAction("action:builtin:/builtin.languages")
    public void language(final Hashtable<String,String[]> langs) throws LocalizationException {
		PureLibSettings.PURELIB_LOCALIZER.setCurrentLocale(SupportedLanguages.valueOf(langs.get("lang")[0]).getLocale());
	}	

	@OnAction("action:/manual")
	public void manual() {
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(URI.create(getHelpServerURI().toString()+"/static/index.html"));
			} catch (IOException exc) {
				getLogger().message(Severity.error, exc, exc.getLocalizedMessage());
			}
		}
		else {
			getLogger().message(Severity.error, KEY_APPLICATION_MESSAGE_DESKTOP_IS_NOT_SUPPORTED);
		}
	}
	
	@OnAction("action:/about")
	public void about() {
		SwingUtils.showAboutScreen(this, localizer, KEY_APPLICATION_HELP_TITLE, KEY_APPLICATION_HELP_CONTENT, URI.create("root://"+getClass().getCanonicalName()+"/chav1961/csce/avatar.jpg"), new Dimension(640, 400));
	}

	public JEnableMaskManipulator getEnableMaskManipulator() {
		return emm;
	}
	
	void loadLRU(final String path) {
		final File	f = new File(path);
		
		if (f.exists() && f.isFile() && f.canRead()) {
			try{fcm.openFile(path);
			} catch (IOException e) {
				getLogger().message(Severity.error, e, e.getLocalizedMessage());
			}
		}
		else {
			fcm.removeFileNameFromLRU(path);
			getLogger().message(Severity.warning, KEY_APPLICATION_MESSAGE_FILE_NOT_EXISTS, path);
		}
	}

	void exportLRU(final String path) {
		try(final FileSystemInterface	fsi = repo.clone().open(path)) {
			for (ExportFormat item : ExportFormat.values()) {
				if (item.getCallbackFilter().accept(fsi)) {
					exportFormat = item;
					if (!fsi.exists()) {
						try(final OutputStream	os = fsi.create().write()) {
							Utils.copyStream(toInputStream(item), os);
						}
						getLogger().message(Severity.info, KEY_APPLICATION_MESSAGE_READY);
					}
					else if (fsi.isFile() && new JLocalizedOptionPane(getLocalizer()).confirm(this
							, new LocalizedFormatter(KEY_APPLICATION_CONFIRM_REPLACE_MESSAGE, path)
							, KEY_APPLICATION_CONFIRM_REPLACE_TITLE
							, JOptionPane.QUESTION_MESSAGE
							, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						try(final OutputStream	os = fsi.write()) {
							Utils.copyStream(toInputStream(item), os);
						}
						getLogger().message(Severity.info, KEY_APPLICATION_MESSAGE_READY);
					}
					return;
				}
			}
		} catch (IOException e) {
			getLogger().message(Severity.error, e, e.getLocalizedMessage());
		}
	}
	
	private void fillLRU(final List<String> lastUsed) {
		if (lastUsed.isEmpty()) {
			getEnableMaskManipulator().setEnableMaskOff(FILE_LRU);
		}
		else {
			final JMenu	menu = (JMenu)SwingUtils.findComponentByName(menuBar, MENU_FILE_LRU);
			
			menu.removeAll();
			for (String file : lastUsed) {
				final JMenuItem	item = new JMenuItem(file);
				
				item.addActionListener((e)->loadLRU(item.getText()));
				menu.add(item);
			}
			getEnableMaskManipulator().setEnableMaskOn(FILE_LRU);
		}
	}

	private void fillExportLRU(final List<String> lastUsed) {
		if (lastUsed.isEmpty()) {
			getEnableMaskManipulator().setEnableMaskOff(FILE_EXPORT_LRU);
		}
		else {
			final JMenu	menu = (JMenu)SwingUtils.findComponentByName(menuBar, MENU_FILE_EXPORT_LRU);
			
			menu.removeAll();
			for (String file : lastUsed) {
				final JMenuItem	item = new JMenuItem(file);
				
				item.addActionListener((e)->exportLRU(item.getText()));
				menu.add(item);
			}
			getEnableMaskManipulator().setEnableMaskOn(FILE_EXPORT_LRU);
		}
	}
	
	private void processLRU(final FileContentChangedEvent<?> event) {
		switch (event.getChangeType()) {
			case LRU_LIST_REFRESHED			:
				fillLRU(fcm.getLastUsed());
				break;
			case FILE_LOADED 				:
				project.setProjectFileName(fcm.getCurrentPathOfTheFile());						
				if (viewer == null) {
					placeViewer();
				}
				getEnableMaskManipulator().setEnableMaskOn(FILE_SAVEAS | FILE_EXPORT | EDIT | TOOLS_VALIDATE | TOOLS_PREVIEW | TOOLS_BUILD_INDEX);
				fillTitle();
				break;
			case FILE_STORED 				:
				fcm.clearModificationFlag();
				break;
			case FILE_STORED_AS 			:
				project.setProjectFileName(fcm.getCurrentPathOfTheFile());						
				fcm.clearModificationFlag();
				fillTitle();
				break;
			case MODIFICATION_FLAG_CLEAR 	:
				getEnableMaskManipulator().setEnableMaskOff(FILE_SAVE);
				fillTitle();
				break;
			case MODIFICATION_FLAG_SET 		:
				getEnableMaskManipulator().setEnableMaskOn(FILE_SAVEAS | (Utils.checkEmptyOrNullString(project.getProjectFileName()) ? 0 : FILE_SAVE));
				fillTitle();
				break;
			case NEW_FILE_CREATED 			:
				if (viewer == null) {
					placeViewer();
				}
				getEnableMaskManipulator().setEnableMaskOn(FILE_SAVEAS | FILE_EXPORT | EDIT | TOOLS_VALIDATE | TOOLS_PREVIEW | TOOLS_BUILD_INDEX);
				fillTitle();
				break;
			default :
				throw new UnsupportedOperationException("Change type ["+event.getChangeType()+"] is not supported yet");
		}
	}

	private void processExportLRU(final FileContentChangedEvent<?> event) {
		switch (event.getChangeType()) {
			case LRU_LIST_REFRESHED			:
				fillExportLRU(exportFcm.getLastUsed());
				break;
			case FILE_LOADED : case FILE_STORED : case MODIFICATION_FLAG_CLEAR : case MODIFICATION_FLAG_SET : case NEW_FILE_CREATED : case FILE_STORED_AS :
				break;
			default :
				throw new UnsupportedOperationException("Change type ["+event.getChangeType()+"] is not supported yet");
		}
	}

	private InputStream toInputStream(final ExportFormat format) throws IOException {
		try(final ByteArrayOutputStream	os = new ByteArrayOutputStream()) {
			switch (format) {
				case AS_DIRECTORY	:
					try(final ZipOutputStream	zos = new ZipOutputStream(os);
						final HTMLBuilder		builder = new HTMLBuilder(getLocalizer(), project)) {
						
						builder.upload(zos);
						zos.flush();
					}
					return new ByteArrayInputStream(os.toByteArray());
				case AS_WAR			:
					try(final ZipOutputStream	zos = new ZipOutputStream(os);
						final HTMLBuilder		builder = new HTMLBuilder(getLocalizer(), project)) {
						
						builder.upload(zos);
						zos.flush();
					}
					return new ByteArrayInputStream(os.toByteArray());
				case AS_SCORM_2004	:
					try(final ZipOutputStream	zos = new ZipOutputStream(os);
						final HTMLBuilder		builder = new HTMLBuilder(getLocalizer(), project)) {
						
						builder.upload(zos);
						zos.flush();
					}
					return new ByteArrayInputStream(os.toByteArray());
				default:
					throw new UnsupportedOperationException("Export format ["+format+"] is not supported yet"); 
			}
		} catch (ContentException e) {
			throw new IOException(e); 
		}
	}
	
	private final void insertSomething(final ItemType type, final FilterCallback... filters) {
		if (viewer.isProjectNavigatorItemSelected()) {
			final ProjectNavigatorItem	pni = viewer.getProjectNavigatorItemSelected();
			boolean		wasSelected = false;
			
			try{for (String item : JFileSelectionDialog.select(this, getLocalizer(), repo, JFileSelectionDialog.OPTIONS_CAN_SELECT_FILE | JFileSelectionDialog.OPTIONS_CAN_MULTIPLE_SELECT | JFileSelectionDialog.OPTIONS_FILE_MUST_EXISTS | JFileSelectionDialog.OPTIONS_FOR_OPEN, filters)) {
					try(final FileSystemInterface  	fsi = repo.clone().open(item);
						final InputStream			is = fsi.read()) {
						project.addProjectPart(pni.id, type, fsi.getName(), is);
						wasSelected = true;
					}
				}
				if (wasSelected) {
					fcm.setModificationFlag();
				}
			} catch (IOException e) {
				getLogger().message(Severity.error, e, e.getLocalizedMessage());
			}
		}
	}
	
	private void fillLocalizationStrings() {
		fillTitle();
	}

	private void fillTitle() {
		setTitle(localizer.getValue(KEY_APPLICATION_FRAME_TITLE, (fcm.wasChanged() ? "* " : ""), fcm.getCurrentNameOfTheFile()));
	}
	
	private void placeViewer() {
		viewer = new ProjectViewer(Application.this, project, mdi);
		project.addProjectChangeListener((e)->{
			fcm.setModificationFlag();
			viewer.refreshProject(e);
		});
		getContentPane().remove(firstScreen);
        getContentPane().add(viewer, BorderLayout.CENTER);
        ((JComponent)getContentPane()).revalidate();
		viewer.refreshProject(new ProjectChangeEvent(project, ProjectChangeType.PROJECT_LOADED));
        viewer.addProjectViewerChangeListener((e)->refreshProjectMenu(e));
	}

	private void fixProject() {
		viewer.fixProject();
	}
	
	private void refreshProjectMenu(final ProjectViewerChangeEvent e) {
		switch (e.getChangeType()) {
			case NAVIGATOR_ITEM_DESELECTED	:
				getEnableMaskManipulator().setEnableMaskOff(INSERT);
				break;
			case NAVIGATOR_ITEM_SELECTED	:
				if (!project.getProjectNavigator().getItem((long)e.getParameters()[0]).type.isLeafItem()) {
					getEnableMaskManipulator().setEnableMaskOn(INSERT);
				}
				break;
			default:
				throw new UnsupportedOperationException("Change type ["+e.getChangeType()+"] is not supported yet");
		}
	}

	private void refreshPasteMenu() {
		boolean	imageFlavor = false;
		
		try{	// Clipboard artifact!!!
			for(DataFlavor item : Toolkit.getDefaultToolkit().getSystemClipboard().getAvailableDataFlavors()) {
				if (item.equals(DataFlavor.imageFlavor)) {
					imageFlavor = true;
				}
			}
		} catch (IllegalStateException exc) {
		}
		getEnableMaskManipulator().setEnableMaskTo(INSERT_IMAGE_FROM_CLIPBOARD, imageFlavor);
	}
	
	public static void main(String[] args) {
		final ArgParser	parser = new ApplicationArgParser();
		int				retcode = 0;	

		try(final JSimpleSplash	jss = new JSimpleSplash()) {
			
			jss.start("Loading...", 4);
		
			try(final StaticHelp	help = new StaticHelp(PureLibSettings.CURRENT_LOGGER, new URI("root://"+Application.class.getCanonicalName()+"/chav1961/csce/static.zip"), "csce.helpcontent")) {
				final ArgParser		parsed = parser.parse(args);
				final SubstitutableProperties	nanoServerProps = new SubstitutableProperties(Utils.mkProps(
													 NanoServiceFactory.NANOSERVICE_PORT, parsed.getValue(ARG_HELP_PORT, String.class)
													,NanoServiceFactory.NANOSERVICE_ROOT, FileSystemInterface.FILESYSTEM_URI_SCHEME+":xmlReadOnly:root://"+Application.class.getCanonicalName()+"/chav1961/csce/helptree.xml"
													,NanoServiceFactory.NANOSERVICE_CREOLE_PROLOGUE_URI, Application.class.getResource("prolog.cre").toString() 
													,NanoServiceFactory.NANOSERVICE_CREOLE_EPILOGUE_URI, Application.class.getResource("epilog.cre").toString() 
												));
	
				jss.processed(1);
				
				try(final NanoServiceFactory	service = new NanoServiceFactory(PureLibSettings.CURRENT_LOGGER, nanoServerProps)) {
					jss.processed(2);
					service.start();
					jss.processed(3);
					
					try(final Application		app = new Application(parsed.getValue(ARG_PROPFILE_LOCATION, File.class), URI.create("http://localhost:"+service.getServerAddress().getPort()))) {
						
						jss.processed(4);
						app.setVisible(true);
						app.getLogger().message(Severity.info, KEY_APPLICATION_MESSAGE_READY);
					}
					service.stop();
				}
			}
		} catch (CommandLineParametersException exc) {
			System.err.println(exc.getLocalizedMessage());
			System.err.println(parser.getUsage("csce"));
			retcode = 128;
		} catch (URISyntaxException | EnvironmentException | ContentException | IOException exc) {
			exc.printStackTrace();
			retcode = 129;
		}
		System.exit(retcode);
	}

	public static <T> boolean ask(final T instance, final Localizer localizer, final int width, final int height) throws ContentException {
		final ContentMetadataInterface	mdi = ContentModelFactory.forAnnotatedClass(instance.getClass());
		
		try(final AutoBuiltForm<T,?>	abf = new AutoBuiltForm<>(mdi, localizer, PureLibSettings.INTERNAL_LOADER, instance, (FormManager<?,T>)instance)) {
			
			((ModuleAccessor)instance).allowUnnamedModuleAccess(abf.getUnnamedModules());
			abf.setPreferredSize(new Dimension(width,height));
			return AutoBuiltForm.ask((JFrame)null,localizer,abf);
		}
	}
	
	private static class ApplicationArgParser extends ArgParser {
		private static final ArgParser.AbstractArg[]	KEYS = {
			new IntegerArg(ARG_HELP_PORT, true, "Help port to use for help browser", 0),
			new FileArg(ARG_PROPFILE_LOCATION, false, "Property file location", DEFAULT_ARG_PROPFILE_LOCATION),
		};
		
		private ApplicationArgParser() {
			super(KEYS);
		}
	}
	
}
