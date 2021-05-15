package paragon.minecraft.library.client.ui;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import paragon.minecraft.library.Utilities;
import paragon.minecraft.library.client.ClientUtilities;

/**
 * Simple {@link ContainerScreen} implementation suitable for screens that merely draw a background texture and appropriate titles.
 * <p>
 * Implementors should ensure that they correctly set values for {@link #getXSize()} and {@link #getYSize()}, as well as that of the player inventory title position.
 * Setting these values in the constructor will be sufficient for most use cases.
 * 
 * @author Malcolm Riley
 *
 * @param <C> The {@link Container} type
 */
@OnlyIn(Dist.CLIENT)
public abstract class SimpleContainerScreen<C extends Container> extends ContainerScreen<C> {
	
	public SimpleContainerScreen(C screenContainer, PlayerInventory inventory, ITextComponent title) {
		super(screenContainer, inventory, title);
	}

	/* Supertype Override Methods */

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
		ClientUtilities.Render.drawCenteredBackgroundLayer(this, matrixStack, this.getBackgroundTexture(), this.getXSize(), this.getYSize());
	}
	
	/* Abstract Methods */
	
	/**
	 * This method should return the {@link ResourceLocation} of the background texture to draw.
	 * <p>
	 * The path should be fully qualified within the resource domain, including the file suffix.
	 * @return A {@link ResourceLocation} path to the texture to use for the background.
	 */
	protected abstract ResourceLocation getBackgroundTexture();

	/* Internal Methods */
	
	/**
	 * Convenience method for returning a path to a texture, potentially useful with creating a {@link ResourceLocation} for use in {@link #getBackgroundTexture()}.
	 * <p>
	 * The returned path will be the concatenation of {@code textures/gui/container}, followed by the passed filename, followed by the {@code .png} suffix.
	 * <p>
	 * This resulting path will mirror the expected base-game location for similar textures.
	 * 
	 * @param filename - The endpoint filename of the texture
	 * @return A suitable texture path.
	 */
	protected static String backgroundTexturePath(String filename) {
		return Utilities.Strings.texturePath("textures", "gui", "container", filename);
	}

}
