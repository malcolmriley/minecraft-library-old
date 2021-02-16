package paragon.minecraft.library;

import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.base.Objects;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Specialized {@link LazyTransformer} intended to be used on {@link IForgeRegistryEntry} derivatives.
 * 
 * Different from a {@link RegistryObject} in that it can deliberately contain null values, and that it can 
 * be updated to indicate a new target by calling either {@link #setSource(Object)} or {@link #setReference(IForgeRegistryEntry)}.
 * 
 * Unlike the supertype, it is kinda-sorta bidirectional insofar as one can set the "result" via {@link #setReference(IForgeRegistryEntry)},
 * and the underlying {@link ResourceLocation} used for future lookups will also be updated.
 * 
 * @author Malcolm Riley
 *
 * @param <T> The {@link IForgeRegistryEntry} type.
 */
public class LazyLookup<T extends IForgeRegistryEntry<T>> extends LazyTransformer<ResourceLocation, T> {

	public LazyLookup(Function<ResourceLocation, T> lookup) {
		super(lookup);
	}
	
	/* Public Methods */
	
	/**
	 * Creates a new {@link LazyLookup} using a simple lookup on the passed {@link IForgeRegistry}.
	 * 
	 * @param <K> The registry type
	 * @param registry - The registry to use for lookups
	 * @return A suitable {@link LazyLookup}.
	 */
	public static <K extends IForgeRegistryEntry<K>> LazyLookup<K> using(IForgeRegistry<K> registry) {
		return new LazyLookup<>(registry::getValue);
	}
	
	/**
	 * Returns the {@link ResourceLocation} associated with this {@link LazyLookup}.
	 * 
	 * This is the value that will be used to lookup the resulting {@link IForgeRegistryEntry} type
	 * if {@link #procure()} is called and the cached value is not present.
	 * 
	 * @return The {@link ResourceLocation} contained by this {@link LazyLookup}.
	 */
	public Optional<ResourceLocation> getID() {
		return this.source;
	}
	
	/**
	 * Sets the cached value referred to by this {@link LazyLookup}, also updating the source {@link ResourceLocation} reference.
	 * 
	 * @param instance - The value to set on this {@link LazyLookup}.
	 */
	public void setReference(@Nullable T instance) {
		this.cachedResult = Optional.ofNullable(instance);
		if (this.cachedResult.isPresent()) {
			this.source = Optional.ofNullable(this.cachedResult.get().getRegistryName());
		}
	}
	
	/* Supertype Override Methods */
	
	/**
	 * Unlike the {@link #setSource(Object)} method in the supertype, this method only invalidates
	 * the cached value if the registry name of the cached value differs from the passed value.
	 */
	@Override
	public void setSource(@Nullable ResourceLocation input) {
		this.source = Optional.ofNullable(input);
		if (this.cachedResult.isPresent()) {
			if (!Objects.equal(this.cachedResult.get().getRegistryName(), input)) {
				this.cachedResult = Optional.empty();
			}
		}
	}
}
