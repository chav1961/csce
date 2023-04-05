package chav1961.csce.project;

import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import chav1961.csce.project.ProjectChangeEvent.ProjectChangeType;
import chav1961.csce.project.ProjectNavigator.ItemType;
import chav1961.csce.project.ProjectNavigator.ProjectNavigatorItem;
import chav1961.csce.utils.SearchUtils;
import chav1961.csce.utils.SearchUtils.CreoleLink;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.StringLoggerFacade;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.MutableJsonLocalizer;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.LocalizerOwner;
import chav1961.purelib.i18n.interfaces.MutableLocalizedString;
import chav1961.purelib.json.JsonNode;
import chav1961.purelib.json.JsonUtils;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;


/*
 * Project is a *.zip file with mandatory part '.project.properties'. It's preferred to be the same first part in the *.zip content.
 * Part '.project.properties' contains a set of keys:
 * - project.version: Descriptor version. Mandatory. Default is 1.0. now
 * - project.name: Project name. Mandatory. Will be appeared in the browser tab. Must be multi-language string
 * - project.icon - Project icon. List of images, splitted by \\n char. Must contain at least one element with size 16x16.
 * 		All the images names must be presented in the *.zip content. Element with size 16x16 will be appeared in the browser tab
 * - project.author: Copyright notice. Mandatory. Must be multi-language string
 * - project.descriptor: Project descriptor. Mandatory. Must be multi-language string
 * - project.licenses: Project licenses. List of license names, splitted by \\n char. Must contain at least one element. 
 * 		All the license names must be presented in the *.zip content
 * - project.tree: Navigator tree of the project. Mandatory. Must points to one of the *.zip content parts
 * - project.root: Root page of the project. Mandatory. Must be a some name inside the project.tree content part 
 * - project.lang: List of languages supported. Optional. Must be chav1961.purelib.i18n.interfaces.SupportedLanguages names was split by comma.
 * 		If missing, ru defaults
 * - project.localization: part name with localized strings. Mandatory. Must points to one of the *.zip content parts
 * - project.externals: External references of the project. Optional. Must points to one of the *.zip content parts
 * Content of *.zip also must contain:
 * - exactly one part with structure project description. Name of the part must be references by 'project.tree' key from '.project.properties' part. 
 * 		It's extension must be '.json'
 * - at least one license part with license content. Content of the part can be formatted by one of markup languges. Names(s) of the part(s) 
 * 		must be references by 'project.licenses' key from '.project.properties' part
 * Content of *.zip also can contain:
 * - a set of content pages part. Names of all the pages must have '.cre' extension and it's content must be Creole-based markup language.
 * - a set of image files part. Names of all the pages must have '.png' extension. The only supported format of the images must be *.png
 * - a set of additional files. It's name must have neither '.cre' nor '.png' extensions.
 * Structure project description has a json content. See...
 */

public class ProjectContainer implements LocalizerOwner {
	public static final String		PROJECT_VERSION = "project.version";
	public static final String		DEFAULT_PROJECT_VERSION = "1.0";
	public static final String		PROJECT_NAME = "project.name";
	public static final String		PROJECT_ICON = "project.icon";
	public static final String		PROJECT_AUTHOR = "project.author";
	public static final String		PROJECT_DESCRIPTOR = "project.descriptor";
	public static final String		PROJECT_LICENSES = "project.licenses";
	public static final String		PROJECT_EMAIL = "project.email";
	public static final String		PROJECT_TREE = "project.tree";
	public static final String		PROJECT_ROOT = "project.root"; 
	public static final String		PROJECT_LANG = "project.lang";
	public static final String		PROJECT_LOCALIZATION = "project.localization";
	public static final String		PROJECT_EXTERNALS = "project.externals";

	public static final String		WAR_NAME = "war.name";
	public static final String		WAR_DESCRIPTOR = "war.descriptor";
	public static final String		WAR_PATH = "war.path";
	public static final String		WAR_LANGUAGE = "war.language";	

	public static final String		SCORM2004_NAME = "scorm2004.name";
	public static final String		SCORM2004_DESCRIPTOR = "scorm2004.descriptor";
	public static final String		SCORM2004_PATH = "scorm2004.path";
	public static final String		SCORM2004_LANGUAGE = "scorm2004.language";	
	
