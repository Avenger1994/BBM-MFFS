package resonantinduction.core.blocks;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import resonant.lib.prefab.block.BlockTile;
import resonantinduction.core.Reference;

/**
 * A block used to build machines or decoration.
 * 
 * @author Calclavia
 * 
 */
public class BlockIndustrialStone extends BlockTile
{
	String[] iconNames = new String[] { "material_stone_brick", "material_stone_brick2", "material_stone_chiseled", "material_stone_cobble", "material_stone_cracked", "material_stone", "material_stone_slab", "material_stone_mossy", "material_steel_dark", "material_steel_tint", "material_steel" };
	IIcon[] icons = new IIcon[iconNames.length];

	public BlockIndustrialStone(int id)
	{
		super(id, Material.rock);
		setHardness(1F);
		setStepSound(Block.soundStoneFootstep);
	}

	@Override
	public int damageDropped(int par1)
	{
		return par1;
	}

	@Override
	public IIcon getIcon(int side, int metadata)
	{
		return icons[metadata];
	}

	@Override
	public void registerIcons(IIconRegister par1IIconRegister)
	{
		this.blockIcon = par1IIconRegister.registerIcon(this.getTextureName());

		for (int i = 0; i < iconNames.length; i++)
			icons[i] = par1IIconRegister.registerIcon(Reference.PREFIX + iconNames[i]);
	}

	@Override
	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		for (int i = 0; i < iconNames.length; i++)
			par3List.add(new ItemStack(par1, 1, i));
	}
}
