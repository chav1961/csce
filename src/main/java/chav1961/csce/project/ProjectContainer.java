package chav1961.csce.project;

import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import chav1961.csce.Application;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.StringLoggerFacade;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.json.JsonNode;
import chav1961.purelib.json.JsonUtils;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;


/*
 * Project is a *.zip file with mandatory part '.project.properties'. It's preferred to be the same first part in the *.zip content.
 * Part '.project.properties' contains a set of keys:
 * - project.version: Descriptor version. Mandatory. Default is 1.0. now
 * - project.name: Project name. Mandatory. Will be appeared in the browser tab. Must be multi-language string
 * - project.icon - Project icon. List of images, splitted by \\n char. Must contain at least one element with size 16x16.
 * 		All the images names must be presented in the *.zip content. Element with size 16x16 will be appeared in the browser tab
 * - project.author: Copyright notice. Mandatory. Must be multi-language string
 * - project.descriptor: Project descriptor. Mandatory. Must be multi-language string
 * - project.licenses: Project licenses. List of license names, splitted by \\n char. Must contain at least one element. 
 * 		All the license names must be presented in the *.zip content
 * - project.tree: Navigator tree of the project. Mandatory. Must points to one of the *.zip content parts
 * - project.root: Root page of the project. Mandatory. Must be a some name inside the project.tree content part 
 * - project.lang: List of languages supported. Optional. Must be chav1961.purelib.i18n.interfaces.SupportedLanguages names was split by comma.
 * 		If missing, ru defaults
 * - project.localization: part name with localized strings. Mandatory. Must points to one of the *.zip content parts
 * - project.externals: External references of the project. Optional. Must points to one of the *.zip content parts
 * Content of *.zip also must contain:
 * - exactly one part with structure project description. Name of the part must be references by 'project.tree' key from '.project.properties' part. 
 * 		It's extension must be '.json'
 * - at least one license part with license content. Content of the part can be formatted by one of markup languges. Names(s) of the part(s) 
 * 		must be references by 'project.licenses' key from '.project.properties' part
 * Content of *.zip also can contain:
 * - a set of content pages part. Names of all the pages must have '.cre' extension and it's content must be Creole-based markup language.
 * - a set of image files part. Names of all the pages must have '.png' extension. The only supported format of the images must be *.png
 * - a set of additional files. It's name must have neither '.cre' nor '.png' extensions.
 * Structure project description has a json content. See...
 */

public class ProjectContainer {
	public static final String		PROJECT_VERSION = "project.version";
	public static final String		DEFAULT_PROJECT_VERSION = "1.0";
	public static final String		PROJECT_NAME = "project.name";
	public static final String		PROJECT_ICON = "project.icon";
	public static final String		PROJECT_AUTHOR = "project.author";
	public static final String		PROJECT_DESCRIPTOR = "project.descriptor";
	public static final String		PROJECT_LICENSES = "project.licenses";
	public static final String		PROJECT_TREE = "project.tree";
	public static final String		PROJECT_ROOT = "project.root"; 
	public static final String		PROJECT_LANG = "project.lang";
	public static final String		PROJECT_LOCALIZATION = "project.localization";
	public static final String		PROJECT_EXTERNALS = "project.externals";
	
	private static final String		PART_DESCRIPTION = ".project.properties";	
	private static final String		CREOLE_EXT = ".cre";	
	private static final String		IMAGE_EXT = ".png";	
	private static final String		JSON_EXT = ".json";
	private static final String[]	PARTS = {
											"project.tree.json",
											"localization.xml",
											"project.tree.json",
											"project.default.license.cre"
										};
	
	private final Application				app;
	private final SubstitutableProperties	props = new SubstitutableProperties();
	private final Map<String, Object>		content = new HashMap<>();
	private String							projectFileName = null;
	
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
	
	public boolean validateProject(final LoggerFacade logger) {
		return validateProject(logger, props, content);
	}

