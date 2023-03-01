package chav1961.csce.builders;

import java.awt.Image;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import chav1961.csce.project.ProjectContainer;
import chav1961.csce.project.ProjectNavigator;
import chav1961.csce.project.ProjectNavigator.ItemType;
import chav1961.csce.project.ProjectNavigator.ProjectNavigatorItem;
import chav1961.csce.utils.SimpleLocalizedString;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.MarkupOutputFormat;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.SupportedLanguages;
import chav1961.purelib.streams.char2char.CreoleWriter;

//Builder:
//1. Build full navigation tree by project structure. It includes:
//- part ref
//- creole pages ref
//- documents ref
//2. If Any creole ref is directly under the root, build index.html from it
//3. Walk on navigation tree and build every part:
//- root skips
//- part creates new folder with the name
//- creole page creates <name>.html file
//- documents places content as-is
//- images places contents as-is
//4. Build JS vocabulary for search
//
//Html generation:
//- build prolog:
//-- project name
//-- page title
//-- style references
//- build left navigation bar
//-- build search string
//-- build reference to the tree content
//- build right content
//- build epilog
//-- project contacts
//-- project copyrights
public class HTMLBuilder implements Closeable {
	private static final String		OVERVIEW_PAGE_NAME = "index";
	private static final String		CREOLE_PAGE_SUFFIX = ".cre";
	private static final String		HTML_PAGE_SUFFIX = ".html";
	private static final String		PROLOGUE;
	private static final String		EPILOGUE;
	
	private final Localizer			localizer;
	private final ProjectContainer	project;
	
	static {
		try{
			PROLOGUE = Utils.fromResource(HTMLBuilder.class.getResource("html.prologue.template"));
			EPILOGUE = Utils.fromResource(HTMLBuilder.class.getResource("html.epilogue.template"));
		} catch (IOException exc) {
			throw new PreparationException(exc);
		}
	}
	
	public HTMLBuilder(final Localizer localizer, final ProjectContainer project) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else if (project == null) {
			throw new NullPointerException("Project container can't be null"); 
		}
		else {
			this.localizer = localizer;
			this.project = project;
		}
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	public void upload(final ZipOutputStream os) throws IOException, ContentException {
		final ProjectNavigatorItem		root = project.getProjectNavigator().getItem(0);
		final ProjectNavigatorItem[]	children = project.getProjectNavigator().getChildren(root.id);
		boolean							overviewFound = false;

		for (SupportedLanguages lang : SupportedLanguages.values()) {
			for (ProjectNavigatorItem pni : children) {
				if (pni.type == ItemType.CreoleRef) {
					buildOverviewPage(pni, lang, os);
					overviewFound = true;
					break;
				}
			}
			if (!overviewFound) {
				buildDefaultOverviewPage(root, lang, os);
			}
			buildLangSpecificContent(project.getProjectNavigator(), lang, os);
		}
		buildCommonContent(project.getProjectNavigator(), os);
		for (String item : project.getPartNames()) {
			if (item.endsWith(".css") || item.endsWith(".js")) {
				storePartContent(item, project.getProjectPartContent(item), os);
			}
		}
	}
	
	private void buildDefaultOverviewPage(final ProjectNavigatorItem item, final SupportedLanguages lang, final ZipOutputStream os) throws IOException, ContentException {
		final ZipEntry		ze = new ZipEntry(OVERVIEW_PAGE_NAME+'_'+lang.name()+HTML_PAGE_SUFFIX);
		final String		content = project.getProjectPartContent(project.getPartNameById(item.id));
		final String		navigatorTree = buildNavigatorTree(project.getProjectNavigator(), lang, "/");
		
		ze.setMethod(ZipEntry.DEFLATED);
		os.putNextEntry(ze);
		
		final Writer		wr = new OutputStreamWriter(os, PureLibSettings.DEFAULT_CONTENT_ENCODING);
		
		try(final CreoleWriter	cwr = new CreoleWriter(wr, MarkupOutputFormat.XML2HTML
										, (Writer wrP, Object instP)->writePrologue(wrP, "/", getPageTitle(item), navigatorTree)
										, (Writer wrE, Object instE)->writeEpilogue(wrE))) {
			cwr.write(content);
		}
		wr.flush();
		os.closeEntry();
	}

