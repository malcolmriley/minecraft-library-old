package paragon.minecraft.library.client.ui;

import java.util.Objects;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

/**
 * Extended {@link AbstractContainerMenu} implementation with some convenience methods.
 *
 * @author Malcolm Riley
 */
public abstract class SimpleMenu extends AbstractContainerMenu {

	/* Constants */

	/**
	 * The default size, in magic UI-units, of an Item inventory slot.
	 */
	protected static final int DEFAULT_SLOT_SIZE = 16;
	/**
	 * The default separation, in magic UI-units, between elements of a grid of Item inventory slots.
	 */
	protected static final int DEFAULT_SLOT_SEPARATION = 2;
	/**
	 * The default separation, in magic UI-units, between two labeled "large field" elements - such as the inventory of a chest, and the player's inventory.
	 * <p>
	 * Generally, enough space to fit a textual element between.
	 */
	protected static final int DEFAULT_OFFSET_LARGE = 14;
	/**
	 * The default separation, in magic UI-units, between two unlabeled "large field" elements - such as the player's main inventory, and their hotbar.
	 */
	protected static final int DEFAULT_OFFSET_SMALL = 4;
	/**
	 * The default width of the player hotbar.
	 */
	protected static final int PLAYER_HOTBAR_WIDTH = 9;
	/**
	 * The default height of the player hotbar.
	 */
	protected static final int PLAYER_HOTBAR_HEIGHT = 1;
	/**
	 * The default width of the player inventory.
	 */
	protected static final int PLAYER_INVENTORY_WIDTH = 9;
	/**
	 * The default height of the player inventory.
	 */
	protected static final int PLAYER_INVENTORY_HEIGHT = 3;

	protected SimpleMenu(MenuType<?> type, int id) {
		super(type, id);
	}

	protected SimpleMenu(RegistryObject<MenuType<?>> type, int id) {
		this(type.get(), id);
	}

	/* Supertype Override Methods */

	/**
	 * {@inheritDoc}
	 * <p>
	 * {@link SimpleMenu#quickMoveStack(Player, int)} is a basic implementation of shift-click response that takes care of some boilerplate, namely the case when an empty {@link Slot} is called for transfer.
	 * If the provided index leads to a null or empty source {@link Slot}, then {@link ItemStack#EMPTY} is returned.
	 * <p>
	 * If not, it is assumed that the {@link ItemStack} in the source {@link Slot} is a valid shift-click transfer source, and the method delegates to {@link #onStackTransfer(Player, int, ItemStack)}.
	 * <p>
	 * Implementors should prefer overriding {@link #onStackTransfer(Player, int, ItemStack)} to this method.
	 */
	@Override
	public ItemStack quickMoveStack(Player player, int sourceSlotIndex) {
		final Slot sourceSlot = this.getSlot(sourceSlotIndex);
		if (Objects.isNull(sourceSlot) || !sourceSlot.hasItem()) {
			return ItemStack.EMPTY;
		}
		return this.onStackTransfer(player, sourceSlotIndex, sourceSlot.getItem());
	}

	/* Subtype Helper Methods */

	/**
	 * This method handles the behavior wherein a player has requested a shift-click transfer of the provided non-null, non-empty {@link ItemStack} within the currently open {@link SimpleMenu}.
	 * <p>
	 * The method should return {@link ItemStack#EMPTY} if exactly zero of the input stack could be transfered, and a copy of the source stack in all other circumstances.
	 * <p>
	 * The base implementation of this method merely returns {@link ItemStack#EMPTY}, implying that the transfer failed completely (zero items from the incoming stack were transfered).
	 *
	 * @param player - The {@link Player} performing the action
	 * @param sourceIndex - The index of the {@link Slot} from which the action was performed
	 * @param sourceStack - The non-null, non-empty {@link ItemStack} being shift-clicked.
	 * @return {@link ItemStack#EMPTY} if the transfer completely failed, or {@link ItemStack#copy()} of the source stack.
	 */
	protected ItemStack onStackTransfer(Player player, int sourceIndex, ItemStack sourceStack) {
		return ItemStack.EMPTY;
	}

