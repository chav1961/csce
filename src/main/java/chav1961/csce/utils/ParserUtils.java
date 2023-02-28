package chav1961.csce.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import chav1961.csce.project.ProjectContainer;
import chav1961.csce.utils.interfaces.SyntaxNodeType;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.cdb.SyntaxNode;
import chav1961.purelib.i18n.interfaces.SupportedLanguages;

public class ParserUtils {
	public static void parseProjectContent(final ProjectContainer container, final SupportedLanguages lang, final ZipOutputStream zos) throws IOException {
		if (container == null) {
			throw new NullPointerException("Project container can't be null");
		}
		else if (lang == null) {
			throw new NullPointerException("Supported language can't be null");
		}
		else if (zos == null) {
			throw new NullPointerException("ZIP output stream can't be null");
		}
		else {
			for (String item : container.getPartNames()) {
				if (item.endsWith(".cre")) {
					final ZipEntry	ze = new ZipEntry(item);
					
					ze.setMethod(ZipEntry.DEFLATED);
					zos.putNextEntry(ze);
					switch (lang) {
						case en		:
							parseProjectContent(item, container.getProjectPartContent(item), zos);
							break;
						case ru		:
							parseProjectContent(item, container.getProjectPartContent(item), zos);
							break;
						default	:
							throw new UnsupportedOperationException("Language ["+lang+"] is not supported yet");
					}
					zos.closeEntry();
				}
			}
		}
	}

	public static SyntaxNode<SyntaxNodeType, SyntaxNode> parseQuery(final String query) {
		if (Utils.checkEmptyOrNullString(query)) {
			throw new IllegalArgumentException("Query string can't be null or empty");
		}
		else {
			final char[]	content = CharUtils.terminateAndConvert2CharArray(query, '\n');
			int				from = 0;
			
			for(;;) {
				from = CharUtils.skipBlank(content, from, true);
				switch (content[from]) {
					case '\n' :
						break;
				}
			}
		}
	}
	
	private static <T> void parseProjectContent(final String part, final T content, final OutputStream os) throws IOException {
		// TODO Auto-generated method stub
	}
}
