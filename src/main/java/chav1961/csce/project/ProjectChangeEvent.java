package chav1961.csce.project;

import java.awt.AWTEvent;

import chav1961.purelib.basic.Utils;

public class ProjectChangeEvent extends AWTEvent {
	private static final long serialVersionUID = 1871822799791039071L;

	public static enum ProjectChangeType {
		PROJECT_FILENAME_CHANGED,
		PART_INSERTED,
		PART_REMOVED,
		ITEM_INSERTED,
		ITEM_REMOVED,
		PART_CHANGED,
		ITEM_CHANGED
	}
	
	private final ProjectChangeType	type;
	private final Object[]			parameters;
	
	public ProjectChangeEvent(final Object source, final ProjectChangeType changeType, final Object... parameters) {
		super(source, changeType.ordinal());
		if (parameters == null || Utils.checkArrayContent4Nulls(parameters) >= 0) {
			throw new IllegalArgumentException("Parameters list is null or contains nulls inside"); 
		}
		else {
			this.type = changeType;
			this.parameters = parameters.clone();
		}
	}
	
	public ProjectChangeType getChangeType() {
		return type;
	}
	
	public Object[] getParameters() {
		return parameters;
	}
}
