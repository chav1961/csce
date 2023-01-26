package chav1961.csce;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import chav1961.csce.project.ProjectContainer;
import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
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
import chav1961.purelib.ui.interfaces.LRUPersistence;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JFileContentManipulator;
import chav1961.purelib.ui.swing.useful.JStateString;
import chav1961.purelib.ui.swing.useful.interfaces.FileContentChangeListener;
import chav1961.purelib.ui.swing.useful.interfaces.FileContentChangedEvent;

// https://burakkanber.com/blog/machine-learning-full-text-search-in-javascript-relevance-scoring/
public class Application  extends JFrame implements AutoCloseable, NodeMetadataOwner, LocaleChangeListener, LoggerFacadeOwner, LocalizerOwner  {
	private static final long 	serialVersionUID = 8855923580582029585L;
	
	public static final String	ARG_HELP_PORT = "helpPort";
	public static final String	ARG_PROPFILE_LOCATION = "prop";
	private static final String	LRU_PREFIX = "lru";
	
	public static final String	KEY_APPLICATION_FRAME_TITLE = "chav1961.csce.Application.frame.title";
	public static final String	KEY_APPLICATION_HELP_TITLE = "chav1961.csce.Application.help.title";
	public static final String	KEY_APPLICATION_HELP_CONTENT = "chav1961.csce.Application.help.content";
	
	public static final String	KEY_APPLICATION_MESSAGE_READY = "chav1961.csce.Application.message.ready";

	private final ContentMetadataInterface	mdi;
	private final Localizer					localizer;
	private final FileSystemInterface		repo = FileSystemFactory.createFileSystem(URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":file:/"));
	private final JMenuBar					menuBar;
	private final JStateString				state;
	private final LRUPersistence			lru;
	private final JFileContentManipulator	fcm;
	private final CountDownLatch			latch = new CountDownLatch(1);
	
	private ProjectContainer				project = null;
	
	public Application(final File propFile) throws ContentException, IOException {
		try(final InputStream		is = this.getClass().getResourceAsStream("application.xml");) {
			
			this.mdi = ContentModelFactory.forXmlDescription(is);
			this.localizer = Localizer.Factory.newInstance(mdi.getRoot().getLocalizerAssociated());
	        this.localizer.addLocaleChangeListener(this);
			
			PureLibSettings.PURELIB_LOCALIZER.push(localizer);
		}

		this.menuBar =  SwingUtils.toJComponent(mdi.byUIPath(URI.create("ui:/model/navigation.top.mainmenu")), JMenuBar.class); 
		this.state = new JStateString(localizer, 100);
		this.lru = LRUPersistence.of(propFile, LRU_PREFIX); 
		this.fcm = new JFileContentManipulator(repo, localizer, 
				()->project.toIntputStream(), 
				()->project.fromOutputStream(), 
				lru);
		this.fcm.addFileContentChangeListener(new FileContentChangeListener<Application>() {
			@Override
			public void actionPerformed(FileContentChangedEvent<Application> event) {
				switch (event.getChangeType()) {
					case LRU_LIST_REFRESHED			:
						fillLRU(fcm.getLastUsed());
						break;
					case FILE_LOADED 				:
					case FILE_STORED 				:
					case FILE_STORED_AS 			:
					case MODIFICATION_FLAG_CLEAR 	:
					case MODIFICATION_FLAG_SET 		:
					case NEW_FILE_CREATED 			:
						break;
					default :
						throw new UnsupportedOperationException("Change type ["+event.getChangeType()+"] is not supported yet");
				}
			}
		});

		setJMenuBar(menuBar);		
        SwingUtils.assignActionListeners(menuBar, this);
		SwingUtils.assignExitMethod4MainWindow(this,()->exit());
		
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

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		// TODO Auto-generated method stub
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
	}
	
	@OnAction("action:/openProject")
	public void openProject() {
	}
	
	@OnAction("action:/saveProject")
	public void saveProject() {
	}
	
	@OnAction("action:/saveProjectAs")
	public void saveProjectAs() {
	}
	
	@OnAction("action:/exportProject")
	public void exportProject() {
	}
	
	@OnAction("action:/exit")
	public void exit() {
		latch.countDown();
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
	}
	
	@OnAction("action:/insertPage")
	public void insertPage() {
	}
	
	@OnAction("action:/insertImage")
	public void insertImage() {
	}
	
	@OnAction("action:/insertUri")
	public void insertUri() {
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
	
	@OnAction("action:/about")
	public void about() {
		SwingUtils.showAboutScreen(this, localizer, KEY_APPLICATION_HELP_TITLE, KEY_APPLICATION_HELP_CONTENT, URI.create("root://"+getClass().getCanonicalName()+"/chav1961/csce/avatar.jpg"), new Dimension(640, 400));
	}

	private void fillLRU(final List<String> lastUsed) {
		final JMenu	menu = ((JMenu)SwingUtils.findComponentByName(menuBar, "menu.main.file.lru"));
		
		if (lastUsed.isEmpty()) {
			menu.setEnabled(false);
		}
		else {
			menu.removeAll();
			for (String file : lastUsed) {
				final JMenuItem	item = new JMenuItem(file);
				
				item.addActionListener((e)->loadLRU(item.getText()));
				menu.add(item);
			}
			menu.setEnabled(true);
		}
	}

	private void loadLRU(final String path) {
	}
	
	private void fillLocalizationStrings() {
		setTitle(localizer.getValue(KEY_APPLICATION_FRAME_TITLE));
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		final ArgParser	parser = new ApplicationArgParser();
		int				retcode = 0;	
		
		try{final ArgParser			parsed = parser.parse(args);
		
			try(final Application		app = new Application(parsed.getValue(ARG_PROPFILE_LOCATION, File.class))) {
				
				app.setVisible(true);
				app.getLogger().message(Severity.info, KEY_APPLICATION_MESSAGE_READY);
			}
		
		
		} catch (CommandLineParametersException exc) {
			System.err.println(exc.getLocalizedMessage());
			System.err.println(parser.getUsage("csce"));
			retcode = 128;
		} catch (ContentException | IOException exc) {
			exc.printStackTrace();
			retcode = 129;
		}
		System.exit(retcode);
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
