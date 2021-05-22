package paragon.minecraft.library;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Container class for various static utility functions.
 *
 * @author Malcolm Riley
 */
public final class Utilities {

	private Utilities() {}

	/**
	 * Container class for time-related static utility functions.
	 */
	public static final class Time {

		private Time() {}

		/* Public Fields */

		/** The burn time it takes to smelt a single item in an ordinary furnace. */
		public static final int BURN_TICKS_SINGLE_ITEM = 200;
		/** The (ideal) number of in-game ticks per real-world second. */
		public static final int TICKS_PER_SECOND = 20;

		/**
		 * Returns the burn time, in ticks, required to smelt the passed whole quantity of items in an ordinary furnace.
		 *
		 * @param quantityItems - The quantity of single items to smelt
		 * @return The burn time, in ticks, required to smelt those items.
		 */
		public static final int burnTimeFor(int quantityItems) {
			return Time.BURN_TICKS_SINGLE_ITEM * quantityItems;
		}

		/**
		 * Converts the quantity of real-world seconds into an (ideally) equivalent quantity of in-game ticks.
		 *
		 * @param seconds - The quantity of seconds to convert
		 * @return An equivalent number of ingame ticks.
		 */
		public static final int secondsToTicks(double seconds) {
			return MathHelper.floor(Time.TICKS_PER_SECOND * seconds);
		}

		/**
		 * Converts the quantity of real-world minutes into an (ideally) equivalent quantity of in-game ticks.
		 *
		 * @param minutes - The quantity of minutes to convert
		 * @return An equivalent number of ingame ticks.
		 */
		public static final int minutesToTicks(double minutes) {
			return MathHelper.floor(Time.TICKS_PER_SECOND * 60 * minutes);
		}

	}
	
	/**
	 * Container for {@link BlockState}-related static helper methods.
	 */
	public static final class States {
		
		private States() { }
		
		/**
		 * Convenience method that returns whether the {@link BlockState} below the provided {@link BlockPos} has a solid upward surface, via {@link BlockState#isSolidSide(IBlockReader, BlockPos, Direction)}.
		 * 
		 * @param world - The {@link IBlockReader} to use for {@link BlockState} access
		 * @param position - The position above the queried block
		 * @return Whether the {@link BlockState} below the provided {@link BlockPos} has a solid upward surface.
		 */
		public static boolean isBlockBelowSolidAbove(IBlockReader world, BlockPos position) {
			BlockPos below = position.down();
			return world.getBlockState(below).isSolidSide(world, below, Direction.UP);
		}
		
		/**
		 * Deduces the horizontal-only facing from the provided {@link BlockItemUseContext}.
		 * <p>
		 * If the facing is {@value Direction#UP} or {@value Direction#DOWN}, returns the opposite of {@link BlockItemUseContext#getPlacementHorizontalFacing()}; otherwise returns {@link BlockItemUseContext#getFace()}.
		 * 
		 * @param context - The context to deduce the facing from
		 * @return The block placement facing direction.
		 */
		public static Direction deduceHorizontalFacing(BlockItemUseContext context) {
			return (context.getFace() == Direction.UP) || (context.getFace() == Direction.DOWN) ? context.getPlacementHorizontalFacing() : context.getFace().getOpposite();
		}
		
		/**
		 * Applies a waterlog state to the incoming {@link BlockState} derived from the provided {@link BlockItemUseContext}.
		 * <p>
		 * Intended for use within {@link Block#getStateForPlacement(BlockItemUseContext)}.
		 * 
		 * @param context - The use context for the {@link BlockState}
		 * @param incoming - The inbound {@link BlockState}
		 * @return The {@link BlockState} with waterlog properties applied.
		 */
		public static BlockState applyWaterlogPlacementState(BlockItemUseContext context, BlockState incoming) {
			Fluid fluid = context.getWorld().getFluidState(context.getPos()).getFluid();
			return incoming.with(BlockStateProperties.WATERLOGGED, Boolean.valueOf((fluid == Fluids.WATER) || (fluid == Fluids.FLOWING_WATER)));
		}
		
		/**
		 * Convenience method to queue a fluid tick post-placement if the provided {@link BlockState} is waterlogged.
		 * <p>
		 * Intended for use within {@link Block#updatePostPlacement(BlockState, Direction, BlockState, IWorld, BlockPos, BlockPos)}.
		 * 
		 * @param state - The world {@link BlockState}
		 * @param world - The world reference
		 * @param position - The position of the {@link BlockState}
		 */
		public static void applyWaterlogPostPlacement(BlockState state, IWorld world, BlockPos position) {
			if (state.get(BlockStateProperties.WATERLOGGED).booleanValue()) {
				world.getPendingFluidTicks().scheduleTick(position, Fluids.WATER, Fluids.WATER.getTickRate(world));
			}
		}
		
	}

	public static class Misc {

