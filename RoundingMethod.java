package paragon.minecraft.library;

import java.util.Locale;

import javax.annotation.Nullable;

import net.minecraft.util.Mth;

/**
 * Enum class specifying/implementing a number of common rounding methods.
 * 
 * @author Malcolm Riley
 */
public enum RoundingMethod implements IRoundingMethod {
	
	FLOOR(Mth::floor),
	CEILING(Mth::ceil),
	ROUND_UP(value -> Math.round((float)value)),
	ROUND_DOWN(value -> value > 0.5 ? 1 : 0);
	;
	
	private IRoundingMethod operator;
	
	private RoundingMethod(IRoundingMethod function) {
		this.operator = function;
	}
	
	@Override
	public int round(double value) {
		return this.operator.round(value);
	}
	
	/**
	 * Performs a reverse by-name lookup to a {@link RoundingMethod} using the provided {@link String}.
	 * 
	 * @param name - The name of the {@link RoundingMethod} to look up
	 * @return The corresponding {@link RoundingMethod} if any, else {@code null}.
	 */
	public static @Nullable IRoundingMethod byName(String name) {
		switch(name.toLowerCase(Locale.US)) {
			case "floor" : return RoundingMethod.FLOOR;
			case "ceiling": return RoundingMethod.CEILING;
			case "round_up": return RoundingMethod.ROUND_UP;
			case "round_down": return RoundingMethod.ROUND_DOWN;
		}
		return null;
	}
	
}