package chav1961.csce.builders;

import java.awt.Image;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import chav1961.csce.project.ProjectContainer;
import chav1961.csce.project.ProjectNavigator;
import chav1961.csce.project.ProjectNavigator.ItemType;
import chav1961.csce.project.ProjectNavigator.ProjectNavigatorItem;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.MarkupOutputFormat;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.streams.char2char.CreoleWriter;
import chav1961.purelib.streams.interfaces.PrologueEpilogueMaster;

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
	private static final String		OVERVIEW_PAGE_NAME = "index.html";
	private static final String		NAVIGATOR_PAGE_NAME = "navigator.html";
	
	private final Localizer			localizer;
	private final ProjectContainer	project;
	
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
		final ProjectNavigatorItem		root = project.getProjectNavigator().getItem(1);
		final ProjectNavigatorItem[]	children = project.getProjectNavigator().getChildren(root.id);
		boolean							overviewFound = false;
		
		for (ProjectNavigatorItem pni : children) {
			if (pni.type == ItemType.CreoleRef) {
				buildOverviewPage(pni, os);
				overviewFound = true;
				break;
			}
		}
		if (!overviewFound) {
			buildDefaultOverviewPage(root, os);
		}
		buildNavigatorTree(project.getProjectNavigator(), os);
		buildContent(project.getProjectNavigator(), os);
	}
	
	private void buildDefaultOverviewPage(final ProjectNavigatorItem item, final ZipOutputStream os) throws IOException {
		final ZipEntry		ze = new ZipEntry(OVERVIEW_PAGE_NAME);
		final String		content = project.getProjectPartContent(project.getPartNameById(item.id));
		
		ze.setMethod(ZipEntry.DEFLATED);
		os.putNextEntry(ze);
		
		final Writer		wr = new OutputStreamWriter(os, PureLibSettings.DEFAULT_CONTENT_ENCODING);
		
		try(final CreoleWriter	cwr = new CreoleWriter(wr, MarkupOutputFormat.XML2HTML, (Writer wrP, Object instP)->writePrologue(wrP), (Writer wrE, Object instE)->writeEpilogue(wrE))) {
			cwr.write(content);
		}
		wr.flush();
		os.closeEntry();
	}

	private void buildOverviewPage(final ProjectNavigatorItem item, final ZipOutputStream os) throws IOException {
		final ZipEntry		ze = new ZipEntry(OVERVIEW_PAGE_NAME);
		final String		content = project.getProjectPartContent(project.getPartNameById(item.id));
		
		ze.setMethod(ZipEntry.DEFLATED);
		os.putNextEntry(ze);
		
		final Writer		wr = new OutputStreamWriter(os, PureLibSettings.DEFAULT_CONTENT_ENCODING);
		
		try(final CreoleWriter	cwr = new CreoleWriter(wr, MarkupOutputFormat.XML2HTML, (Writer wrP, Object instP)->writePrologue(wrP), (Writer wrE, Object instE)->writeEpilogue(wrE))) {
			cwr.write(content);
		}
		wr.flush();
		os.closeEntry();
	}

	private void buildNavigatorTree(final ProjectNavigator navigator, final ZipOutputStream os) throws ContentException, IOException {
		final StringBuilder	sb = new StringBuilder();
		final List<String>	path = new ArrayList<>();
		
		navigator.walkDown((mode, node)->{
			if (mode == NodeEnterMode.ENTER) {
				switch (node.type) {
					case Subtree	:
						path.add(node.name);
						for (int index = 0; index < path.size(); index++) {
							sb.append('*');
						}
						sb.append(' ').append(node.desc).append('\n');
						break; 
					case DocumentRef:
						for (int index = 0; index < path.size(); index++) {
							sb.append('*');
						}
						sb.append(" [[./").append(project.getPartNameById(node.id)).append('|').append(node.desc).append("]]\n");
						break;
					case CreoleRef: 
						for (int index = 0; index < path.size(); index++) {
							sb.append('*');
						}
						sb.append(" [[./").append(project.getPartNameById(node.id)).append('|').append(node.desc).append("]]\n");
						break;
					case ImageRef : case Root : 
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
		
		final ZipEntry		ze = new ZipEntry(NAVIGATOR_PAGE_NAME);
		
		ze.setMethod(ZipEntry.DEFLATED);
		os.putNextEntry(ze);
		
		final Writer		wr = new OutputStreamWriter(os, PureLibSettings.DEFAULT_CONTENT_ENCODING);
		
		try(final CreoleWriter	cwr = new CreoleWriter(wr, MarkupOutputFormat.XML2HTML, (Writer wrP, Object instP)->writePrologue(wrP), (Writer wrE, Object instE)->writeEpilogue(wrE))) {
			cwr.write(sb.toString());
		}
		wr.flush();
		os.closeEntry();
	}	
	
	private void buildContent(final ProjectNavigator navigator, final ZipOutputStream os) throws ContentException, IOException {
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
					case CreoleRef: 
						storeCreolePage(toPath(path), node, os);
						break;
					case Root: 
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


	private void storeCreolePage(final String path, final ProjectNavigatorItem node, final ZipOutputStream os) throws IOException {
		final ZipEntry	ze = new ZipEntry(path+project.getPartNameById(node.id));
		final String		content = project.getProjectPartContent(project.getPartNameById(node.id));
		
		ze.setMethod(ZipEntry.DEFLATED);
		os.putNextEntry(ze);
		
		final Writer		wr = new OutputStreamWriter(os, PureLibSettings.DEFAULT_CONTENT_ENCODING);
		
		try(final CreoleWriter	cwr = new CreoleWriter(wr, MarkupOutputFormat.XML2HTML, (Writer wrP, Object instP)->writePrologue(wrP), (Writer wrE, Object instE)->writeEpilogue(wrE))) {
			cwr.write(content);
		}
		wr.flush();
		os.closeEntry();
	}

	private boolean writePrologue(final Writer wr) throws IOException {
		wr.write("<!DOCTYPE html>\r\n"
				+ "<html>\r\n"
				+ " <head>\r\n"
				+ "  <meta charset=\"utf-8\">\r\n"
				+ "  <title>Пример страницы</title>\r\n"
				+ "  <style>\r\n"
				+ "  p { color:  navy; }\r\n"
				+ "  </style>\r\n"
				+ " </head>\r\n"
				+ " <body class=\"creole\">");
		return true;
	}

	private boolean writeEpilogue(final Writer wr) throws IOException {
		wr.write(" </body>\r\n"
				+ "</html>");
		return true;
	}
	
	
}