		/**
		 * Returns whether the two {@link Optional} contain equal items, via {@link Objects#equals(Object)} on
		 * each of their {@link Optional#orElse(Object)} methods.
		 *
		 * @param <T> The type parameter of the {@link Optional}s
		 * @param first - The first {@link Optional} to examine
		 * @param second - The second {@link Optional} to examine
		 * @return Whether both {@link Optional} contain equivalent objects.
		 */
		public static <T> boolean sameContents(Optional<T> first, Optional<T> second) {
			return Objects.equals(first.orElse(null), second.orElse(null));
		}

	}

	public static class Game {

		private Game() {}

		/**
		 * Returns whether the chunk at the indicated {@link BlockPos} is currently loaded.
		 *
		 * @param world - A reference to the world containing the chunk
		 * @param position - The {@link BlockPos} to check
		 * @return Whether the chunk containing {@link BlockPos} is loaded.
		 */
		public static boolean isBlockLoaded(IWorld world, BlockPos position) {
			return world.chunkExists(position.getX() >> 4, position.getZ() >> 4);
		}

		/**
		 * Attempts to parse the received {@link String} as a {@link ResourceLocation}, and attempts to look up that {@link ResourceLocation} in the {@link ForgeRegistries#ITEMS}
		 * registry, returning the result (if any).
		 *
		 * @param input - The input {@link String}, ideally parseable as a {@link ResourceLocation}.
		 * @return The discovered {@link Item}.
		 * @throws ResourceLocationException - If the input {@link String} cannot validly be converted into a {@link ResourceLocation} for registry lookup.
		 */
		public static @Nullable Item tryParseItem(String input) throws ResourceLocationException {
			return ForgeRegistries.ITEMS.getValue(new ResourceLocation(input.trim()));
		}

		/**
		 * Attempts to parse the received {@link String} into an {@link ItemStack} by the following means:
		 * <li>The input {@link String} is split by whitespace into an array.</li>
		 * <li>The first element of the result is attempted to be parsed as an {@link Item} using {@link Game#tryParseItem(String)}.</li>
		 * <li>The second element of the result (if any) is attempted to be parsed as a {@link CompoundNBT} from JSON format.</li>
		 * <li>The {@link ItemStack} of the detected {@link Item}, with any parsed {@link CompoundNBT} applied, is returned.</li>
		 *
		 * @param input - The input {@link String}, ideally parseable as a {@link ResourceLocation} followed by whitespace and then a JSON-format {@link CompoundNBT}.
		 * @return An {@link ItemStack} constructed from a {@link Item} registry lookup with any applicable {@link CompoundNBT} applied.
		 * @throws ResourceLocationException - If the input {@link String} cannot validly be converted into a {@link ResourceLocation} for registry lookup.
		 * @throws CommandSyntaxException - If the additional parameters cannot be parsed as a {@link CompoundNBT}.
		 */
		public static @Nonnull ItemStack tryParseItemStack(String input) throws ResourceLocationException, CommandSyntaxException {
			String[] elements = Strings.splitByWhitespace(input);
			if (elements.length > 0) {
				Item discovered = Game.tryParseItem(elements[0]);
				if (Objects.nonNull(discovered)) {
					ItemStack stack = new ItemStack(discovered);
					if (elements.length > 1) {
						stack.setTag(JsonToNBT.getTagFromJson(elements[1])); // TODO: Method is tagged with @VisibleForTesting, seek better way
					}
					return stack;
				}
			}
			return ItemStack.EMPTY;
		}

		/**
		 * Creates a new {@link ItemGroup} from the provided name and {@link Supplier}.
		 *
		 * @param name - The name for this {@link ItemGroup}
		 * @param icon - The {@link Supplier} from which an icon will be derived.
		 * @return A suitable {@link ItemGroup} instance.
		 */
		public static ItemGroup createGroupFrom(String name, Supplier<Item> icon) {
			return new ItemGroup(name) {

				@Override
				public ItemStack createIcon() {
					return new ItemStack(icon.get());
				}

			};
		}
		
		/**
		 * Returns the {@link Vector3i} component corresponding to the provided {@link Axis}.
		 * 
		 * @param position - The vector to query
		 * @param axis - The axis to examine
		 * @return The component of the provided {@link Vector3i} corresponding to the provided {@link Axis}.
		 */
		public static int componentFrom(final Vector3i position, final Axis axis) {
			switch(axis) {
				case X : return position.getX();
				case Y : return position.getY();
				case Z : return position.getZ();
			}
			return 0;
		}

		/**
		 * Returns a {@link Stream} over all blocks within the given cubic "radius" centered on the passed origin {@link BlockPos}.
		 *
		 * @param origin - The origin {@link BlockPos}
		 * @param radius - The "radius" to seek
		 * @return A {@link Stream} over all {@link BlockPos} at most {@code radius} blocks away from the origin {@link BlockPos}.
		 */
		public static Stream<BlockPos> streamCubicRadius(BlockPos origin, final int radius) {
			return BlockPos.getAllInBox(origin.getX() - radius, origin.getY() - radius, origin.getZ() - radius, origin.getX() + radius, origin.getY() + radius, origin.getZ() + radius);
		}

