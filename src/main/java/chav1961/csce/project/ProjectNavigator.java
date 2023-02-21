package chav1961.csce.project;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import chav1961.csce.project.ProjectChangeEvent.ProjectChangeType;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.json.JsonNode;
import chav1961.purelib.json.JsonUtils;
import chav1961.purelib.json.interfaces.JsonNodeType;
import chav1961.purelib.json.interfaces.JsonTreeWalkerCallback;
import chav1961.purelib.model.ContentMetadataFilter;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.MutableContentNodeMetadata;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;

/*
 * Project description is a *.json containing:
 * - version : string - json version (currently 1.00)
 * - items : []  
 * -- id : long - item id
 * -- parent : long - parent item id. Root parent is always -1
 * -- name : string - item name
 * -- type : string - item type (enum)
 * -- descriptor : string - item descriptor
 * -- titleId : string - title id
 * -- reference : string - reference to content (part name inside the project) or long - reference to subtree item id
 */
public class ProjectNavigator {
	public static final String	F_VERSION = "version";
	public static final String	F_VERSION_DEFAULT = "1.00";
	public static final String	F_ITEMS = "items";
	public static final String	F_ID = "id";
	public static final String	F_PARENT = "parent";
	public static final String	F_NAME = "name";
	public static final String	F_TYPE = "type";
	public static final String	F_DESCRIPTOR = "descriptor";
	public static final String	F_TITLE_ID = "titleId";
	public static final String	F_REFERENCE = "reference";

	@FunctionalInterface
	public interface WalkDownCallback {
		ContinueMode process(final NodeEnterMode mode, ProjectNavigatorItem node) throws ContentException, IOException;
	}
	
	public static enum ItemType {
		Root("favicon16x16.png", false, false),
		CreoleRef("favicon16x16.png", true, true),
		DocumentRef("favicon16x16.png", true, false),
		ImageRef("favicon16x16.png", true, false),
		Subtree("favicon16x16.png", false, true);
		
		private final URI		icon;
		private final boolean	isLeaf;
		private final boolean	isEditingSupported;
		
		private ItemType(final String icon, final boolean isLeaf, final boolean isEditingSupported) {
			try {
				this.icon = getClass().getResource(icon).toURI();
				this.isLeaf = isLeaf;
				this.isEditingSupported = isEditingSupported;
			} catch (URISyntaxException e) {
				throw new PreparationException(e);
			}
		}
		
		private URI getIconURI() {
			return icon;
		}
		
		public boolean isLeafItem() {
			return isLeaf;
		}
		
		public boolean isEditingSipported() {
			return isEditingSupported;
		}
	}
	
	private static final ProjectNavigatorItem[]	EMPTY_ARRAY = new ProjectNavigatorItem[0];
	
	private final ProjectContainer			container;
	private final String					version;
	private final String					rootName;
	private ProjectNavigatorItem[]			items;
	private long							uniqueId;
	
	public ProjectNavigator(final ProjectContainer container, final JsonNode root, final String rootName) throws ContentException {
		if (container == null) {
			throw new NullPointerException("Container can't be null"); 
		}
		else if (root == null) {
			throw new NullPointerException("Json root can't be null"); 
		}
		else if (Utils.checkEmptyOrNullString(rootName)) {
			throw new IllegalArgumentException("Root name can't be null or empty"); 
		}
		else if (!root.hasName(F_VERSION)) {
			throw new IllegalArgumentException("Json root mandatory field ["+F_VERSION+"] is missing"); 
		}
		else if (!F_VERSION_DEFAULT.equals(root.getChild(F_VERSION).getStringValue())) {
			throw new IllegalArgumentException("Json root unsupported version ["+root.getChild(F_VERSION).getStringValue()+"]"); 
		}
		else {
			final List<ProjectNavigatorItem>	temp = new ArrayList<>();
			final JsonTreeWalkerCallback		itemFilter = JsonUtils.filterOf("/items/[]", (mode, node, parm)->appendItem(temp,mode,node));
	
			JsonUtils.walkDownJson(root, itemFilter);
			this.container = container;
			this.version = root.getChild(F_VERSION).getStringValue();
			this.rootName = rootName;
			this.items = temp.toArray(new ProjectNavigatorItem[temp.size()]);
			
			Arrays.sort(items, (o1,o2)->(int)(o1.id - o2.id));
			int	found = -1;
			
			for(int index = 0; index < items.length; index++) {
				if (items[index].name.equals(rootName)) {
					found = index;
					break;
				}
			}
			if (found == -1) {
				throw new IllegalArgumentException("Root name ["+rootName+"] is missing in the items list");
			}
			
			uniqueId = items[0].id;
			for(int index = 1; index < items.length; index++) {
				uniqueId = Math.max(uniqueId, items[index].id); 
			}
		}
	}
	
