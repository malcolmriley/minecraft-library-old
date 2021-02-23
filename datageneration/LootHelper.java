package paragon.minecraft.library.datageneration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.mojang.datafixers.util.Pair;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.LootTableProvider;
import net.minecraft.loot.LootParameterSet;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTable.Builder;
import net.minecraft.loot.LootTableManager;
import net.minecraft.loot.ValidationTracker;
import net.minecraft.util.ResourceLocation;

/**
 * Data-generator class for assisting with generating JSON {@link LootTable}.
 * 
 * @author Malcolm Riley
 */
public abstract class LootHelper extends LootTableProvider {

	public LootHelper(DataGenerator generator) {
		super(generator);
	}
	
	/* Supertype Override Methods */

	@Override
	public final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, Builder>>>, LootParameterSet>> getTables() {
		List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, Builder>>>, LootParameterSet>> list = new ArrayList<>();
		this.addLootTables(list::add);
		return Collections.unmodifiableList(list);
	}

	@Override
	protected final void validate(Map<ResourceLocation, LootTable> map, ValidationTracker validationtracker) {
		map.forEach((key, value) -> LootTableManager.validateLootTable(validationtracker, key, value));
	}
	
	/* Abstract Methods */
	
	/**
	 * Implementors should use this method to add loot tables.
	 * <p>
	 * The general idea is to invoke the provided {@link Consumer} using {@link Pair#of(Object, Object)} using {@link LootParameterSets} and a supplier to the individual table-providing method or class.
	 * <p>
	 * A suggested implementation for {@link Block} loot tables is to extend {@link BlockLootTables} and then use {@code ExtendingClass::new} to create a {@link Supplier} from it, providing it to this method.
	 * @param registrar
	 */
	protected abstract void addLootTables(Consumer<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, Builder>>>, LootParameterSet>> registrar);

}