		/**
		 * Returns a {@link Stream} over all blocks orthogonal to the passed origin {@link BlockPos} in the indicated {@link Direction}.
		 * Merely calls {@link Streams#stream(Iterable)} using {@link #allOrthogonalTo(BlockPos, int, Direction...)}.
		 *
		 * @param origin - The origin {@link BlockPos}
		 * @param distance - The distance to seek
		 * @param direction - The direction(s) to seek in
		 * @return All {@link BlockPos} orthogonal to the origin in each of the given {@link Direction} all within the passed distance.
		 * @see #allOrthogonalTo(BlockPos, int, Direction...)
		 */
		public static Stream<BlockPos> streamOrthogonalTo(BlockPos origin, int distance, Direction... directions) {
			return Streams.stream(Game.allOrthogonalTo(origin, distance, directions));
		}

		/**
		 * Returns an {@link Iterable} over all blocks orthogonal to the passed origin {@link BlockPos} in the indicated {@link Direction}.
		 * If a {@link Direction} is indicated more than once, those blocks will be present in the resulting {@link Iterable} more than once.
		 * The returned {@link Iterable} is immutable.
		 *
		 * @param origin - The origin {@link BlockPos}
		 * @param distance - The distance to seek
		 * @param direction - The direction(s) to seek in
		 * @return All {@link BlockPos} orthogonal to the origin in each of the given {@link Direction} all within the passed distance.
		 */
		public static Iterable<BlockPos> allOrthogonalTo(BlockPos origin, int distance, Direction... directions) {
			ImmutableList.Builder<BlockPos> builder = new ImmutableList.Builder<>();
			for (Direction iterated : directions) {
				Game.appendOrthogonal(builder, origin, iterated, distance);
			}
			return builder.build();
		}

		/**
		 * Returns a {@link Stream} of all {@link BlockPos} orthogonal to the passed origin {@link BlockPos} in the given {@link Direction} within the given distance.
		 * Merely calls {@link Streams#stream(Iterable)} using {@link #allOrthogonalTo(BlockPos, int, Direction)}.
		 *
		 * @param origin - The origin {@link BlockPos}
		 * @param distance - The distance to seek
		 * @param direction - The direction to seek in
		 * @return All {@link BlockPos} orthogonal to the origin in {@link Direction} within the passed distance.
		 * @see #allOrthogonalTo(BlockPos, int, Direction)
		 */
		public static Stream<BlockPos> streamOrthogonalTo(BlockPos origin, int distance, Direction direction) {
			return Streams.stream(Game.allOrthogonalTo(origin, distance, direction));
		}

		/**
		 * Returns an {@link Iterable} over all blocks orthogonal to the passed origin {@link BlockPos} in the given {@link Direction} within the given distance.
		 * The returned {@link Iterable} is immutable.
		 *
		 * @param origin - The origin {@link BlockPos}
		 * @param distance - The distance to seek
		 * @param direction - The direction to seek in
		 * @return All {@link BlockPos} orthogonal to the origin in {@link Direction} within the passed distance.
		 */
		public static Iterable<BlockPos> allOrthogonalTo(BlockPos origin, int distance, Direction direction) {
			ImmutableList.Builder<BlockPos> builder = new ImmutableList.Builder<>();
			Game.appendOrthogonal(builder, origin, direction, distance);
			return builder.build();
		}

		/**
		 * Returns a {@link Stream} of all orthogonal neighbors of the passed {@link BlockPos}.
		 *
		 * @param position - The origin {@link BlockPos}
		 * @return A {@link Stream} of all orthogonal neighbors of the origin.
		 */
		public static Stream<BlockPos> streamOrthogonalNeighbors(BlockPos origin) {
			return Stream.of(origin.up(), origin.down(), origin.north(), origin.south(), origin.east(), origin.west());
		}
		
		/**
		 * Returns a {@link Stream} of all horizontal neighbors of the passed {@link BlockPos}.
		 * <p>
		 * Iteration order is NORTH, EAST, SOUTH, WEST.
		 * 
		 * @param position - The origin {@link BlockPos}
		 * @return All orthogonal neighbors of the origin.
		 */
		public static Stream<BlockPos> streamHorizontalNeighbors(BlockPos position) {
			return Stream.of(position.north(), position.east(), position.south(), position.west());
		}
		
		/**
		 * Returns a {@link Stream} of all horizontal neighbors of the passed {@link BlockPos}.
		 * <p>
		 * Iteration order is NORTH, EAST, SOUTH, WEST.
		 * 
		 * @param position - The origin {@link BlockPos}
		 * @return All orthogonal neighbors of the origin.
		 */
		public static Iterable<BlockPos> horizontalNeighbors(BlockPos position) {
			ImmutableList.Builder<BlockPos> builder = new ImmutableList.Builder<>();
			builder.add(position.north(), position.east(), position.south(), position.west());
			return builder.build();
		}

		/**
		 * Returns an {@link Iterable} over all orthogonal neighbors of the passed {@link BlockPos}.
		 * Iteration order is UP, DOWN, NORTH, SOUTH, EAST, WEST.
		 * The returned {@link Iterable} is immutable.
		 *
		 * @param position - The origin {@link BlockPos}
		 * @return All orthogonal neighbors of the origin.
		 */
		public static Iterable<BlockPos> orthogonalNeighborsOf(BlockPos position) {
			ImmutableList.Builder<BlockPos> builder = new ImmutableList.Builder<>();
			builder.add(position.up(), position.down(), position.north(), position.south(), position.east(), position.west());
			return builder.build();
		}