	public ProjectNavigatorItem getRoot() {
		for (ProjectNavigatorItem item : items) {
			if (item.name.equals(rootName)) {
				return item;
			}
		}
		throw new IllegalStateException("Root name is missing in the items");
	}
	
	public long getUniqueId() {
		return ++uniqueId;
	}
	
	public int getItemCount() {
		return items.length;
	}

	public boolean hasItemId(final long id) {
		return id2index(id) >= 0;
	}
	
	public ProjectNavigatorItem getItemByIndex(final int index) {
		if (index < 0 || index >= items.length) {
			throw new IllegalArgumentException("Index [] out of range 0.."+(items.length - 1));
		}
		else {
			return items[index];
		}
	}
	
	public ProjectNavigatorItem getItem(final long id) {
		final int 	index = id2index(id);
		
		if (index == -1) {
			throw new IllegalArgumentException("Item id ["+id+"] is missing in the list"); 
		}
		else {
			return getItemByIndex(index);
		}
	}

	public ProjectNavigatorItem[] getChildren(final long id) {
		return getChildren(id,(o1,o2)->o1.name.compareTo(o2.name));
	}	
	
	public ProjectNavigatorItem[] getChildren(final long id, final Comparator<ProjectNavigatorItem> c) {
		if (c == null) {
			throw new NullPointerException("Comparator can't be null");
		}
		else {
			int count = 0;
			
			for (ProjectNavigatorItem item : items) {
				if (item.parent == id) {
					count++;
				}
			}
			if (count == 0) {
				return EMPTY_ARRAY;
			}
			else {
				final ProjectNavigatorItem[]	result = new ProjectNavigatorItem[count];
				
				count = 0;
				for (ProjectNavigatorItem item : items) {
					if (item.parent == id) {
						result[count++] = item;
					}
				}
				Arrays.sort(result, c);
				return result;
			}
		}
	}
	
	public void addItem(final ProjectNavigatorItem item) {
		if (item == null) {
			throw new NullPointerException("Item to add can't be null"); 
		}
		else {
			final ProjectChangeEvent	pce;
			
			items = Arrays.copyOf(items, items.length + 1);
			items[items.length - 1] = item;
			Arrays.sort(items, (o1,o2)->(int)(o1.id - o2.id));
			switch (item.type) {
				case CreoleRef		:
					pce = new ProjectChangeEvent(container, ProjectChangeType.ITEM_INSERTED, item.parent, item.id);
					break;
				case DocumentRef	:
					pce = new ProjectChangeEvent(container, ProjectChangeType.ITEM_INSERTED, item.parent, item.id);
					break;
				case ImageRef		:
					pce = new ProjectChangeEvent(container, ProjectChangeType.ITEM_INSERTED, item.parent, item.id);
					break;
				case Subtree		:
					pce = new ProjectChangeEvent(container, ProjectChangeType.PART_INSERTED, item.parent, item.id);
					break;
				case Root :
				default :
					throw new UnsupportedOperationException("Navigator item type ["+item.type+"] is not supported yet");
			}
			container.fireProjectChangeEvent(pce);
		}
	}

	public ProjectNavigatorItem setItem(final long id, final ProjectNavigatorItem item) {
		if (item == null) {
			throw new NullPointerException("Item to set can't be null"); 
		}
		else {
			final int 	index = id2index(id);
			
			if (index == -1) {
				throw new IllegalArgumentException("Item id ["+id+"] is missing in the list"); 
			}
			else {
				return setItemByIndex(index, item);
			}
		}
	}

	public ProjectNavigatorItem setItemByIndex(final int index, final ProjectNavigatorItem item) {
		if (index < 0 || index >= items.length) {
			throw new IllegalArgumentException("Index [] out of range 0.."+(items.length - 1));
		}
		else {
			final ProjectNavigatorItem	result = items[index];
			final ProjectChangeEvent	pce;
			
			items[index] = item;
			if (item.id != result.id) {
				Arrays.sort(items, (o1,o2)->(int)(o1.id - o2.id));
			}
			switch (result.type) {
				case CreoleRef		:
					pce = new ProjectChangeEvent(container, ProjectChangeType.ITEM_CHANGED, result.parent, result.id);
					break;
				case DocumentRef	:
					pce = new ProjectChangeEvent(container, ProjectChangeType.ITEM_CHANGED, result.parent, result.id);
					break;
				case ImageRef		:
					pce = new ProjectChangeEvent(container, ProjectChangeType.ITEM_CHANGED, result.parent, result.id);
					break;
				case Subtree		:
					pce = new ProjectChangeEvent(container, ProjectChangeType.PART_CHANGED, result.parent, result.id);
					break;
				case Root	:
				default :
					throw new UnsupportedOperationException("Navigator item type ["+result.type+"] is not supported yet");
			}
			container.fireProjectChangeEvent(pce);
			return result;
		}
	}
	