	public InputStream toIntputStream() {
		try(final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final ZipOutputStream		zos = new ZipOutputStream(baos)) {
			
			ZipEntry	ze = new ZipEntry(PART_DESCRIPTION);

			ze.setMethod(ZipEntry.DEFLATED);
			zos.putNextEntry(ze);
			props.store(zos, "");
			for (Entry<String, Object> item : content.entrySet()) {
				ze = new ZipEntry(item.getKey());
				ze.setMethod(ZipEntry.DEFLATED);
				zos.putNextEntry(ze);
				storePart(item.getKey(), item.getValue(), zos);
				zos.flush();				
			}
			zos.finish();
			
			return new ByteArrayInputStream(baos.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public OutputStream fromOutputStream() {
		return new ByteArrayOutputStream() {
			public void close() throws java.io.IOException {
				super.close();
				final byte[]	content = toByteArray();
				
				if (content.length == 0) {
					createNewProject();
				}
				else {
					loadProject(content);
				}
			};
		};
	}
	
	private void createNewProject() throws IOException {
		final SubstitutableProperties	projectProps = new SubstitutableProperties();
		final Map<String, Object>		projectParts = new HashMap<>();
		
		try(final InputStream	is = getClass().getResourceAsStream("project.template.properties")) {
			projectProps.load(is);
		}
		for (String item : PARTS) {
			try(final InputStream	is = getClass().getResourceAsStream(item)) {
				loadPart(item, is, projectParts);
			}
		}
		try(final LoggerFacade	logger = new StringLoggerFacade()) {
			if (validateProject(logger, projectProps, projectParts)) {
				props.clear();
				props.putAll(projectProps);
				content.clear();
				content.putAll(projectParts);
			}
			else {
				throw new IOException("Project validation failed : "+logger.toString()); 
			}
		}
	}
	
	private boolean validateProject(final LoggerFacade logger, final SubstitutableProperties props, final Map<String, Object> parts) {
		return true;
	}

	private void loadPart(final String name, final InputStream is, final Map<String, Object> target) throws IOException {
		if (name.endsWith(CREOLE_EXT)) {
			final Reader	rdr = new InputStreamReader(is, PureLibSettings.DEFAULT_CONTENT_ENCODING);
			final Writer	wr = new StringWriter();
			
			Utils.copyStream(rdr, wr);
			target.put(name, wr.toString());
		}
		else if (name.endsWith(IMAGE_EXT)) {
			target.put(name, ImageIO.read(is));
		}
		else if (name.endsWith(JSON_EXT)) {
			final Reader			rdr = new InputStreamReader(is, PureLibSettings.DEFAULT_CONTENT_ENCODING);
			final JsonStaxParser	parser = new JsonStaxParser(rdr);
			
			try{parser.next();
				target.put(name, JsonUtils.loadJsonTree(parser));
			} catch (SyntaxException e) {
				throw new IOException(e);
			}
		}
		else {
			final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
			
			Utils.copyStream(is, baos);
			target.put(name, baos.toByteArray());
		}
	}

	private void storePart(final String name, final Object content, final OutputStream os) throws IOException {
		if (name.endsWith(CREOLE_EXT)) {
			final Writer	wr = new OutputStreamWriter(os, PureLibSettings.DEFAULT_CONTENT_ENCODING);
			
			wr.write(content.toString());
			wr.flush();
		}
		else if (name.endsWith(IMAGE_EXT)) {
			ImageIO.write((RenderedImage)content, "png", os);
		}
		else if (name.endsWith(JSON_EXT)) {
			final Writer			wr = new OutputStreamWriter(os, PureLibSettings.DEFAULT_CONTENT_ENCODING);
			final JsonStaxPrinter	prn = new JsonStaxPrinter(wr);
			
			try{
				JsonUtils.unloadJsonTree((JsonNode)content, prn);
				prn.flush();
			} catch (PrintingException e) {
				throw new IOException(e);
			}
		}
		else {
			os.write((byte[])content);
			os.flush();
		}
	}
	
	private void loadProject(final byte[] projectContent) throws IOException {
		final SubstitutableProperties	projectProps = new SubstitutableProperties();
		final Map<String, Object>		projectParts = new HashMap<>();
		
		try(final InputStream		is = new ByteArrayInputStream(projectContent);
			final ZipInputStream	zis = new ZipInputStream(is)) {
			boolean		propsDetected = false;
			
			ZipEntry	ze;
			
			while ((ze = zis.getNextEntry()) != null) {
				if (PART_DESCRIPTION.equals(ze.getName())) {
					projectProps.load(zis);
					propsDetected = true;
				}
				else {
					loadPart(ze.getName(), zis, projectParts);
				}
			}
			if (!propsDetected) {
				throw new IOException("Project structure corrupted: mandatory part ["+PART_DESCRIPTION+"] is missing"); 
			}
			else if (!DEFAULT_PROJECT_VERSION.equals(projectProps.getProperty(PROJECT_VERSION))) {
				throw new IOException("Project loading failed : unsupported project version ["+projectProps.getProperty(PROJECT_VERSION)+"]"); 
			}
			else {
				props.clear();
				props.putAll(projectProps);
				content.clear();
				content.putAll(projectParts);
			}
		}
	}
}
