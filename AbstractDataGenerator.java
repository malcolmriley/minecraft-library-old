package paragon.minecraft.library;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.ResourceLocation;

/**
 * Partial implementation of a {@link IDataProvdier} for {@link IRegistryNamed} instances. Handles some boilerplate associated
 * with generating data.
 * 
 * @author Malcolm Riley
 *
 * @param <T> The {@link IRegistryNamed} type.
 */
public abstract class AbstractDataGenerator<T extends IRegistryNamed> implements IDataProvider {
	
	/* Internal Fields */
	private final DataGenerator GENERATOR;
	private final Gson GSON;
	private final String DIRECTORY_NAME;
	
	/* Constants */
	private static final String ROOT_DIRECTORY = "data/";
	private static final String OUTPUT_SUFFIX = ".json";
	
	protected AbstractDataGenerator(DataGenerator generator, Gson gson, String directory) {
		this.GENERATOR = generator;
		this.GSON = gson;
		this.DIRECTORY_NAME = directory;
	}
	
	/* IDataProvider Compliance Methods */

	@Override
	public final void act(DirectoryCache cache) throws IOException {
		Set<ResourceLocation> knownLocations = new HashSet<>();
		this.generate(instance -> this.createAndAdd(knownLocations, this::toJson, instance, cache));
	}
	
	/* Abstract Methods */
	
	/**
	 * This method should serialize the passed instance to a {@link JsonElement}.
	 * 
	 * @param instance - The instance to serialize
	 * @return A {@link JsonElement} of the passed instance.
	 */
	protected abstract JsonElement toJson(T instance);
	
	/**
	 * This method should be overridden to serialize all known content. Feed individual instances to the provided {@link Consumer}
	 * in order to have them generated.
	 * 
	 * @param generator - The generator to use.
	 */
	protected abstract void generate(Consumer<T> generator);
	
	/* Internal Methods */
	
	protected Path resolveOutputPath(T instance) {
		return this.GENERATOR.getOutputFolder().resolve(Paths.get(ROOT_DIRECTORY, instance.getRegistryName().getNamespace(), this.DIRECTORY_NAME, instance.getRegistryName().getPath() + OUTPUT_SUFFIX));
	}
	
	private final void createAndAdd(final Set<ResourceLocation> locations, Function<T, JsonElement> serializer, final T instance, DirectoryCache cache) {
		if (!locations.add(instance.getRegistryName())) {
			throw new IllegalStateException("Duplicate instance registered to ID: " + instance.getRegistryName());
		}
		try {
			IDataProvider.save(this.GSON, cache, serializer.apply(instance), this.resolveOutputPath(instance));
		}
		catch (IOException exception) {
			Logger logger = LogManager.getLogger();
			logger.error("Error while serializing data for directory \"{}\"", this.DIRECTORY_NAME);
			logger.error(exception);
		}
	}
	
}
