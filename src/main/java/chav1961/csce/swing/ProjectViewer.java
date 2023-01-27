package chav1961.csce.swing;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import chav1961.csce.Application;
import chav1961.csce.project.ProjectContainer;

public class ProjectViewer extends JSplitPane {
	private static final long serialVersionUID = 5507071556068623361L;

	private final Application		parent;
	private final ProjectContainer 	project;
	private final ProjectTabbedPane	tabs = new ProjectTabbedPane();
	private final ProjectTree		tree = new ProjectTree();
	private final ScreenLogger		screenLogger = new ScreenLogger();
	
	public ProjectViewer(final Application parent, final ProjectContainer project) {
		super(JSplitPane.HORIZONTAL_SPLIT);
		this.parent = parent;
		this.project = project;

		final JSplitPane	rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabs, new JScrollPane(screenLogger));

		rightSplit.setDividerLocation(600);
		
		setLeftComponent(new JScrollPane(tree));
		setRightComponent(rightSplit);
		
		setDividerLocation(300);
	}
	
	public void refreshProject() {
	}
}
