package paragon.minecraft.library.item;

import java.util.function.Predicate;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Extension of {@link BlockItem} that checks the targeted {@link BlockState} before placement.
 * <p>
 * Useful for {@link BlockItem} that can only be placed in certain contexts.
 * 
 * @author Malcolm Riley
 *
 */
public class CheckedBlockItem extends BlockItem {
	
	/* Internal Fields */
	protected final Predicate<BlockPlaceContext> CONTEXT_CHECK;

	public CheckedBlockItem(Block blockIn, Properties properties, Predicate<BlockPlaceContext> checker) {
		super(blockIn, properties);
		this.CONTEXT_CHECK = checker;
	}
	
	@Override
	public InteractionResult place(BlockPlaceContext context) {
		return this.CONTEXT_CHECK.test(context) ? super.place(context) : InteractionResult.FAIL;
	}

}
