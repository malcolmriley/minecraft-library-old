package paragon.minecraft.library.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;

/**
 * Container class for various static utility functions that should be restricted to client-sided use only.
 *
 * @author Malcolm Riley
 */
public class ClientUtilities {
	
	/**
	 * Utility class for various client-sided UI methods.
	 */
	public static class UI {

		private UI() { }
		
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
	
	/**
	 * Utility class for various render-related methods.
	 */
	public static class Render {
		
		private Render() { }
		
		/**
		 * Method for drawing a simple centered background layer texture, suitable for use as a {@link ContainerScreen}-derived backdrop.
		 * <p>
		 * Resets the render color, binds the provided texture, and then blits the texture using the provided {@link MatrixStack}.
		 * <p>
		 * The provided {@link ResourceLocation} should be a fully-qualified path with the appropriate suffix.
		 * 
		 * @param <S> The {@link Screen} type
		 * @param screenReference - A reference to the calling {@link Screen} derivative (callers may simply supply {@code this})
		 * @param currentStack - A reference to the current {@link MatrixStack}
		 * @param texture - A {@link ResourceLocation} corresponding to the location of the texture to draw
		 * @param textureWidth - The width of the texture, in pixels
		 * @param textureHeight - The height of the texture, in pixels
		 */
		public static <S extends Screen> void drawCenteredBackgroundLayer(S screenReference, MatrixStack currentStack, ResourceLocation texture, int textureWidth, int textureHeight) {
			Render.resetRenderColor();
			screenReference.getMinecraft().getTextureManager().bindTexture(texture);
			int xPos = (screenReference.width - textureWidth) / 2;
			int yPos = (screenReference.height - textureHeight) / 2;
			screenReference.blit(currentStack, xPos, yPos, 0, 0, textureWidth, textureHeight);
		}

		/* Internal Methods */
		
		@SuppressWarnings("deprecation") // RenderSystem.color4f is deprecated, but all vanilla UI uses it to set the render color for bound UI textures.
		protected static void resetRenderColor() {
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		}
		
	}

}
