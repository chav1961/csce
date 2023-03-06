package chav1961.csce.utils.interfaces;

public enum SyntaxGroup {
	OR,
	AND, 
	NOT,
	WEIGHT,
	FIELD,
	TERM;
	
	public SyntaxGroup prev() {
		return values()[ordinal()-1];
	}

	public SyntaxGroup next() {
		return values()[ordinal()+1];
	}
}
