package chav1961.csce.utils.interfaces;

public enum SyntaxNodeType {
	OR(SyntaxGroup.OR),
	AND(SyntaxGroup.AND), 
	NOT(SyntaxGroup.NOT),
	FIELD(SyntaxGroup.FIELD),
	EQUALS(SyntaxGroup.TERM),
	MATCH(SyntaxGroup.TERM),
	BETWEEN(SyntaxGroup.TERM),
	INSIDE(SyntaxGroup.TERM),
	PROXIMITY(SyntaxGroup.WEIGHT),
	BOOSTS(SyntaxGroup.WEIGHT);
	
	private final SyntaxGroup	group;
	
	private SyntaxNodeType(final SyntaxGroup group) {
		this.group = group;
	}
	
	public SyntaxGroup getSyntaxGroup() {
		return group;
	}
}
