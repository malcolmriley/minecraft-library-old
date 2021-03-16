package paragon.minecraft.library.capabilities;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemStackHandler;

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
	 * Tries to insert the provided {@link ItemStack} into the first available space in this {@link InventoryHandler}, returning the overflow (or {@link ItemStack#EMPTY} if none remains).
	 * 
	 * @param inputStack - The {@link ItemStack} to attempt to insert
	 * @return The remainder after attempting to insert, or {@link ItemStack#EMPTY} if no overflow results.
	 */
	public ItemStack tryInsert(final ItemStack inputStack) {
		ItemStack remainder = inputStack;
		for (int index = 0; index < this.stacks.size() && !remainder.isEmpty(); index += 1) {
			if (this.isItemValid(index, inputStack)) {
				remainder = this.insertItem(index, remainder, false);
			}
		}
		return remainder;
	}

	/**
	 * Returns the {@link NonNullList} of {@link ItemStack} backing this {@link InventoryHandler}.
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