		/**
		 * Streams all {@link BlockPos} in a horizontal cubic radius from the provided origin point.
		 *
		 * @param origin - The origin point
		 * @param radius - The radius, in blocks, within which {@link BlockPos} should be returned
		 * @return A {@link Stream} over all {@link BlockPos} at most {@code radius} from the origin
		 */
		public static Stream<BlockPos> streamHorizontalRadius(BlockPos origin, int radius) {
			return BlockPos.getAllInBox(origin.getX() - radius, origin.getY(), origin.getZ() - radius, origin.getX() + radius, origin.getY(), origin.getZ() + radius);
		}

		/**
		 * Calls {@link #spawnItemNear(World, ItemStack, double, double, double, Random, double)} for each item in the passed {@link Iterable},
		 * starting at the "block center" of the passed position.
		 *
		 * @param world - The world to spawn the {@link ItemEntity} in
		 * @param position - The base coordinate to spawn at
		 * @param items - The items to spawn.
		 * @param RNG - The {@link Random} instance to use
		 * @param spread - The maximum offset from the passed position
		 */
		public static void spawnItemsNear(IWorld world, Vector3i position, Iterable<ItemStack> items, Random RNG, double spread) {
			Game.spawnItemsNear(world, position.getX() + 0.5D, position.getY() + 0.5D, position.getZ() + 0.5D, items, RNG, spread);
		}

		/**
		 * Calls {@link #spawnItem(World, ItemStack, double, double, double)} at the passed position, offset by 0.5D for each coordinate.
		 * This is useful for spawning {@link ItemEntity} at the precise center of a block.
		 *
		 * @param world - The world to spawn the {@link ItemEntity} in
		 * @param position - The base coordinate to spawn at
		 * @param items - The items to spawn.
		 */
		public static void spawnItems(IWorld world, Vector3i position, Iterable<ItemStack> items) {
			Game.spawnItems(world, position.getX() + 0.5D, position.getY() + 0.5D, position.getZ() + 0.5D, items);
		}

		/**
		 * Calls {@link #spawnItemsNear(World, double, double, double, Iterable, Random, double)} for each item in the passed {@link Iterable}.
		 *
		 * @param world - The world to spawn the {@link ItemEntity} in
		 * @param item - The {@link ItemStack} that the {@link ItemEntity} should contain
		 * @param xPos - The X coordinate to spawn at
		 * @param yPos - The Y coordinate to spawn at
		 * @param zPos - The Z coordinate to spawn at
		 * @param items - The items to spawn
		 * @param RNG - The {@link Random} instance to use
		 * @param spread - The maximum offset from the passed position
		 */
		public static void spawnItemsNear(IWorld world, double xPos, double yPos, double zPos, Iterable<ItemStack> items, Random RNG, double spread) {
			items.forEach(item -> Game.spawnItemNear(world, item, xPos, yPos, zPos, RNG, spread));
		}

		/**
		 * Calls {@link #spawnItem(World, ItemStack, double, double, double)} for each item in the passed {@link Iterable}.
		 *
		 * @param world - The world to spawn the {@link ItemEntity} in
		 * @param item - The {@link ItemStack} that the {@link ItemEntity} should contain
		 * @param xPos - The X coordinate to spawn at
		 * @param yPos - The Y coordinate to spawn at
		 * @param zPos - The Z coordinate to spawn at
		 * @param items - The items to spawn
		 * @see #spawnItem(World, ItemStack, double, double, double)
		 */
		public static void spawnItems(IWorld world, double xPos, double yPos, double zPos, Iterable<ItemStack> items) {
			items.forEach(item -> Game.spawnItem(world, item, xPos, yPos, zPos));
		}

		/**
		 * Calls {@link #spawnItem(World, ItemStack, double, double, double)} with a slightly-randomized offset for each coordinate.
		 *
		 * @param world - The world to spawn the {@link ItemEntity} in
		 * @param item - The {@link ItemStack} that the {@link ItemEntity} should contain
		 * @param xPos - The X coordinate to spawn at
		 * @param yPos - The Y coordinate to spawn at
		 * @param zPos - The Z coordinate to spawn at
		 * @param item - The items to spawn
		 * @param RNG - The {@link Random} instance to use
		 * @param spread - The maximum offset from the passed position
		 * @return The created {@link ItemEntity} or {@code null}, as described by {@link #spawnItem(World, ItemStack, double, double, double)}.
		 * @see #spawnItem(World, ItemStack, double, double, double)
		 */
		public static @Nullable ItemEntity spawnItemNear(IWorld world, @Nullable ItemStack item, double xPos, double yPos, double zPos, Random RNG, double spread) {
			return Game.spawnItem(world, item, xPos + Game.getOffset(RNG, spread), yPos + Game.getOffset(RNG, spread), zPos + Game.getOffset(RNG, spread));
		}