	/**
	 * Attempts to transfer the provided {@link ItemStack} into each of the provided {@link SlotGroup}, in sequence.
	 * <p>
	 *
	 * @param sourceStack - The stack to transfer
	 * @param reverseOrder - Whether the {@link ItemStack} should be merged into the {@link SlotGroup} in forward or reverse {@link Slot} index order
	 * @param targets - The sequence of {@link SlotGroup} targets the transfer should be attempted for
	 * @return {@code TRUE} if the transfer completed, and {@code FALSE} otherwise.
	 */
	protected boolean tryTransferTo(ItemStack sourceStack, boolean reverseOrder, SlotGroup... targets) {
		for (SlotGroup iterated : targets) {
			if (this.tryTransferTo(sourceStack, reverseOrder, iterated)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Attempts to apply a transfer operation to the sequence of provided {@link SlotGroup}.
	 * <p>
	 * Akin to the shift-click behavior of other vanilla {@link Container}-deriving classes, returns a copy of the source {@link ItemStack}
	 * if the transfer completed in full, and {@link ItemStack#EMPTY} in all other cases.
	 *
	 * @param sourceStack - The stack to transfer
	 * @param reverseOrder - Whether the {@link ItemStack} should be merged into the {@link SlotGroup} in forward or reverse {@link Slot} index order
	 * @param targets - The sequence of {@link SlotGroup} targets the transfer should be attempted for
	 * @return {@link ItemStack#EMPTY} if the transfer completely failed, or {@link ItemStack#copy()} of the source stack.
	 */
	protected ItemStack tryApplyTransfer(ItemStack sourceStack, boolean reverseOrder, SlotGroup... targets) {
		final ItemStack copy = sourceStack.copy();
		return this.tryTransferTo(sourceStack, reverseOrder, targets) ? copy : ItemStack.EMPTY;
	}

	/**
	 * Attempts to apply a transfer operation to the provided {@link SlotGroup}.
	 * <p>
	 * Akin to the shift-click behavior of other vanilla {@link Container}-deriving classes, returns a copy of the source {@link ItemStack}
	 * if the transfer completed in full, and {@link ItemStack#EMPTY} in all other cases.
	 *
	 * @param sourceStack - The stack to transfer
	 * @param reverseOrder - Whether the {@link ItemStack} should be merged into the {@link SlotGroup} in forward or reverse {@link Slot} index order
	 * @param target - The{@link SlotGroup} target the transfer should be attempted for
	 * @return {@link ItemStack#EMPTY} if the transfer completely failed, or {@link ItemStack#copy()} of the source stack.
	 */
	protected ItemStack tryApplyTransfer(ItemStack sourceStack, boolean reverseOrder, SlotGroup target) {
		final ItemStack copy = sourceStack.copy();
		return this.tryTransferTo(sourceStack, reverseOrder, target) ? copy : ItemStack.EMPTY;
	}

	/**
	 * Attempts to transfer the provided {@link ItemStack} into the provided {@link SlotGroup}.
	 * <p>
	 *
	 * @param sourceStack - The stack to transfer
	 * @param reverseOrder - Whether the {@link ItemStack} should be merged into the {@link SlotGroup} in forward or reverse {@link Slot} index order
	 * @param target - The {@link SlotGroup} target the transfer should be attempted for
	 * @return {@code TRUE} if the transfer completed, and {@code FALSE} otherwise.
	 */
	protected boolean tryTransferTo(ItemStack sourceStack, boolean reverseOrder, SlotGroup target) {
		if (target.isContiguous()) {
			if (this.moveItemStackTo(sourceStack, target.getMinIndex(), target.getMaxIndex() + 1, reverseOrder)) {
				return true;
			}
		}
		else {
			final int begin = reverseOrder ? target.getMaxIndex() : target.getMinIndex();
			final int end = reverseOrder ? target.getMinIndex() : target.getMaxIndex();
			final int increment = reverseOrder ? -1 : 1;
			for (int index = begin; index <= end; index += increment) {
				if (target.holdsSlot(index) && this.moveItemStackTo(sourceStack, index, index + 1, false)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Creates and adds both the player main inventory and player hotbar grids to the {@link SimpleMenu}, returning the constituent {@link Slot} of both within a {@link SlotGroup}.
	 *
	 * @param inventory - The player inventory to create the grid from
	 * @param xPosition - The starting top-left x-position of the grid
	 * @param yPosition - The starting top-left y-position of the grid
	 * @return The constituent {@link Slot} of such a grid, contained as a {@link SlotGroup}.
	 */
	protected SlotGroup addPlayerGrid(Inventory inventory, int xPosition, int yPosition) {
		final SlotGroup.Builder groupBuilder = SlotGroup.Builder.create((PLAYER_INVENTORY_WIDTH * PLAYER_INVENTORY_HEIGHT) + (PLAYER_HOTBAR_WIDTH * PLAYER_HOTBAR_HEIGHT));
		final int hotbarYOffset = SimpleMenu.calculateSlotPosition(0, PLAYER_INVENTORY_HEIGHT + 1, DEFAULT_SLOT_SEPARATION) + DEFAULT_OFFSET_SMALL;
		this.addPlayerHotbarTo(groupBuilder, inventory, xPosition, yPosition + hotbarYOffset);
		this.addPlayerInventoryTo(groupBuilder, inventory, xPosition, yPosition);
		return groupBuilder.build();
	}

	/**
	 * Creates and adds the standard player main inventory grid to the {@link SimpleMenu}, returning the constituent {@link Slot} within a {@link SlotGroup}.
	 * <p>
	 * This will be a {@value SimpleMenu#PLAYER_INVENTORY_WIDTH} by {@value SimpleMenu#PLAYER_INVENTORY_HEIGHT} grid of ordinary {@link Slot}.
	 *
	 * @param inventory - The player inventory to create the grid from
	 * @param xPosition - The starting top-left x-position of the grid
	 * @param yPosition - The starting top-left y-position of the grid
	 * @return The constituent {@link Slot} of such a grid, contained as a {@link SlotGroup}.
	 */
	protected SlotGroup addPlayerInventory(Inventory inventory, int xPosition, int yPosition) {
		SlotGroup.Builder groupBuilder = SlotGroup.Builder.create(PLAYER_INVENTORY_WIDTH * PLAYER_INVENTORY_HEIGHT);
		this.addPlayerInventoryTo(groupBuilder, inventory, xPosition, yPosition);
		return groupBuilder.build();
	}

	/**
	 * Creates and adds the standard player hotbar inventory grid to the {@link SimpleMenu}, returning the constituent {@link Slot} within a {@link SlotGrid}.
	 * <p>
	 * This will be a {@value SimpleMenu#PLAYER_HOTBAR_WIDTH} by {@value SimpleMenu#PLAYER_HOTBAR_HEIGHT} grid of ordinary {@link Slot}.
	 *
	 * @param inventory - The player inventory to create the grid from
	 * @param xPosition - The starting top-left x-position of the grid
	 * @param yPosition - The starting top-left y-position of the grid
	 * @return The constituent {@link Slot} of such a grid, contained as a {@link SlotGroup}.
	 */
	protected SlotGroup addPlayerHotbar(Inventory inventory, int xPosition, int yPosition) {
		SlotGroup.Builder groupBuilder = SlotGroup.Builder.create(PLAYER_HOTBAR_WIDTH * PLAYER_HOTBAR_HEIGHT);
		this.addPlayerHotbarTo(groupBuilder, inventory, xPosition, yPosition);
		return groupBuilder.build();
	}

	/**
	 * Adds a grid of simple {@link Slot} instances to this {@link SimpleMenu}, based on the provided parameters, with a default x and y separation between {@link Slot} elements of {@value SimpleMenu#DEFAULT_SLOT_SEPARATION}.
	 * <p>
	 * For each position within a grid, the provided {@link ISlotFactory} will be invoked, and the received instance will be added to this {@link SimpleMenu} as well as the returned {@link SlotGroup}.
	 *
	 * @param <S> The actual type of the {@link Slot}-inheriting class
	 * @param inventory - The {@link Container} instance that the {@link Slot} will be linked to
	 * @param xPosition - The starting x position of the grid (from top left)
	 * @param yPosition - The starting y position of the grid (from top left)
	 * @param width - The width of the grid, in quantity of {@link Slot}
	 * @param height - The height of the grid, in quantity of {@link Slot}
	 * @return A {@link SlotGroup} holding all of the newly-created {@link Slot}-derived instances.
	 */
	protected <S extends Slot> SlotGroup addSlotGrid(Container inventory, int xPosition, int yPosition, int width, int height) {
		return this.addSlotGrid(inventory, xPosition, yPosition, width, height, Slot::new);
	}

	/**
	 * Adds a grid of simple {@link Slot} instances to this {@link SimpleMenu}, based on the provided parameters, with an equivalent x and y separation between {@link Slot} elements.
	 * <p>
	 * For each position within a grid, the provided {@link ISlotFactory} will be invoked, and the received instance will be added to this {@link SimpleMenu} as well as the returned {@link SlotGroup}.
	 *
	 * @param <S> The actual type of the {@link Slot}-inheriting class
	 * @param inventory - The {@link Container} instance that the {@link Slot} will be linked to
	 * @param xPosition - The starting x position of the grid (from top left)
	 * @param yPosition - The starting y position of the grid (from top left)
	 * @param width - The width of the grid, in quantity of {@link Slot}
	 * @param height - The height of the grid, in quantity of {@link Slot}
	 * @param separation - The horizontal separation between individual {@link Slot} within the grid
	 * @return A {@link SlotGroup} holding all of the newly-created {@link Slot}-derived instances.
	 */
	protected <S extends Slot> SlotGroup addSlotGrid(Container inventory, int xPosition, int yPosition, int width, int height, int separation) {
		return this.addSlotGrid(inventory, xPosition, yPosition, width, height, separation, separation, Slot::new);
	}

	/**
	 * Adds a grid of simple {@link Slot} instances to this {@link SimpleMenu}, based on the provided parameters.
	 * <p>
	 * For each position within a grid, the provided {@link ISlotFactory} will be invoked, and the received instance will be added to this {@link SimpleMenu} as well as the returned {@link SlotGroup}.
	 *
	 * @param <S> The actual type of the {@link Slot}-inheriting class
	 * @param inventory - The {@link Container} instance that the {@link Slot} will be linked to
	 * @param xPosition - The starting x position of the grid (from top left)
	 * @param yPosition - The starting y position of the grid (from top left)
	 * @param width - The width of the grid, in quantity of {@link Slot}
	 * @param height - The height of the grid, in quantity of {@link Slot}
	 * @param horizontalSeparation - The horizontal separation between individual {@link Slot} within the grid
	 * @param verticalSeparation - The vertical separation between individual {@link Slot} within the grid
	 * @return A {@link SlotGroup} holding all of the newly-created {@link Slot}-derived instances.
	 */
	protected <S extends Slot> SlotGroup addSlotGrid(Container inventory, int xPosition, int yPosition, int width, int height, int horizontalSeparation, int verticalSeparation) {
		return this.addSlotGrid(inventory, xPosition, yPosition, width, height, horizontalSeparation, verticalSeparation, Slot::new);
	}

	/**
	 * Adds a grid of {@link Slot}-derived instances to this {@link SimpleMenu}, based on the provided parameters, with a default x and y separation between {@link Slot} elements of {@value SimpleMenu#DEFAULT_SLOT_SEPARATION}.
	 * <p>
	 * For each position within a grid, the provided {@link ISlotFactory} will be invoked, and the received instance will be added to this {@link SimpleMenu} as well as the returned {@link SlotGroup}.
	 *
	 * @param <S> The actual type of the {@link Slot}-inheriting class
	 * @param inventory - The {@link Container} instance that the {@link Slot} will be linked to
	 * @param xPosition - The starting x position of the grid (from top left)
	 * @param yPosition - The starting y position of the grid (from top left)
	 * @param width - The width of the grid, in quantity of {@link Slot}
	 * @param height - The height of the grid, in quantity of {@link Slot}
	 * @param factory - A means of constructing new {@link Slot}-derived instances of the desired type.
	 * @return A {@link SlotGroup} holding all of the newly-created {@link Slot}-derived instances.
	 */
	protected <S extends Slot> SlotGroup addSlotGrid(Container inventory, int xPosition, int yPosition, int width, int height, ISlotFactory<S> factory) {
		return this.addSlotGrid(inventory, xPosition, yPosition, width, height, DEFAULT_SLOT_SEPARATION, factory);
	}

	/**
	 * Adds a grid of {@link Slot}-derived instances to this {@link SimpleMenu}, based on the provided parameters, with an equivalent x and y separation between {@link Slot} elements.
	 * <p>
	 * For each position within a grid, the provided {@link ISlotFactory} will be invoked, and the received instance will be added to this {@link SimpleMenu} as well as the returned {@link SlotGroup}.
	 *
	 * @param <S> The actual type of the {@link Slot}-inheriting class
	 * @param inventory - The {@link Container} instance that the {@link Slot} will be linked to
	 * @param xPosition - The starting x position of the grid (from top left)
	 * @param yPosition - The starting y position of the grid (from top left)
	 * @param width - The width of the grid, in quantity of {@link Slot}
	 * @param height - The height of the grid, in quantity of {@link Slot}
	 * @param separation - The horizontal separation between individual {@link Slot} within the grid
	 * @param factory - A means of constructing new {@link Slot}-derived instances of the desired type.
	 * @return A {@link SlotGroup} holding all of the newly-created {@link Slot}-derived instances.
	 */
	protected <S extends Slot> SlotGroup addSlotGrid(Container inventory, int xPosition, int yPosition, int width, int height, int separation, ISlotFactory<S> factory) {
		return this.addSlotGrid(inventory, xPosition, yPosition, width, height, separation, separation, factory);
	}

	/**
	 * Adds a grid of {@link Slot}-derived instances to this {@link SimpleMenu}, based on the provided parameters.
	 * <p>
	 * For each position within a grid, the provided {@link ISlotFactory} will be invoked, and the received instance will be added to this {@link SimpleMenu} as well as the returned {@link SlotGroup}.
	 *
	 * @param <S> The actual type of the {@link Slot}-inheriting class
	 * @param inventory - The {@link Container} instance that the {@link Slot} will be linked to
	 * @param xPosition - The starting x position of the grid (from top left)
	 * @param yPosition - The starting y position of the grid (from top left)
	 * @param width - The width of the grid, in quantity of {@link Slot}
	 * @param height - The height of the grid, in quantity of {@link Slot}
	 * @param horizontalSeparation - The horizontal separation between individual {@link Slot} within the grid
	 * @param verticalSeparation - The vertical separation between individual {@link Slot} within the grid
	 * @param factory - A means of constructing new {@link Slot}-derived instances of the desired type.
	 * @return A {@link SlotGroup} holding all of the newly-created {@link Slot}-derived instances.
	 */
	protected <S extends Slot> SlotGroup addSlotGrid(Container inventory, int xPosition, int yPosition, int width, int height, int horizontalSeparation, int verticalSeparation, ISlotFactory<S> factory) {
		return this.constructGridGroup(inventory, 0, xPosition, yPosition, width, height, horizontalSeparation, verticalSeparation, factory);
	}

	/**
	 * Calculates the axial position for an inventory slot given the starting point, ordinal of the slot along that axis, and the given pixel separation between each slot.
	 * <p>
	 * Useful for aligning individual {@link Slot} to a grid of like elements.
	 *
	 * @param start - The offset starting position
	 * @param axialOrdinal - The ordinal of the {@link Slot} element along the desired axis
	 * @param separation - The separation in UI-units between each element along this axis
	 * @return A suitable position, in UI-units, for a spaced UI element.
	 */
	protected static final int calculateSlotPosition(int start, int axialOrdinal, int separation) {
		return start + (axialOrdinal * (DEFAULT_SLOT_SIZE + separation));
	}

	/**
	 * Calculates the axial position for an inventory slot given the starting point, ordinal of the slot along that axis, assuming the default slot separation value of {@value SimpleMenu#DEFAULT_SLOT_SEPARATION}.
	 * <p>
	 * Useful for aligning individual {@link Slot} to a grid of like elements.
	 *
	 * @param start - The offset starting position
	 * @param axialOrdinal - The ordinal of the {@link Slot} element along the desired axis
	 * @return A suitable position, in UI-units, for a spaced UI element.
	 */
	protected static final int calculateSlotPosition(int start, int axialOrdinal) {
		return SimpleMenu.calculateSlotPosition(start, axialOrdinal, SimpleMenu.DEFAULT_SLOT_SEPARATION);
	}

	/* Internal Methods */

	private <S extends Slot> void addPlayerHotbarTo(SlotGroup.Builder groupBuilder, Inventory inventory, int xPosition, int yPosition) {
		this.addGridToGroup(groupBuilder, inventory, 0, xPosition, yPosition, PLAYER_HOTBAR_WIDTH, PLAYER_HOTBAR_HEIGHT, DEFAULT_SLOT_SEPARATION, DEFAULT_SLOT_SEPARATION, Slot::new);
	}

	private <S extends Slot> void addPlayerInventoryTo(SlotGroup.Builder groupBuilder, Inventory inventory, int xPosition, int yPosition) {
		final int indexOffset = PLAYER_HOTBAR_WIDTH * PLAYER_HOTBAR_HEIGHT;
		this.addGridToGroup(groupBuilder, inventory, indexOffset, xPosition, yPosition, PLAYER_INVENTORY_WIDTH, PLAYER_INVENTORY_HEIGHT, DEFAULT_SLOT_SEPARATION, DEFAULT_SLOT_SEPARATION, Slot::new);
	}

	private <S extends Slot> SlotGroup constructGridGroup(Container inventory, int indexOffset, int xPosition, int yPosition, int width, int height, int horizontalSeparation, int verticalSeparation, ISlotFactory<S> factory) {
		SlotGroup.Builder groupBuilder = SlotGroup.Builder.create(width * height);
		this.addGridToGroup(groupBuilder, inventory, indexOffset, xPosition, yPosition, width, height, horizontalSeparation, verticalSeparation, factory);
		return groupBuilder.build();
	}

	private <S extends Slot> void addGridToGroup(SlotGroup.Builder groupBuilder, Container inventory, int indexOffset, int xPosition, int yPosition, int width, int height, int horizontalSeparation, int verticalSeparation, ISlotFactory<S> factory) {
		for (int y = 0; y < height; y += 1) {
			for (int x = 0; x < width; x += 1) {
				final int index = x + (y * width);
				final S slot = factory.create(inventory, indexOffset + index, SimpleMenu.calculateSlotPosition(xPosition, x, horizontalSeparation), SimpleMenu.calculateSlotPosition(yPosition, y, verticalSeparation));
				this.addAndGroup(groupBuilder, slot);
			}
		}
	}

	private final void addAndGroup(final SlotGroup.Builder groupBuilder, final Slot slot) {
		this.addSlot(slot);
		groupBuilder.add(slot);
	}

	/**
	 * Factory interface for creating {@link Slot} instances.
	 */
	@FunctionalInterface
	public static interface ISlotFactory<S extends Slot> {

		/**
		 * This method should create a new {@link Slot}-derived instance given the passed parameters.
		 * <p>
		 * Implementors should take care to return unique instances each time this method is invoked.
		 *
		 * @param inventory - The {@link Container} that the {@link Slot} is linked to
		 * @param index - The index of the slot (distinguished from the number within the container)
		 * @param xPos - The x-position of the {@link Slot}
		 * @param yPos - The y-position of the {@link Slot}
		 * @return A suitable {@link Slot}-derived instance
		 */
		public S create(Container inventory, int index, int xPos, int yPos);

	}

}
