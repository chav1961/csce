package chav1961.csce.project;

import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import chav1961.csce.project.ProjectNavigator.ItemType;
import chav1961.csce.project.ProjectNavigator.ProjectNavigatorItem;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.fsys.AbstractFileSystem;
import chav1961.purelib.fsys.interfaces.DataWrapperInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

public class ProjectFileSystem extends AbstractFileSystem {
	public static final URI		SERVE = URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":csce:/");
	public static final String	NAVIGATION_NODE = "navigationNode";
	
	private static final URI[]	EMPTY_LIST = new URI[0];
	
	private final Map<String, ReentrantReadWriteLock>	locks = new HashMap<>();
	private final ProjectContainer	container;
	private final ProjectFileSystem	parent;
	private ReentrantReadWriteLock	lock = null;
	
	public ProjectFileSystem(final ProjectContainer container) {
		super(SERVE);
		this.container = container;
		this.parent = null;
	}

	private ProjectFileSystem(final URI rootPath, final ProjectFileSystem parent) {
		super(rootPath);
		this.container = null;
		this.parent = parent;
	}
	
	@Override
	public boolean canServe(final URI uriSchema) {
		if (uriSchema == null) {
			throw new NullPointerException("URI schema can't be null");
		}
		else {
			return URIUtils.canServeURI(uriSchema, SERVE);
		}
	}

	@Override
	public FileSystemInterface newInstance(final URI uriSchema) throws EnvironmentException {
		if (uriSchema == null) {
			throw new NullPointerException("URI schema can't be null");
		}
		else {
			return new ProjectFileSystem(rootPath, this);
		}
	}

	@Override
	public FileSystemInterface clone() {
		try {
			return new ProjectFileSystem(URIUtils.appendRelativePath2URI(rootPath, getPath()), this);
		} catch (IOException e) {
			throw new IllegalArgumentException("CLone failed: "+e.getLocalizedMessage(), e); 
		}
	}

	@Override
	public DataWrapperInterface createDataWrapper(final URI actualPath) throws IOException {
		return new ProjectDataWrapper(URIUtils.appendRelativePath2URI(rootPath, getPath()), actualPath);
	}
	
	ProjectContainer getContainer() {
		if (container != null) {
			return container;
		}
		else {
			return parent.getContainer();
		}
	}
	
	private class ProjectDataWrapper implements DataWrapperInterface {
		private final URI				rootPath;
		private final URI				actualPath;
		private final String			uri;
		private ProjectNavigatorItem	node;
		private String					lastName = "";
		private boolean					exists = true;
		
		private ProjectDataWrapper(final URI rootPath, final URI actualPath) throws IOException {
			ProjectNavigatorItem	currentItem = getContainer().getProjectNavigator().getRoot();
			final String			totalPath = (rootPath.getPath() != null ? rootPath.getPath() : "") + (actualPath.getPath() != null ? actualPath.getPath() : ""); 
			
loop:		for(String item : totalPath.split("/")) {
				if (!item.isEmpty()) {
					for(ProjectNavigatorItem child : getContainer().getProjectNavigator().getChildren(currentItem.id)) {
						if (child.name.equals(item)) {
							currentItem = child;
							continue loop;
						}
					}
					exists = false;
					break loop;
				}
			}
			this.rootPath = rootPath;
			this.actualPath = actualPath;
			this.uri = "/";
			this.node = currentItem;
		}
		
		@Override
		public boolean tryLock(final String path, final boolean sharedMode) throws IOException {
			if (Utils.checkEmptyOrNullString(path)) {
				throw new IllegalArgumentException("Path to lock can't be null or empty");
			}
			else if (lock != null) {
				throw new IllegalStateException("Attempt to lock already locked entity");
			}
			else {
				final ReentrantReadWriteLock	rwl;
				
				synchronized (locks) {
					if (!locks.containsKey(path)) {
						locks.put(path, new ReentrantReadWriteLock());
					}
					rwl = locks.get(path);
				}
				if (sharedMode) {
					if (rwl.readLock().tryLock()) {
						lock = rwl;
						return true;
					}
					else {
						lock = null;
						return false;
					}
				}
				else {
					if (rwl.writeLock().tryLock()) {
						lock = rwl;
						return true;
					}
					else {
						lock = null;
						return false;
					}
				}
			}
		}

		@Override
		public void lock(final String path, final boolean sharedMode) throws IOException {
			if (Utils.checkEmptyOrNullString(path)) {
				throw new IllegalArgumentException("Path to lock can't be null or empty");
			}
			else if (lock != null) {
				throw new IllegalStateException("Attempt to lock already locked entity");
			}
			else {
				final ReentrantReadWriteLock	rwl;
				
				synchronized (locks) {
					if (!locks.containsKey(path)) {
						locks.put(path, new ReentrantReadWriteLock());
					}
					rwl = locks.get(path);
				}
				if (sharedMode) {
					rwl.readLock().lock();
					lock = rwl;
				}
				else {
					rwl.writeLock().lock();
					lock = rwl;
				}
			}
		}

		@Override
		public void unlock(final String path, final boolean sharedMode) throws IOException {
			if (Utils.checkEmptyOrNullString(path)) {
				throw new IllegalArgumentException("Path to lock can't be null or empty");
			}
			else if (lock == null) {
				throw new IllegalStateException("Attempt to unlock lockless entity");
			}
			else {
				if (sharedMode) {
					lock.readLock().unlock();
				}
				else {
					lock.writeLock().unlock();
				}
				lock = null;
			}
		}

		@Override
		public URI[] list(final Pattern pattern) throws IOException {
			if (pattern == null) {
				throw new NullPointerException("Pattern to find can't be null");
			}
			else if (exists) {
				final ProjectNavigatorItem[]	children = getContainer().getProjectNavigator().getChildren(node.id);
				final URI[]						result = new URI[children.length];
				
				for(int index = 0; index < result.length; index++) {
					result[index] = URIUtils.appendRelativePath2URI(rootPath, children[index].name);
				}
				return result;
			}
			else {
				return EMPTY_LIST;
			}
		}

		@Override
		public void mkDir() throws IOException {
			if (exists) {
				throw new IOException("Path ["+uri+"] already exists");
			}
			else {
				ProjectNavigatorItem	currentItem = getContainer().getProjectNavigator().getRoot();
				final String[]			pathParts = (URIUtils.extractSubURI(rootPath,FileSystemInterface.FILESYSTEM_URI_SCHEME).getPath()+uri).split("/"); 
				
loop:			for(String item : pathParts) {
					if (!item.isEmpty()) {
						for(ProjectNavigatorItem child : getContainer().getProjectNavigator().getChildren(currentItem.id)) {
							if (child.name.equals(item)) {
								currentItem = child;
								continue loop;
							}
						}
						final ProjectNavigatorItem	pni = new ProjectNavigatorItem(getContainer().getProjectNavigator().getUniqueId(), currentItem.id, item, ItemType.Subtree, "new subtree", getContainer().createUniqueLocalizationString(), -1);
						
						getContainer().getProjectNavigator().addItem(pni);
						currentItem = pni;
					}
				}
				exists = true;
				node = currentItem;
			}
		}

		@Override
		public void create() throws IOException {
			if (exists) {
				throw new IOException("Path ["+uri+"] already exists");
			}
			else {
				ProjectNavigatorItem	currentItem = getContainer().getProjectNavigator().getRoot();
				final String[]			pathParts = (URIUtils.extractSubURI(rootPath,FileSystemInterface.FILESYSTEM_URI_SCHEME).getPath()+uri).split("/"); 
				
loop:			for(int index = 0; index < pathParts.length - 1; index++) {
					if (!pathParts[index].isEmpty()) {
						for(ProjectNavigatorItem child : getContainer().getProjectNavigator().getChildren(currentItem.id)) {
							if (child.name.equals(pathParts[index])) {
								currentItem = child;
								continue loop;
							}
						}
						final ProjectNavigatorItem	pni = new ProjectNavigatorItem(getContainer().getProjectNavigator().getUniqueId(), currentItem.id, pathParts[index], ItemType.Subtree, "new subtree", getContainer().createUniqueLocalizationString(), -1);
						
						getContainer().getProjectNavigator().addItem(pni);
						currentItem = pni;
					}
				}
				final long	unique = getContainer().uncheckedGetIdByPartName(pathParts[pathParts.length-1]);
				final ProjectNavigatorItem	pni = new ProjectNavigatorItem(unique, currentItem.id, pathParts[pathParts.length-1], ItemType.CreoleRef, "new Creole file", getContainer().createUniqueLocalizationString(), -1);
				
				getContainer().getProjectNavigator().addItem(pni);
				exists = true;
				node = pni;
			}
		}

		@Override
		public void setName(final String name) throws IOException {
			if (Utils.checkEmptyOrNullString(name)) {
				throw new IllegalArgumentException("Name to set can't be null or empty");
			}
			else if (!exists) {
				throw new IOException("Path ["+uri+"] is not exists");
			}
			else if (node.parent == -1) {
				throw new IOException("Root node can't be renamed"); 
			}
			else if (checkDuplicates(name)) {
				throw new IOException("Name ["+name+"] already exists in the ["+URIUtils.appendRelativePath2URI(rootPath, uri).resolve("../")+"]"); 
			}
			else {
				final ProjectNavigatorItem	pni = new ProjectNavigatorItem(node.id, node.parent, name, node.type, node.desc, node.titleId, node.subtreeRef);
				final Object	content = getContainer().getProjectPartContent(node.name); 
				
				getContainer().getProjectNavigator().removeItem(node.id);
				getContainer().removeProjectPartContent(node.name);
				getContainer().getProjectNavigator().addItem(pni);
				getContainer().addProjectPartContent(name, content);
			}
		}

		@Override
		public void delete() throws IOException {
			if (!exists) {
				throw new IOException("Path ["+uri+"] is not exists");
			}
			else if (getContainer().getProjectNavigator().getChildren(node.id).length > 0) {
				throw new IOException("Entity to remove ["+uri+"] contains children inside");
			}
			else {
				getContainer().removeProjectPartContent(node.name);
				getContainer().getProjectNavigator().removeItem(node.id);
				exists = false;
			}
		}

		@Override
		public OutputStream getOutputStream(final boolean append) throws IOException {
			if (!exists) {
				throw new IOException("Path ["+uri+"] is not exists"); 
			}
			else {
				switch (node.type) {
					case CreoleRef :
						return new DocumentOutputStream(getContainer(), node);
					case DocumentRef : case ImageRef :
						if (append) {
							throw new IOException("Path ["+uri+"] : append mode is not supprted for the given document type"); 
						}
						else {
							return new DocumentOutputStream(getContainer(), node);
						}
					case Root : case Subtree :
						throw new IOException("Path ["+uri+"] : attempt to write to directory or root"); 
					default:
						throw new UnsupportedOperationException("Node type ["+node.type+"] is not supported yet");
				}
			}
		}

		@Override
		public InputStream getInputStream() throws IOException {
			if (!exists) {
				throw new IOException("Path ["+uri+"] is not exists"); 
			}
			else {
				switch (node.type) {
					case CreoleRef :
						return new ByteArrayInputStream(((String)getContainer().getProjectPartContent(node.name)).getBytes(PureLibSettings.DEFAULT_CONTENT_ENCODING));
					case DocumentRef :
						return new ByteArrayInputStream((byte[])getContainer().getProjectPartContent(node.name));
					case ImageRef :
						try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
							ImageIO.write((RenderedImage)getContainer().getProjectPartContent(node.name),"png",baos);
							return new ByteArrayInputStream(baos.toByteArray());
						}
					case Root : case Subtree :
						throw new IOException("Path ["+uri+"] : attempt to read from directory or root"); 
					default:
						throw new UnsupportedOperationException("Node type ["+node.type+"] is not supported yet");
				}
			}
		}

		@Override
		public Map<String, Object> getAttributes() throws IOException {
			if (exists) {
				switch (node.type) {
					case CreoleRef		:
						return Utils.mkMap(DataWrapperInterface.ATTR_SIZE, getContentLength(node),
								DataWrapperInterface.ATTR_NAME, node.name, 
								DataWrapperInterface.ATTR_ALIAS, getContainer().getLocalizationString(node.titleId).getValue(), 
								DataWrapperInterface.ATTR_LASTMODIFIED, 0L, 
								DataWrapperInterface.ATTR_DIR, false, 
								DataWrapperInterface.ATTR_EXIST, true, 
								DataWrapperInterface.ATTR_CANREAD, true, 
								DataWrapperInterface.ATTR_CANWRITE, true, 
								NAVIGATION_NODE, node);						
					case DocumentRef	:
						return Utils.mkMap(DataWrapperInterface.ATTR_SIZE, getContentLength(node), 
								DataWrapperInterface.ATTR_NAME, node.name, 
								DataWrapperInterface.ATTR_ALIAS, getContainer().getLocalizationString(node.titleId).getValue(), 
								DataWrapperInterface.ATTR_LASTMODIFIED, 0L, 
								DataWrapperInterface.ATTR_DIR, false, 
								DataWrapperInterface.ATTR_EXIST, true, 
								DataWrapperInterface.ATTR_CANREAD, true, 
								DataWrapperInterface.ATTR_CANWRITE, true, 
								NAVIGATION_NODE, node);						
					case ImageRef		:
						return Utils.mkMap(DataWrapperInterface.ATTR_SIZE, 0L, 
								DataWrapperInterface.ATTR_NAME, node.name, 
								DataWrapperInterface.ATTR_ALIAS, getContainer().getLocalizationString(node.titleId).getValue(), 
								DataWrapperInterface.ATTR_LASTMODIFIED, 0L, 
								DataWrapperInterface.ATTR_DIR, false, 
								DataWrapperInterface.ATTR_EXIST, true, 
								DataWrapperInterface.ATTR_CANREAD, true, 
								DataWrapperInterface.ATTR_CANWRITE, true, 
								NAVIGATION_NODE, node);						
					case Root			:
						return Utils.mkMap(DataWrapperInterface.ATTR_SIZE, 0L, 
								DataWrapperInterface.ATTR_NAME, "/", 
								DataWrapperInterface.ATTR_ALIAS, "/", 
								DataWrapperInterface.ATTR_LASTMODIFIED, 0L, 
								DataWrapperInterface.ATTR_DIR, true, 
								DataWrapperInterface.ATTR_EXIST, true, 
								DataWrapperInterface.ATTR_CANREAD, true, 
								DataWrapperInterface.ATTR_CANWRITE, true, 
								NAVIGATION_NODE, node);						
					case Subtree		:
						return Utils.mkMap(DataWrapperInterface.ATTR_SIZE, 0L, 
								DataWrapperInterface.ATTR_NAME, node.name, 
								DataWrapperInterface.ATTR_ALIAS, getContainer().getLocalizationString(node.titleId).getValue(), 
								DataWrapperInterface.ATTR_LASTMODIFIED, 0L,
								DataWrapperInterface.ATTR_DIR, true, 
								DataWrapperInterface.ATTR_EXIST, true, 
								DataWrapperInterface.ATTR_CANREAD, true,
								DataWrapperInterface.ATTR_CANWRITE, true, 
								NAVIGATION_NODE, node);						
					default	:
						throw new UnsupportedOperationException("Node type ["+node.type+"] is not supported yet");
				}
			}
			else {
				return Utils.mkMap(DataWrapperInterface.ATTR_SIZE, 0L, 
								DataWrapperInterface.ATTR_NAME, lastName, 
								DataWrapperInterface.ATTR_ALIAS, getContainer().getLocalizationString(node.titleId).getValue(), 
								DataWrapperInterface.ATTR_LASTMODIFIED, 0L, 
								DataWrapperInterface.ATTR_DIR, false, 
								DataWrapperInterface.ATTR_EXIST, false, 
								DataWrapperInterface.ATTR_CANREAD, false, 
								DataWrapperInterface.ATTR_CANWRITE, false);						
			}
		}

		@Override
		public void linkAttributes(final Map<String, Object> attributes) throws IOException {
			if (attributes == null) {
				throw new NullPointerException("Attributes to link can't be null");
			}
			else if (attributes.containsKey(NAVIGATION_NODE)) {
				final ProjectNavigatorItem	newNode = (ProjectNavigatorItem)attributes.get(NAVIGATION_NODE);
				
				if (newNode.id != node.id || newNode.parent != node.parent || newNode.subtreeRef != node.subtreeRef) {
					throw new IOException("New attribute value for ["+NAVIGATION_NODE+"] conflicted with the current one. You can't change *.id, *.parent or *.subtreeRef values in it");
				}
				else {
					getContainer().getProjectNavigator().removeItem(node.id);
					getContainer().getProjectNavigator().addItem(newNode);
				}
			}
		}
		
		private boolean checkDuplicates(final String name) {
			for (ProjectNavigatorItem item : getContainer().getProjectNavigator().getChildren(node.parent)) {
				if (item.name.equals(name)) {
					return true; 
				}
			}
			return false;
		}
		
		private long getContentLength(final ProjectNavigatorItem item) throws UnsupportedEncodingException {
			if (!getContainer().hasProjectPart(item.name)) {
				return 0;
			}
			else {
				switch (item.type) {
					case CreoleRef		:
						return ((String)getContainer().getProjectPartContent(item.name)).getBytes(PureLibSettings.DEFAULT_CONTENT_ENCODING).length;
					case DocumentRef	:
						return ((byte[])getContainer().getProjectPartContent(node.name)).length;
					case ImageRef		:
						return 0;
					case Root : case Subtree :
						return 0;
					default	:
						throw new UnsupportedOperationException("Node type ["+node.type+"] is not supported yet");
				}
			}
			
		}
	}

	private static class DocumentOutputStream extends ByteArrayOutputStream {
		private final ProjectContainer		container;
		private final ProjectNavigatorItem	node;
		
		private DocumentOutputStream(final ProjectContainer container, final ProjectNavigatorItem node) {
			this.container = container;
			this.node = node;
		}
		
		@Override
		public void close() throws IOException {
			super.close();
			switch (node.type) {
				case CreoleRef		:
					if (container.hasProjectPart(node.name)) {
						container.setProjectPartContent(node.name, new String(toByteArray(), PureLibSettings.DEFAULT_CONTENT_ENCODING));
					}
					else {
						container.addProjectPartContent(node.name, new String(toByteArray(), PureLibSettings.DEFAULT_CONTENT_ENCODING));
					}
					break;
				case DocumentRef	:
					if (container.hasProjectPart(node.name)) {
						container.setProjectPartContent(node.name, toByteArray());
					}
					else {
						container.addProjectPartContent(node.name, toByteArray());
					}
					break;
				case ImageRef		:
					if (container.hasProjectPart(node.name)) {
						container.setProjectPartContent(node.name, ImageIO.read(new ByteArrayInputStream(toByteArray())));
					}
					else {
						container.addProjectPartContent(node.name, ImageIO.read(new ByteArrayInputStream(toByteArray())));
					}
					break;
				case Root : case Subtree :
					break;
				default:
					throw new UnsupportedOperationException("Node type ["+node.type+"] is not supported yet");
			}
		}
	}
}
