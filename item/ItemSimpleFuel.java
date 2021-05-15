package paragon.minecraft.library.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Simple fuel item.
 *
 * @author Malcolm Riley
 */
public class ItemSimpleFuel extends Item {

	/* Internal Fields */
	private final int BURN_TIME;

	public ItemSimpleFuel(Properties properties, int burnTime) {
		super(properties);
		this.BURN_TIME = burnTime;
	}

	@Override
	public int getBurnTime(ItemStack passedStack) {
		return this.BURN_TIME;
	}

}
