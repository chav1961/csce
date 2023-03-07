package chav1961.csce.utils.interfaces;

public enum SyntaxGroup {
	TERM,
	UNARY,
	FIELD,
	WEIGHT,
	NOT,
	AND, 
	OR,
	ROOT;
	
	public SyntaxGroup prev() {
		return values()[ordinal()-1];
	}

	public SyntaxGroup next() {
		return values()[ordinal()+1];
	}
}
