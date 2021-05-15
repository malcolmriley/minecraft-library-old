package paragon.minecraft.library.capabilities;

import java.util.function.Supplier;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

/**
 * Dummy {@link IItemHandler} that does not accept insertion or extraction, or any operations
 * at all for that matter.
 * <p>
 * Only useful because within the capabilities system, {@link LazyOptional} must be given a {@link Supplier} that returns a
 * non-null value, and there are circumstances where a Forge Capability must be supplied even where the lack of a capability
 * would make the most sense.
 * <p>
 * For instance, {@link LockableLootTileEntity} has the helpful, forge-supplied method {@code createUnSidedHandler()}, so subclasses will need
 * to override this if they want sided (or lockable) inventory handling.
 * 
 * @author Malcolm Riley
 *
 */
public class DummyItemHandler implements IItemHandler {
	
	/* Supertype Override Methods */

	@Override
	public int getSlots() {
		return 0;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return null;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		return stack;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		return ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit(int slot) {
		return 0;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return false;
	}

}
