package paragon.minecraft.library;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * This interface is intended for use on {@link BlockEntity} whose companion {@link BlockState} instances
 * should drop different {@link ItemStack} depending on internal conditions of the {@link BlockEntity}.
 *
 * @author Malcolm Riley
 */
public interface ISpecialDrops {

	/**
	 * This method should return an {@link Iterable} of {@link ItemStack} to be spawned in the world as {@link ItemEntity}.
	 *
	 * @param world - The {@link ServerLevel} of the block broken
	 * @param state - The {@link BlockState} of the block broken
	 * @param pos - The {@link BlockPos} of the block broken
	 * @param breakingEntity - The {@link Player} responsible for breaking the block.
	 * @return The {@link Block} drops.
	 */
	public Iterable<ItemStack> getTileDrops(final ServerLevel world, final BlockState state, final BlockPos pos, final @Nullable Player breakingEntity);

}
