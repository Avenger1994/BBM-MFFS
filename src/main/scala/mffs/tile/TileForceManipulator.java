package mffs.tile;

import mffs.field.thread.ManipulatorCalculationThread;
import resonant.api.mffs.Blacklist;
import resonant.api.mffs.EventForceManipulate.EventCheckForceManipulate;
import resonant.api.mffs.card.ICoordLink;
import resonant.api.mffs.modules.IModule;
import resonant.api.mffs.modules.IProjectorMode;
import resonant.api.mffs.security.Permission;
import com.google.common.io.ByteArrayDataInput;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import mffs.event.DelayedEvent;
import mffs.MFFSHelper;
import mffs.ModularForceFieldSystem;
import mffs.Settings;
import mffs.card.ItemCard;
import mffs.event.BlockPreMoveDelayedEvent;
import mffs.render.IEffectController;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import resonant.lib.network.PacketHandler;
import universalelectricity.api.vector.Vector3;
import universalelectricity.api.vector.VectorWorld;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class TileForceManipulator extends TileFieldInteraction implements IEffectController
{
	public static final int PACKET_DISTANCE = 60;
	public static final int ANIMATION_TIME = 20;
	private final Set<Vector3> failedPositions = new LinkedHashSet<Vector3>();
	public Vector3 anchor = null;
	/**
	 * The display mode. 0 = none, 1 = minimal, 2 = maximal.
	 */
	public int displayMode = 1;
	public boolean isCalculatingManipulation = false;
	public Set<Vector3> manipulationVectors = null;
	public boolean doAnchor = true;
	public int clientMoveTime;
	/**
	 * Marking failures
	 */
	public boolean markFailMove = false;
	private boolean markActive = false;

	/**
	 * Used ONLY for teleporting.
	 */
	private int moveTime = 0;
	private boolean canRenderMove = true;

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (this.anchor == null)
		{
			this.anchor = new Vector3();
		}

		if (this.getMode() != null && Settings.ENABLE_MANIPULATOR)
		{
			/**
			 * Manipulator activated and passed all checks. Actually moving the blocks now.
			 */
			if (!this.worldObj.isRemote)
			{
				if (this.manipulationVectors != null && this.manipulationVectors.size() > 0 && !this.isCalculatingManipulation)
				{
					/**
					 * This section is called when blocks set events are set and animation packets
					 * are to be sent.
					 */
					NBTTagCompound nbt = new NBTTagCompound();
					NBTTagList nbtList = new NBTTagList();

					// Number of blocks we're actually moving.
					int i = 0;

					for (Vector3 position : this.manipulationVectors)
					{
						if (this.moveBlock(position) && this.isBlockVisibleByPlayer(position) && i < Settings.MAX_FORCE_FIELDS_PER_TICK)
						{
							nbtList.appendTag(position.writeToNBT(new NBTTagCompound()));
							i++;
						}
					}

					if (i > 0)
					{
						queueEvent(new DelayedEvent(this, getMoveTime())
						{
							@Override protected void onEvent()
							{
								moveEntities();
								PacketHandler.sendPacketToClients(ModularForceFieldSystem.PACKET_TILE.getPacket(TileForceManipulator.this, TilePacketType.FIELD.ordinal()));
							}
						});

						nbt.setByte("type", (byte) 2);
						nbt.setTag("list", nbtList);

						if (!this.isTeleport())
						{
							PacketHandler.sendPacketToClients(ModularForceFieldSystem.PACKET_TILE.getPacket(this, TilePacketType.FXS.ordinal(), (byte) 1, nbt), worldObj, new Vector3(this), PACKET_DISTANCE);

							if (this.getModuleCount(ModularForceFieldSystem.itemModuleSilence) <= 0)
							{
								this.worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, ModularForceFieldSystem.PREFIX + "fieldmove", 0.6f, (1 - this.worldObj.rand.nextFloat() * 0.1f));
							}

							if (this.doAnchor)
							{
								this.anchor = this.anchor.translate(this.getDirection());
							}
						}
						else
						{
							PacketHandler.sendPacketToClients(ModularForceFieldSystem.PACKET_TILE.getPacket(this, TilePacketType.FXS.ordinal(), (byte) 2, this.getMoveTime(), this.getAbsoluteAnchor().translate(0.5), this.getTargetPosition().translate(0.5).writeToNBT(new NBTTagCompound()), false, nbt), worldObj, new Vector3(this), PACKET_DISTANCE);
							this.moveTime = this.getMoveTime();
						}
					}
					else
					{
						this.markFailMove = true;
					}

					this.manipulationVectors = null;
					this.onInventoryChanged();
				}
			}

			/**
			 * While the field is being TELEPORTED ONLY.
			 */
			if (this.moveTime > 0)
			{
				if (this.isTeleport() && this.requestFortron(this.getFortronCost(), true) >= this.getFortronCost())
				{
					if (this.getModuleCount(ModularForceFieldSystem.itemModuleSilence) <= 0 && this.ticks % 10 == 0)
					{
						int moveTime = this.getMoveTime();
						this.worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, ModularForceFieldSystem.PREFIX + "fieldmove", 1.5f, 0.5f + 0.8f * (moveTime - this.moveTime) / moveTime);
					}

					if (--this.moveTime == 0)
					{
						this.worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, ModularForceFieldSystem.PREFIX + "teleport", 0.6f, (1 - this.worldObj.rand.nextFloat() * 0.1f));
					}
				}
				else
				{
					this.markFailMove = true;
				}
			}

			/**
			 * Force Manipulator activated, try start moving...
			 */
			if (isActive())
			{
				markActive = true;
			}

			if (ticks % 20 == 0 && markActive)
			{
				if (moveTime <= 0 && requestFortron(this.getFortronCost(), false) > 0)
				{
					if (!worldObj.isRemote)
					{
						requestFortron(getFortronCost(), true);
						// Start multi-threading calculations
						(new ManipulatorCalculationThread(this)).start();
					}

					moveTime = 0;
				}

				if (!worldObj.isRemote)
				{
					setActive(false);
				}

				markActive = false;
			}

			/**
			 * Render preview
			 */
			if (!this.worldObj.isRemote)
			{
				if (!this.isCalculated)
				{
					this.calculateForceField();
				}

				// Manipulation area preview
				if (this.ticks % 120 == 0 && !this.isCalculating && Settings.HIGH_GRAPHICS && this.delayedEvents.size() <= 0 && this.displayMode > 0)
				{
					NBTTagCompound nbt = new NBTTagCompound();
					NBTTagList nbtList = new NBTTagList();

					int i = 0;
					for (Vector3 position : this.getInteriorPoints())
					{
						if (this.isBlockVisibleByPlayer(position) && (this.displayMode == 2 || !this.worldObj.isAirBlock(position.intX(), position.intY(), position.intZ()) && i < Settings.MAX_FORCE_FIELDS_PER_TICK))
						{
							i++;
							nbtList.appendTag(new Vector3(position).writeToNBT(new NBTTagCompound()));
						}
					}

					nbt.setByte("type", (byte) 1);
					nbt.setTag("list", nbtList);

					if (this.isTeleport())
					{
						Vector3 targetPosition;
						if (getTargetPosition().world == null)
						{
							targetPosition = new Vector3(getTargetPosition());
						}
						else
						{
							targetPosition = getTargetPosition();
						}

						PacketHandler.sendPacketToClients(ModularForceFieldSystem.PACKET_TILE.getPacket(this, TilePacketType.FXS.ordinal(), (byte) 2, 60, getAbsoluteAnchor().translate(0.5), targetPosition.translate(0.5).writeToNBT(new NBTTagCompound()), true, nbt), worldObj, new Vector3(this), PACKET_DISTANCE);
					}
					else
					{
						PacketHandler.sendPacketToClients(ModularForceFieldSystem.PACKET_TILE.getPacket(this, TilePacketType.FXS.ordinal(), (byte) 1, nbt), worldObj, new Vector3(this), PACKET_DISTANCE);
					}
				}
			}

			if (this.markFailMove)
			{
				this.moveTime = 0;
				delayedEvents.clear();
				this.worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, ModularForceFieldSystem.PREFIX + "powerdown", 0.6f, (1 - this.worldObj.rand.nextFloat() * 0.1f));
				PacketHandler.sendPacketToClients(ModularForceFieldSystem.PACKET_TILE.getPacket(this, TilePacketType.RENDER.ordinal()), this.worldObj, new Vector3(this), PACKET_DISTANCE);
				this.markFailMove = false;

				/**
				 * Send failed positions to client to inform them WHICH blocks are causing the field
				 * to fail.
				 */
				NBTTagCompound nbt = new NBTTagCompound();
				NBTTagList nbtList = new NBTTagList();

				for (Vector3 position : this.failedPositions)
				{
					nbtList.appendTag(position.writeToNBT(new NBTTagCompound()));
				}

				nbt.setByte("type", (byte) 1);
				nbt.setTag("list", nbtList);

				this.failedPositions.clear();
				PacketHandler.sendPacketToClients(ModularForceFieldSystem.PACKET_TILE.getPacket(this, TilePacketType.FXS.ordinal(), (byte) 3, nbt), this.worldObj, new Vector3(this), PACKET_DISTANCE);
			}
		}
		else if (!worldObj.isRemote && isActive())
		{
			setActive(false);
		}
	}

	public boolean isBlockVisibleByPlayer(Vector3 position)
	{
		int i = 0;

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
		{
			Vector3 checkPos = position.clone().translate(direction);
			int blockID = checkPos.getBlockID(this.worldObj);

			if (blockID > 0)
			{
				if (Block.blocksList[blockID] != null)
				{
					if (Block.blocksList[blockID].isOpaqueCube())
					{
						i++;
					}
				}
			}
		}

		return !(i >= 6);
	}

	@Override
	public ArrayList getPacketData(int packetID)
	{
		ArrayList objects = super.getPacketData(packetID);
		objects.add(this.moveTime > 0 ? this.moveTime : this.getMoveTime());
		return objects;
	}

	@Override
	public void onReceivePacket(int packetID, ByteArrayDataInput dataStream) throws IOException
	{
		super.onReceivePacket(packetID, dataStream);

		if (this.worldObj.isRemote)
		{
			if (packetID == TilePacketType.FXS.ordinal())
			{
				switch (dataStream.readByte())
				{
					case 1:
					{
						/**
						 * Holographic FXs
						 */
						NBTTagCompound nbt = PacketHandler.readNBTTagCompound(dataStream);
						byte type = nbt.getByte("type");

						NBTTagList nbtList = (NBTTagList) nbt.getTag("list");

						for (int i = 0; i < nbtList.tagCount(); i++)
						{
							Vector3 vector = new Vector3((NBTTagCompound) nbtList.tagAt(i)).translate(0.5);

							if (type == 1)
							{
								// Blue, PREVIEW
								ModularForceFieldSystem.proxy.renderHologram(this.worldObj, vector, 1, 1, 1, 30, vector.clone().translate(this.getDirection()));
							}
							else if (type == 2)
							{
								// Green, DO MOVE
								ModularForceFieldSystem.proxy.renderHologram(this.worldObj, vector, 0, 1, 0, 30, vector.clone().translate(this.getDirection()));
							}
						}
						break;
					}
					case 2:
					{
						int animationTime = dataStream.readInt();
						Vector3 anchorPosition = new Vector3(dataStream.readDouble(), dataStream.readDouble(), dataStream.readDouble());
						VectorWorld targetPosition = new VectorWorld(PacketHandler.readNBTTagCompound(dataStream));
						boolean isPreview = dataStream.readBoolean();

						/**
						 * Holographic Orbit FXs
						 */
						NBTTagCompound nbt = PacketHandler.readNBTTagCompound(dataStream);

						NBTTagList nbtList = (NBTTagList) nbt.getTag("list");

						for (int i = 0; i < nbtList.tagCount(); i++)
						{
							// Render hologram for starting position
							Vector3 vector = new Vector3((NBTTagCompound) nbtList.tagAt(i)).translate(0.5);

							if (isPreview)
							{
								ModularForceFieldSystem.proxy.renderHologramOrbit(this, this.worldObj, anchorPosition, vector, 1, 1, 1, animationTime, 30f);
							}
							else
							{
								ModularForceFieldSystem.proxy.renderHologramOrbit(this, this.worldObj, anchorPosition, vector, 0.1f, 1, 0, animationTime, 30f);
							}

							if (targetPosition.world != null && targetPosition.world.getChunkProvider().chunkExists(targetPosition.intX(), targetPosition.intZ()))
							{
								// Render hologram for destination position
								Vector3 destination = vector.clone().difference(anchorPosition).add(targetPosition);

								if (isPreview)
								{
									ModularForceFieldSystem.proxy.renderHologramOrbit(this, targetPosition.world, targetPosition, destination, 1, 1, 1, animationTime, 30f);
								}
								else
								{
									ModularForceFieldSystem.proxy.renderHologramOrbit(this, targetPosition.world, targetPosition, destination, 0.1f, 1, 0, animationTime, 30f);
								}
							}
						}

						this.canRenderMove = true;
						break;
					}
					case 3:
					{
						/**
						 * Holographic FXs: FAILED TO MOVE
						 */
						NBTTagCompound nbt = PacketHandler.readNBTTagCompound(dataStream);

						NBTTagList nbtList = (NBTTagList) nbt.getTag("list");

						for (int i = 0; i < nbtList.tagCount(); i++)
						{
							Vector3 vector = new Vector3((NBTTagCompound) nbtList.tagAt(i)).translate(0.5);
							ModularForceFieldSystem.proxy.renderHologram(this.worldObj, vector, 1, 0, 0, 30, null);
						}

						break;
					}
				}
			}
			else if (packetID == TilePacketType.RENDER.ordinal())
			{
				this.canRenderMove = false;
			}
			else if (packetID == TilePacketType.FIELD.ordinal())
			{
				this.moveEntities();
			}
			else if (packetID == TilePacketType.DESCRIPTION.ordinal())
			{
				this.clientMoveTime = dataStream.readInt();
			}
		}
		else
		{
			if (packetID == TilePacketType.TOGGLE_MODE.ordinal())
			{
				this.anchor = null;
				this.onInventoryChanged();
			}
			else if (packetID == TilePacketType.TOGGLE_MODE_2.ordinal())
			{
				this.displayMode = (this.displayMode + 1) % 3;
			}
			else if (packetID == TilePacketType.TOGGLE_MODE_3.ordinal())
			{
				this.doAnchor = !this.doAnchor;
			}
		}
	}

	@Override
	public int doGetFortronCost()
	{
		return (int) Math.round(super.doGetFortronCost() + (this.anchor != null ? this.anchor.getMagnitude() * 1000 : 0));
	}

	@Override
	public void onInventoryChanged()
	{
		super.onInventoryChanged();
		this.isCalculated = false;
	}

	/**
	 * Scan target area...
	 */
	protected boolean canMove()
	{
		Set<Vector3> mobilizationPoints = this.getInteriorPoints();
		/** The center in which we want to translate into */
		VectorWorld targetCenterPosition = this.getTargetPosition();

		for (Vector3 position : mobilizationPoints)
		{
			if (!this.worldObj.isAirBlock(position.intX(), position.intY(), position.intZ()))
			{
				// The relative position between this coordinate and the anchor.
				Vector3 relativePosition = position.clone().subtract(this.getAbsoluteAnchor());
				VectorWorld targetPosition = (VectorWorld) targetCenterPosition.clone().add(relativePosition);

				if (!this.canMove(new VectorWorld(this.worldObj, position), targetPosition))
				{
					this.failedPositions.add(position);
					return false;
				}
			}
		}

		return true;
	}

	public boolean canMove(VectorWorld position, VectorWorld target)
	{
		/**
		 * Search for possible failing conditions for the starting position.
		 */
		if (Blacklist.forceManipulationBlacklist.contains(position.getBlockID()))
		{
			return false;
		}

		EventCheckForceManipulate evt = new EventCheckForceManipulate(position.world, position.intX(), position.intY(), position.intZ(), target.intX(), target.intY(), target.intZ());
		MinecraftForge.EVENT_BUS.post(evt);

		if (evt.isCanceled())
		{
			return false;
		}

		TileEntity tileEntity = position.getTileEntity();

		/** Check Permissions */
		if (this.getBiometricIdentifier() != null)
		{
			if (!MFFSHelper.hasPermission(this.worldObj, position, Permission.BLOCK_ALTER, this.getBiometricIdentifier().getOwner()) && !MFFSHelper.hasPermission(target.world, target, Permission.BLOCK_ALTER, this.getBiometricIdentifier().getOwner()))
			{
				return false;
			}
		}
		else if (!MFFSHelper.hasPermission(this.worldObj, position, Permission.BLOCK_ALTER, "") || !MFFSHelper.hasPermission(target.world, target, Permission.BLOCK_ALTER, ""))
		{
			return false;
		}

		if (target.getTileEntity() == this)
		{
			return false;
		}

		/** Check if the target position is current occupied by a block that is GOING to be moved. */
		for (Vector3 checkPos : this.getInteriorPoints())
		{
			if (checkPos.equals(target))
			{
				return true;
			}
		}

		/** Check Target */
		int targetBlockID = target.getBlockID();

		if (!(target.world.isAirBlock(target.intX(), target.intY(), target.intZ()) || (targetBlockID > 0 && (Block.blocksList[targetBlockID].isBlockReplaceable(target.world, target.intX(), target.intY(), target.intZ())))))
		{
			return false;
		}

		return true;
	}

	protected boolean moveBlock(Vector3 position)
	{
		if (!this.worldObj.isRemote)
		{
			Vector3 relativePosition = position.clone().subtract(this.getAbsoluteAnchor());
			VectorWorld newPosition = (VectorWorld) this.getTargetPosition().clone().add(relativePosition);

			TileEntity tileEntity = position.getTileEntity(this.worldObj);
			int blockID = position.getBlockID(this.worldObj);

			if (!this.worldObj.isAirBlock(position.intX(), position.intY(), position.intZ()) && tileEntity != this)
			{
				queueEvent(new BlockPreMoveDelayedEvent(this, getMoveTime(), this.worldObj, position, newPosition));
				return true;
			}
		}

		return false;
	}

	public AxisAlignedBB getSearchAxisAlignedBB()
	{
		Vector3 positiveScale = new Vector3(this).translate(this.getTranslation()).add(this.getPositiveScale()).add(1);
		Vector3 negativeScale = new Vector3(this).translate(this.getTranslation()).subtract(this.getNegativeScale());

		Vector3 minScale = positiveScale.min(negativeScale);
		Vector3 maxScale = positiveScale.max(negativeScale);

		return AxisAlignedBB.getAABBPool().getAABB(minScale.intX(), minScale.intY(), minScale.intZ(), maxScale.intX(), maxScale.intY(), maxScale.intZ());
	}

	/**
	 * Gets the position in which the manipulator will try to translate the field into.
	 *
	 * @return A vector of the target position.
	 */
	public VectorWorld getTargetPosition()
	{
		if (this.isTeleport())
		{
			return ((ICoordLink) this.getCard().getItem()).getLink(this.getCard());
		}

		return (VectorWorld) new VectorWorld(this.worldObj, this.getAbsoluteAnchor()).clone().translate(this.getDirection());
	}

	/**
	 * Gets the movement time required in TICKS.
	 *
	 * @return The time it takes to teleport (using a link card) to another coordinate OR
	 * ANIMATION_TIME for
	 * default move
	 */
	public int getMoveTime()
	{
		if (this.isTeleport())
		{
			int time = (int) (20 * this.getTargetPosition().distance(this.getAbsoluteAnchor()));

			if (this.getTargetPosition().world != this.worldObj)
			{
				time += 20 * 60;
			}

			return time;
		}

		return ANIMATION_TIME;
	}

	private boolean isTeleport()
	{
		if (this.getCard() != null && Settings.allowForceManipulatorTeleport)
		{
			if (this.getCard().getItem() instanceof ICoordLink)
			{
				return ((ICoordLink) this.getCard().getItem()).getLink(this.getCard()) != null;
			}
		}

		return false;
	}

	public Vector3 getAbsoluteAnchor()
	{
		if (this.anchor != null)
		{
			return new Vector3(this).add(this.anchor);
		}
		return new Vector3(this);
	}

	protected void moveEntities()
	{
		VectorWorld targetLocation = this.getTargetPosition();
		AxisAlignedBB axisalignedbb = this.getSearchAxisAlignedBB();

		if (axisalignedbb != null)
		{
			List<Entity> entities = this.worldObj.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb);

			for (Entity entity : entities)
			{
				Vector3 relativePosition = new VectorWorld(entity).clone().subtract(this.getAbsoluteAnchor().translate(0.5));
				VectorWorld newLocation = (VectorWorld) targetLocation.clone().translate(0.5).add(relativePosition);
				moveEntity(entity, newLocation);
			}
		}
	}

	protected void moveEntity(Entity entity, VectorWorld location)
	{
		if (entity != null && location != null)
		{
			if (entity.worldObj.provider.dimensionId != location.world.provider.dimensionId)
			{
				entity.travelToDimension(location.world.provider.dimensionId);
			}

			entity.motionX = 0;
			entity.motionY = 0;
			entity.motionZ = 0;

			if (entity instanceof EntityPlayerMP)
			{
				((EntityPlayerMP) entity).playerNetServerHandler.setPlayerLocation(location.x, location.y, location.z, entity.rotationYaw, entity.rotationPitch);
			}
			else
			{
				entity.setPositionAndRotation(location.x, location.y, location.z, entity.rotationYaw, entity.rotationPitch);
			}
		}
	}

	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemStack)
	{
		if (slotID == 0 || slotID == 1)
		{
			return itemStack.getItem() instanceof ItemCard;
		}
		else if (slotID == MODULE_SLOT_ID)
		{
			return itemStack.getItem() instanceof IProjectorMode;
		}
		else if (slotID >= 15)
		{
			return true;
		}

		return itemStack.getItem() instanceof IModule;
	}

	/**
	 * NBT Methods
	 */
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.anchor = new Vector3(nbt.getCompoundTag("anchor"));
		this.displayMode = nbt.getInteger("displayMode");
		this.doAnchor = nbt.getBoolean("doAnchor");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		if (this.anchor != null)
		{
			nbt.setCompoundTag("anchor", this.anchor.writeToNBT(new NBTTagCompound()));
		}

		nbt.setInteger("displayMode", this.displayMode);
		nbt.setBoolean("doAnchor", this.doAnchor);
	}

	@Override
	public Vector3 getTranslation()
	{
		return super.getTranslation().clone().add(this.anchor);
	}

	@Override
	public int getSizeInventory()
	{
		return 3 + 18;
	}

	@Override
	public String[] getMethodNames()
	{
		return new String[] { "isActivate", "setActivate", "resetAnchor", "canMove" };
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments)
			throws Exception
	{
		switch (method)
		{
			case 2:
			{
				this.anchor = null;
				return null;
			}
			case 3:
			{
				Object[] result = { false };

				if (this.isActive() || this.isCalculatingManipulation)
				{
					// Don't call canMove while it is working because it alters failedPositions
					// return false
					return result;
				}
				else
				{

					result[0] = this.canMove();
					// Clean up the failed positions list so it doesn't trip up the call in update
					// entity later
					this.failedPositions.clear();
					return result;
				}
			}
		}

		return super.callMethod(computer, context, method, arguments);
	}

	@Override
	public boolean canContinueEffect()
	{
		return this.canRenderMove;
	}
}