		/**
		 * Calls {@link #spawnItem(World, ItemStack, double, double, double)} with a slightly-randomized offset for each coordinate.
		 *
		 * @param world - The world to spawn the {@link ItemEntity} in
		 * @param item - The {@link ItemStack} that the {@link ItemEntity} should contain
		 * @param position - The coordinates to spawn at
		 * @param item - The items to spawn
		 * @param RNG - The {@link Random} instance to use
		 * @param spread - The maximum offset from the passed position
		 * @return The created {@link ItemEntity} or {@code null}, as described by {@link #spawnItem(World, ItemStack, double, double, double)}.
		 * @see #spawnItem(World, ItemStack, double, double, double)
		 */
		public static @Nullable ItemEntity spawnItemNear(IWorld world, @Nullable ItemStack item, Vector3i position, Random RNG, double spread) {
			return Game.spawnItem(world, item, position.getX() + 0.5D + Game.getOffset(RNG, spread), position.getY() + 0.5D + Game.getOffset(RNG, spread), position.getZ() + 0.5D + Game.getOffset(RNG, spread));
		}

		/**
		 * Spawns the passed {@link ItemStack} in the world as an {@link ItemEntity} at the indicated coordinates and with the default item pickup delay (10 ticks).
		 * If the {@link ItemStack} is {@code null} or returns {@code true} for {@link ItemStack#isEmpty()}, this method will perform no action and return {@code null}.
		 * Otherwise, it will return the created {@link ItemEntity}.
		 *
		 * @param world - The world to spawn the {@link ItemEntity} in
		 * @param item - The {@link ItemStack} that the {@link ItemEntity} should contain
		 * @param xPos - The X coordinate to spawn at
		 * @param yPos - The Y coordinate to spawn at
		 * @param zPos - The Z coordinate to spawn at
		 * @return The created {@link ItemEntity} or {@code null}, as described above.
		 * @see #spawnItem(World, ItemStack, double, double, double, int)
		 */
		public static @Nullable ItemEntity spawnItem(IWorld world, @Nullable ItemStack item, double xPos, double yPos, double zPos) {
			return Game.spawnItem(world, item, xPos, yPos, zPos, 10);
		}

		/**
		 * Spawns the passed {@link ItemStack} in the world as an {@link ItemEntity} at the indicated coordinates and with the passed pickup delay (in ticks).
		 * If the {@link ItemStack} is {@code null} or returns {@code true} for {@link ItemStack#isEmpty()}, this method will perform no action and return {@code null}.
		 * Otherwise, it will return the created {@link ItemEntity}.
		 *
		 * @param world - The world to spawn the {@link ItemEntity} in
		 * @param item - The {@link ItemStack} that the {@link ItemEntity} should contain
		 * @param xPos - The X coordinate to spawn at
		 * @param yPos - The Y coordinate to spawn at
		 * @param zPos - The Z coordinate to spawn at
		 * @param pickupDelay - The delay before the created {@link ItemEntity} can be picked up
		 * @return The created {@link ItemEntity} or {@code null}, as described above.
		 */
		public static @Nullable ItemEntity spawnItem(IWorld world, @Nullable ItemStack item, double xPos, double yPos, double zPos, int pickupDelay) {
			if (Game.stackValid(item)) {
				ItemEntity instance = new ItemEntity((World) world, xPos, yPos, zPos, item);
				instance.setPickupDelay(pickupDelay);
				world.addEntity(instance);
				return instance;
			}
			return null;
		}

		/**
		 * Returns whether the passed {@link ItemStack} reference is valid.
		 * Returns {@code true} if the {@link ItemStack} is non-{@code null} and {@link ItemStack#isEmpty()} returns {@code false}.
		 *
		 * @param stack - The stack to examine
		 * @return Whether the {@link ItemStack} is valid.
		 */
		public static boolean stackValid(@Nullable ItemStack stack) {
			return Objects.nonNull(stack) && !stack.isEmpty();
		}

		/* Internal Methods */

		protected static void appendOrthogonal(ImmutableList.Builder<BlockPos> list, BlockPos origin, Direction direction, int distance) {
			for (int count = 1; count <= distance; count += 1) {
				list.add(origin.offset(direction, count));
			}
		}

		protected static double getOffset(Random RNG, double spread) {
			return spread - (RNG.nextDouble() * 2.0D * spread);
		}

	}

	public static final class Tags {

		private Tags() {}

		/**
		 * Creates a {@link String} suitable for use as an {@link INamedTag} name.
		 *
		 * @param domain - The domain of the tag
		 * @param path - The tag's "path".
		 * @return A suitably-constructed tag string.
		 */
		public static String createName(String domain, String... path) {
			StringBuilder builder = new StringBuilder();
			builder.append(domain);
			builder.append(Strings.DELIMITER_RESOURCE);
			Strings.concatenateUsing(builder, Strings.DELIMITER_PATH, path);
			return builder.toString();
		}
		
