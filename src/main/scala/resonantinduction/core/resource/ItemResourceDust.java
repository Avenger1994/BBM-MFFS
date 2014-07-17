package resonantinduction.core.resource;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import resonant.lib.utility.LanguageUtility;
import resonantinduction.core.ResonantInduction;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * An item used for auto-generated dusts based on registered ingots in the OreDict.
 *
 * @author Calclavia
 */
public class ItemResourceDust extends Item
{
	private Block block = ResonantInduction.blockRefinedDust;

	public ItemResourceDust()
	{
		setHasSubtypes(true);
		setMaxDamage(0);
	}

	public static String getMaterialFromStack(ItemStack itemStack)
	{
		return ResourceGenerator.getName(itemStack.getItemDamage());
	}

	@Override
	public String getItemDisplayName(ItemStack is)
	{
		String material = getMaterialFromStack(is);

		if (material != null)
		{
			List<ItemStack> list = OreDictionary.getOres("ingot" + material.substring(0, 1).toUpperCase() + material.substring(1));

			if (list.size() > 0)
			{
				ItemStack type = list.get(0);

				String name = type.getDisplayName().replace(LanguageUtility.getLocal("misc.resonantinduction.ingot"), "").replaceAll("^ ", "").replaceAll(" $", "");
				return (LanguageUtility.getLocal(getUnlocalizedName() + ".name")).replace("%v", name).replace("  ", " ");
			}
		}

		return "";
	}

	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		/**
		 * Allow refined dust to be placed down.
		 */
		if (itemStack.getItem() == ResonantInduction.itemDust || itemStack.getItem() == ResonantInduction.itemRefinedDust)
		{
			blockID = itemStack.getItem() == ResonantInduction.itemRefinedDust ? ResonantInduction.blockRefinedDust.blockID : ResonantInduction.blockDust.blockID;

			if (itemStack.stackSize == 0)
			{
				return false;
			}
			else if (!player.canPlayerEdit(x, y, z, side, itemStack))
			{
				return false;
			}
			else
			{
				TileEntity tile = world.getTileEntity(x, y, z);

				if (world.getBlockId(x, y, z) == blockID && tile instanceof TileMaterial)
				{
					if (getMaterialFromStack(itemStack).equals(((TileMaterial) tile).name))
					{
						Block block = Block.blocksList[blockID];
						int j1 = world.getBlockMetadata(x, y, z);
						int k1 = j1 & 7;

						if (k1 <= 6 && world.checkNoEntityCollision(block.getCollisionBoundingBoxFromPool(world, x, y, z)) && world.setBlockMetadataWithNotify(x, y, z, k1 + 1 | j1 & -8, 2))
						{
							world.playSoundEffect(x + 0.5F, y + 0.5F, z + 0.5F, block.stepSound.getPlaceSound(), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getPitch() * 0.8F);
							--itemStack.stackSize;
							return true;
						}
					}
				}

				if (side == 0)
				{
					--y;
				}

				if (side == 1)
				{
					++y;
				}

				if (side == 2)
				{
					--z;
				}

				if (side == 3)
				{
					++z;
				}

				if (side == 4)
				{
					--x;
				}

				if (side == 5)
				{
					++x;
				}

				if (world.canPlaceEntityOnSide(blockID, x, y, z, false, side, player, itemStack))
				{
					Block block = Block.blocksList[blockID];
					int j1 = this.getMetadata(itemStack.getItemDamage());
					int k1 = Block.blocksList[blockID].onBlockPlaced(world, x, y, z, side, hitX, hitY, hitZ, j1);

					if (placeBlockAt(itemStack, player, world, x, y, z, side, hitX, hitY, hitZ, k1))
					{
						world.playSoundEffect(x + 0.5F, y + 0.5F, z + 0.5F, block.stepSound.getPlaceSound(), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getPitch() * 0.8F);
						--itemStack.stackSize;
					}

					return true;
				}
			}
		}

		return false;
	}

	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
	{
		blockID = stack.getItem() == ResonantInduction.itemRefinedDust ? ResonantInduction.blockRefinedDust.blockID : ResonantInduction.blockDust.blockID;

		if (!world.setBlock(x, y, z, this.blockID, metadata, 3))
		{
			return false;
		}

		if (world.getBlockId(x, y, z) == this.blockID)
		{
			Block.blocksList[this.blockID].onBlockPlacedBy(world, x, y, z, player, stack);
			Block.blocksList[this.blockID].onPostBlockPlaced(world, x, y, z, metadata);
		}

		return true;
	}

	public ItemStack getStackFromMaterial(String name)
	{
		ItemStack itemStack = new ItemStack(this);
		itemStack.setItemDamage(ResourceGenerator.getID(name));
		return itemStack;
	}

	@Override
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		for (String materialName : ResourceGenerator.materials.keySet())
		{
			par3List.add(getStackFromMaterial(materialName));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack itemStack, int par2)
	{
		/**
		 * Auto-color based on the texture of the ingot.
		 */
		String name = ItemResourceDust.getMaterialFromStack(itemStack);

		if (ResourceGenerator.materialColorCache.containsKey(name))
		{
			return ResourceGenerator.materialColorCache.get(name);
		}

		return 16777215;
	}
}
