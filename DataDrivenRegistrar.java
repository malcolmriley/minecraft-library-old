package paragon.minecraft.library;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.logging.log4j.Logger;

import com.google.common.collect.Streams;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.JsonOps;

import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;

/**
 * A special registration class that holds an {@link IForgeRegistry}, but also examines datapacks for local overrides.
 * It also receives a {@link DeferredRegister} for registering content to the {@link IForgeRegistry} directly.
 * This class is intended to be used for {@link IForgeRegistryEntry}-derivatives that do not already have a corresponding {@link IForgeRegistry}.
 * In such cases, {@link ContentProvider} should be preferred.
 *
 * @author Malcolm Riley
 * @param <T> The {@link IForgeRegistryEntry} type managed by this registrar.
 */
public class DataDrivenRegistrar<T extends IForgeRegistryEntry<T>> implements IEventBusListener {

	/* Shared Static Fields */
	protected static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).disableHtmlEscaping().setPrettyPrinting().create();

	/* Internal Fields */
	protected final Class<T> TYPE;
	protected final String MODID;
	protected final String NAME;

	protected Supplier<IForgeRegistry<T>> registry = null;
	protected Optional<Map<ResourceLocation, T>> overrides = Optional.empty();

	public DataDrivenRegistrar(Class<T> registryType, String modid, String name) {
		this.TYPE = Objects.requireNonNull(registryType);
		this.MODID = Objects.requireNonNull(modid);
		this.NAME = Objects.requireNonNull(name);
	}

	/* Public Methods */

	/**
	 * Returns the instance corresponding to the passed {@link ResourceLocation}.
	 * The internal list of datapack-driven overrides is queried first, and then the underlying {@link IForgeRegistry}.
	 *
	 * @param identifier - The identifier of the content to locate
	 * @return The corresponding instance, or {@code null} if no such instance exists.
	 */
	public final @Nullable T get(ResourceLocation identifier) {
		T instance = null;
		if (this.overrides.isPresent()) {
			instance = this.overrides.get().get(identifier);
		}
		if (Objects.isNull(instance)) {
			instance = this.registry.get().getValue(identifier);
		}
		return instance;
	}

	/**
	 * Finds the first located instance that passes the test of the received {@link Predicate}, returning it in an {@link Optional}, or an empty {@link Optional} if no such instance exists.
	 * The datapack-driven local overrides are examined first, and then the underlying {@link IForgeRegistry}.
	 *
	 * @param filter - The filter to apply
	 * @return An {@link Optional} of the first compliant instance or {@link Optional#empty()}.
	 */
	public final Optional<T> find(Predicate<T> filter) {
		Optional<T> found = this.findIn(this.streamOverrides(), filter);
		return found.isPresent() ? found : this.findIn(this.streamRegistered(), filter);
	}

	/**
	 * Returns an {@link Iterable} over the datapack-driven local overrides for this {@link DataDrivenRegistrar}.
	 *
	 * @return An {@link Iterable} over the datapack-driven local overrides.
	 */
	public final Iterable<T> getOverrides() {
		if (this.overrides.isPresent()) {
			return Collections.unmodifiableCollection(this.overrides.get().values());
		}
		return Collections.emptyList();
	}

	/**
	 * Returns an {@link Iterable} over the content registered to the underlying {@link IForgeRegistry}.
	 *
	 * @return An {@link Iterable} over the underlying {@link IForgeRegistry}.
	 */
	public final Iterable<T> getRegistered() {
		return this.registry.get().getValues();
	}

	/**
	 * Returns an {@link Stream} over the datapack-driven local overrides for this {@link DataDrivenRegistrar}.
	 *
	 * @return An {@link Stream} over the datapack-driven local overrides.
	 */
	public final Stream<T> streamOverrides() {
		return Streams.stream(this.getOverrides());
	}

	/**
	 * Returns an {@link Stream} over the content registered to the underlying {@link IForgeRegistry}.
	 *
	 * @return An {@link Stream} over the underlying {@link IForgeRegistry}.
	 */
	public final Stream<T> streamRegistered() {
		return this.registry.get().getValues().stream();
	}

	/**
	 * Creates a new {@link JsonRegistryReloader} for this {@link DataDrivenRegistrar}.
	 *
	 * @param decoder - The decode method for the reloader to use
	 * @param logger - A logger (for printing relevant exceptions)
	 * @return A new {@link JsonRegistryReloader} instance.
	 */
	public final JsonRegistryReloader<T> createReloadListener(Decoder<T> decoder, Logger logger) {
		return new JsonRegistryReloader<>(GSON, this.NAME, decoder, this, logger);
	}

	/* IEventBusListener Compliance Method */

	@Override
	public final void registerTo(IEventBus bus) {
		DeferredRegister<T> registrar = DeferredRegister.create(this.TYPE, this.MODID);
		this.registry = registrar.makeRegistry(this.NAME, () -> this.createRegistry());
		this.registerContent(registrar);
		registrar.register(bus);
	}

	/* Internal Methods */

	/**
	 * This method should be overridden to register content to the {@link IForgeRegistry}.
	 * 
	 * The content is not guaranteed to be immediately available in the backing {@link IForgeRegistry},
	 * as this method uses {@link DeferredRegister}.
	 * 
	 * @param registrar - The {@link DeferredRegister} instance to use to register content.
	 */
	protected void registerContent(DeferredRegister<T> registrar) {

	}
	
	/**
	 * This method should be used to create a {@link RegistryBuilder} suitable for creating the backing {@link IForgeRegistry}.
	 * 
	 * @return - A {@link RegistryBuilder} that will be used to construct the backing {@link IForgeRegistry}.
	 */
	protected RegistryBuilder<T> createRegistry() {
		return new RegistryBuilder<T>().allowModification().disableSaving();
	}

	protected final void addOverride(ResourceLocation id, T instance) {
		if (!this.overrides.isPresent()) {
			this.overrides = Optional.of(new HashMap<>());
		}
		this.overrides.get().put(id, instance);
	}

	protected final void clearOverrides() {
		this.overrides = Optional.empty();
	}

	private final Optional<T> findIn(Stream<T> stream, Predicate<T> predicate) {
		return stream.filter(predicate).findFirst();
	}

	/* JsonReloadListener Extension Implementation */

	/**
	 * A specialized {@link JsonReloadListener} intended to be used with {@link DataDrivenRegistrar}.
	 * To obtain an instance, use {@link DataDrivenRegistrar#createReloadListener(Decoder, Logger)}.
	 *
	 * @author Malcolm Riley
	 * @param <T> The {@link IForgeRegistryEntry} type to be deserialized from JSON.
	 * @see DataDrivenRegistrar#createReloadListener(Decoder, Logger)
	 */
	protected static final class JsonRegistryReloader<T extends IForgeRegistryEntry<T>> extends JsonReloadListener {

		/* Internal Fields */
		protected final Decoder<T> DECODER;
		protected final DataDrivenRegistrar<T> REGISTRAR;
		protected final Logger LOGGER;

		protected JsonRegistryReloader(Gson gson, String directory, Decoder<T> codec, DataDrivenRegistrar<T> registrar, Logger logger) {
			super(gson, directory);
			this.DECODER = Objects.requireNonNull(codec);
			this.REGISTRAR = Objects.requireNonNull(registrar);
			this.LOGGER = Objects.requireNonNull(logger);
		}

		/* Supertype Override Methods */

		@Override
		protected void apply(Map<ResourceLocation, JsonElement> elements, IResourceManager manager, IProfiler profiler) {
			this.REGISTRAR.clearOverrides();
			elements.forEach(this::parseToRegistrar);
		}

		/* Internal Methods */

		protected void parseToRegistrar(ResourceLocation id, JsonElement element) {
			DataResult<Pair<T, JsonElement>> result = this.DECODER.decode(JsonOps.INSTANCE, element);
			try {
				T instance = result.getOrThrow(false, this.LOGGER::error).getFirst();
				instance.setRegistryName(id);
				this.REGISTRAR.addOverride(id, instance);
			}
			catch (RuntimeException exception) {
				this.LOGGER.error(exception);
			}
		}

	}

}