		/**
		 * Convenience method to create a Forge-domain "ingot" tag.
		 * <p>
		 * The resulting tag will be in {@code data.forge.tags.items.ingot}.
		 * 
		 * @param type - The type of ingot
		 * @return A suitable {@link INamedTag}.
		 */
		public static INamedTag<Item> createIngotTag(String type) {
			return Tags.forgeItemTag("ingot", type);
		}
		
		/**
		 * Convenience method to create a Forge-domain "nugget" tag.
		 * <p>
		 * The resulting tag will be in {@code data.forge.tags.items.nugget}.
		 * 
		 * @param type - The type of ingot
		 * @return A suitable {@link INamedTag}.
		 */
		public static INamedTag<Item> createNuggetTag(String type) {
			return Tags.forgeItemTag("nugget", type);
		}
		
		/**
		 * Convenience method to create a Forge-domain "storage_blocks" tag.
		 * <p>
		 * The resulting tag will be in {@code data.forge.tags.items.block}.
		 * 
		 * @param type - The type of ingot
		 * @return A suitable {@link INamedTag}.
		 */
		public static INamedTag<Item> createBlockTag(String type) {
			return Tags.forgeItemTag("storage_blocks", type);
		}
		
		/**
		 * Convenience method to create a Forge-domain "ore" tag.
		 * <p>
		 * The resulting tag will be in {@code data.forge.tags.items.ore}.
		 * 
		 * @param type - The type of ingot
		 * @return A suitable {@link INamedTag}.
		 */
		public static INamedTag<Item> createOreTag(String type) {
			return Tags.forgeItemTag("ore", type);
		}

		/**
		 * Gets or creates an {@link INamedTag} in the Minecraft Forge domain with the passed "path".
		 *
		 * @param path - The {@link Item} tag "path" to use
		 * @return The existing or created {@link INamedTag}.
		 */
		public static INamedTag<Item> forgeItemTag(String... path) {
			return Tags.itemTag(Strings.DOMAIN_FORGE, path);
		}

		/**
		 * Gets or creates an {@link INamedTag} in the Minecraft Forge domain with the passed "path".
		 *
		 * @param path - The {@link Block} tag "path" to use
		 * @return The existing or created {@link INamedTag}.
		 */
		public static INamedTag<Block> forgeBlockTag(String... path) {
			return Tags.blockTag(Strings.DOMAIN_FORGE, path);
		}

		/**
		 * Gets or creates an {@link INamedTag} in the indicated domain, with the passed "path".
		 *
		 * @param domain - The domain to use
		 * @param path - The {@link Item} tag "path" to use
		 * @return The existing or created {@link INamedTag}.
		 */
		public static INamedTag<Item> itemTag(String domain, String... path) {
			return Tags.createTag(ItemTags::makeWrapperTag, domain, path);
		}

		/**
		 * Gets or creates an {@link INamedTag} in the indicated domain, with the passed "path".
		 *
		 * @param domain - The domain to use
		 * @param path - The {@link Block} tag "path" to use
		 * @return The existing or created {@link INamedTag}.
		 */
		public static INamedTag<Block> blockTag(String domain, String... path) {
			return Tags.createTag(BlockTags::makeWrapperTag, domain, path);
		}

		/**
		 * Gets or creates an {@link INamedTag} in the indicated domain using the passed transformer function and tag "path".
		 *
		 * @param <T> The registry type of the desired {@link INamedTag}
		 * @param transformer - The transformer function (See {@link ItemTags#makeWrapperTag(String)} and {@link BlockTags#makeWrapperTag(String)})
		 * @param domain - The domain to use
		 * @param path - The {@link Block} tag "path" to use
		 * @see {@link ItemTags#makeWrapperTag(String)}, {@link BlockTags#makeWrapperTag(String)}
		 * @return
		 */
		public static <T extends IForgeRegistryEntry<T>> INamedTag<T> createTag(Function<String, INamedTag<T>> transformer, String domain, String... path) {
			return transformer.apply(Tags.createName(domain, path));
		}

	}

	public static final class Strings {

		/* Constants */
		public static final String DOMAIN_FORGE = "forge";
		public static final String DOMAIN_MINECRAFT = "minecraft";
		public static final String FILE_SUFFIX_PNG = "png";

		public static final String EMPTY = "";
		public static final String[] EMPTY_ARRAY = {};
		public static final String DELIMITER_PATH = "/";
		public static final String DELIMITER_NAME = "_";
		public static final String DELIMITER_TYPE = ".";
		public static final String DELIMITER_RESOURCE = ":";

		private Strings() {}

		/**
		 * Converts the passed {@link String} path into a {@link ResourceLocation} in the {@value #DOMAIN_MINECRAFT} namespace.
		 *
		 * @param path - The resource path to use
		 * @return A {@link ResourceLocation} in the {@value #DOMAIN_MINECRAFT} namespace.
		 */
		public static @Nonnull ResourceLocation minecraftResource(@Nonnull String path) {
			return new ResourceLocation(DOMAIN_MINECRAFT, path);
		}