	public static final String		LINK_ALIAS = "link.alias";
	public static final String		LINK_IS_EXTERNAL = "link.isExternal";
	public static final String		LINK_BASE = "link.base";
	public static final String		LINK_EXTERNAL_URI = "link.externalURI";

	public static final String		SUBST_PREFIX = "subst.";
	public static final Pattern		SUBST_PATTERN = Pattern.compile("subst\\..*");
	
	private static final String		PART_DESCRIPTION = ".project.properties";	
	private static final String		CREOLE_EXT = ".cre";	
	private static final String		IMAGE_EXT = ".png";	
	private static final String		JSON_EXT = ".json";
	private static final String		JSON_TREE_PART = "project.tree.json";
	private static final String		LOCALIZATION_PART = "localization.data";
	private static final String[]	PARTS = {
											JSON_TREE_PART,
											LOCALIZATION_PART,
											"project.default.license.cre",
											"style.css",
											"utils.js",
										};
	
	private static final Pattern	CREOLE_PATTERN = Pattern.compile(ItemType.CreoleRef.getPartNamePrefix()+"(\\d+)\\.cre");
	private static final Pattern	DOCUMENT_PATTERN = Pattern.compile(ItemType.DocumentRef.getPartNamePrefix()+"(\\d+)\\.doc");
	private static final Pattern	IMAGE_PATTERN = Pattern.compile(ItemType.ImageRef.getPartNamePrefix()+"(\\d+)\\.png");
	private static final Pattern[]	PATTERNS = {CREOLE_PATTERN, DOCUMENT_PATTERN, IMAGE_PATTERN};
	
	private final LocalizerOwner			owner;
	private final ContentMetadataInterface	mdi;
	private final SubstitutableProperties	props = new SubstitutableProperties();
	private final Map<String, Object>		content = new HashMap<>();
	private final LightWeightListenerList<ProjectChangeListener>	listeners = new LightWeightListenerList<>(ProjectChangeListener.class);  
	private MutableJsonLocalizer			localizer = null;
	private ProjectNavigator				navigator = null;
	private String							projectFileName = "";
	private boolean							prepared = false;
	
