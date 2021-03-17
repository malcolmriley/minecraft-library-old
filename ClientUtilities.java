package paragon.minecraft.library;

import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.fml.RegistryObject;

/**
 * Container class for various static utility functions that should be restricted to client-sided use only.
 *
 * @author Malcolm Riley
 */
public class ClientUtilities {
	
	/**
	 * Utility class for various client-sided UI utility methods.
	 */
	public static class UI {
		
		/**
		 * Convenience method for binding a {@link ContainerType} {@link RegistryObject} to a {@link ScreenManager.IScreenFactory}.
		 * <p>
		 * Callers should ensure that the type parameter of the {@link ContainerType} matches the expected type of the factory.
		 * 
		 * @param <C> The type of the {@link Container}
		 * @param <S> The type of the {@link Screen}
		 * @param source - The {@link RegistryObject} containing the {@link ContainerType} to bind
		 * @param factory - The {@link ScreenManager.IScreenFactory} to bind to that type
		 */
		public static <C extends Container, S extends Screen & IHasContainer<C>> void bindGuiFactory(RegistryObject<ContainerType<?>> source, ScreenManager.IScreenFactory<C, S> factory) {
			ScreenManager.registerFactory(UI.castFromRegistryObject(source), factory);
		}
		
		/* Internal Methods */
		
		@SuppressWarnings("unchecked") // Unchecked cast of erased parameterized type from RegistryObject for use in ScreenManager.registerFactory, which expects explicitly-matching type parameters
		protected static <T extends Container> ContainerType<T> castFromRegistryObject(RegistryObject<ContainerType<?>> source) {
			return (ContainerType<T>)source.get();
		}
		
	}

}
