package paragon.minecraft.library.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;

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
	public int getBurnTime(ItemStack stack, RecipeType<?> recipeType) {
		return this.BURN_TIME;
	}

}
