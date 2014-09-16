package resonantinduction.mechanical.mech;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraftforge.common.util.ForgeDirection;
import resonant.content.prefab.java.TileNode;
import resonant.engine.ResonantEngine;
import resonant.lib.network.discriminator.PacketTile;
import resonant.lib.network.discriminator.PacketType;
import resonant.lib.network.handle.IPacketIDReceiver;
import universalelectricity.api.core.grid.INode;
import universalelectricity.api.core.grid.INodeProvider;
import universalelectricity.core.transform.vector.Vector3;
import codechicken.multipart.ControlKeyModifer;

/** Prefab for resonantinduction.mechanical tiles
 *
 * @author Calclavia */
public abstract class TileMechanical extends TileNode implements INodeProvider, IPacketIDReceiver
{
    protected static final int PACKET_NBT = 0;
    protected static final int PACKET_VELOCITY = 1;

    /** Node that handles most mechanical actions */
    public MechanicalNode mechanicalNode;

    /** External debug GUI */
    DebugFrameMechanical frame = null;

    public TileMechanical(Material material)
    {
        super(material);
        this.mechanicalNode = new MechanicalNode(this);
    }

    @Override
    public void update()
    {
        super.update();

        if(frame != null)
        {
            frame.update();
            if(!frame.isVisible())
            {
                frame.dispose();
                frame = null;
            }
        }

        if (!this.getWorldObj().isRemote)
        {
            if (ticks() % 3 == 0 && (mechanicalNode.markTorqueUpdate || mechanicalNode.markRotationUpdate))
            {
                //ResonantInduction.LOGGER.info("[mechanicalNode] Sending Update");
                sendRotationPacket();
                mechanicalNode.markRotationUpdate = false;
                mechanicalNode.markTorqueUpdate = false;
            }
        }
    }

    @Override
    public boolean use(EntityPlayer player, int side, Vector3 hit)
    {
        ItemStack itemStack = player.getHeldItem();
        if (ResonantEngine.runningAsDev)
        {
            if (itemStack != null && !world().isRemote)
            {
                if (itemStack.getItem() == Items.stick)
                {
                    //Set the nodes debug mode
                    if (ControlKeyModifer.isControlDown(player))
                    {
                        //Opens a debug GUI
                        if (frame == null)
                        {
                            frame = new DebugFrameMechanical(this);
                            frame.showDebugFrame();
                        } //Closes the debug GUI
                        else
                        {
                            frame.closeDebugFrame();
                            frame = null;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public INode getNode(Class<? extends INode> nodeType, ForgeDirection from)
    {
        return mechanicalNode;
    }

    @Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return ResonantEngine.instance.packetHandler.toMCPacket(new PacketTile(this, PACKET_NBT, tag));
    }

    /** Sends the torque and angular velocity to the client */
    private void sendRotationPacket()
    {
        ResonantEngine.instance.packetHandler.sendToAllAround(new PacketTile(this, PACKET_VELOCITY, mechanicalNode.angularVelocity, mechanicalNode.torque), this);
    }

    @Override
    public boolean read(ByteBuf data, int id, EntityPlayer player, PacketType type)
    {
        if (world().isRemote)
        {
            if (id == PACKET_NBT)
            {
                readFromNBT(ByteBufUtils.readTag(data));
                return true;
            }
            else if (id == PACKET_VELOCITY)
            {
                mechanicalNode.angularVelocity = data.readDouble();
                mechanicalNode.torque = data.readDouble();
                return true;
            }
        }
        return false;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        mechanicalNode.load(nbt);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        mechanicalNode.save(nbt);
    }
}
