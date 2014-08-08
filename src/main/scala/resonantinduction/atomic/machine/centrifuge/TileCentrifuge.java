package resonantinduction.atomic.machine.centrifuge;


import io.netty.buffer.ByteBuf;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import resonant.api.IRotatable;
import resonant.engine.ResonantEngine;
import resonant.lib.network.discriminator.PacketTile;
import resonant.lib.network.discriminator.PacketType;
import resonant.lib.network.handle.IPacketReceiver;
import resonantinduction.atomic.Atomic;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.Settings;
import universalelectricity.compatibility.Compatibility;
import universalelectricity.core.transform.vector.Vector3;

import com.google.common.io.ByteArrayDataInput;
import resonant.lib.content.prefab.java.TileElectricInventory;

/** Centrifuge TileEntity */
public class TileCentrifuge extends TileElectricInventory implements IPacketReceiver, IFluidHandler
{
    public static final int SHI_JIAN = 20 * 60;

    public static final long DIAN = 500000;
    public final FluidTank gasTank = new FluidTank(Atomic.FLUIDSTACK_URANIUM_HEXAFLOURIDE.copy(), FluidContainerRegistry.BUCKET_VOLUME * 5);
    public int timer = 0;
    public float rotation = 0;

    public TileCentrifuge()
    {
        super(Material.iron);
        isOpaqueCube(true);
        normalRender(false);
        electricNode().energy().setCapacity(DIAN * 2);
        setSizeInventory(4);
    }

    @Override
    public void update()
    {
        super.update();

        if (timer > 0)
        {
            rotation += 0.45f;
        }

        if (!this.worldObj.isRemote)
        {
            /** Look for nearby tanks that contains uranium gas and try to extract it. */
            if (this.ticks() % 20 == 0)
            {
                for (int i = 0; i < 6; i++)
                {
                    ForgeDirection direction = ForgeDirection.getOrientation(i);
                    TileEntity tileEntity = new Vector3(this).add( direction).getTileEntity(world());

                    if (tileEntity instanceof IFluidHandler && tileEntity.getClass() != this.getClass())
                    {
                        IFluidHandler fluidHandler = ((IFluidHandler) tileEntity);

                        if (fluidHandler != null)
                        {
                            FluidStack requestFluid = Atomic.FLUIDSTACK_URANIUM_HEXAFLOURIDE.copy();
                            requestFluid.amount = this.gasTank.getCapacity() - Atomic.getFluidAmount(this.gasTank.getFluid());
                            FluidStack receiveFluid = fluidHandler.drain(direction.getOpposite(), requestFluid, true);

                            if (receiveFluid != null)
                            {
                                if (receiveFluid.amount > 0)
                                {
                                    if (this.gasTank.fill(receiveFluid, false) > 0)
                                    {
                                        this.gasTank.fill(receiveFluid, true);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (this.nengYong())
            {
                this.discharge(getStackInSlot(0));

                if (electricNode().energy().extractEnergy(TileCentrifuge.DIAN, false) >= DIAN)
                {
                    if (this.timer == 0)
                    {
                        this.timer = TileCentrifuge.SHI_JIAN;
                    }

                    if (this.timer > 0)
                    {
                        this.timer--;

                        if (this.timer < 1)
                        {
                            this.yong();
                            this.timer = 0;
                        }
                    }
                    else
                    {
                        this.timer = 0;
                    }

                    electricNode().energy().extractEnergy(DIAN, true);
                }
            }
            else
            {
                this.timer = 0;
            }

            if (this.ticks() % 10 == 0)
            {
                //for (EntityPlayer player : this.getPlayersUsing())
                //{
                //    PacketDispatcher.sendPacketToPlayer(getDescriptionPacket(), (Player) player);
                //}
            }
        }
    }

    @Override
    public boolean use(EntityPlayer player, int side, Vector3 hit)
    {
        openGui(player, Atomic.INSTANCE);
        return true;
    }

    @Override
    public void read(ByteBuf data, EntityPlayer player, PacketType type)
    {
        try
        {
            this.timer = data.readInt();
            this.gasTank.setFluid(new FluidStack(Atomic.FLUIDSTACK_URANIUM_HEXAFLOURIDE.fluidID, data.readInt()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public Packet getDescriptionPacket()
    {
        return ResonantEngine.instance.packetHandler.toMCPacket(new PacketTile(this, this.timer, Atomic.getFluidAmount(this.gasTank.getFluid())));
    }

    /** @return If the machine can be used. */
    public boolean nengYong()
    {
        if (this.gasTank.getFluid() != null)
        {
            if (this.gasTank.getFluid().amount >= Settings.uraniumHexaflourideRatio())
            {
                return isItemValidForSlot(2, new ItemStack(Atomic.itemUranium)) && isItemValidForSlot(3, new ItemStack(Atomic.itemUranium, 1, 1));
            }
        }

        return false;
    }

    /** Turn one item from the furnace source stack into the appropriate smelted item in the furnace result stack */
    public void yong()
    {
        if (this.nengYong())
        {
            this.gasTank.drain(Settings.uraniumHexaflourideRatio(), true);

            if (this.worldObj.rand.nextFloat() > 0.6)
            {
                this.incrStackSize(2, new ItemStack(Atomic.itemUranium));
            }
            else
            {
                this.incrStackSize(3, new ItemStack(Atomic.itemUranium, 1, 1));
            }
        }
    }

    /** Reads a tile entity from NBT. */
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        this.timer = nbt.getInteger("smeltingTicks");

        NBTTagCompound compound = nbt.getCompoundTag("gas");
        this.gasTank.setFluid(FluidStack.loadFluidStackFromNBT(compound));
    }

    /** Writes a tile entity to NBT. */
    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setInteger("smeltingTicks", this.timer);

        if (this.gasTank.getFluid() != null)
        {
            NBTTagCompound compound = new NBTTagCompound();
            this.gasTank.getFluid().writeToNBT(compound);
            nbt.setTag("gas", compound);
        }
    }

    /** Tank Methods */
    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
    {
        if (Atomic.FLUIDSTACK_URANIUM_HEXAFLOURIDE.isFluidEqual(resource))
        {
            return this.gasTank.fill(resource, doFill);
        }

        return 0;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
    {
        return null;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
    {
        return null;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid)
    {
        return Atomic.FLUIDSTACK_URANIUM_HEXAFLOURIDE.fluidID == fluid.getID();
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid)
    {
        return false;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from)
    {
        return new FluidTankInfo[]
        { this.gasTank.getInfo() };
    }

    /** Inventory */
    @Override
    public int[] getAccessibleSlotsFromSide(int side)
    {
        return side == 1 ? new int[]
        { 0, 1 } : new int[]
        { 2, 3 };
    }

    @Override
    public boolean canInsertItem(int slotID, ItemStack itemStack, int side)
    {
        return slotID == 1 && this.isItemValidForSlot(slotID, itemStack);
    }

    @Override
    public boolean canExtractItem(int slotID, ItemStack itemstack, int j)
    {
        return slotID == 2 || slotID == 3;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemStack)
    {
        switch (i)
        {
        case 0:
            return Compatibility.isHandler(itemStack.getItem());
        case 1:
            return true;
        case 2:
            return itemStack.getItem() == Atomic.itemUranium;
        case 3:
            return itemStack.getItem() == Atomic.itemUranium;
        }

        return false;
    }
}
