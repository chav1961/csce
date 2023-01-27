package chav1961.csce.swing;

import javax.swing.JSplitPane;

import chav1961.csce.Application;
import chav1961.csce.project.ProjectContainer;

public class ProjectViewer extends JSplitPane {
	private static final long serialVersionUID = 5507071556068623361L;

	private final Application		parent;
	private final ProjectContainer 	project;
	
	public ProjectViewer(final Application parent, final ProjectContainer project) {
		this.parent = parent;
		this.project = project;
		
		setDividerLocation(300);
	}
	
	public void refreshProject() {
	}
}
