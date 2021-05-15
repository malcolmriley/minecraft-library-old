package paragon.minecraft.library.capabilities;

import java.util.Objects;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

/**
 * Simple {@link InventoryHandler} derivative with means for checking whether a provided {@link ItemStack} can be inserted into a particular {@link Slot}.
 * 
 * @author Malcolm Riley
 */
public class FilteredInventoryHandler extends InventoryHandler {

	/* Internal Fields */
	protected final IInventorySlotPredicate FILTER;

	public FilteredInventoryHandler(int inventorySize, TileEntity holder, @Nonnull IInventorySlotPredicate filter) {
		super(inventorySize, holder);
		this.FILTER = Objects.requireNonNull(filter);
	}

	@Override
	public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
    	return this.FILTER.validForSlot(slot, stack);
    }
	
	/* IInventorySlotPredicate Implementation */
	
	/**
	 * Specialized two-argument predicate for testing whether an {@link ItemStack} is valid in the provided slot index of an inventory.
	 * <p>
	 * For use with the companion class {@link FilteredInventoryHandler}
	 * 
	 * @author Malcolm Riley
	 */
	public static interface IInventorySlotPredicate {
		
		public boolean validForSlot(int slot, @Nonnull ItemStack stack);
		
	}

}
