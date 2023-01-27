package chav1961.csce.project;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import chav1961.csce.Application;

public class ProjectContainer {

	private final Application	app;
	private String				projectFileName = null;
	
	public ProjectContainer(final Application app) {
		if (app == null) {
			throw new NullPointerException("Application can't be null");
		}
		else {
			this.app = app;
		}
	}
	
	public String getProjectFileName() {
		return projectFileName;
	}
	
	public void setProjectFileName(final String name) {
		projectFileName = name;
	}
	
	public InputStream toIntputStream() {
		System.err.println("Save project");
		return new ByteArrayInputStream(new byte[0]);
	}

	public OutputStream fromOutputStream() {
		return new ByteArrayOutputStream() {
			public void close() throws java.io.IOException {
				super.close();
				System.err.println("Load project: "+toByteArray().length);
			};
		};
	}
}
