package paragon.minecraft.library;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public abstract class TEBase extends TileEntity {

	public TEBase(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	/* Supertype Override Methods */

	/**
	 * Overridden here because Forge adds additional data to {@link TileEntity} instances in order to support
	 * {@link Capability}, and it would be problematic to forget that.
	 * {@inheritDoc}
	 */
	@Override
	public final void read(final BlockState state, final CompoundNBT toRead) {
		super.read(state, toRead);
		this.readData(state, toRead);
	}

	/**
	 * Overridden here because Forge adds additional data to {@link TileEntity} instances in order to support
	 * {@link Capability}, and it would be problematic to forget that.
	 * {@inheritDoc}
	 */
	@Override
	public final CompoundNBT write(final CompoundNBT toWrite) {
		CompoundNBT compound = super.write(toWrite);
		this.writeData(compound);
		return compound;
	}

	/* Abstract Methods */

	/**
	 * This method should read the provided {@link CompoundNBT} data into the state of this {@link TileEntity}.
	 *
	 * @param state - The companion {@link BlockState} to this {@link TileEntity} instance
	 * @param compound - The state data source
	 */
	protected abstract void readData(final BlockState state, final CompoundNBT compound);

	/**
	 * This method should write state data of this {@link TileEntity} into the provided {@link CompoundNBT}.
	 *
	 * @param compound - The destination for this {@link TileEntity} state data.
	 */
	protected abstract void writeData(final CompoundNBT compound);

}