	public ProjectContainer(final LocalizerOwner owner, final ContentMetadataInterface mdi) {
		if (owner == null) {
			throw new NullPointerException("Localizer owner can't be null");
		}
		else if (mdi == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else {
			this.owner = owner;
			this.localizer = null;
			this.mdi = mdi;
		}
	}

	@Override
	public Localizer getLocalizer() {
		ensurePrepared();
		return localizer;
	}
	
	public String getUniqueLocalizationKey() {
		ensurePrepared();
		return "localizer."+UUID.randomUUID().toString();
	}
	
	public String createUniqueLocalizationString() {
		ensurePrepared();
		return localizer.createLocalValue(getUniqueLocalizationKey()).getId();
	}
	
	public MutableLocalizedString getLocalizationString(final String id) {
		if (Utils.checkEmptyOrNullString(id)) {
			throw new IllegalArgumentException("String id can't be null or empty"); 
		}
		else {
			ensurePrepared();
			return (MutableLocalizedString) localizer.getLocalizedString(id);
		}
	}
	
	public void addProjectChangeListener(final ProjectChangeListener l) {
		if (l == null) {
			throw new NullPointerException("Project change listener to add can't be null"); 
		}
		else {
			listeners.addListener(l);
		}
	}

	public void removeProjectChangeListener(final ProjectChangeListener l) {
		if (l == null) {
			throw new NullPointerException("Project change listener to remove can't be null"); 
		}
		else {
			listeners.removeListener(l);
		}
	}
	
	public String getProjectFileName() {
		return projectFileName;
	}
	
	public void setProjectFileName(final String name) {
		if (Utils.checkEmptyOrNullString(name)) {
			throw new IllegalArgumentException("Project file name to set can't be null or empty");
		}
		else if (!Objects.equals(projectFileName, name)) {
			final ProjectChangeEvent pce = new ProjectChangeEvent(this, ProjectChangeEvent.ProjectChangeType.PROJECT_FILENAME_CHANGED, getProjectFileName(), name);

			projectFileName = name;
			fireProjectChangeEvent(pce);
		}
	}
	
	public ProjectNavigator getProjectNavigator() {
		ensurePrepared();
		return navigator;
	}

	public boolean hasProjectPart(final String partName) {
		if (Utils.checkEmptyOrNullString(partName)) {
			throw new IllegalArgumentException("Part name can't be null or empty"); 
		}
		else {
			ensurePrepared();
			return content.containsKey(partName);
		}
	}
	
	public <T> T getProjectPartContent(final String partName) {
		if (Utils.checkEmptyOrNullString(partName)) {
			throw new IllegalArgumentException("Part name can't be null or empty"); 
		}
		else if (!content.containsKey(partName)) {
			throw new IllegalArgumentException("Part name ["+partName+"] is missing in the project"); 
		}
		else {
			ensurePrepared();
			return (T)content.get(partName);
		}
	}

	public <T> void addProjectPartContent(final String partName, final T data) {
		if (Utils.checkEmptyOrNullString(partName)) {
			throw new IllegalArgumentException("Part name can't be null or empty"); 
		}
		else if (content.containsKey(partName)) {
			throw new IllegalArgumentException("Part name ["+partName+"] already existst in the project"); 
		}
		else if (data == null) {
			throw new NullPointerException("Data to add can't be null"); 
		}
		else {
			ensurePrepared();
			content.put(partName, data);
			notifyContentChanges(partName);
		}
	}

	public long addProjectPart(final long navigatorNodeId, final ItemType type, final File content) throws IOException {
		if (!getProjectNavigator().hasItemId(navigatorNodeId)) {
			throw new IllegalArgumentException("Navigation node id ["+navigatorNodeId+"] is not exists"); 
		}
		else if (getProjectNavigator().getItem(navigatorNodeId).type != ItemType.Root && getProjectNavigator().getItem(navigatorNodeId).type != ItemType.Subtree) {
			throw new IllegalArgumentException("Navigation node id ["+navigatorNodeId+"] has type ["+getProjectNavigator().getItem(navigatorNodeId).type+"], but only Subtree and Root are available here"); 
		}
		else if (type == null || !(type == ItemType.DocumentRef || type == ItemType.ImageRef)) {
			throw new IllegalArgumentException("Item type can be DocumentRef or ImageRef only"); 
		}
		else if (content == null) {
			throw new IllegalArgumentException("File content can't be null"); 
		}
		else {
			ensurePrepared();
			try(final InputStream	is = new FileInputStream(content)) {
				return addProjectPart(navigatorNodeId, type, content.getName(), is);
			}
		}
	}
	
	public long addProjectPart(final long navigatorNodeId, final ItemType type, final String itemName, final InputStream content) throws IOException {
		if (!getProjectNavigator().hasItemId(navigatorNodeId)) {
			throw new IllegalArgumentException("Navigation node id ["+navigatorNodeId+"] is not exists"); 
		}
		else if (getProjectNavigator().getItem(navigatorNodeId).type != ItemType.Root && getProjectNavigator().getItem(navigatorNodeId).type != ItemType.Subtree) {
			throw new IllegalArgumentException("Navigation node id ["+navigatorNodeId+"] has type ["+getProjectNavigator().getItem(navigatorNodeId).type+"], but only Subtree and Root are available here"); 
		}
		else if (type == null || !(type == ItemType.DocumentRef || type == ItemType.ImageRef || type == ItemType.CreoleRef)) {
			throw new IllegalArgumentException("Item type can be CreoleRef, DocumentRef or ImageRef only"); 
		}
		else if (Utils.checkEmptyOrNullString(itemName)) {
			throw new IllegalArgumentException("Item name can't be null or empty"); 
		}
		else if (content == null) {
			throw new IllegalArgumentException("File content can't be null"); 
		}
		else {
			ensurePrepared();
			
			final long					unique = getProjectNavigator().getUniqueId();
			final ProjectNavigatorItem	toAdd = new ProjectNavigatorItem(unique
													, navigatorNodeId
													, type.getPartNamePrefix()+unique
													, type
													, type.getPartNamePrefix()+' '+itemName
													, createUniqueLocalizationString()
													, -1);
			getProjectNavigator().addItem(toAdd);
			
			final String	partName = getPartNameById(unique);
			
			switch (type) {
				case DocumentRef	:
					try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
						
						Utils.copyStream(content, baos);
						addProjectPartContent(partName, baos.toByteArray());
					}
					break;
				case ImageRef		:
					addProjectPartContent(partName, ImageIO.read(content));
					break;
				case CreoleRef		:
					try(final Reader	rdr = new InputStreamReader(content, PureLibSettings.DEFAULT_CONTENT_ENCODING);
						final Writer	wr = new StringWriter()) {
						
						Utils.copyStream(rdr, wr);
						addProjectPartContent(partName, wr.toString());
					}
					break;
				default:
					throw new UnsupportedOperationException("Item type ["+type+"] is not supported yet");
			}
			return unique;
		}
	}
	
