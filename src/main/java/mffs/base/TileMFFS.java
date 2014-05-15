package mffs.base;

import calclavia.api.mffs.IActivatable;
import resonant.lib.network.IPacketReceiver;
import resonant.lib.network.IPacketSender;
import resonant.api.IPlayerUsing;
import resonant.api.IRotatable;
import resonant.lib.prefab.tile.TileAdvanced;
import com.google.common.io.ByteArrayDataInput;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import dan200.computercraft.api.peripheral.IPeripheral;
import mffs.ModularForceFieldSystem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraftforge.common.ForgeDirection;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;

public abstract class TileMFFS extends TileAdvanced
		implements IPacketReceiver, IPacketSender, IPlayerUsing, IRotatable, IActivatable, IPeripheral
{
	/**
	 * The players to send packets to for machine update info.
	 */
	public final HashSet<EntityPlayer> playersUsing = new HashSet<EntityPlayer>();
	/**
	 * Used for client side animations.
	 */
	public float animation = 0;
	/**
	 * Is this machine switched on internally via GUI?
	 */
	public boolean isRedstoneActive = false;
	/**
	 * Is the machine active and working?
	 */
	private boolean isActive = false;

	/**
	 * Override this for packet updating list.
	 */
	@Override
	public ArrayList getPacketData(int packetID)
	{
		ArrayList data = new ArrayList();

		if (packetID == TilePacketType.DESCRIPTION.ordinal())
		{
			data.add(TilePacketType.DESCRIPTION.ordinal());
			data.add(isActive);
			data.add(isRedstoneActive);
		}

		return data;
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		/**
		 * Packet Update for Client only when GUI is open.
		 */
		if (this.ticks % 3 == 0 && this.playersUsing.size() > 0)
		{
			for (EntityPlayer player : this.playersUsing)
			{
				PacketDispatcher.sendPacketToPlayer(this.getDescriptionPacket(), (Player) player);
			}
		}
	}

	@Override
	public Packet getDescriptionPacket()
	{
		return ModularForceFieldSystem.PACKET_TILE.getPacket(this, this.getPacketData(TilePacketType.DESCRIPTION.ordinal()).toArray());
	}

	@Override
	public void onReceivePacket(ByteArrayDataInput data, EntityPlayer player, Object... obj)
	{
		try
		{
			this.onReceivePacket(data.readInt(), data);
		}
		catch (Exception e)
		{
			ModularForceFieldSystem.LOGGER.severe(MessageFormat.format("Packet receiving failed: {0}", this.getClass().getSimpleName()));
			e.printStackTrace();
		}
	}

	/**
	 * Inherit this function to receive packets. Make sure this function is supered.
	 *
	 * @throws IOException
	 */
	public void onReceivePacket(int packetID, ByteArrayDataInput dataStream) throws IOException
	{
		if (packetID == TilePacketType.DESCRIPTION.ordinal())
		{
			boolean prevActive = this.isActive;
			isActive = dataStream.readBoolean();
			isRedstoneActive = dataStream.readBoolean();

			if (prevActive != this.isActive)
			{
				this.worldObj.markBlockForRenderUpdate(this.xCoord, this.yCoord, this.zCoord);
			}
		}
		else if (packetID == TilePacketType.TOGGLE_ACTIVATION.ordinal())
		{
			this.isRedstoneActive = !this.isRedstoneActive;

			if (isRedstoneActive)
			{
				setActive(true);
			}
			else
			{
				setActive(false);
			}
		}
	}

	public boolean isPoweredByRedstone()
	{
		return this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readFromNBT(nbttagcompound);
		this.isActive = nbttagcompound.getBoolean("isActive");
		this.isRedstoneActive = nbttagcompound.getBoolean("isRedstoneActive");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound)
	{
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setBoolean("isActive", this.isActive);
		nbttagcompound.setBoolean("isRedstoneActive", this.isRedstoneActive);
	}

	@Override
	public boolean isActive()
	{
		return this.isActive;
	}

	@Override
	public void setActive(boolean flag)
	{
		this.isActive = flag;
		this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
	}

	/**
	 * Direction Methods
	 */
	@Override
	public ForgeDirection getDirection()
	{
		return ForgeDirection.getOrientation(this.getBlockMetadata());
	}

	@Override
	public void setDirection(ForgeDirection facingDirection)
	{
		this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, facingDirection.ordinal(), 3);
	}

	public void onPowerOn()
	{
		this.setActive(true);
	}

	public void onPowerOff()
	{
		if (!this.isRedstoneActive && !this.worldObj.isRemote)
		{
			this.setActive(false);
		}
	}

	@Override
	public HashSet<EntityPlayer> getPlayersUsing()
	{
		return this.playersUsing;
	}

	
	@Override public boolean equals(IPeripheral other)
	{
		return equals(other);
	}

	public enum TilePacketType
	{
		NONE, DESCRIPTION, FREQUENCY, FORTRON, TOGGLE_ACTIVATION, TOGGLE_MODE, INVENTORY, STRING,
		FXS, TOGGLE_MODE_2, TOGGLE_MODE_3, TOGGLE_MODE_4, FIELD, RENDER;
	}

}