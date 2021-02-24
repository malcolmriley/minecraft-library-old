package paragon.minecraft.library.datageneration;

import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * Data-Generator class for generating blockstate files and block models.
 * <p>
 * @author Malcolm Riley
 */
public abstract class BlockStateHelper extends BlockStateProvider {
	
	public BlockStateHelper(DataGenerator generator, String modid, ExistingFileHelper helper) {
		super(generator, modid, helper);
		// TODO Auto-generated constructor stub
	}
	
	/* Internal Methods */

	protected ConfiguredModel[] variants(Block baseBlock, int quantity) {
		ConfiguredModel[] variants = new ConfiguredModel[quantity];
		ResourceLocation name = baseBlock.getRegistryName();
		for (int index = 1; index <= quantity; index += 1) {
			final String variantName = ModelProvider.BLOCK_FOLDER + "/" + name.getPath() + "_" + index;
			ConfiguredModel model = ConfiguredModel.builder().modelFile(this.models().cubeAll(variantName, new ResourceLocation(name.getNamespace(), variantName))).buildLast();
			variants[index - 1] = model;
		}
		return variants;
	}

}
