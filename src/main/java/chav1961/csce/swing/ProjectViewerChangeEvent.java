package chav1961.csce.swing;

import java.awt.AWTEvent;

import chav1961.purelib.basic.Utils;

public class ProjectViewerChangeEvent  extends AWTEvent {
	private static final long serialVersionUID = 1871822799791039071L;

	public static enum ProjectChangeType {
		NAVIGATOR_ITEM_DESELECTED,
		NAVIGATOR_ITEM_SELECTED,
	}

	private final ProjectChangeType	type;
	private final Object[]			parameters;
	
	public ProjectViewerChangeEvent(final Object source, final ProjectChangeType changeType, final Object... parameters) {
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
