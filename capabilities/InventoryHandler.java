package paragon.minecraft.library.capabilities;

import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import paragon.minecraft.library.Utilities;

/**
 * Specialized {@link ItemStackHandler} that publicly exposes its internal {@link NonNullList} of {@link ItemStack}.
 *
 * @author Malcolm Riley
 */
public class InventoryHandler extends ItemStackHandler {

	/* Internal Fields */
	protected final TileEntity HOLDER;

	/* Constants */
	private static final String TAG_NAME = "ItemInventory";

	public InventoryHandler(int inventorySize, TileEntity holder) {
		super(inventorySize);
		this.HOLDER = holder;
	}

	/* Public Methods */

	/**
	 * Utility method for dropping the {@link ItemStack} contents of an {@link InventoryHandler} (or an {@link ItemStackHandler}) if the {@link BlockState} at the position of the
	 * host {@link TileEntity} has changed to a non-matching state.
	 * 
	 * @param originalState - The original {@link BlockState}
	 * @param world - A reference to the {@link World} containing the {@link BlockState}
	 * @param position - The {@link BlockPos} of the changed {@link BlockState}
	 * @param newState - The new {@link BlockState}.
	 */
	public static void dropContents(BlockState originalState, World world, BlockPos position, BlockState newState) {
		if (originalState.hasTileEntity() && !newState.isIn(originalState.getBlock())) {
			TileEntity discovered = world.getTileEntity(position);
			if (Objects.nonNull(discovered)) {
				discovered.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(handler -> {
					for (int index = 0; index < handler.getSlots(); index += 1) {
						Utilities.Game.spawnItemNear(world, handler.getStackInSlot(index), position, ThreadLocalRandom.current(), 0.12D);
					}
				});
			}
		}
	}
	
	/**
	 * Returns a {@link Stream} over all {@link ItemStack} contained by this {@link InventoryHandler}.
	 * 
	 * @return All {@link ItemStack} contained by this {@link InventoryHandler}.
	 */
	public Stream<ItemStack> streamItems() {
		return this.stacks.stream();
	}
	
	/**
	 * Performs the provided operation on each non-empty stack contained by this {@link InventoryHandler}.
	 * 
	 * @param stackOperation - The operation to perform
	 */
	public void forNonEmpty(Consumer<ItemStack> stackOperation) {
		for(int index = 0; index < this.stacks.size() ; index += 1) {
			ItemStack iterated = this.getStackInSlot(index);
			if (!iterated.isEmpty()) {
				stackOperation.accept(iterated);
			}
		}
	}

	/**
	 * Tries to insert the provided {@link ItemStack} into the first available space in this {@link InventoryHandler}, returning the overflow (or {@link ItemStack#EMPTY} if none remains).
	 *
	 * @param inputStack - The {@link ItemStack} to attempt to insert
	 * @return The remainder after attempting to insert, or {@link ItemStack#EMPTY} if no overflow results.
	 */
	public ItemStack tryInsert(final ItemStack inputStack) {
		ItemStack remainder = inputStack;
		for (int index = 0; (index < this.stacks.size()) && !remainder.isEmpty(); index += 1) {
			if (this.isItemValid(index, inputStack)) {
				remainder = this.insertItem(index, remainder, false);
			}
		}
		return remainder;
	}

	/**
	 * Sorts the internal {@link NonNullList} of {@link ItemStack} using the provided {@link Comparator}.
	 *
	 * @param comparisonFunction - The {@link Comparator} to use for sorting.
	 */
	public void sortUsing(Comparator<? super ItemStack> comparisonFunction) {
		this.stacks.sort(comparisonFunction);
	}

	/**
	 * Returns whether this {@link InventoryHandler} currently holds any {@link ItemStack}.
	 *
	 * @return Whether this {@link InventoryHandler} is empty.
	 */
	public boolean isEmpty() {
		return this.stacks.isEmpty();
	}

	/**
	 * Returns a copy of the {@link NonNullList} of {@link ItemStack} backing this {@link InventoryHandler}.
	 *
	 * @return A copy of the {@link NonNullList} of {@link ItemStack}s this {@link InventoryHandler} manages.
	 */
	public NonNullList<ItemStack> copyItems() {
		NonNullList<ItemStack> clone = NonNullList.create();
		this.stacks.forEach(stack -> clone.add(stack));
		return clone;
	}

	/**
	 * Returns the {@link NonNullList} of {@link ItemStack} backing this {@link InventoryHandler}.
	 * <p>
	 * Care should be taken when using this method, and usage of {@link #copyItems()} should be preferred.
	 *
	 * @return The {@link NonNullList} of {@link ItemStack}s this {@link InventoryHandler} manages.
	 */
	public NonNullList<ItemStack> getItems() {
		return this.stacks;
	}

	/**
	 * Writes this {@link InventoryHandler}'s internal data to the provided {@link CompoundNBT}
	 * <p>
	 * The {@link ItemStack} list will be written as a {@link CompoundNBT} field to the passed {@link CompoundNBT}, keyed to {@value InventoryHandler#TAG_NAME}.
	 *
	 * @param compound - The compound to write to
	 */
	public void writeTo(@Nonnull CompoundNBT compound) {
		compound.put(TAG_NAME, this.serializeNBT());
	}

	/**
	 * Attempts to read an {@link ItemStack} list from the passed {@link CompoundNBT} into this {@link InventoryHandler}, overwriting the existing contents.
	 * <p>
	 * The method will attempt to read a {@link CompoundNBT} field from he passed {@link CompoundNBT}, keyed to {@value InventoryHandler#TAG_NAME}.
	 *
	 * @param compound - The compound to read from
	 */
	public void readFrom(@Nonnull CompoundNBT compound) {
		if (compound.contains(TAG_NAME, Constants.NBT.TAG_COMPOUND)) {
			this.deserializeNBT(compound.getCompound(TAG_NAME));
		}
	}

	/* Supertype Override Methods */

	@Override
	protected void onContentsChanged(int slot) {
		super.onContentsChanged(slot);
		this.HOLDER.markDirty();
	}

}