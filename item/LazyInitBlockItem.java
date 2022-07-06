package paragon.minecraft.library.item;

import java.util.function.Supplier;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

/**
 * Extension of the simple {@link BlockItem} that takes a {@link Supplier} instead of a direct block reference.
 * 
 * @author Malcolm Riley
 */
public class LazyInitBlockItem extends BlockItem {

	public LazyInitBlockItem(Supplier<Block> blockSupplier, Properties builder) {
		super(blockSupplier.get(), builder);
	}

}
