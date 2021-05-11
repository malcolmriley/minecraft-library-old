package paragon.minecraft.library.client.ui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

/**
 * Represents a removal-only variant of a {@link Slot}, usually for crafting results.
 * 
 * @author Malcolm Riley
 */
public class CraftResultSlot extends Slot {

	/* Internal Fields */
	private final PlayerEntity PLAYER;
	private int removalCount;

	public CraftResultSlot(PlayerEntity player, IInventory inventory, int index, int xPosition, int yPosition) {
		super(inventory, index, xPosition, yPosition);
		this.PLAYER = player;
	}

	/* Supertype Override Methods */

	@Override
	public boolean isItemValid(ItemStack stack) {
		return false;
	}

	@Override
	public ItemStack decrStackSize(int amount) {
		if (this.getHasStack()) {
			this.removalCount += Math.min(amount, this.getStack().getCount());
		}
		return super.decrStackSize(amount);
	}

	@Override
	public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack) {
		this.onCrafting(stack);
		super.onTake(thePlayer, stack);
		return stack;
	}

	/* Internal Methods */

	@Override
	protected void onCrafting(ItemStack stack, int amount) {
		this.removalCount += amount;
		this.onCrafting(stack);
	}

	@Override
	protected void onCrafting(ItemStack stack) {
		stack.onCrafting(this.PLAYER.world, this.PLAYER, this.removalCount);
		this.removalCount = 0;
	}

}