	public <T> void setProjectPartContent(final String partName, final T data) {
		if (Utils.checkEmptyOrNullString(partName)) {
			throw new IllegalArgumentException("Part name can't be null or empty"); 
		}
		else if (!content.containsKey(partName)) {
			throw new IllegalArgumentException("Part name ["+partName+"] is missing in the project"); 
		}
		else if (data == null) {
			throw new NullPointerException("Data to set can't be null"); 
		}
		else {
			ensurePrepared();
			content.put(partName, data);
			notifyContentChanges(partName);
		}
	}

	public <T> void removeProjectPartContent(final String partName) {
		if (Utils.checkEmptyOrNullString(partName)) {
			throw new IllegalArgumentException("Part name can't be null or empty"); 
		}
		else if (!content.containsKey(partName)) {
			throw new IllegalArgumentException("Part name ["+partName+"] is missing in the project"); 
		}
		else {
			ensurePrepared();
			content.remove(partName);
			notifyContentChanges(partName);
		}
	}
	
	public String getPartNameById(final long id) {
		ensurePrepared();
		if (!getProjectNavigator().hasItemId(id)) {
			throw new IllegalArgumentException("Id ["+id+"] doesn't exist in the navigator"); 
		}
		else {
			final ProjectNavigatorItem	item = getProjectNavigator().getItem(id);
			
			switch (item.type) {
				case CreoleRef		:
					return item.type.getPartNamePrefix()+id+".cre";
				case DocumentRef	:
					return item.type.getPartNamePrefix()+id+".doc";
				case ImageRef		:
					return item.type.getPartNamePrefix()+id+".png";
				case Root : case Subtree :
					throw new IllegalArgumentException("Item type ["+item.type+"] can't have part name");
				default :
					throw new UnsupportedOperationException("Item type ["+item.type+"] is not supported yet"); 
			}
		}
	}
	
	public long getIdByPartName(final String partName) {
		if (Utils.checkEmptyOrNullString(partName)) {
			throw new IllegalArgumentException("Part name can't be null or empty"); 
		}
		else {
			ensurePrepared();
			for (Pattern item : PATTERNS) {
				final Matcher	m = item.matcher(partName); 
				
				if (m.find()) {
					final long	id = Long.valueOf(m.group(1));
					
					if (getProjectNavigator().hasItemId(id)) {
						return id;
					}
					else {
						throw new IllegalArgumentException("Part name ["+partName+"] doesn't have appropriative item in the navigator");
					}
				}
			}
			throw new IllegalArgumentException("Part name ["+partName+"] doesn't match any part templates");
		}
	}

	public SubstitutableProperties getProperties() {
		ensurePrepared();
		return props;
	}
	
	public String[] getPartNames() {
		ensurePrepared();

		final String[]	result = new String[content.size() - PARTS.length];
		int	index = 0;
		
loop:	for(Entry<String, Object> item : content.entrySet()) {
			final String	name = item.getKey(); 
			
			for(String exclude : PARTS) {
				if (exclude.equals(name)) {
					continue loop;
				}
			}
			result[index++] = name;
		}
		return result;
	}
	
	public boolean validateProject(final LoggerFacade logger) {
		ensurePrepared();

		return validateProject(logger, props, content);
	}