		/**
		 * Splits the passed {@link String} around occurrences of one or more whitespace characters,
		 * returning the resulting array.
		 * If the {@link String} received by this method is null or empty, an empty array will be returned instead.
		 *
		 * @param input - The {@link String} to split
		 * @return A suitably-split {@link String} array, or an empty one.
		 */
		public static @Nonnull String[] splitByWhitespace(@Nullable String input) {
			return isNullOrEmpty(input) ? EMPTY_ARRAY : input.trim().split("\\s+");
		}

		/**
		 * Returns whether the passed {@link String} is null or empty.
		 *
		 * @param input - The {@link String} to check
		 * @return Whether the {@link String} is null or empty.
		 */
		public static boolean isNullOrEmpty(@Nullable String input) {
			return Objects.isNull(input) ? true : input.isEmpty();
		}

		/**
		 * Concatenates the {@link String} in the passed {@link Iterable} with {@link #DELIMITER_PATH} betwixt.
		 *
		 * @param elements - The {@link String}s to concatenate
		 * @return A suitably-concatenated {@link String}.
		 */
		public static String path(Iterable<String> elements) {
			return Strings.concatenate(DELIMITER_PATH, elements);
		}

		/**
		 * Concatenates the passed {@link String}s with {@link #DELIMITER_PATH} betwixt.
		 *
		 * @param elements - The {@link String}s to concatenate
		 * @return A suitably-concatenated {@link String}.
		 */
		public static String path(String... elements) {
			return Strings.concatenate(DELIMITER_PATH, elements);
		}

		/**
		 * Concatenates the passed {@link String}s with {@link #DELIMITER_PATH} betwixt, and appends the {@value #FILE_SUFFIX_PNG} file suffix to the end.
		 * <p>
		 * Suitable for use as an explicit path to a texture resource.
		 *
		 * @param elements - The {@link String}s to concatenate
		 * @return A suitably-concatenated {@link String}.
		 */
		public static String texturePath(Iterable<String> elements) {
			return Strings.filePath(FILE_SUFFIX_PNG, elements);
		}

		/**
		 * Concatenates the passed {@link String}s with {@link #DELIMITER_PATH} betwixt, and appends the {@value #FILE_SUFFIX_PNG} file suffix to the end.
		 * <p>
		 * Suitable for use as an explicit path to a texture resource.
		 *
		 * @param elements - The {@link String}s to concatenate
		 * @return A suitably-concatenated {@link String}.
		 */
		public static String texturePath(String... elements) {
			return Strings.filePath(FILE_SUFFIX_PNG, elements);
		}

		/**
		 * Concatenates the passed {@link String}s with {@link #DELIMITER_PATH} betwixt, and appends the file suffix to the end.
		 * <p>
		 * Suitable for use as an explicit path to a file asset.
		 *
		 * @param suffix - The file suffix to append
		 * @param elements - The {@link String}s to concatenate
		 * @return A suitably-concatenated {@link String}.
		 */
		public static String filePath(String suffix, Iterable<String> elements) {
			StringBuilder builder = new StringBuilder();
			Strings.concatenateUsing(builder, DELIMITER_PATH, elements);
			builder.append(DELIMITER_TYPE).append(FILE_SUFFIX_PNG);
			return builder.toString();
		}

		/**
		 * Concatenates the passed {@link String}s with {@link #DELIMITER_PATH} betwixt, and appends the file suffix to the end.
		 * <p>
		 * Suitable for use as an explicit path to a file asset.
		 *
		 * @param suffix - The file suffix to append
		 * @param elements - The {@link String}s to concatenate
		 * @return A suitably-concatenated {@link String}.
		 */
		public static String filePath(String suffix, String... elements) {
			StringBuilder builder = new StringBuilder();
			Strings.concatenateUsing(builder, DELIMITER_PATH, elements);
			builder.append(DELIMITER_TYPE).append(FILE_SUFFIX_PNG);
			return builder.toString();
		}

		/**
		 * Concatenates the {@link String} in the passed {@link Iterable} with {@link #DELIMITER_NAME} betwixt.
		 *
		 * @param elements - The {@link String}s to concatenate
		 * @return A suitably-concatenated {@link String}.
		 */
		public static String name(Iterable<String> elements) {
			return Strings.concatenate(DELIMITER_NAME, elements);
		}

		/**
		 * Concatenates the passed {@link String}s with {@link #DELIMITER_NAME} betwixt.
		 *
		 * @param elements - The {@link String}s to concatenate
		 * @return A suitably-concatenated {@link String}.
		 */
		public static String name(String... elements) {
			return Strings.concatenate(DELIMITER_NAME, elements);
		}

		/**
		 * Concatenates the {@link String} in the passed {@link Iterable} with {@link #DELIMITER_TYPE} betwixt.
		 *
		 * @param elements - The {@link String}s to concatenate
		 * @return A suitably-concatenated {@link String}.
		 */
		public static String type(Iterable<String> elements) {
			return Strings.concatenate(DELIMITER_TYPE, elements);
		}

		/**
		 * Concatenates the passed {@link String}s with {@link #DELIMITER_TYPE} betwixt.
		 *
		 * @param elements - The {@link String}s to concatenate
		 * @return A suitably-concatenated {@link String}.
		 */
		public static String type(String... elements) {
			return Strings.concatenate(DELIMITER_TYPE, elements);
		}