	public ProjectNavigatorItem removeItem(final long id) {
		final int 	index = id2index(id);
		
		if (index == -1) {
			throw new IllegalArgumentException("Item id ["+id+"] is missing in the list"); 
		}
		else {
			return removeItemByIndex(index);
		}
	}

	public ProjectNavigatorItem removeItemByIndex(final int index) {
		if (index < 0 || index >= items.length) {
			throw new IllegalArgumentException("Index [] out of range 0.."+(items.length - 1));
		}
		else {
			final ProjectNavigatorItem	result = items[index];
			final ProjectChangeEvent	pce;

			System.arraycopy(items, index+1, items, index, items.length-index-1);
			items = Arrays.copyOf(items, items.length - 1); 
			Arrays.sort(items, (o1,o2)->(int)(o1.id - o2.id));
			switch (result.type) {
				case ImageRef		:
					pce = new ProjectChangeEvent(container, ProjectChangeType.ITEM_REMOVED, result.parent, result.id);
					break;
				case DocumentRef	:
					pce = new ProjectChangeEvent(container, ProjectChangeType.ITEM_REMOVED, result.parent, result.id);
					break;
				case CreoleRef		:
					pce = new ProjectChangeEvent(container, ProjectChangeType.ITEM_REMOVED, result.parent, result.id);
					break;
				case Subtree		:
					pce = new ProjectChangeEvent(container, ProjectChangeType.PART_REMOVED, result.parent, result.id);
					break;
				case Root :
				default :
					throw new UnsupportedOperationException("Navigator item type ["+result.type+"] is not supported yet");
			}
			container.fireProjectChangeEvent(pce);
			return result;
		}
	}

	public ContinueMode walkDown(final WalkDownCallback callback) throws ContentException, IOException {
		if (callback == null) {
			throw new NullPointerException("Callback can't be null"); 
		}
		else {
			return walkDown(getItem(0), callback);
		}
	}
	
	public JsonNode buildJsonNode() {
		final JsonNode	list = new JsonNode(JsonNodeType.JsonArray).setName(F_ITEMS);
		final JsonNode	result = new JsonNode(JsonNodeType.JsonObject, new JsonNode(version).setName(F_VERSION), list);
		
		for (ProjectNavigatorItem item : items) {
			final JsonNode	toAdd = new JsonNode(JsonNodeType.JsonObject
										, new JsonNode(item.id).setName(F_ID)
										, new JsonNode(item.parent).setName(F_PARENT)
										, new JsonNode(item.name).setName(F_NAME)
										, new JsonNode(item.type.name()).setName(F_TYPE)
										, new JsonNode(item.desc).setName(F_DESCRIPTOR)
										, new JsonNode(item.titleId).setName(F_TITLE_ID)
										, new JsonNode(item.subtreeRef).setName(F_REFERENCE)
									);
			list.addChild(toAdd);
		}
		
		return result;
	}

