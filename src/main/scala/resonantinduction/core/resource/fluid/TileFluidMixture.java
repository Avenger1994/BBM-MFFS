package resonantinduction.core.resource.fluid;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;
import resonant.api.recipe.MachineRecipes;
import resonantinduction.core.ResonantInduction.RecipeType;
import resonantinduction.core.resource.ResourceGenerator;
import resonantinduction.core.resource.TileMaterial;

/**
 * NO-OP. Not yet properly implemented. We're not using TEs for now.
 * 
 * @author Calclavia
 * 
 */
@Deprecated
public class TileFluidMixture extends TileMaterial
{
	public final Set<ItemStack> items = new HashSet<ItemStack>();
	public final Set<FluidStack> fluids = new HashSet<FluidStack>();

    public TileFluidMixture()
    {
        super(Material.water);
    }

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	public boolean mix(ItemStack itemStack)
	{
		if (MachineRecipes.INSTANCE.getOutput(RecipeType.MIXER().toString(), itemStack).length > 0 && getBlockMetadata() < 8)
		{
			// TODO: Maybe we need to merge the stacks?
			items.add(itemStack);

			if (name() == null)
			{
				name_$eq(ResourceGenerator.getName(itemStack));
			}

			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, getBlockMetadata() + 1, 3);
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			return true;
		}

		return false;
	}

	public void mix(FluidStack fluid)
	{
		if (!fluid.getFluid().isGaseous())
		{
			if (fluids.contains(fluid))
			{
				for (FluidStack checkFluid : fluids)
				{
					if (fluid.equals(checkFluid))
					{
						checkFluid.amount += fluid.amount;
					}
				}
			}
			else
			{
				fluids.add(fluid);
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		readFluidFromNBT(nbt);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		writeFluidToNBT(nbt);
	}

	public void readFluidFromNBT(NBTTagCompound nbt)
	{
		fluids.clear();
		items.clear();

		NBTTagList fluidList = nbt.getTagList("Fluids", 0);

		for (int i = 0; i < fluidList.tagCount(); ++i)
		{
			NBTTagCompound fluidNBT = (NBTTagCompound) fluidList.getCompoundTagAt(i);
			fluids.add(FluidStack.loadFluidStackFromNBT(fluidNBT));
		}

		NBTTagList itemList = nbt.getTagList("Items", 0);

		for (int i = 0; i < itemList.tagCount(); ++i)
		{
			NBTTagCompound stackTag = (NBTTagCompound) itemList.getCompoundTagAt(i);
			items.add(ItemStack.loadItemStackFromNBT(stackTag));
		}
	}

	public void writeFluidToNBT(NBTTagCompound nbt)
	{
		NBTTagList fluidList = new NBTTagList();

		for (FluidStack fluid : fluids)
		{
			NBTTagCompound nbtElement = new NBTTagCompound();
			fluid.writeToNBT(nbtElement);
			fluidList.appendTag(nbtElement);
		}

		nbt.setTag("Fluids", fluidList);

		NBTTagList itemList = new NBTTagList();

		for (ItemStack itemStack : items)
		{
			NBTTagCompound var4 = new NBTTagCompound();
			itemStack.writeToNBT(var4);
			itemList.appendTag(var4);
		}

		nbt.setTag("Items", itemList);
	}
}