	private void buildOverviewPage(final ProjectNavigatorItem item, final SupportedLanguages lang, final ZipOutputStream os) throws IOException, ContentException {
		final ZipEntry		ze = new ZipEntry(OVERVIEW_PAGE_NAME+'_'+lang.name()+HTML_PAGE_SUFFIX);
		final String		content = project.getProjectPartContent(project.getPartNameById(item.id));
		final String		navigatorTree = buildNavigatorTree(project.getProjectNavigator(), lang, "/");
		
		ze.setMethod(ZipEntry.DEFLATED);
		os.putNextEntry(ze);
		
		final Writer		wr = new OutputStreamWriter(os, PureLibSettings.DEFAULT_CONTENT_ENCODING);
		
		try(final CreoleWriter	cwr = new CreoleWriter(wr, MarkupOutputFormat.XML2HTML
										, (Writer wrP, Object instP)->writePrologue(wrP, "/", getPageTitle(item),navigatorTree)
										, (Writer wrE, Object instE)->writeEpilogue(wrE))) {
			cwr.write(content);
		}
		wr.flush();
		os.closeEntry();
	}

	private String buildNavigatorTree(final ProjectNavigator navigator, final SupportedLanguages lang, final String currentPath) throws ContentException, IOException {
		final StringBuilder	sb = new StringBuilder();
		final List<String>	path = new ArrayList<>();
		
		navigator.walkDown((mode, node)->{
			switch (mode) {
				case ENTER	:
					switch (node.type) {
						case Subtree	:
							path.add(node.name);
							for (int index = 0; index < path.size(); index++) {
								sb.append('*');
							}
							sb.append('*').append(' ').append(node.desc).append('\n');
							break; 
						case DocumentRef:
							for (int index = 0; index < path.size(); index++) {
								sb.append('*');
							}
							sb.append(" [[").append(relativize(toPath(path),currentPath)).append(project.getPartNameById(node.id)).append('|').append(node.desc).append("]]\n");
							break;
						case CreoleRef: 
							for (int index = 0; index < path.size(); index++) {
								sb.append('*');
							}
							sb.append(" [[").append(relativize(toPath(path),currentPath)).append(project.getPartNameById(node.id).replace(CREOLE_PAGE_SUFFIX, "")+'_'+lang.name())
											.append(HTML_PAGE_SUFFIX).append('|').append(project.getLocalizer().getValue4Locale(lang.getLocale(), node.titleId)).append("]]\n");
							break;
						case ImageRef : case Root : 
							break;
						default:
							throw new UnsupportedOperationException("Node type ["+node.type+"] is not supported yet"); 
					}
					break;
				case EXIT	:
					if (node.type == ItemType.Subtree) {
						path.remove(path.size()-1);
					}
					break;
				default:
					throw new UnsupportedOperationException("Mode ["+mode+"] is not supported yet");
			}
			return ContinueMode.CONTINUE;
		});
		
		try(final Writer			wr = new StringWriter()) {
			try(final CreoleWriter	cwr = new CreoleWriter(wr, MarkupOutputFormat.XML2HTML
										, (Writer wrP, Object instP)->true
										, (Writer wrE, Object instE)->true)) {
				
				cwr.write(sb.toString());
			}
			wr.flush();
			return wr.toString();
		}
	}	

	private void buildCommonContent(final ProjectNavigator navigator, final ZipOutputStream os) throws ContentException, IOException {
		final List<String>	path = new ArrayList<>();
		
		navigator.walkDown((mode, node)->{
			if (mode == NodeEnterMode.ENTER) {
				switch (node.type) {
					case Subtree	:
						path.add(node.name);
						break;
					case ImageRef	:
						storeImage(toPath(path), node, os);
						break;
					case DocumentRef:
						storeDocument(toPath(path), node, os);
						break;
					case CreoleRef: case Root: 
						break;
					default:
						throw new UnsupportedOperationException("Node type ["+node.type+"] is not supported yet"); 
				}
			}
			else if (node.type == ItemType.Subtree) {
				path.remove(path.size()-1);
			}
			return ContinueMode.CONTINUE;
		});
	}	
	
	private void buildLangSpecificContent(final ProjectNavigator navigator, final SupportedLanguages lang, final ZipOutputStream os) throws ContentException, IOException {
		final List<String>	path = new ArrayList<>();
		
		navigator.walkDown((mode, node)->{
			if (mode == NodeEnterMode.ENTER) {
				final String	navigatorTree = buildNavigatorTree(project.getProjectNavigator(), lang, toPath(path));
				
				switch (node.type) {
					case Subtree	:
						path.add(node.name);
						break;
					case CreoleRef: 
						storeCreolePage(toPath(path), node.titleId, navigatorTree, node, lang, os);
						break;
					case Root : case ImageRef : case DocumentRef :
						break;
					default:
						throw new UnsupportedOperationException("Node type ["+node.type+"] is not supported yet"); 
				}
			}
			else if (node.type == ItemType.Subtree) {
				path.remove(path.size()-1);
			}
			return ContinueMode.CONTINUE;
		});
	}

