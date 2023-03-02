package chav1961.csce.utils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.SyntaxException;

public class SearchUtils {
	public static enum CreoleLinkType {
		CreoleLink,
		ImageLink,
		ExternalLink
	}
	
	public static String[] extractCreoleAnchors(final String source) {
		if (Utils.checkEmptyOrNullString(source)) {
			throw new IllegalArgumentException("Source string can't be null");
		}
		else {
			final List<String>	anchors = new ArrayList<>();
			
			for (String item : source.split("\n")) {
				final String	line = item.trim();
				
				if (line.startsWith("=")) {
					anchors.add(line);
				}
			}
			return anchors.toArray(new String[anchors.size()]);
		}
	}
	
	public static CreoleLink[] extractCreoleLinks(final String source) throws SyntaxException {
		if (Utils.checkEmptyOrNullString(source)) {
			throw new IllegalArgumentException("Source string can't be null");
		}
		else {
			final List<CreoleLink>	links = new ArrayList<>(); 
			int	row = 0, col, end;
			
			for (String line : source.split("\n")) {
				col = 0;
				while ((col = line.indexOf("[[",col)) >= 0) {
					end = line.indexOf("]]", col+1);
					
					if (end == -1) {
						throw new SyntaxException(row, col, "Unpaired [[|]] brackets");
					}
					else {
						final String[]	content = line.substring(col+2, end-2).split("\\|");
						
						links.add(new CreoleLink(row, col, CreoleLinkType.CreoleLink, content[0], content.length > 1 ? content[1] : ""));
						col = end + 1;
					}
				}
				col = 0;
				while ((col = line.indexOf("{{",col)) >= 0) {
					end = line.indexOf("}}", col+1);
					
					if (end == -1) {
						throw new SyntaxException(row, col, "Unpaired {{|}} brackets");
					}
					else {
						final String[]	content = line.substring(col+2, end-2).split("\\|");
						
						links.add(new CreoleLink(row, col, CreoleLinkType.ImageLink, content[0], content.length > 1 ? content[1] : ""));
						col = end + 1;
					}
				}
				col = 0;
				while ((col = line.indexOf("http://",col)) >= 0) {
					end = skipNonBlank(line,col);
					
					final String	content = line.substring(col, end);
					
					links.add(new CreoleLink(row, col, CreoleLinkType.ExternalLink, content, ""));
					col = end + 1;
				}
				col = 0;
				while ((col = line.indexOf("https://",col)) >= 0) {
					end = skipNonBlank(line,col);
					
					final String	content = line.substring(col, end);
					
					links.add(new CreoleLink(row, col, CreoleLinkType.ExternalLink, content, ""));
					col = end + 1;
				}
				row++;
			}
			return links.toArray(new CreoleLink[links.size()]);
		}
	}

	private static int skipNonBlank(final String line, final int col) {
		for(int index = col; index < line.length(); index++) {
			if (Character.isWhitespace(line.charAt(index))) {
				return index;
			}
		}
		return line.length()-1;
	}

	public static class CreoleLink {
		public final int			row;
		public final int			col;
		public final CreoleLinkType	type;
		public final String			ref;
		public final String			title;
		
		public CreoleLink(int row, int col, CreoleLinkType type, String ref, String title) {
			this.row = row;
			this.col = col;
			this.type = type;
			this.ref = ref;
			this.title = title;
		}

		public URI toURI(final SubstitutableProperties props) {
			if (props == null) {
				throw new NullPointerException("Props to substitute can't be null");
			}
			else {
				return URI.create(CharUtils.substitute("ref", ref, props));
			}
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + col;
			result = prime * result + ((ref == null) ? 0 : ref.hashCode());
			result = prime * result + row;
			result = prime * result + ((title == null) ? 0 : title.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			CreoleLink other = (CreoleLink) obj;
			if (col != other.col) return false;
			if (ref == null) {
				if (other.ref != null) return false;
			} else if (!ref.equals(other.ref)) return false;
			if (row != other.row) return false;
			if (title == null) {
				if (other.title != null) return false;
			} else if (!title.equals(other.title)) return false;
			if (type != other.type) return false;
			return true;
		}

		@Override
		public String toString() {
			return "CreoleLink [row=" + row + ", col=" + col + ", type=" + type + ", ref=" + ref + ", title=" + title + "]";
		}
	}
}
