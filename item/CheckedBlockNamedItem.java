package paragon.minecraft.library.item;

import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.BlockNamedItem;
import net.minecraft.util.ActionResultType;

/**
 * Extension of {@link BlockNamedItem} that checks the targeted {@link BlockState} before placement.
 * <p>
 * Useful for {@link BlockItem} that can only be placed in certain contexts.
 * 
 * @author Malcolm Riley
 *
 */
public class CheckedBlockNamedItem extends BlockNamedItem {
	
	/* Internal Fields */
	protected final Predicate<BlockItemUseContext> CONTEXT_CHECK;

	public CheckedBlockNamedItem(Block blockIn, Properties properties, Predicate<BlockItemUseContext> checker) {
		super(blockIn, properties);
		this.CONTEXT_CHECK = checker;
	}
	
	public ActionResultType tryPlace(BlockItemUseContext context) {
		return this.CONTEXT_CHECK.test(context) ? super.tryPlace(context) : ActionResultType.FAIL;
	}

}
