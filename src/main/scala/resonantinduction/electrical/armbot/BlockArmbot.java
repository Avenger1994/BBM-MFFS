package resonantinduction.electrical.armbot;

import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import resonant.core.ResonantEngine;
import resonant.lib.multiblock.IBlockActivate;
import resonant.lib.multiblock.IMultiBlock;
import resonant.lib.prefab.block.BlockTile;
import resonant.lib.render.block.BlockRenderingHandler;
import universalelectricity.api.UniversalElectricity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** A stationary robotic claw used to do simple coded tasks in the world
 * 
 * @author Darkguardsman */
public class BlockArmbot extends BlockTile
{
    public BlockArmbot(int id)
    {
        super(id, UniversalElectricity.machine);
    }

    @Override
    public boolean canBlockStay(World world, int x, int y, int z)
    {
        return world.getBlockMaterial(x, y - 1, z).isSolid();
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z)
    {
        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

        if (tileEntity instanceof IMultiBlock)
        {
            ResonantEngine.blockMulti.createMultiBlockStructure((IMultiBlock) tileEntity);
        }
    }

    @Override
    public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9)
    {
        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

        if (tileEntity instanceof IBlockActivate)
        {
            return ((IBlockActivate) tileEntity).onActivated(player);
        }

        return false;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, int par5, int par6)
    {
        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

        if (tileEntity instanceof TileArmbot)
        {
            ((TileArmbot) tileEntity).dropHeldObject();
            ResonantEngine.blockMulti.destroyMultiBlockStructure((TileArmbot) tileEntity);
        }

        this.dropBlockAsItem_do(world, x, y, z, new ItemStack(this));
        super.breakBlock(world, x, y, z, par5, par6);
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
    {
        return new ItemStack(this);
    }

    @Override
    public int quantityDropped(Random par1Random)
    {
        return 0;
    }

    @Override
    public TileEntity createNewTileEntity(World var1)
    {
        return new TileArmbot();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getRenderType()
    {
        return BlockRenderingHandler.ID;
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }
}
