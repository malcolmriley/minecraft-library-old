package paragon.minecraft.library.item;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

/**
 * Simple fuel {@link BlockItem}.
 *
 * @author Malcolm Riley
 */
public class BlockItemSimpleFuel extends BlockItem {

	/* Internal Fields */
	protected final int BURN_TIME;

	public BlockItemSimpleFuel(Block block, Properties properties, int burnTime) {
		super(block, properties);
		this.BURN_TIME = burnTime;
	}

	@Override
	public int getBurnTime(ItemStack stack) {
		return this.BURN_TIME;
	}

}
