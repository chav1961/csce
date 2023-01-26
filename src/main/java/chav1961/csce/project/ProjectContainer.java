package chav1961.csce.project;

import java.io.InputStream;
import java.io.OutputStream;

import chav1961.csce.Application;

public class ProjectContainer {

	private final Application	app;
	
	public ProjectContainer(final Application app) {
		if (app == null) {
			throw new NullPointerException("Application can't be null");
		}
		else {
			this.app = app;
		}
	}
	
	public InputStream toIntputStream() {
		return null;
	}

	public OutputStream fromOutputStream() {
		return null;
	}
}
