package paragon.minecraft.library.datageneration;

import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.IForgeRegistryEntry;
import paragon.minecraft.library.Utilities;

/**
 * Data-Generator class for generating {@link IFinishedRecipe}.
 * <p>
 * Contains some useful utility methods that the base class does not.
 *
 * @author Malcolm Riley
 */
public abstract class RecipeHelper extends RecipeProvider {

	/* Internal Fields */
	protected static final String CRITERION_PREFIX = "has_";

	public RecipeHelper(DataGenerator generator) {
		super(generator);
	}

	@Override
	public abstract void registerRecipes(final Consumer<IFinishedRecipe> registrar);

	/* Utility Methods */

	/**
	 * Convenience method to create an {@link InventoryChangeTrigger.Instance} for the possesion of the {@link Item} contained in the provided {@link RegistryObject}.
	 * <p>
	 * Simply calls {@link RegistryObject#get()} and provides the result to {@link RecipeHelper#hasItem(IItemProvider)}.
	 * 
	 * @param registryObject - The {@link RegistryObject} containing the desired {@link Item}
	 * @return A suitable {@link InventoryChangeTrigger.Instance}
	 */
	protected static InventoryChangeTrigger.Instance hasItem(RegistryObject<Item> registryObject) {
		return RecipeHelper.hasItem(registryObject.get());
	}
	
	/**
	 * Convenience method to create an {@link InventoryChangeTrigger.Instance} for the possesion of the {@link Block} contained in the provided {@link RegistryObject}.
	 * <p>
	 * Simply calls {@link RegistryObject#get()} and provides the result to {@link RecipeHelper#hasItem(IItemProvider)}.
	 * 
	 * @param registryObject - The {@link RegistryObject} containing the desired {@link Block}
	 * @return A suitable {@link InventoryChangeTrigger.Instance}
	 */
	protected static InventoryChangeTrigger.Instance hasBlock(RegistryObject<Block> registryObject) {
		return RecipeHelper.hasItem(registryObject.get());
	}

	/**
	 * Helper method for creating a recipe name automatically from the passed two components.
	 * <p>
	 * The path of the resulting {@link ResourceLocation} will be the result of concatenating the registry path of the output {@link IItemProvider}'s {@link Item}, then the {@link String} literal {@code "_from_"}, and then
	 * the registry path of the input {@link IItemProvider}'s {@link Item}.
	 * <p>
	 * The first parameter, the {@link Function} must be a means of transforming a {@link String} registry path into a domain-qualified {@link ResourceLocation}.
	 *
	 * @param transformer - A path-to-qualified-domain transformer
	 * @param output - An "output" {@link IItemProvider}
	 * @param input - An "input" {@link IItemProvider}
	 * @return A {@link ResourceLocation} suitable for use as the name of an {@link IFinishedRecipe}.
	 */
	protected static ResourceLocation nameFromIngredients(Function<String, ResourceLocation> transformer, IItemProvider output, IItemProvider input) {
		return RecipeHelper.nameFrom(transformer, output, RecipeHelper.registryPathOf(input));
	}
	
	/**
	 * Helper method for creating a recipe name automatically from the passed two components.
	 * <p>
	 * The path of the resulting {@link ResourceLocation} will be the result of concatenating the registry path of the output {@link IItemProvider}'s {@link Item}, then the {@link String} literal {@code "_from_"}, and then
	 * the value of {@code outputSource}.
	 * <p>
	 * The first parameter, the {@link Function} must be a means of transforming a {@link String} registry path into a domain-qualified {@link ResourceLocation}.
	 *
	 * @param transformer - A path-to-qualified-domain transformer
	 * @param output - An "output" {@link IItemProvider}
	 * @param outputSource - The "source" of the output item
	 * @return A {@link ResourceLocation} suitable for use as the name of an {@link IFinishedRecipe}.
	 */
	protected static ResourceLocation nameFrom(Function<String, ResourceLocation> transformer, IItemProvider output, String outputSource) {
		return transformer.apply(Utilities.Strings.name(RecipeHelper.registryPathOf(output), "from", outputSource));
	}

