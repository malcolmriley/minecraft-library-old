package paragon.minecraft.library.datageneration;

import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.ModelFile.UncheckedModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import paragon.minecraft.library.Utilities;


/**
 * Data-Generator class for generating item models.
 * <p>
 * Contains some useful utility methods that the base class does not.
 *
 * @author Malcolm Riley
 */
public abstract class ItemModelHelper extends ItemModelProvider {

	/* Internal Fields */
	protected static final String TEXTURE_DEFAULT = "layer0";
	protected static final String ITEM_PREFIX = ITEM_FOLDER + Utilities.Strings.DELIMITER_PATH;
	protected static final String BLOCK_PREFIX = BLOCK_FOLDER + Utilities.Strings.DELIMITER_PATH;

	protected static final ModelFile GENERATED = new UncheckedModelFile(ITEM_PREFIX + "generated");
	
	public ItemModelHelper(DataGenerator generator, String modid, ExistingFileHelper existingFileHelper) {
		super(generator, modid, existingFileHelper);
	}

	/* Internal Methods */

	/**
	 * Convenience method for creating a simple block-parented {@link Item} model.
	 * <p>
	 * The {@link ItemModelHelper} will look in the default location of the mod block model assets folder for
	 * a block model with the same name as the passed {@link String}.
	 * 
	 * @param name - The name of the parent block model.
	 */
	protected void blockItem(String name) {
		this.getBuilder(name).parent(this.getExistingFile(this.modLoc(BLOCK_PREFIX + name)));
	}
	
	/**
	 * Convenience method for creating a simple, single-texture {@link Item}.
	 * <p>
	 * The {@link ItemModelHelper} will look in the default location of the mod item texture assets
	 * for a texture of the same name as the passed {@link String}.
	 * 
	 * @param name - The name of the texture.
	 */
	protected void simpleItem(String name) {
		this.getBuilder(name).parent(GENERATED).texture(TEXTURE_DEFAULT, this.modLoc(ITEM_PREFIX + name));
	}

}