	/**
	 * <p>Get stream to unload project content. Content must be used to load project by {@linkplain #fromOutputStream()} method</p> 
	 * @return String to unload project content. Can't be null
	 * @see #fromOutputStream()
	 */
	public InputStream toInputStream() {
		ensurePrepared();
		try(final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final ZipOutputStream		zos = new ZipOutputStream(baos)) {
			
			ZipEntry	ze = new ZipEntry(PART_DESCRIPTION);

			ze.setMethod(ZipEntry.DEFLATED);
			zos.putNextEntry(ze);
			props.store(zos, "");
			for (Entry<String, Object> item : content.entrySet()) {
				ze = new ZipEntry(item.getKey());
				ze.setMethod(ZipEntry.DEFLATED);
				zos.putNextEntry(ze);
				storePart(item.getKey(), item.getValue(), zos);
				zos.flush();
			}
			zos.finish();
			
			return new ByteArrayInputStream(baos.toByteArray());
		} catch (IOException e) {
			PureLibSettings.CURRENT_LOGGER.message(Severity.error, e, e.getLocalizedMessage());
			return new InputStream() {
				@Override
				public int read() throws IOException {
					return -1;
				}
			};
		}
	}

	/**
	 * <p>Get stream to load or create new project. New project will be created when loaded content length is equals 0, otherwise content must be
	 * well-formed project descriptor, created by {@linkplain #toInputStream()} method</p>   
	 * @return stream to load project content to. Can't be null
	 * @see #toInputStream() 
	 */
	public OutputStream fromOutputStream() {
		return new ByteArrayOutputStream() {
			public void close() throws java.io.IOException {
				super.close();
				final byte[]	content = toByteArray();
				
				if (content.length == 0) {
					createNewProject();
					prepared = true;
				}
				else {
					loadProject(content);
					prepared = true;
				}
			};
		};
	}
	
	protected void fireProjectChangeEvent(final ProjectChangeEvent event) {
		listeners.fireEvent((l)->l.processEvent(event));
	}

	private void ensurePrepared() {
		if (!prepared) {
			throw new IllegalStateException("Project is not prepared yet. Load existent or empty project by calling fromOutputStream() method"); 
		}
	}

	
	private void createNewProject() throws IOException {
		final SubstitutableProperties	projectProps = new SubstitutableProperties();
		final Map<String, Object>		projectParts = new HashMap<>();
		
		try(final InputStream	is = getClass().getResourceAsStream("project.template.properties")) {
			projectProps.load(new InputStreamReader(is, PureLibSettings.DEFAULT_CONTENT_ENCODING));
		}
		for (String item : PARTS) {
			try(final InputStream	is = getClass().getResourceAsStream(item)) {
				loadPart(item, is, projectProps, projectParts);
			}
		}
		try(final LoggerFacade	logger = new StringLoggerFacade()) {
			if (validateProject(logger, projectProps, projectParts)) {
				props.clear();
				props.putAll(projectProps);
				content.clear();
				content.putAll(projectParts);
				navigator = prepareProjectNavigator((JsonNode)content.get(props.getProperty(PROJECT_TREE)), props.getProperty(PROJECT_ROOT));
			}
			else {
				throw new IOException("Project validation failed : "+logger.toString()); 
			}
		}
	}
	
	private ProjectNavigator prepareProjectNavigator(final JsonNode content, final String rootName) throws IOException {
		try{
			return new ProjectNavigator(this, content, rootName);
		} catch (ContentException e) {
			throw new IOException(e); 
		}
	}

	private MutableJsonLocalizer prepareLocalizer(final byte[] content) {
		final MutableJsonLocalizer	newlocalizer = (MutableJsonLocalizer)LocalizerFactory.getLocalizer(
										URI.create(Localizer.LOCALIZER_SCHEME+":mutablejson:"+URIUtils.convert2selfURI(content).toString())
									);

		if (localizer != null) {
			owner.getLocalizer().pop(localizer);
		}
		owner.getLocalizer().push(newlocalizer);
		return newlocalizer;
	}
	
