package paragon.minecraft.library;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Collection of check methods for various parameters. These methods throw {@link IllegalArgumentException} with an optionally-customizable message
 * if their requisite check is not passed. These exceptions are not declared with the method signature; the intention is that a failure of the provided
 * checks be considered terminal. 
 * 
 * @author Malcolm Riley
 */
public final class Require {
	
	private Require() { }
	
	/**
	 * Checks the provided instance against the provided {@link Predicate}. if {@link Predicate#test(Object)} returns {@code FALSE},
	 * an {@link IllegalArgumentException} is thrown.
	 * 
	 * @param <T>
	 * @param instance - The instance to check
	 * @param check - The check to perform
	 * @return The instance if the check passed, else an {@link IllegalArgumentException} is thrown.
	 */
	public static final <T> T check(T instance, Predicate<T> check) {
		return Require.check(instance, check, String.format("Check failed for value \"%s\"", String.valueOf(instance)));
	}

	/**
	 * Checks the provided instance against the provided {@link Predicate}. if {@link Predicate#test(Object)} returns {@code FALSE},
	 * an {@link IllegalArgumentException} is thrown.
	 * 
	 * @param <T>
	 * @param instance - The instance to check
	 * @param check - The check to perform
	 * @param message - The message to supply to the {@link IllegalArgumentException}.
	 * @return The instance if the check passed, else an {@link IllegalArgumentException} is thrown.
	 */
	public static final <T> T check(T instance, Predicate<T> check, String message) {
		return Require.throwIfFalse(instance, check.test(instance), message);
	}

	/**
	 * Checks the provided instance against the provided {@link Predicate}. if {@link Predicate#test(Object)} returns {@code FALSE},
	 * an {@link IllegalArgumentException} is thrown.
	 * <p>
	 * This variation accepts a {@link Supplier} for the exception message and should be preferred over {@link Require#check(Object, Predicate, String)}
	 * if the composition of the message is particularly heavy performance-wise.
	 * 
	 * @param <T>
	 * @param instance - The instance to check
	 * @param check - The check to perform
	 * @param message - The supplier of the message for the {@link IllegalArgumentException}
	 * @return The instance if the check passed, else an {@link IllegalArgumentException} is thrown.
	 */
	public static final <T> T check(T instance, Predicate<T> check, Supplier<String> messageProvider) {
		return Require.throwIfFalse(instance, check.test(instance), messageProvider);
	}
	
	/* Internal Methods */
	
	protected static final <Z, T extends Comparable<Z>> boolean positive(T value, Z zero) {
		return Objects.nonNull(value) && value.compareTo(zero) < 0;
	}
	
	protected static final <Z, T extends Comparable<Z>> boolean nonPositive(T value, Z zero) {
		return Objects.nonNull(value) && value.compareTo(zero) >= 0;
	}
	
	protected static final <Z, T extends Comparable<Z>> boolean negative(T value, Z zero) {
		return Objects.nonNull(value) && value.compareTo(zero) > 0;
	}
	
	protected static final <Z, T extends Comparable<Z>> boolean nonNegative(T value, Z zero) {
		return Objects.nonNull(value) && value.compareTo(zero) <= 0;
	}
	
	protected static final <Z, T extends Comparable<Z>> boolean zero(T value, Z zero) {
		return Objects.nonNull(value) && value.compareTo(zero) == 0;
	}
	
	protected static final <Z, T extends Comparable<Z>> boolean nonZero(T value, Z zero) {
		return Objects.nonNull(value) && value.compareTo(zero) != 0;
	}
	
	protected static final <T> T throwIfFalse(T instance, boolean shouldThrow, Supplier<String> messageProvider) {
		if (shouldThrow) {
			throw new IllegalArgumentException(messageProvider.get());
		}
		return instance;
	}
	
	protected static final <T> T throwIfFalse(T instance, boolean shouldThrow, String message) {
		if (shouldThrow) {
			throw new IllegalArgumentException(message);
		}
		return instance;
	}

}