	/**
	 * Helper method for creating a criterion name from the registry path of the passed {@link IItemProvider}.
	 * <p>
	 * Will query {@link IItemProvider#asItem()} and then {@link ResourceLocation#getPath()} on the returned {@link ResourceLocation},
	 * prefixing it with {@value #CRITERION_PREFIX}.
	 *
	 * @param item - The {@link IForgeRegistryEntry} to generate a "criterion name" from.
	 * @return A {@link String} suitable for use as an advancement criterion name.
	 */
	protected static String criterionName(IItemProvider item) {
		return RecipeHelper.createCriterionName(item.asItem());
	}

	/**
	 * Helper method for creating a criterion name from the registry path of the passed {@link INamedTag}.
	 * <p>
	 * Will query {@link INamedTag#getName()} and then {@link ResourceLocation#getPath()} on the returned {@link ResourceLocation},
	 * prefixing it with {@value #CRITERION_PREFIX}.
	 *
	 * @param item - The {@link INamedTag} to generate a "criterion name" from.
	 * @return A {@link String} suitable for use as an advancement criterion name.
	 */
	protected static String criterionName(INamedTag<?> item) {
		return CRITERION_PREFIX + item.getName().getPath();
	}

	/**
	 * Helper method for creating a criterion name from the registry path of the passed {@link IForgeRegistryEntry}.
	 * <p>
	 * Will call {@link RegistryObject#get()} and then prefix the path of the returned registry name with {@value #CRITERION_PREFIX}.
	 *
	 * @param item - The {@link IForgeRegistryEntry} to generate a "criterion name" from.
	 * @return A {@link String} suitable for use as an advancement criterion name.
	 */
	protected static <T extends IForgeRegistryEntry<T>> String criterionName(RegistryObject<T> item) {
		return RecipeHelper.createCriterionName(item.get());
	}

	/**
	 * Helper method for creating a criterion name from the registry path of the passed {@link IForgeRegistryEntry}.
	 * <p>
	 * Will prefixing the path of the registry name the passed {@link IForgeRegistryEntry} with {@value #CRITERION_PREFIX}.
	 *
	 * @param item - The {@link IForgeRegistryEntry} to generate a "criterion name" from.
	 * @return A {@link String} suitable for use as an advancement criterion name.
	 */
	protected static <T extends IForgeRegistryEntry<T>> String createCriterionName(T item) {
		return CRITERION_PREFIX + RecipeHelper.getRegistryPath(item);
	}

	/**
	 * Convenience method to return the {@link ResourceLocation#getPath()} value of the {@link Item} obtained from the passed {@link IItemProvider}.
	 *
	 * @param item - The {@link IItemProvider} to query
	 * @return The {@link ResourceLocation} path of the {@link Item} from the passed {@link IItemProvider}.
	 */
	protected static String registryPathOf(IItemProvider item) {
		return RecipeHelper.getRegistryPath(item.asItem());
	}

	/**
	 * Convenience method to return the {@link ResourceLocation#getPath()} value of the {@link Item} obtained from the passed {@link RegistryObject}.
	 *
	 * @param item - The {@link RegistryObject} to query
	 * @return The {@link ResourceLocation} path of the {@link Item} from the passed {@link RegistryObject}.
	 */
	protected static <T extends IForgeRegistryEntry<T>> String registryPathOf(RegistryObject<T> entry) {
		return RecipeHelper.getRegistryPath(entry.get());
	}

	/**
	 * Convenience method to return the {@link ResourceLocation#getPath()} value of the {@link Item} obtained from the passed {@link IForgeRegistryEntry}.
	 *
	 * @param item - The {@link IForgeRegistryEntry} to query
	 * @return The {@link ResourceLocation} path of the registry name of the passed {@link IForgeRegistryEntry}.
	 */
	protected static <T extends IForgeRegistryEntry<T>> String getRegistryPath(T element) {
		return element.getRegistryName().getPath();
	}

}