	private boolean validateProject(final LoggerFacade logger, final SubstitutableProperties props, final Map<String, Object> parts) {
		final SubstitutableProperties	subst = new SubstitutableProperties(); 
		final Map<String, Set<String>>	anchors = new HashMap<>();
		boolean 						result = true;
		
		for(String item : props.availableKeys(SUBST_PATTERN)) {
			subst.setProperty(item.substring(item.indexOf('.')+1), props.getPropertyAsIs(item));
		}
		
		for(Entry<String, Object> entry : parts.entrySet()) {	// Collect anchors
			for (ItemType item : ItemType.values()) {
				if (entry.getKey().startsWith(item.getPartNamePrefix())) {
					switch (item) {
						case CreoleRef		:
							anchors.put(entry.getKey(), new HashSet<>(Arrays.asList(SearchUtils.extractCreoleAnchors((String)entry.getValue()))));
							break;
						case DocumentRef		:
							anchors.put(entry.getKey(), new HashSet<>());
							break;
						case ImageRef		:
							anchors.put(entry.getKey(), new HashSet<>());
							break;
						default :
					}
				}
			}
		}
		
		for(Entry<String, Object> entry : parts.entrySet()) {	// Collect and check links anchors
			for (ItemType item : ItemType.values()) {
				if (entry.getKey().startsWith(item.getPartNamePrefix())) {
					switch (item) {
						case CreoleRef		:
							try {
								for (CreoleLink link : SearchUtils.extractCreoleLinks((String)entry.getValue())) {
									final URI	uri = link.toURI(subst);
									
									switch(link.type) {
										case CreoleLink		:
											final String	path = uri.getPath(), fragment = uri.getFragment();
											
											if (!anchors.containsKey(path)) {
												logger.message(Severity.warning, buildHyperlink(entry.getKey(),link) + "missing ref "+path);
												result = false;
											}
											else if (fragment != null && !anchors.get(path).contains(fragment)) {
												logger.message(Severity.warning, buildHyperlink(entry.getKey(),link) + "missing fragment "+fragment+" inside ref "+path);
												result = false;
											}											
											break;
										case ExternalLink	:
											break;
										case ImageLink		:
											if (!anchors.containsKey(link.ref)) {
												logger.message(Severity.warning, buildHyperlink(entry.getKey(),link) + "missing ref "+link.ref);
												result = false;
											}
											break;
										default :
											throw new UnsupportedOperationException("Link type ["+link.type+"] is not supported yet"); 
									}
								}
							} catch (IllegalArgumentException | SyntaxException exc) {
								logger.message(Severity.error, exc, exc.getLocalizedMessage());
								result = false;
							}
							break;
						default :
					}
				}
			}
		}
		return result;
	}

	private String buildHyperlink(final String partName, final CreoleLink link) {
		return "<a href=\""+partName+"?row="+link.row+"&col="+link.col+"\">"+partName+"["+link.row+","+link.col+"]</a>";
	}
	
	private void loadPart(final String name, final InputStream is, final SubstitutableProperties props, final Map<String, Object> target) throws IOException {
		if (name.equals(LOCALIZATION_PART)) {
			final ByteArrayOutputStream	baos = new ByteArrayOutputStream();

			Utils.copyStream(is, baos);
			target.put(props.getProperty(PROJECT_LOCALIZATION), baos.toByteArray());
			localizer = prepareLocalizer((byte[])target.get(props.getProperty(PROJECT_LOCALIZATION)));
		}
		else if (name.endsWith(CREOLE_EXT)) {
			final Reader	rdr = new InputStreamReader(is, PureLibSettings.DEFAULT_CONTENT_ENCODING);
			final Writer	wr = new StringWriter();
			
			Utils.copyStream(rdr, wr);
			target.put(name, wr.toString());
		}
		else if (name.endsWith(IMAGE_EXT)) {
			target.put(name, ImageIO.read(is));
		}
		else if (name.endsWith(JSON_EXT)) {
			final Reader			rdr = new InputStreamReader(is, PureLibSettings.DEFAULT_CONTENT_ENCODING);
			final JsonStaxParser	parser = new JsonStaxParser(rdr);
			
			try{parser.next();
				target.put(name, JsonUtils.loadJsonTree(parser));
			} catch (SyntaxException e) {
				throw new IOException(e);
			}
		}
		else {
			final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
			
			Utils.copyStream(is, baos);
			target.put(name, baos.toByteArray());
		}
	}

