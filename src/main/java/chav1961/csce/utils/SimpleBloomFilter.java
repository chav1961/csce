package chav1961.csce.utils;

public class SimpleBloomFilter {
	private static final double	LN2 = Math.log(2);

	private final HashCalculator[]	functions;
	private final boolean[]			bits;
	
	@FunctionalInterface
	private interface HashCalculator {
		int calculate(CharSequence source);
	}
	
	public SimpleBloomFilter(final int maxNumberOfItems, final float failProbability) {
	    final double	size = -(maxNumberOfItems * Math.log(failProbability)) / (LN2 * LN2);
	    final double	count = (size / maxNumberOfItems) * LN2;

	    this.bits = new boolean[(int) Math.round(size)];
	    this.functions = new HashCalculator[(int) Math.round(count)];
	    for (int index = 0; index < this.functions.length; index++) {
	    	this.functions[index] = new HashCalculatorImpl();
	    }
	}

	public void add(final CharSequence seq) {
        for (HashCalculator item : functions) {
        	bits[item.calculate(seq)] = true;
        }
	}

	public boolean contains(final CharSequence seq) {
        for (HashCalculator item : functions) {
        	if (!bits[item.calculate(seq)]) {
        		return false;
        	}
        }
        return true;
	}
	
	private static class HashCalculatorImpl implements HashCalculator {
		private final int	seed = (int) (Math.floor(Math.random() * 32) + 32);

		@Override
		public int calculate(final CharSequence seq) {
			int	result = 1;

			for (int index = 0; index < seq.length(); index++) {
	            result = (seed * result + seq.charAt(index)) & 0xFFFFFFFF;
			}
			return result;
		}
	}
}
