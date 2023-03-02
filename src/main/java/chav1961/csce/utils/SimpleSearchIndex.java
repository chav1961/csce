package chav1961.csce.utils;

import java.util.HashSet;
import java.util.Set;

import chav1961.csce.utils.SearchUtils.CreoleToken;
import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

public class SimpleSearchIndex<T> {
	public SyntaxTreeInterface<CreoleToken<T>>[]	indices;
	
	public SimpleSearchIndex(final int maxPrefixLen) {
		this.indices = new SyntaxTreeInterface[maxPrefixLen];
		
		for(int index = 0; index < indices.length; index++) {
			indices[index] = new AndOrTree<CreoleToken<T>>();
		}
	}
	
	public void add(final CreoleToken<T> token) {
		final char[]	content = token.token.toCharArray();
		
		for(int index = 0; index < indices.length; index++) {
			add(indices[index], content, index);
		}
	}

	public Iterable<CreoleToken<T>> seek(final String source) {
		final char[]				content = source.toCharArray();
		final Set<CreoleToken<T>>	result = new HashSet<>(); 
		
		seek(indices[0], content, 0, result);
		return result;
	}

	public Iterable<CreoleToken<T>> seekAnywhere(final String source) {
		if (source.indexOf('*') >=  0 || source.indexOf('?') >=  0) {
			final char[]				content = source.toCharArray();
			final Set<CreoleToken<T>>	result = new HashSet<>(); 
			
			for(int index = 0; index < indices.length; index++) {
				seek(indices[index], content, index, result);
			}
			return result;
		}
		else {
			final char[]				content = source.toCharArray();
			final Set<CreoleToken<T>>	result = new HashSet<>(); 
			
			for(int index = 0; index < indices.length; index++) {
				seek(indices[index], content, index, result);
			}
			return result;
		}
	}
	
	private void add(final SyntaxTreeInterface<CreoleToken<T>> tree, final char[] content, final int from) {
		tree.placeName(content, from, content.length, null);
	}
	
	private void seek(SyntaxTreeInterface<CreoleToken<T>> tree, final char[] content, int from, final Set<CreoleToken<T>> result) {
		final long	id = tree.seekName(content, from, content.length);
		
		if (id >= 0) {
			result.add(tree.getCargo(id));
		}
	}
}
