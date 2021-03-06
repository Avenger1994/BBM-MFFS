package mffs.base;

import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.core.network.packet.PacketTile;
import com.builtbroken.mc.core.network.packet.PacketType;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import mffs.util.FortronUtility;
import mffs.util.TransferMode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.*;
import resonant.api.mffs.fortron.FrequencyGridRegistry;
import resonant.api.mffs.fortron.IFortronFrequency;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/19/2016.
 */
public abstract class TileForton extends TileFrequency implements IFluidHandler, IFortronFrequency
{
    public boolean markSendFortron = true;
    protected FluidTank fortronTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME);

    public TileForton(String name)
    {
        super(name);
    }

    @Override
    public void update()
    {
        super.update();

        if (!worldObj.isRemote && ticks % 60 == 0)
        {
            sendFortronToClients();
        }
    }

    public void sendFortronToClients()
    {
        sendPacket(new PacketTile(this, TilePacketType.fortron.ordinal(), fortronTank.getFluidAmount()));
    }

    @Override
    public void invalidate()
    {
        if (this.markSendFortron)
        {
            FortronUtility.transferFortron(this, FrequencyGridRegistry.SERVER_INSTANCE.getNodes(IFortronFrequency.class, worldObj, toPos(), 100, this.getFrequency()), TransferMode.drain(), Integer.MAX_VALUE);
        }
        super.invalidate();
    }

    @Override
    public void write(ByteBuf buf, int id)
    {
        super.write(buf, id);

        if (id == TilePacketType.fortron.ordinal())
        {
            //TODO improve to reduce sending unneeded data
            ByteBufUtils.writeTag(buf, fortronTank.writeToNBT(new NBTTagCompound()));
        }
    }

    @Override
    public boolean read(ByteBuf buf, int id, EntityPlayer player, PacketType packetType)
    {
        if (!super.read(buf, id, player, packetType))
        {
            if (id == TilePacketType.fortron.ordinal())
            {
                fortronTank.readFromNBT(ByteBufUtils.readTag(buf));
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        fortronTank.setFluid(FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("fortron")));
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        if (fortronTank.getFluid() != null)
        {
            nbt.setTag("fortron", this.fortronTank.getFluid().writeToNBT(new NBTTagCompound()));
        }
    }

    /**
     * Fluid Functions.
     */
    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
    {
        if (resource.isFluidEqual(FortronUtility.fluidstackFortron()))
        {
            return this.fortronTank.fill(resource, doFill);
        }
        return 0;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
    {
        if (resource == null || !resource.isFluidEqual(fortronTank.getFluid()))
        {
            return null;
        }
        return fortronTank.drain(resource.amount, doDrain);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
    {
        return fortronTank.drain(maxDrain, doDrain);
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid)
    {
        return true;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid)
    {
        return true;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from)
    {
        return new FluidTankInfo[]{this.fortronTank.getInfo()};
    }

    @Override
    public int getFortronEnergy()
    {
        return FortronUtility.getAmount(this.fortronTank);
    }

    @Override
    public void setFortronEnergy(int energy)
    {
        this.fortronTank.setFluid(FortronUtility.getFortron(energy));
    }

    @Override
    public int getFortronCapacity()

    {
        return this.fortronTank.getCapacity();
    }

    @Override
    public int requestFortron(int energy, boolean doUse)
    {
        return FortronUtility.getAmount(this.fortronTank.drain(energy, doUse));
    }

    @Override
    public int provideFortron(int energy, boolean doUse)
    {
        return this.fortronTank.fill(FortronUtility.getFortron(energy), doUse);
    }

    /**
     * Gets the amount of empty space this tank has.
     */
    public int getFortronEmpty()
    {
        return fortronTank.getCapacity() - fortronTank.getFluidAmount();
    }
}