	private String toPath(final List<String> path) {
		final StringBuilder	sb = new StringBuilder();
		
		for(String item : path) {
			sb.append('/').append(item);
		}
		return sb.append('/').toString();
	}
	
	private String relativize(final String path, final String relatedTo) {
		if (path.equals(relatedTo)) {
			return "";
		}
		else {
			final Path first = Paths.get(path);
			final Path second = Paths.get(relatedTo);
	        final Path pathRelative = second.relativize(first);			
		
	        return pathRelative.toString()+'/';
		}
	}
	
	private void storeImage(final String path, final ProjectNavigatorItem node, final ZipOutputStream os) throws IOException {
		final ZipEntry	ze = new ZipEntry(path+project.getPartNameById(node.id));
		
		ze.setMethod(ZipEntry.DEFLATED);
		os.putNextEntry(ze);
		os.write(project.getProjectPartContent(project.getPartNameById(node.id)));
		os.closeEntry();
	}

	private void storeDocument(final String path, final ProjectNavigatorItem node, final ZipOutputStream os) throws IOException {
		final ZipEntry	ze = new ZipEntry(path+project.getPartNameById(node.id));
		
		ze.setMethod(ZipEntry.DEFLATED);
		os.putNextEntry(ze);
		os.write(project.getProjectPartContent(project.getPartNameById(node.id)));
		os.closeEntry();
	}


	private void storeCreolePage(final String path, final String comment, final String navigatorTree, final ProjectNavigatorItem node, final SupportedLanguages lang, final ZipOutputStream os) throws IOException {
		final ZipEntry	ze = new ZipEntry(path+project.getPartNameById(node.id).replace(CREOLE_PAGE_SUFFIX,"")+'_'+lang.name()+HTML_PAGE_SUFFIX);
		final String	content = project.getProjectPartContent(project.getPartNameById(node.id));
		
		ze.setMethod(ZipEntry.DEFLATED);
		os.putNextEntry(ze);
		
		final Writer		wr = new OutputStreamWriter(os, PureLibSettings.DEFAULT_CONTENT_ENCODING);
		
		try(final CreoleWriter	cwr = new CreoleWriter(wr, MarkupOutputFormat.XML2HTML
										, (Writer wrP, Object instP)->writePrologue(wrP, path, comment, navigatorTree)
										, (Writer wrE, Object instE)->writeEpilogue(wrE))) {
			cwr.write(content);
		}
		wr.flush();
		os.closeEntry();
	}

	private void storePartContent(final String partName, final byte[] content, final ZipOutputStream os) throws IOException {
		final ZipEntry	ze = new ZipEntry(partName);
		
		ze.setMethod(ZipEntry.DEFLATED);
		os.putNextEntry(ze);
		os.write(content);
		os.closeEntry();
	}

	private boolean writePrologue(final Writer wr, final String path, final String comment, final String navigatorTree) throws IOException {
		wr.write(CharUtils.substitute("prologue", PROLOGUE, (s)->{
			try{switch (s) {
					case "path" 	:
						return relativize("/", path);
					case "comment" 	:
						return getLocalizedValue(s, comment);
					case "navigator":
						return navigatorTree;
					default :
						if (project.getProperties().containsKey(s)) {
							return  getLocalizedValue(s, project.getProperties().getProperty(s));
						}
						else {
							return getLocalizedValue(s, s);
						}
				}
			} catch (SyntaxException e) {
				return s+"???"+e.getLocalizedMessage()+"???";
			}
		}));
		return true;
	}

	private boolean writeEpilogue(final Writer wr) throws IOException {
		wr.write(CharUtils.substitute("epilologue", EPILOGUE, (s)->{
			try{switch (s) {
					case "year" 	:
						return ""+(1900 + new Date(System.currentTimeMillis()).getYear());
					default :
						if (project.getProperties().containsKey(s)) {
							return getLocalizedValue(s, project.getProperties().getProperty(s));
						}
						else {
							return getLocalizedValue(s, s);
						}
				}
			} catch (SyntaxException e) {
				return s+"???"+e.getLocalizedMessage()+"???";
			}
		}));
		return true;
	}

	private String getPageTitle(final ProjectNavigatorItem node) {
		return project.getLocalizer().getValue(node.titleId);
	}
	
	private String getLocalizedValue(final String key, final String value) throws SyntaxException {
		if (value.startsWith("{")) {
			return new SimpleLocalizedString(key, value).getValue(project.getLocalizer().currentLocale().getLocale());
		}
		else if (value.startsWith("localizer.")) {
			return project.getLocalizer().getValue(value);
		}
		else {
			return value;
		}
	}
}
