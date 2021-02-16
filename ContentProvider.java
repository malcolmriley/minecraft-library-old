package paragon.minecraft.library;

import com.google.common.base.Supplier;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Convenience class for registering content to Forge.
 *
 * @author Malcolm Riley
 * @param <T> The {@link IForgeRegistryEntry} subtype
 */
public abstract class ContentProvider<T extends IForgeRegistryEntry<T>> implements IEventBusListener {

	/* Internal Fields */
	protected final DeferredRegister<T> ALL;

	public ContentProvider(IForgeRegistry<T> registry, String modID) {
		this.ALL = DeferredRegister.create(registry, modID);
	}

	/**
	 * Registers internal {@link DeferredRegister} with the passed {@link IEventBus}, so that the content
	 * of this {@link ContentProvider} will be registered when the time comes.
	 *
	 * @param bus - The bus to register the {@link DeferredRegister} to.
	 */
	public void registerTo(IEventBus bus) {
		this.ALL.register(bus);
	}

	/* Internal Methods */

	protected RegistryObject<T> add(String name, Supplier<T> supplier) {
		return this.ALL.register(name, supplier);
	}

}
