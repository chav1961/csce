package chav1961.csce;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
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
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.border.EtchedBorder;

import chav1961.csce.builders.HTMLBuilder;
import chav1961.csce.project.ProjectContainer;
import chav1961.csce.project.ProjectNavigator.ItemType;
import chav1961.csce.project.ProjectNavigator.ProjectNavigatorItem;
import chav1961.csce.swing.ProjectItemEditor;
import chav1961.csce.swing.ProjectPartEditor;
import chav1961.csce.swing.ProjectViewer;
import chav1961.csce.swing.ProjectViewerChangeEvent;
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
import chav1961.purelib.ui.swing.useful.JEnableMaskManipulator;
import chav1961.purelib.ui.swing.useful.JFileContentManipulator;
import chav1961.purelib.ui.swing.useful.JFileSelectionDialog;
import chav1961.purelib.ui.swing.useful.JFileSelectionDialog.FilterCallback;
import chav1961.purelib.ui.swing.useful.JLocalizedOptionPane;
import chav1961.purelib.ui.swing.useful.JStateString;
import chav1961.purelib.ui.swing.useful.LocalizedFormatter;
import chav1961.purelib.ui.swing.useful.interfaces.FileContentChangeListener;
import chav1961.purelib.ui.swing.useful.interfaces.FileContentChangedEvent;

// https://burakkanber.com/blog/machine-learning-full-text-search-in-javascript-relevance-scoring/
// https://stackoverflow.com/questions/4437910/java-pdf-viewer
public class Application  extends JFrame implements AutoCloseable, NodeMetadataOwner, LocaleChangeListener, LoggerFacadeOwner, LocalizerOwner  {
	private static final long 		serialVersionUID = 8855923580582029585L;
	
	public static final String		ARG_HELP_PORT = "helpPort";
	public static final String		ARG_PROPFILE_LOCATION = "prop";
	
	static final String				PROJECT_SUFFIX = "csc";

	private static final String		LRU_PREFIX = "lru";
	private static final FilterCallback	FILE_FILTER = FilterCallback.of("CSC project", "*."+PROJECT_SUFFIX);
	
	public static final String		KEY_APPLICATION_FRAME_TITLE = "chav1961.csce.Application.frame.title";
	public static final String		KEY_APPLICATION_HELP_TITLE = "chav1961.csce.Application.help.title";
	public static final String		KEY_APPLICATION_HELP_CONTENT = "chav1961.csce.Application.help.content";

	public static final String		KEY_APPLICATION_CONFIRM_DELETE_TITLE = "chav1961.csce.Application.confirm.delete.title";
	public static final String		KEY_APPLICATION_CONFIRM_DELETE_MESSAGE = "chav1961.csce.Application.confirm.delete.message";
	
	public static final String		KEY_APPLICATION_MESSAGE_READY = "chav1961.csce.Application.message.ready";
	public static final String		KEY_APPLICATION_MESSAGE_FILE_NOT_EXISTS = "chav1961.csce.Application.message.file.not.exists";

	public static final String		KEY_FILTER_PDF_FILE = "chav1961.csce.Application.filter.pdf.file";
	public static final String		KEY_FILTER_DJVU_FILE = "chav1961.csce.Application.filter.djvu.file";
	public static final String		KEY_FILTER_IMAGE_FILE = "chav1961.csce.Application.filter.image.file";
	public static final String		KEY_FILTER_ZIP_FILE = "chav1961.csce.Application.filter.zip.file";
	
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
										MENU_TOOLS_BUILD_INDEX
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

