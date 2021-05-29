package paragon.minecraft.library;

/**
 * Interface representing an object that transforms a {@code double} primitive into an {@code int} primitive.
 * <p>
 * The interface is intended to be used with the companion {@code enum}, {@link RoundingMethod}.
 * 
 * @author Malcolm Riley
 *
 */
public interface IRoundingMethod {
	
	/**
	 * Perform the rounding operation on the provided value.
	 * 
	 * @param value - The value to round
	 * @return A suitably-rounded integer value.
	 */
	public int round(double value);
	
}