package paragon.minecraft.library;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

/**
 * This interface is intended for use on {@link TileEntity} whose companion {@link BlockState} instances
 * should drop different {@link ItemStack} depending on internal conditions of the {@link TileEntity}.
 *
 * @author Malcolm Riley
 */
public interface ISpecialDrops {

	/**
	 * This method should return an {@link Iterable} of {@link ItemStack} to be spawned in the world as {@link ItemEntity}.
	 *
	 * @param world - The {@link ServerWorld} of the block broken
	 * @param state - The {@link BlockState} of the block broken
	 * @param pos - The {@link BlockPos} of the block broken
	 * @param breakingEntity - The {@link PlayerEntity} responsible for breaking the block.
	 * @return The {@link Block} drops.
	 */
	public Iterable<ItemStack> getTileDrops(final ServerWorld world, final BlockState state, final BlockPos pos, final @Nullable PlayerEntity breakingEntity);

}
