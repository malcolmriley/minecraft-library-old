package paragon.minecraft.library;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;

/**
 * Specialized {@link ItemStackHandler} that publicly exposes its internal {@link NonNullList} of {@link ItemStack}.
 *
 * @author Malcolm Riley
 */
public class ExtendedStackHandler extends ItemStackHandler {

	/* Internal Fields */
	protected final TileEntity HOLDER;

	public ExtendedStackHandler(int inventorySize, TileEntity holder) {
		super(inventorySize);
		this.HOLDER = holder;
	}
	
	/* Supertype Override Methods */

	@Override
	protected void onContentsChanged(int slot) {
		super.onContentsChanged(slot);
		this.HOLDER.markDirty();
	}
	
	/* Public Methods */

	/**
	 * Returns the {@link NonNullList} of {@link ItemStack} backing this {@link ExtendedStackHandler}.
	 *
	 * @return
	 */
	public NonNullList<ItemStack> getItems() {
		return this.stacks;
	}

}