		/**
		 * Concatenates the passed String array with the passed delimiter placed betwixt each entry.
		 *
		 * @param delimiter - The String to place between each String in {@code elements}
		 * @param elements - The Strings to concatenate
		 * @return A suitably-concatenated String.
		 */
		public static String concatenate(String delimiter, Iterable<String> elements) {
			StringBuilder builder = new StringBuilder();
			Strings.concatenateUsing(builder, delimiter, elements);
			return builder.toString();
		}

		/**
		 * Concatenates the passed String varargs array with the passed delimiter placed betwixt each entry.
		 *
		 * @param delimiter - The String to place between each String in {@code elements}
		 * @param elements - The array of Strings to concatenate
		 * @return A suitably-concatenated String.
		 */
		public static String concatenate(String delimiter, String... elements) {
			StringBuilder builder = new StringBuilder();
			Strings.concatenateUsing(builder, delimiter, elements);
			return builder.toString();
		}

		/**
		 * Appends each {@link String} in the passed {@link Iterable} to the passed {@link StringBuilder}, with the passed delimiter placed betwixt each entry.
		 *
		 * @param builder - The {@link StringBuilder} instance to use
		 * @param delimiter - The String to place between each String in {@code elements}
		 */
		public static void concatenateUsing(StringBuilder builder, String delimiter, Iterable<String> elements) {
			Iterator<String> iterator = elements.iterator();
			while (iterator.hasNext()) {
				builder.append(iterator.next());
				if (iterator.hasNext()) {
					builder.append(delimiter);
				}
			}
		}

		/**
		 * Appends the passed {@link String} varargs array to the passed {@link StringBuilder}, with the passed delimiter placed betwixt each entry.
		 *
		 * @param builder - The {@link StringBuilder} instance to use
		 * @param delimiter - The String to place between each String in {@code elements}
		 */
		public static void concatenateUsing(StringBuilder builder, String delimiter, String... elements) {
			for (int index = 0; index < elements.length; index += 1) {
				builder.append(elements[index]);
				if (index < (elements.length - 1)) {
					builder.append(delimiter);
				}
			}
		}

	}

	/**
	 * Utility class for various UI-related utilities.
	 * <p>
	 * Not all methods in this class are client-only.
	 *
	 * @author Malcolm Riley
	 */
	public static class UI {

		private UI() {}

		/**
		 * Helper method to open the UI for a named-{@link Container}-providing {@link TileEntity} supposedly at
		 * the indicated {@link BlockPos}.
		 * <p>
		 * If there is a {@link TileEntity} at the indicated {@link BlockPos} and it implements {@link INamedContainerProvider}, and furthermore if the passed
		 * {@link PlayerEntity} is also be an instance of {@link ServerPlayerEntity}, then {@link NetworkHooks#openGui(ServerPlayerEntity, INamedContainerProvider, BlockPos)}
		 * is called and the method returns {@code true}.
		 * In all other cases, the method returns {@code false}.
		 *
		 * @param player - The player opening the UI
		 * @param world - The {@link World} containing the {@link TileEntity}
		 * @param position - The {@link BlockPos} of the {@link TileEntity}
		 * @return {@code true} if an attempt was made to open the UI, and {@code false} otherwise.
		 */
		public static boolean openUIFor(PlayerEntity player, World world, BlockPos position) {
			final TileEntity discovered = world.getTileEntity(position);
			if ((discovered instanceof INamedContainerProvider) && (player instanceof ServerPlayerEntity)) {
				final ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
				final INamedContainerProvider provider = (INamedContainerProvider) discovered;
				NetworkHooks.openGui(serverPlayer, provider, position);
				return true;
			}
			return false;
		}
		
		/**
		 * Helper method that can be used as a stand-in for {@link Block#onBlockActivated(BlockState, World, BlockPos, PlayerEntity, net.minecraft.util.Hand, net.minecraft.util.math.BlockRayTraceResult)}.
		 * <p>
		 * If the provided {@link World} is not remote, calls {@link #openUIFor(PlayerEntity, World, BlockPos)}, returning {@link ActionResultType#SUCCESS} for a successful UI opening and {@link ActionResultType#CONSUME} for failure.
		 * 
		 * @see #openUIFor(PlayerEntity, World, BlockPos)
		 * @param player - The player opening the UI
		 * @param world - The {@link World} containing the {@link TileEntity}
		 * @param position - The {@link BlockPos} of the {@link TileEntity}
		 * @return {@link ActionResultType#SUCCESS} if the UI was opened, {@link ActionResultType#CONSUME} if the UI was not, and {@link ActionResultType#SUCCESS} on the standalone client always.
		 */
		public static ActionResultType openBlockUIFor(PlayerEntity player, World world, BlockPos position) {
			if (!world.isRemote()) {
				return UI.openUIFor(player, world, position) ? ActionResultType.SUCCESS : ActionResultType.CONSUME;
			}
			return ActionResultType.func_233537_a_(world.isRemote());
		}

	}

}
