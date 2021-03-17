package paragon.minecraft.library.client.ui;

import java.util.Objects;
import java.util.function.Predicate;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

/**
 * Specialized {@link Slot} implementation that accepts a {@link Predicate} as a filter for {@link ItemStack}.
 * 
 * @author Malcolm Riley
 */
public class FilteredSlot extends Slot {

	/* Internal Fields */
	protected final Predicate<ItemStack> CHECK;

	public FilteredSlot(IInventory inventoryIn, int index, int xPosition, int yPosition, Predicate<ItemStack> stackPredicate) {
		super(inventoryIn, index, xPosition, yPosition);
		this.CHECK = Objects.requireNonNull(stackPredicate);
	}
	
	/* Public Methods */
	
	/**
	 * Attempts to put the {@link ItemStack} into this {@link FilteredSlot}.
	 * <p>
	 * Calls {@link #isItemValid(ItemStack)}, and if successful, calls {@link #putStack(ItemStack)}.
	 * <p>
	 * This method should be preferred over {@link #putStack(ItemStack)}, as {@link #putStack(ItemStack)} will ALWAYS
	 * insert the item, regardless of whether {@link #isItemValid(ItemStack)} would succeed.
	 * 
	 * @param stack - The {@link ItmStack} to attempt to insert
	 * @return - Whether insertion succeeded.
	 */
	public boolean tryPutStack(ItemStack stack) {
		if (this.isItemValid(stack)) {
			this.putStack(stack);
			return true;
		}
		return false;
	}
	
	/* Supertype Override Methods */

	@Override
	public boolean isItemValid(ItemStack stack) {
		return this.CHECK.test(stack);
	}

}