	private void storePart(final String name, final Object content, final OutputStream os) throws IOException {
		if (name.equals(LOCALIZATION_PART)) {
			final Writer			wr = new OutputStreamWriter(os, PureLibSettings.DEFAULT_CONTENT_ENCODING);
			final JsonStaxPrinter	prn = new JsonStaxPrinter(wr);
			
			try{
				localizer.saveContent(prn);
				prn.flush();
			} catch (PrintingException e) {
				throw new IOException(e);
			}
		}
		else if (name.equals(JSON_TREE_PART)) {
			final Writer			wr = new OutputStreamWriter(os, PureLibSettings.DEFAULT_CONTENT_ENCODING);
			final JsonStaxPrinter	prn = new JsonStaxPrinter(wr);
			
			try{
				JsonUtils.unloadJsonTree(getProjectNavigator().buildJsonNode(), prn);
				prn.flush();
			} catch (PrintingException e) {
				throw new IOException(e);
			}
		}
		else if (name.endsWith(CREOLE_EXT)) {
			final Writer	wr = new OutputStreamWriter(os, PureLibSettings.DEFAULT_CONTENT_ENCODING);
			
			wr.write(content.toString());
			wr.flush();
		}
		else if (name.endsWith(IMAGE_EXT)) {
			ImageIO.write((RenderedImage)content, "png", os);
		}
		else if (name.endsWith(JSON_EXT)) {
			final Writer			wr = new OutputStreamWriter(os, PureLibSettings.DEFAULT_CONTENT_ENCODING);
			final JsonStaxPrinter	prn = new JsonStaxPrinter(wr);
			
			try{
				JsonUtils.unloadJsonTree((JsonNode)content, prn);
				prn.flush();
			} catch (PrintingException e) {
				throw new IOException(e);
			}
		}
		else {
			os.write((byte[])content);
			os.flush();
		}
	}
	
	private void loadProject(final byte[] projectContent) throws IOException {
		final SubstitutableProperties	projectProps = new SubstitutableProperties();
		final Map<String, Object>		projectParts = new HashMap<>();
		
		try(final InputStream		is = new ByteArrayInputStream(projectContent);
			final ZipInputStream	zis = new ZipInputStream(is)) {
			boolean		propsDetected = false;
			
			ZipEntry	ze;
			
			while ((ze = zis.getNextEntry()) != null) {
				if (PART_DESCRIPTION.equals(ze.getName())) {
					projectProps.load(new InputStreamReader(zis, PureLibSettings.DEFAULT_CONTENT_ENCODING));
					propsDetected = true;
				}
				else {
					loadPart(ze.getName(), zis, projectProps, projectParts);
				}
			}
			if (!propsDetected) {
				throw new IOException("Project structure corrupted: mandatory part ["+PART_DESCRIPTION+"] is missing"); 
			}
			else if (!DEFAULT_PROJECT_VERSION.equals(projectProps.getProperty(PROJECT_VERSION))) {
				throw new IOException("Project loading failed : unsupported project version ["+projectProps.getProperty(PROJECT_VERSION)+"]"); 
			}
			else {
				props.clear();
				props.putAll(projectProps);
				content.clear();
				content.putAll(projectParts);
				navigator = prepareProjectNavigator((JsonNode)content.get(props.getProperty(PROJECT_TREE)), props.getProperty(PROJECT_ROOT));
			}
		}
	}
	
	private void notifyContentChanges(final String partName) {
		final ProjectNavigatorItem	pni = getProjectNavigator().getItem(getIdByPartName(partName));
		final ProjectChangeEvent	pce;
		
		switch (pni.type) {
			case ImageRef		:
				pce = new ProjectChangeEvent(this, ProjectChangeType.ITEM_CONTENT_CHANGED, pni.parent, pni.id);
				break;
			case DocumentRef	:
				pce = new ProjectChangeEvent(this, ProjectChangeType.ITEM_CONTENT_CHANGED, pni.parent, pni.id);
				break;
			case CreoleRef		:
				pce = new ProjectChangeEvent(this, ProjectChangeType.ITEM_CONTENT_CHANGED, pni.parent, pni.id);
				break;
			case Subtree		:
				pce = new ProjectChangeEvent(this, ProjectChangeType.PART_CONTENT_CHANGED, pni.parent, pni.id);
				break;
			case Root :
			default :
				throw new UnsupportedOperationException("Navigator item type ["+pni.type+"] is not supported yet");
		}
		fireProjectChangeEvent(pce);
	}
}