	private static final FilterCallback		PDF_FILTER = FilterCallback.of(KEY_FILTER_PDF_FILE, "*.pdf"); 	
	private static final FilterCallback		DJVU_FILTER = FilterCallback.of(KEY_FILTER_DJVU_FILE, "*.djv", "*.djvu"); 	
	private static final FilterCallback		IMAGE_FILTER = FilterCallback.of(KEY_FILTER_IMAGE_FILE, "*.png", "*.jpg"); 	
	private static final FilterCallback		ZIP_FILTER = FilterCallback.of(KEY_FILTER_PDF_FILE, "*.zip"); 	
	
	
	private final URI						helpServerURI;
	private final ContentMetadataInterface	mdi;
	private final Localizer					localizer;
	private final FileSystemInterface		repo = FileSystemFactory.createFileSystem(URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":file:/"));
	private final JMenuBar					menuBar;
	private final JStateString				state;
	private final LRUPersistence			lru;
	private final JFileContentManipulator	fcm;
	private final ProjectContainer			project;
	private final CountDownLatch			latch = new CountDownLatch(1);
	private final JEnableMaskManipulator	emm;

	private FirstScreen						firstScreen = null; 
	private ProjectViewer					viewer = null;
	
	public Application(final File propFile, final URI helpServerURI) throws ContentException, IOException {
		try(final InputStream		is = this.getClass().getResourceAsStream("application.xml");) {
			
			this.mdi = ContentModelFactory.forXmlDescription(is);
			this.project = new ProjectContainer(this, mdi);
			this.localizer = Localizer.Factory.newInstance(mdi.getRoot().getLocalizerAssociated());
	        this.localizer.addLocaleChangeListener(this);
			
			PureLibSettings.PURELIB_LOCALIZER.push(localizer);
		}

		this.helpServerURI = helpServerURI;
		this.menuBar = SwingUtils.toJComponent(mdi.byUIPath(URI.create("ui:/model/navigation.top.mainmenu")), JMenuBar.class); 
		this.emm = new JEnableMaskManipulator(MENUS, this.menuBar);
		this.state = new JStateString(localizer, 100);
		this.lru = LRUPersistence.of(propFile, LRU_PREFIX); 
		this.fcm = new JFileContentManipulator(repo, localizer, 
				()->project.toIntputStream(), 
				()->project.fromOutputStream(), 
				lru);
		this.fcm.setFilters(FILE_FILTER);
		this.fcm.addFileContentChangeListener(new FileContentChangeListener<Application>() {
			@Override
			public void actionPerformed(FileContentChangedEvent<Application> event) {
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
		});
		
		state.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		setJMenuBar(menuBar);		
        getContentPane().add(firstScreen = new FirstScreen(this), BorderLayout.CENTER);
        getContentPane().add(state, BorderLayout.SOUTH);
        
        SwingUtils.assignActionListeners(menuBar, this);
		SwingUtils.assignExitMethod4MainWindow(this,()->exit());
        fillLRU(fcm.getLastUsed());
		
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
		try{fcm.saveFile();
		} catch (IOException e) {
			getLogger().message(Severity.error, e, e.getLocalizedMessage());
		}
	}
	
	@OnAction("action:/saveProjectAs")
	public void saveProjectAs() {
		try{fcm.saveFileAs();
		} catch (IOException e) {
			getLogger().message(Severity.error, e, e.getLocalizedMessage());
		}
	}
	
	@OnAction("action:/exportProjectAsWar")
	public void exportProjectAsWar() {
	}
	
	@OnAction("action:/exportProjectAsScorm2004")
	public void exportProjectAsScorm2004() {
	}
	
	@OnAction("action:/exportProjectAsSubdir")
	public void exportProjectAsSubdir() {
		try{for (String item : JFileSelectionDialog.select(this, getLocalizer(), repo, JFileSelectionDialog.OPTIONS_CAN_SELECT_FILE | JFileSelectionDialog.OPTIONS_FOR_SAVE | JFileSelectionDialog.OPTIONS_ALLOW_MKDIR | JFileSelectionDialog.OPTIONS_CONFIRM_REPLACEMENT, ZIP_FILTER)) {
				try(final OutputStream		os = new FileOutputStream(item);
					final ZipOutputStream	zos = new ZipOutputStream(os);
					final HTMLBuilder		builder = new HTMLBuilder(getLocalizer(), project)) {
					
					builder.upload(zos);
					zos.flush();
				}
			}
			getLogger().message(Severity.info, KEY_APPLICATION_MESSAGE_READY);
		} catch (IOException e) {
			getLogger().message(Severity.error, e, e.getLocalizedMessage());
		}
	}
	
	@OnAction("action:/exit")
	public void exit() throws IOException {
		if (fcm.commit()) {
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
	public void properties() {
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
	
	@OnAction("action:/insertPage")
	public void insertPage() {
		if (viewer.isProjectNavigatorItemSelected()) {
			final ProjectNavigatorItem	pni = viewer.getProjectNavigatorItemSelected();
			final long					unique = project.getProjectNavigator().getUniqueId();
			final ProjectNavigatorItem	toAdd = new ProjectNavigatorItem(unique
																	, pni.id
																	, "CreoleContent"+unique
																	, ItemType.CreoleRef
																	, "Creole content"
																	, project.createUniqueLocalizationString()
																	, -1);
			final ProjectItemEditor		pie = new ProjectItemEditor(getLogger(), project, toAdd);
			
			try{if (ask(pie, getLocalizer(), 400, 180)) {
					project.getProjectNavigator().addItem(pie.getNavigatorItem());
					project.addProjectPartContent(project.getPartNameById(toAdd.id), "");
				}
			} catch (ContentException e) {
				getLogger().message(Severity.error, e, e.getLocalizedMessage());
			}
		}
	}

	@OnAction("action:/insertDocument")
	public void insertDocument() {
		if (viewer.isProjectNavigatorItemSelected()) {
			final ProjectNavigatorItem	pni = viewer.getProjectNavigatorItemSelected();
			boolean		wasSelected = false;
			
			try{for (String item : JFileSelectionDialog.select(this, getLocalizer(), repo, JFileSelectionDialog.OPTIONS_CAN_SELECT_FILE | JFileSelectionDialog.OPTIONS_CAN_MULTIPLE_SELECT | JFileSelectionDialog.OPTIONS_FILE_MUST_EXISTS | JFileSelectionDialog.OPTIONS_FOR_OPEN, PDF_FILTER, DJVU_FILTER)) {
					final long					unique = project.getProjectNavigator().getUniqueId();
					final ProjectNavigatorItem	toAdd = new ProjectNavigatorItem(unique
															, pni.id
															, "Document"+unique
															, ItemType.DocumentRef
															, "Document "+item
															, project.createUniqueLocalizationString()
															, -1);
					project.getProjectNavigator().addItem(toAdd);
					wasSelected = true;
				}
				if (wasSelected) {
					fcm.setModificationFlag();
				}
			} catch (IOException e) {
				getLogger().message(Severity.error, e, e.getLocalizedMessage());
			}
		}
	}
	
	@OnAction("action:/insertImage")
	public void insertImage() {
		if (viewer.isProjectNavigatorItemSelected()) {
			final ProjectNavigatorItem	pni = viewer.getProjectNavigatorItemSelected();
			boolean		wasSelected = false;
			
			try{for (String item : JFileSelectionDialog.select(this, getLocalizer(), repo, JFileSelectionDialog.OPTIONS_CAN_SELECT_FILE | JFileSelectionDialog.OPTIONS_CAN_MULTIPLE_SELECT | JFileSelectionDialog.OPTIONS_FILE_MUST_EXISTS | JFileSelectionDialog.OPTIONS_FOR_OPEN, IMAGE_FILTER)) {
					final long					unique = project.getProjectNavigator().getUniqueId();
					final ProjectNavigatorItem	toAdd = new ProjectNavigatorItem(unique
															, pni.id
															, "Image"+unique
															, ItemType.ImageRef
															, "Image "+item
															, project.createUniqueLocalizationString()
															, -1);
					project.getProjectNavigator().addItem(toAdd);
					project.addProjectPartContent(project.getPartNameById(toAdd.id), ImageIO.read(new File(item)));
					wasSelected = true;
				}
				if (wasSelected) {
					fcm.setModificationFlag();
				}
			} catch (IOException e) {
				getLogger().message(Severity.error, e, e.getLocalizedMessage());
			}
		}
	}
	
	@OnAction("action:/insertUri")
	public void insertUri() {
	}

	@OnAction("action:/editItem")
	public void editItem() {
		if (viewer.isProjectNavigatorItemSelected()) {
			try{final ProjectNavigatorItem	pni = viewer.getProjectNavigatorItemSelected();
				
				switch (pni.type) {
					case CreoleRef	:
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
	}
	
	@OnAction("action:/previewProject")
	public void previewProject() {
	}
	
	@OnAction("action:/buildIndex")
	public void buildIndex() {
	}
	
	@OnAction("action:/settings")
	public void settings() {
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
        viewer.addProjectViewerChangeListener((e)->refreshProjectMenu(e));
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

	public static void main(String[] args) {
		final ArgParser	parser = new ApplicationArgParser();
		int				retcode = 0;	
		
		try(final StaticHelp	help = new StaticHelp(PureLibSettings.CURRENT_LOGGER, new URI("root://"+Application.class.getCanonicalName()+"/chav1961/csce/static.zip"), "csce.helpcontent")) {
			final ArgParser		parsed = parser.parse(args);
		
			final SubstitutableProperties	nanoServerProps = new SubstitutableProperties(Utils.mkProps(
												 NanoServiceFactory.NANOSERVICE_PORT, parsed.getValue(ARG_HELP_PORT, String.class)
												,NanoServiceFactory.NANOSERVICE_ROOT, FileSystemInterface.FILESYSTEM_URI_SCHEME+":xmlReadOnly:root://"+Application.class.getCanonicalName()+"/chav1961/csce/helptree.xml"
												,NanoServiceFactory.NANOSERVICE_CREOLE_PROLOGUE_URI, Application.class.getResource("prolog.cre").toString() 
												,NanoServiceFactory.NANOSERVICE_CREOLE_EPILOGUE_URI, Application.class.getResource("epilog.cre").toString() 
											));

			try(final NanoServiceFactory	service = new NanoServiceFactory(PureLibSettings.CURRENT_LOGGER, nanoServerProps)) {
				service.start();
				try(final Application		app = new Application(parsed.getValue(ARG_PROPFILE_LOCATION, File.class), URI.create("http://localhost:"+service.getServerAddress().getPort()))) {
					
					app.setVisible(true);
					app.getLogger().message(Severity.info, KEY_APPLICATION_MESSAGE_READY);
				}
				service.stop();
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
			new FileArg(ARG_PROPFILE_LOCATION, false, "Property file location", "./.csce.properties"),
		};
		
		private ApplicationArgParser() {
			super(KEYS);
		}
	}
	
}
