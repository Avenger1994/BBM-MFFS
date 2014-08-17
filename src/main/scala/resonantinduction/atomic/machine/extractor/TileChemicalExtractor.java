package resonantinduction.atomic.machine.extractor;

import net.minecraft.block.material.Material;
import net.minecraft.network.Packet;
import resonant.engine.ResonantEngine;
import resonant.lib.network.discriminator.PacketAnnotation;
import resonantinduction.atomic.Atomic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import resonant.api.IRotatable;
import resonant.lib.network.Synced;
import resonantinduction.atomic.Atomic;
import resonantinduction.atomic.AtomicContent;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.Settings;
import universalelectricity.compatibility.Compatibility;
import universalelectricity.core.transform.vector.Vector3;

/** Chemical extractor TileEntity */

public class TileChemicalExtractor extends TileProcess implements IFluidHandler
{
    public static final int TICK_TIME = 20 * 14;
    public static final int EXTRACT_SPEED = 100;
    public static final long ENERGY = 5000;
    @Synced
    public final FluidTank inputTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME * 10);
    @Synced
    public final FluidTank outputTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME * 10);
    // How many ticks has this item been extracting for?
    @Synced
    public int time = 0;
    public float rotation = 0;

    public TileChemicalExtractor()
    {
        super(Material.iron);
        energy().setCapacity(ENERGY * 2);
        this.setSizeInventory(7);
        this.isOpaqueCube(false);
        this.normalRender(false);
        inputSlot = 1;
        outputSlot = 2;
        tankInputFillSlot = 3;
        tankInputDrainSlot = 4;
        tankOutputFillSlot = 5;
        tankOutputDrainSlot = 6;
    }

    @Override
    public void update()
    {
        super.update();

        if (time > 0)
        {
            rotation += 0.2f;
        }

        if (!worldObj.isRemote)
        {
            if (canUse())
            {
                discharge(getStackInSlot(0));

                if (energy().checkExtract(ENERGY))
                {
                    if (time == 0)
                    {
                        time = TICK_TIME;
                    }

                    if (time > 0)
                    {
                        time--;

                        if (time < 1)
                        {
                            if (!refineUranium())
                            {
                                if (!extractTritium())
                                {
                                    extractDeuterium();
                                }
                            }

                            time = 0;
                        }
                    }
                    else
                    {
                        time = 0;
                    }
                }

                energy().extractEnergy(ENERGY, true);
            }
            else
            {
                time = 0;
            }

            if (ticks() % 10 == 0)
            {
                //for (EntityPlayer player : getPlayersUsing())
                //{
                //    PacketDispatcher.sendPacketToPlayer(getDescriptionPacket(), (Player) player);
                //}
            }
        }
    }

    @Override
    public Packet getDescriptionPacket()
    {
        return ResonantEngine.instance.packetHandler.toMCPacket(new PacketAnnotation(this));
    }

    @Override
    public boolean use(EntityPlayer player, int side, Vector3 hit)
    {
        openGui(player, Atomic.INSTANCE());
        return true;
    }


    public boolean canUse()
    {
        if (inputTank.getFluid() != null)
        {
            if (inputTank.getFluid().amount >= FluidContainerRegistry.BUCKET_VOLUME && Atomic.isItemStackUraniumOre(getStackInSlot(inputSlot)))
            {
                if (isItemValidForSlot(outputSlot, new ItemStack(AtomicContent.itemYellowCake())))
                {
                    return true;
                }
            }

            if (outputTank.getFluidAmount() < outputTank.getCapacity())
            {
                if (inputTank.getFluid().getFluid().getID() == AtomicContent.FLUID_DEUTERIUM().getID() && inputTank.getFluid().amount >= Settings.deutermiumPerTritium() * EXTRACT_SPEED)
                {
                    if (outputTank.getFluid() == null || AtomicContent.FLUIDSTACK_TRITIUM().equals(outputTank.getFluid()))
                    {
                        return true;
                    }
                }

                if (inputTank.getFluid().getFluid().getID() == FluidRegistry.WATER.getID() && inputTank.getFluid().amount >= Settings.waterPerDeutermium() * EXTRACT_SPEED)
                {
                    if (outputTank.getFluid() == null || AtomicContent.FLUIDSTACK_DEUTERIUM().equals(outputTank.getFluid()))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /** Turn one item from the furnace source stack into the appropriate smelted item in the furnace result stack. */
    public boolean refineUranium()
    {
        if (canUse())
        {
            if (Atomic.isItemStackUraniumOre(getStackInSlot(inputSlot)))
            {
                inputTank.drain(FluidContainerRegistry.BUCKET_VOLUME, true);
                incrStackSize(outputSlot, new ItemStack(AtomicContent.itemYellowCake(), 3));
                decrStackSize(inputSlot, 1);
                return true;
            }
        }

        return false;
    }

    public boolean extractDeuterium()
    {
        if (canUse())
        {
            FluidStack drain = inputTank.drain(Settings.waterPerDeutermium() * EXTRACT_SPEED, false);

            if (drain != null && drain.amount >= 1 && drain.getFluid().getID() == FluidRegistry.WATER.getID())
            {
                if (outputTank.fill(new FluidStack(AtomicContent.FLUIDSTACK_DEUTERIUM(), EXTRACT_SPEED), true) >= EXTRACT_SPEED)
                {
                    inputTank.drain(Settings.waterPerDeutermium() * EXTRACT_SPEED, true);
                    return true;
                }
            }
        }

        return false;
    }

    public boolean extractTritium()
    {
        if (canUse())
        {
            int waterUsage = Settings.deutermiumPerTritium();

            FluidStack drain = inputTank.drain(Settings.deutermiumPerTritium() * EXTRACT_SPEED, false);

            if (drain != null && drain.amount >= 1 && drain.getFluid().getID() == AtomicContent.FLUID_DEUTERIUM().getID())
            {
                if (outputTank.fill(new FluidStack(AtomicContent.FLUIDSTACK_TRITIUM(), EXTRACT_SPEED), true) >= EXTRACT_SPEED)
                {
                    inputTank.drain(Settings.deutermiumPerTritium() * EXTRACT_SPEED, true);
                    return true;
                }
            }
        }

        return false;
    }

    /** Reads a tile entity from NBT. */
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);

        time = nbt.getInteger("time");
        NBTTagCompound water = nbt.getCompoundTag("inputTank");
        inputTank.setFluid(FluidStack.loadFluidStackFromNBT(water));
        NBTTagCompound deuterium = nbt.getCompoundTag("outputTank");
        outputTank.setFluid(FluidStack.loadFluidStackFromNBT(deuterium));
    }

    /** Writes a tile entity to NBT. */
    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setInteger("time", time);

        if (inputTank.getFluid() != null)
        {
            NBTTagCompound compound = new NBTTagCompound();
            inputTank.getFluid().writeToNBT(compound);
            nbt.setTag("inputTank", compound);
        }

        if (outputTank.getFluid() != null)
        {
            NBTTagCompound compound = new NBTTagCompound();
            outputTank.getFluid().writeToNBT(compound);
            nbt.setTag("outputTank", compound);
        }
    }

    /** Tank Methods */
    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
    {
        if (resource != null && canFill(from, resource.getFluid()))
        {
            return inputTank.fill(resource, doFill);
        }

        return 0;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
    {
        return drain(from, resource.amount, doDrain);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
    {
        return outputTank.drain(maxDrain, doDrain);
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid)
    {
        return FluidRegistry.WATER.getID() == fluid.getID() || AtomicContent.FLUID_DEUTERIUM().getID() == fluid.getID();
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid)
    {
        return outputTank.getFluid() != null && outputTank.getFluid().getFluid().getID() == fluid.getID();
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from)
    {
        return new FluidTankInfo[]
        { this.inputTank.getInfo(), this.outputTank.getInfo() };
    }

    @Override
    public boolean isItemValidForSlot(int slotID, ItemStack itemStack)
    {
        // Water input for machine.
        if (slotID == 0)
        {
            return Compatibility.isHandler(itemStack.getItem());
        }

        if (slotID == 1)
        {
            return Atomic.isItemStackWaterCell(itemStack);
        }

        // Empty cell to be filled with deuterium or tritium.
        if (slotID == 2)
        {
            return Atomic.isItemStackDeuteriumCell(itemStack) || Atomic.isItemStackTritiumCell(itemStack);
        }

        // Uranium to be extracted into yellowcake.
        if (slotID == 3)
        {
            return Atomic.isItemStackEmptyCell(itemStack) || Atomic.isItemStackUraniumOre(itemStack) || Atomic.isItemStackDeuteriumCell(itemStack);
        }

        return false;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side)
    {
        return new int[]
        { 1, 2, 3 };
    }

    @Override
    public boolean canInsertItem(int slotID, ItemStack itemStack, int side)
    {
        return this.isItemValidForSlot(slotID, itemStack);
    }

    @Override
    public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
    {
        return slotID == 2;
    }

    @Override
    public FluidTank getInputTank()
    {
        return inputTank;
    }

    @Override
    public FluidTank getOutputTank()
    {
        return outputTank;
    }

}
