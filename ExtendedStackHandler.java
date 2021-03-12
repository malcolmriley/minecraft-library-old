package paragon.minecraft.library;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;

/**
 * Specialized {@link ItemStackHandler} that publicly exposes its internal {@link NonNullList} of {@link ItemStack}.
 * 
 * @author Malcolm Riley
 */
public class ExtendedStackHandler extends ItemStackHandler {
	
	public ExtendedStackHandler(int inventorySize) {
		super(inventorySize);
	}
	
	/**
	 * Returns the {@link NonNullList} of {@link ItemStack} backing this {@link ExtendedStackHandler}.
	 * @return
	 */
	public NonNullList<ItemStack> getItems() {
		return this.stacks;
	}
	
}