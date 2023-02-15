package chav1961.csce.builders;

import java.awt.Image;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import chav1961.csce.project.ProjectContainer;
import chav1961.csce.project.ProjectNavigator;
import chav1961.csce.project.ProjectNavigator.ItemType;
import chav1961.csce.project.ProjectNavigator.ProjectNavigatorItem;
import chav1961.purelib.i18n.interfaces.Localizer;

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
public class HTMLBuilder {
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
	
	public void upload(final ZipOutputStream os) throws IOException {
		final ProjectNavigatorItem	root = project.getProjectNavigator().getItem(1);
		final String[]				children = root.getMetadataChildrenNames();
		boolean						overviewFound = false;
		
		for (String item : children) {
			final ProjectNavigatorItem	pni = BuilderUtils.itemByMetadata(project, root.getNodeMetadata(item));
			
			if (pni.type == ItemType.CreoleRef) {
				buildOverviewPage(pni, os);
				overviewFound = true;
				break;
			}
		}
		if (!overviewFound) {
			buildDefaultOverviewPage(root, os);
		}
		walkNavigatorTree("", root, os);
	}
	
	private void buildDefaultOverviewPage(final ProjectNavigatorItem root, final ZipOutputStream os) throws IOException {
		final ZipEntry	ze = new ZipEntry("index.html");
		
		ze.setMethod(ZipEntry.DEFLATED);
		os.putNextEntry(ze);
		// TODO Auto-generated method stub
	}

	private void buildOverviewPage(final ProjectNavigatorItem item, final ZipOutputStream os) throws IOException {
		final ZipEntry	ze = new ZipEntry("index.html");
		
		ze.setMethod(ZipEntry.DEFLATED);
		os.putNextEntry(ze);
		// TODO Auto-generated method stub
	}
	
	private void buildNavigatorTree(final ProjectNavigator navigator, final ZipOutputStream os) {
		
	}

	private void buildCreolePage(final String creolePage, final ZipOutputStream os) {
		
	}

	private void uploadDocument(final byte[] content, final ZipOutputStream os) {
		
	}

	private void uploadImage(final Image content, final ZipOutputStream os) {
		
	}
	
	private void walkNavigatorTree(final String prefix, final ProjectNavigatorItem item, final ZipOutputStream os) {
		switch (item.type) {
			case CreoleRef		:
				break;
			case DocumentRef	:
				break;
			case ImageRef		:
				break;
			case Subtree		:
				break;
			case Root			:
				break;
			default:
				throw new UnsupportedOperationException("Item type ["+item.type+"] is not supported yet"); 
		}
	}
	
}
