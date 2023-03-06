package chav1961.csce.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import chav1961.csce.project.ProjectContainer;
import chav1961.csce.utils.interfaces.SyntaxNodeType;
import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.SyntaxNode;
import chav1961.purelib.i18n.interfaces.SupportedLanguages;

public class ParserUtils {
	private static final SyntaxTreeInterface<LexType>	KEYWORDS = new AndOrTree<LexType>();
	
	static {
		KEYWORDS.placeName((CharSequence)"AND ", LexType.AND);
		KEYWORDS.placeName((CharSequence)"OR ", LexType.OR);
		KEYWORDS.placeName((CharSequence)"NOT ", LexType.NOT);
		KEYWORDS.placeName((CharSequence)"TO ", LexType.TO);
	}
	
	private static enum LexType {
		SINGLE_TERM,
		WILDCARD_TERM,
		PHRASE,
		FIELD,
		COLON,
		FUZZY,
		NUMBER,
		OPENB,
		CLOSEB,
		OPENF,
		CLOSEF,
		BOOST,
		OPEN,
		CLOSE,
		PLUS,
		MINUS,
		AND,
		OR,
		NOT,
		TO,
		EOF,
		ERROR;
	}
	
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

	// https://lucene.apache.org/core/2_9_4/queryparsersyntax.html
	public static SyntaxNode<SyntaxNodeType, SyntaxNode> parseQuery(final String query) throws SyntaxException {
		if (Utils.checkEmptyOrNullString(query)) {
			throw new IllegalArgumentException("Query string can't be null or empty");
		}
		else {
			final char[]		content = CharUtils.terminateAndConvert2CharArray(query, '\n');
			final int[]			forPosition = new int[2];
			final double[]		forValue = new double[1];
			final StringBuilder	sb = new StringBuilder();
			final List<Lexema>	lex = new ArrayList<>();
			boolean				parseAsNumber = false;
			int					from = 0;
			
loop:		for(;;) {
				from = CharUtils.skipBlank(content, from, true);
				switch (content[from]) {
					case '\n' :
						lex.add(new Lexema(from, LexType.EOF));
						break loop;
					case ':' :
						lex.add(new Lexema(from++, LexType.COLON));
						break;
					case '~' :
						lex.add(new Lexema(from++, LexType.FUZZY));
						parseAsNumber = true;
						break;
					case '^' :
						lex.add(new Lexema(from++, LexType.BOOST));
						parseAsNumber = true;
						break;
					case '(' :
						lex.add(new Lexema(from++, LexType.OPEN));
						break;
					case ')' :
						lex.add(new Lexema(from++, LexType.CLOSE));
						break;
					case '[' :
						lex.add(new Lexema(from++, LexType.OPENB));
						break;
					case ']' :
						lex.add(new Lexema(from++, LexType.CLOSEB));
						break;
					case '{' :
						lex.add(new Lexema(from++, LexType.OPENF));
						break;
					case '}' :
						lex.add(new Lexema(from++, LexType.CLOSEF));
						break;
					case '+' :
						lex.add(new Lexema(from++, LexType.PLUS));
						break;
					case '-' :
						lex.add(new Lexema(from++, LexType.MINUS));
						break;
					case '\"' :
						from = CharUtils.parseString(content, from+1, '\"', sb);
						if (content[from] == '\"') {
							from++;
						}
						break;
					case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
						if (parseAsNumber) {
							from = CharUtils.parseDouble(content, from, forValue, true);
							lex.add(new Lexema(from, LexType.NUMBER, forValue[0]));
						}
						else {
							from = parseAsTerm(content, from, forPosition);
							lex.add(new Lexema(from, containsWildcards(content, forPosition) ? LexType.WILDCARD_TERM : LexType.SINGLE_TERM, Arrays.copyOfRange(content, forPosition[0], forPosition[1])));
						}
						parseAsNumber = false;
						break;
					default :
						if (Character.isAlphabetic(content[from])) {
							from = parseAsTerm(content, from, forPosition);
							
							final long	id = KEYWORDS.seekName(content, forPosition[0], forPosition[0]); 
							
							if (id >= 0) {
								lex.add(new Lexema(from, KEYWORDS.getCargo(id)));
							}
							else {
								lex.add(new Lexema(from, containsWildcards(content, forPosition) ? LexType.WILDCARD_TERM : LexType.SINGLE_TERM, Arrays.copyOfRange(content, forPosition[0], forPosition[1])));
							}
						}
						else {
							lex.add(new Lexema(from++, LexType.ERROR));
						}
						break;
				}
			}

			return null;
		}
	}
	
	private static int parseAsTerm(final char[] content, final int from, final int[] forPosition) {
		forPosition[0] = from;
		
		for(int index = from; index <= content.length; index++) {
			if (!Character.isAlphabetic(content[index])) {
				forPosition[1] = index;
				return index;
			}
		}
		return forPosition[1] = content.length-1;
	}

	private static boolean containsWildcards(final char[] content, final int[] forPosition) {
		for(int index = forPosition[0]; index <= forPosition[1]; index++) {
			if (content[index] == '*' || content[index] == '?') {
				return true;
			}
		}
		return false;
	}
	
	private static <T> void parseProjectContent(final String part, final T content, final OutputStream os) throws IOException {
		// TODO Auto-generated method stub
	}
	
	
	private static class Lexema {
		private final int		col;
		private final LexType	type;
		private final double	value;
		private final char[]	content;
		
		public Lexema(final int col, final LexType type) {
			this.col = col;
			this.type = type;
			this.value = 0;
			this.content = null;
		}
		
		public Lexema(final int col, final LexType type, final double value) {
			this.col = col;
			this.type = type;
			this.value = value;
			this.content = null;
		}
		
		public Lexema(final int col, final LexType type, final char[] content) {
			this.col = col;
			this.type = type;
			this.value = 0;
			this.content = content;
		}
	}
}
