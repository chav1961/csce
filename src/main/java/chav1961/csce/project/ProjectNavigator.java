package chav1961.csce.project;

import java.util.Comparator;

import chav1961.purelib.json.JsonNode;
import chav1961.purelib.json.interfaces.JsonSerializable;

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
 * -- reference : string - reference to content (part name inside th project) or subtree ('#long' reference to subtree item id)
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
	
	private final String					version;
	private final ProjectNavigatorItem[]	items;
	
	public ProjectNavigator(final ProjectContainer container, final JsonNode root, final String rootName) {
		
	}
	
	public ProjectNavigatorItem getRoot() {
		return null;
	}
	
	public ProjectNavigatorItem getItem(final long id) {
		return null;
	}

	public ProjectNavigatorItem[] getChildren(final long id, final Comparator<ProjectNavigatorItem> c) {
		return null;
	}
	
	public JsonNode buildJsonNode() {
		return null;
	}
	
	public static class ProjectNavigatorItem {
		public static enum ItemType {
			CreoleRef,
			Subtree,
		}
		
		final long		id;
		final long		parent;
		final String	name;
		final ItemType	type;
		final String	desc;
		final String	titleId;
		final long		subtreeRef;
		final String	partRef;

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
			this.partRef = partRef;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((desc == null) ? 0 : desc.hashCode());
			result = prime * result + (int) (id ^ (id >>> 32));
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + (int) (parent ^ (parent >>> 32));
			result = prime * result + ((partRef == null) ? 0 : partRef.hashCode());
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
			if (partRef == null) {
				if (other.partRef != null) return false;
			} else if (!partRef.equals(other.partRef)) return false;
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
					+ ", desc=" + desc + ", titleId=" + titleId + ", subtreeRef=" + subtreeRef + ", partRef=" + partRef
					+ "]";
		}
	}
}
