package paragon.minecraft.library;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nullable;

/**
 * Lazily transforms one value into another.
 * 
 * This class assumes that meaningful values of the I and O types are non-null, as it uses a null check to determine
 * whether new results can be calculated or are available. However, the class itself is null-safe (i.e. permits setting the input object to null via {@link #setSource(Object)}.)
 * 
 * This class uses an internal {@link Function} to transform I to O. This function should not assume non-null values for I, due to the above.
 * Because callers of {@link #setSource(Object)} may set the input I to null, the contained {@link Function} should be able to handle that.
 * 
 * @author Malcolm Riley
 *
 * @param <I> The input type
 * @param <O> The output type
 */
public class LazyTransformer<I, O> {
	
	/* Internal Fields */
	protected final Function<I, O> LOOKUP;
	
	protected Optional<I> source = Optional.empty();
	protected Optional<O> cachedResult = Optional.empty();
	
	public LazyTransformer(Function<I, O> lookup) {
		this.LOOKUP = Objects.requireNonNull(lookup);
	}
	
	/**
	 * Creates a new {@link LazyTransformer} with the given source.
	 * 
	 * @param <V> The source type
	 * @param <K> The result type
	 * @param lookup - The lookup method
	 * @param source - The source element
	 * @return A suitably-instantiated {@link LazyTransformer}
	 */
	public static <V, K> LazyTransformer<V, K> create(Function<V, K> lookup, V source) {
		LazyTransformer<V, K> transformer = new LazyTransformer<>(lookup);
		transformer.setSource(source);
		return transformer;
	}
	
	/* Public Methods */
	
	/**
	 * Sets the "source" object of this {@link LazyTransformer} that will be used when {@link #procure()} is called
	 * to calculate the result.
	 * 
	 * This will invalidate the currently cached result if the passed {@link Objects#equals(Object, Object)} returns {@code false}
	 * for the passed input and the currently-contained source object.
	 * 
	 * @param input - The source
	 */
	public void setSource(@Nullable I input) {
		if (!Objects.equals(input, this.source.orElse(null))) {
			this.cachedResult = Optional.empty();
		}
		this.source = Optional.ofNullable(input);
	}
	
	/**
	 * Returns the value stored by this {@link LazyTransformer}.
	 * 
	 * If the value is not cached but an input value is available, the result will be cached before
	 * returning.
	 * 
	 * @return An {@link Optional} containing the value calculated by this {@link LazyTransformer}.
	 */
	public Optional<O> procure() {
		if (!this.cachedResult.isPresent() && this.source.isPresent()) {
			this.cacheResult();
		}
		return this.cachedResult;
	}
	
	/**
	 * Returns whether this {@link LazyTransformer} has a source object or cached result present.
	 * 
	 * @return Whether a result is available (cached or otherwise).
	 */
	public boolean isAvailable() {
		return this.cachedResult.isPresent() || this.source.isPresent();
	}
	
	/* Internal Methods */
	
	protected void cacheResult() {
		this.cachedResult = Optional.ofNullable(this.LOOKUP.apply(this.source.get()));
	}

}