	private int id2index(final long id) {
        int low = 0, high = items.length - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            long midVal = items[mid].id;

            if (midVal < id) {
                low = mid + 1;
            }
            else if (midVal > id) {
                high = mid - 1;
            }
            else {
                return mid;
            }
        }
        return -1;
	}
	
	private ContinueMode appendItem(final List<ProjectNavigatorItem> list, final NodeEnterMode mode, final JsonNode node) {
		if (mode == NodeEnterMode.ENTER && node.getType() == JsonNodeType.JsonObject) {
			final long		id = node.getChild(F_ID).getLongValue();
			final long		parent = node.getChild(F_PARENT).getLongValue();
			final String	name = node.getChild(F_NAME).getStringValue();
			final ItemType	type = ItemType.valueOf(node.getChild(F_TYPE).getStringValue());
			final String	descriptor = node.getChild(F_DESCRIPTOR).getStringValue();
			final String	titleId = node.getChild(F_TITLE_ID).getStringValue();
			
			switch (type) {
				case Root			:
					list.add(new ProjectNavigatorItem(id, parent, name, type, descriptor, titleId, "???"));
					break;
				case CreoleRef		:
					list.add(new ProjectNavigatorItem(id, parent, name, type, descriptor, titleId, node.getChild(F_REFERENCE).getLongValue()));
					break;
				case DocumentRef	:
					list.add(new ProjectNavigatorItem(id, parent, name, type, descriptor, titleId, node.getChild(F_REFERENCE).getLongValue()));
					break;
				case ImageRef		:
					list.add(new ProjectNavigatorItem(id, parent, name, type, descriptor, titleId, node.getChild(F_REFERENCE).getLongValue()));
					break;
				case Subtree		:
					if (node.getChild(F_REFERENCE).getType() == JsonNodeType.JsonInteger) {
						list.add(new ProjectNavigatorItem(id, parent, name, type, descriptor, titleId, node.getChild(F_REFERENCE).getLongValue()));
					}
					else {
						list.add(new ProjectNavigatorItem(id, parent, name, type, descriptor, titleId, -1));
					}
					break;
				default	:
					throw new UnsupportedOperationException("Node type ["+type+"] is not supported yet");
			}
		}
		return ContinueMode.CONTINUE;
	}

	private ContinueMode walkDown(final ProjectNavigatorItem node, final WalkDownCallback callback) throws ContentException, IOException {
		ContinueMode	rc;
		
		switch (rc = callback.process(NodeEnterMode.ENTER, node)) {
			case CONTINUE		:
loop:			for (ProjectNavigatorItem item : getChildren(node.id)) {
					switch (rc = walkDown(item, callback)) {
						case CONTINUE : case SIBLINGS_ONLY :
							break;
						case PARENT_ONLY : case SKIP_CHILDREN : case SKIP_PARENT : case SKIP_SIBLINGS :
							break loop;
						case STOP:
							callback.process(NodeEnterMode.EXIT, node);
							return ContinueMode.STOP;
						default: throw new UnsupportedOperationException("Continue node type ["+rc+"] is not supported yet"); 
					}
				}	
				// break not needed!!!
			case PARENT_ONLY : case SIBLINGS_ONLY : case SKIP_CHILDREN : case SKIP_PARENT :
				return callback.process(NodeEnterMode.EXIT, node);
			case SKIP_SIBLINGS	:
				return callback.process(NodeEnterMode.EXIT, node) == ContinueMode.STOP ? ContinueMode.STOP : ContinueMode.SKIP_CHILDREN;
			case STOP			:
				callback.process(NodeEnterMode.EXIT, node);
				return ContinueMode.STOP;
			default: throw new UnsupportedOperationException("Continue node type ["+rc+"] is not supported yet"); 
		}
	}
	
	public static class ProjectNavigatorItem implements NodeMetadataOwner, Cloneable {
		public final long		id;
		public final long		parent;
		public final String		name;
		public final ItemType	type;
		public final String		desc;
		public final String		titleId;
		public final long		subtreeRef;
		private final ContentNodeMetadata	meta;

		public ProjectNavigatorItem(long id, long parent, String name, ItemType type, String desc, String titleId, long subtreeRef) {
			this(id, parent, name, type, desc, titleId, subtreeRef, "");
		}		

		public ProjectNavigatorItem(long id, long parent, String name, ItemType type, String desc, String titleId, String partRef) {
			this(id, parent, name, type, desc, titleId, -1, partRef);
		}
		
		public ProjectNavigatorItem(long id, long parent, String name, ItemType type, String desc, String titleId, long subtreeRef, String partRef) {
			this.id = id;
			this.parent = parent;
			this.name = name;
			this.type = type;
			this.desc = desc;
			this.titleId = titleId;
			this.subtreeRef = subtreeRef;
			
			this.meta = new MutableContentNodeMetadata(name, getClass(), "./"+name, URI.create("i18n:xml:root://chav1961.csce.Application/chav1961/csce/localization.xml")
							, titleId, null, null, new FieldFormat(getClass()), URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":item:/"+name), type.getIconURI());
		}

		private ProjectNavigatorItem(final ProjectNavigatorItem another) {
			this(another.id, another.parent, another.name, another.type, another.desc, another.titleId, another.subtreeRef);
		}
		
		@Override
		public ContentNodeMetadata getNodeMetadata() {
			return meta;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((desc == null) ? 0 : desc.hashCode());
			result = prime * result + (int) (id ^ (id >>> 32));
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + (int) (parent ^ (parent >>> 32));
			result = prime * result + (int) (subtreeRef ^ (subtreeRef >>> 32));
			result = prime * result + ((titleId == null) ? 0 : titleId.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			ProjectNavigatorItem other = (ProjectNavigatorItem) obj;
			if (desc == null) {
				if (other.desc != null) return false;
			} else if (!desc.equals(other.desc)) return false;
			if (id != other.id) return false;
			if (name == null) {
				if (other.name != null) return false;
			} else if (!name.equals(other.name)) return false;
			if (parent != other.parent) return false;
			if (subtreeRef != other.subtreeRef) return false;
			if (titleId == null) {
				if (other.titleId != null) return false;
			} else if (!titleId.equals(other.titleId)) return false;
			if (type != other.type) return false;
			return true;
		}

		@Override
		public String toString() {
			return "ProjectNavigatorItem [id=" + id + ", parent=" + parent + ", name=" + name + ", type=" + type
					+ ", desc=" + desc + ", titleId=" + titleId + ", subtreeRef=" + subtreeRef + "]";
		}

		@Override
		public Object clone() throws CloneNotSupportedException {
			return new ProjectNavigatorItem(this);
		}
	}
}
