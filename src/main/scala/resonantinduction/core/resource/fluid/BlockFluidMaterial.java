package resonantinduction.core.resource.fluid;

import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import resonantinduction.core.Reference;
import resonantinduction.core.Settings;
import resonantinduction.core.resource.ResourceGenerator;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Fluid class uses for molten materials.
 * 
 * @author Calclavia
 */
public class BlockFluidMaterial extends BlockFluidFinite
{
	public BlockFluidMaterial(Fluid fluid)
	{
		super(fluid, Material.lava);
	}

	public void setQuanta(World world, int x, int y, int z, int quanta)
	{
		if (quanta > 0)
			world.setBlockMetadataWithNotify(x, y, z, quanta, 3);
		else
			world.setBlockToAir(x, y, z);
	}

	/* IFluidBlock */
	@Override
	public FluidStack drain(World world, int x, int y, int z, boolean doDrain)
	{
		FluidStack stack = new FluidStack(getFluid(), (int) (FluidContainerRegistry.BUCKET_VOLUME * this.getFilledPercentage(world, x, y, z)));
		if (doDrain)
			world.setBlockToAir(x, y, z);
		return stack;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int colorMultiplier(IBlockAccess access, int x, int y, int z)
	{
		return ResourceGenerator.getColor(ResourceGenerator.moltenToMaterial(getFluid().getName()));
	}

	@Override
	public boolean canDrain(World world, int x, int y, int z)
	{
		return true;
	}
}
