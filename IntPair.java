package paragon.minecraft.library;

/**
 * Simple primitive integer pair implementation.
 * 
 * @author Malcolm Riley
 */
public final class IntPair {

	/* Shared Public Fields */
	public static final IntPair ZERO = IntPair.of(0, 0);

	/* Public Fields */
	public final int LEFT;
	public final int RIGHT;

	private IntPair(int left, int right) {
		this.LEFT = left;
		this.RIGHT = right;
	}

	/* Public Methods */

	public static final IntPair of(int left, int right) {
		return (left == 0) && (right == 0) ? ZERO : new IntPair(left, right);
	}

	/* Supertype Override Methods */

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("(");
		builder.append(this.LEFT).append(",").append(this.RIGHT);
		builder.append(")");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + this.LEFT;
		result = (prime * result) + this.RIGHT;
		return result;
	}

	@Override
	public boolean equals(Object otherObject) {
		if (otherObject instanceof IntPair) {
			IntPair other = (IntPair) otherObject;
			return (this.LEFT == other.LEFT) && (this.RIGHT == other.RIGHT);
		}
		return false;
	}

}
