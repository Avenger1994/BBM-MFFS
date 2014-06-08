package mffs.base;

import mffs.MFFSCreativeTab;
import mffs.ModularForceFieldSystem;
import mffs.Settings;
import mffs.item.card.ItemCardLink;
import mffs.render.RenderBlockHandler;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.UniversalElectricity;
import resonant.api.blocks.ICamouflageMaterial;
import calclavia.api.mffs.IBiometricIdentifierLink;
import calclavia.api.mffs.security.Permission;
import resonant.lib.prefab.block.BlockRotatable;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class BlockMFFS extends BlockRotatable implements ICamouflageMaterial
{
	public BlockMFFS(int id, String name)
	{
		super(Settings.configuration.getBlock(name, id).getInt(id), UniversalElectricity.machine);
		this.setUnlocalizedName(ModularForceFieldSystem.PREFIX + name);
		this.setHardness(Float.MAX_VALUE);
		this.setResistance(100.0F);
		this.setStepSound(soundMetalFootstep);
		this.setCreativeTab(MFFSCreativeTab.INSTANCE);
		this.setTextureName(ModularForceFieldSystem.PREFIX + name);
	}

	@Override
	public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote)
		{
			if (entityPlayer.getCurrentEquippedItem() != null)
			{
				if (entityPlayer.getCurrentEquippedItem().getItem() instanceof ItemCardLink)
				{
					return false;
				}
			}

			entityPlayer.openGui(ModularForceFieldSystem.instance, 0, world, x, y, z);
		}

		return true;
	}

	@Override
	public boolean onUseWrench(World world, int x, int y, int z, EntityPlayer par5EntityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		return doRotateBlock(world, x, y, z, ForgeDirection.getOrientation(side));
	}

	@Override
	public boolean onSneakUseWrench(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote)
		{
			TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

			if (tileEntity instanceof IBiometricIdentifierLink)
			{
				if (((IBiometricIdentifierLink) tileEntity).getBiometricIdentifier() != null)
				{
					if (((IBiometricIdentifierLink) tileEntity).getBiometricIdentifier().isAccessGranted(entityPlayer.username, Permission.SECURITY_CENTER_CONFIGURE))
					{
						this.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
						world.setBlock(x, y, z, 0);
						return true;
					}
					else
					{
						entityPlayer.addChatMessage("[" + ModularForceFieldSystem.blockBiometricIdentifier.getLocalizedName() + "]" + " Cannot remove machine! Access denied!");
					}
				}
				else
				{
					this.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
					world.setBlock(x, y, z, 0);
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int blockID)
	{
		if (!world.isRemote)
		{
			TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

			if (tileEntity instanceof TileMFFS)
			{
				if (world.isBlockIndirectlyGettingPowered(x, y, z))
				{
					((TileMFFS) tileEntity).onPowerOn();
				}
				else
				{
					((TileMFFS) tileEntity).onPowerOff();
				}
			}
		}
	}

	@Override
	public float getExplosionResistance(Entity entity, World world, int i, int j, int k, double d, double d1, double d2)
	{
		return 100.0F;
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister)
	{
		this.blockIcon = par1IconRegister.registerIcon(ModularForceFieldSystem.PREFIX + "machine");
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderType()
	{
		return RenderBlockHandler.ID;
	}

}