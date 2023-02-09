package chav1961.csce.project;

@FunctionalInterface
public interface ProjectChangeListener {
	void processEvent(ProjectChangeEvent event);
}
