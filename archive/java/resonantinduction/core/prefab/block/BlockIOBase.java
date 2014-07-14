/**
 * 
 */
package resonantinduction.core.prefab.block;

import net.minecraft.block.material.Material;
import net.minecraftforge.common.Configuration;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInductionTabs;
import resonantinduction.core.Settings;
import calclavia.lib.prefab.block.BlockSidedIO;

/**
 * Blocks that have specific sided input and output should extend this.
 * 
 * @author Calclavia
 * 
 */
public class BlockIOBase extends BlockSidedIO
{
	public BlockIOBase(String name, int id)
	{
		super(Settings.config.get(Configuration.CATEGORY_BLOCK, name, id).getInt(id), Material.piston);
		this.setCreativeTab(ResonantInductionTabs.CORE);
		this.setUnlocalizedName(Reference.PREFIX + name);
		this.setTextureName(Reference.PREFIX + name);
		this.setHardness(1f);
	}
}
