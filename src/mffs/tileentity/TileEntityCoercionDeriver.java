package mffs.tileentity;

import ic2.api.item.IElectricItem;

import java.io.IOException;
import java.util.EnumSet;

import mffs.ModularForceFieldSystem;
import mffs.Settings;
import mffs.api.modules.IModule;
import mffs.base.TileEntityMFFSUniversal;
import mffs.fortron.FortronHelper;
import mffs.item.card.ItemCardFrequency;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.item.IItemElectric;
import cofh.api.energy.IEnergyContainerItem;

import com.google.common.io.ByteArrayDataInput;

/**
 * A TileEntity that extract energy into Fortron.
 * 
 * @author Calclavia
 * 
 */
public class TileEntityCoercionDeriver extends TileEntityMFFSUniversal
{
	/**
	 * The amount of KiloWatts this machine uses.
	 */
	public static final int WATTAGE = 20;
	public static final int REQUIRED_TIME = 10 * 20;
	public static final float MULTIPLE_PRODUCTION = 4;
	/** Ration from UE to Fortron. Multiply KJ by this value to convert to Fortron. */
	public static final float UE_FORTRON_RATIO = 1;
	public static final float ENERGY_LOSS = 0.008f;

	public static final int SLOT_FREQUENCY = 0;
	public static final int SLOT_BATTERY = 1;
	public static final int SLOT_FUEL = 2;

	public int processTime = 0;
	public boolean isInversed = false;

	public TileEntityCoercionDeriver()
	{
		super();
		this.capacityBase = 30;
		this.startModuleIndex = 3;
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (!this.worldObj.isRemote)
		{
			if (this.isActive())
			{
				if (this.isInversed && Settings.ENABLE_ELECTRICITY)
				{
					if (this.getEnergyStored() < this.getMaxEnergyStored())
					{
						float withdrawnElectricity = this.requestFortron(this.getProductionRate(), true) / UE_FORTRON_RATIO;
						// Inject electricity from Fortron.
						this.receiveElectricity(withdrawnElectricity * ENERGY_LOSS, true);
					}

					this.recharge(this.getStackInSlot(SLOT_BATTERY));
					this.produce();
				}
				else
				{
					if (this.fortronTank.getFluidAmount() < this.fortronTank.getCapacity())
					{
						// Convert Electricity to Fortron
						this.discharge(this.getStackInSlot(SLOT_BATTERY));

						if (this.provideElectricity(WATTAGE, false).getWatts() >= WATTAGE || (!Settings.ENABLE_ELECTRICITY && this.isItemValidForSlot(SLOT_FUEL, this.getStackInSlot(SLOT_FUEL))))
						{
							// Fill Fortron
							this.fortronTank.fill(FortronHelper.getFortron(this.getProductionRate()), true);
							this.provideElectricity(WATTAGE, true);

							// Use fuel
							if (this.processTime == 0 && this.isItemValidForSlot(SLOT_FUEL, this.getStackInSlot(SLOT_FUEL)))
							{
								this.decrStackSize(SLOT_FUEL, 1);
								this.processTime = REQUIRED_TIME * Math.max(this.getModuleCount(ModularForceFieldSystem.itemModuleSpeed) / 20, 1);
							}

							if (this.processTime > 0)
							{
								// We are processing.
								this.processTime--;

								if (this.processTime < 1)
								{
									this.processTime = 0;
								}
							}
							else
							{
								this.processTime = 0;
							}
						}
					}
				}
			}
		}
		else if (this.isActive())
		{
			this.animation++;
		}
	}

	@Override
	public EnumSet<ForgeDirection> getOutputDirections()
	{
		return EnumSet.allOf(ForgeDirection.class);
	}

	@Override
	public float getRequest(ForgeDirection direction)
	{
		if (this.canConsume())
		{
			return this.getMaxEnergyStored() - this.getEnergyStored();
		}

		return 0;
	}

	@Override
	public float getProvide(ForgeDirection direction)
	{
		if (this.isInversed && this.isActive())
		{
			return this.getEnergyStored();
		}

		return 0;
	}

	/**
	 * @return The Fortron production rate per tick!
	 */
	public int getProductionRate()
	{
		if (this.isActive())
		{
			int production = (int) (WATTAGE * UE_FORTRON_RATIO * Settings.FORTRON_PRODUCTION_MULTIPLIER);

			if (this.processTime > 0)
			{
				production *= MULTIPLE_PRODUCTION;
			}

			return production;
		}

		return 0;
	}

	@Override
	public int getSizeInventory()
	{
		return 6;
	}

	public boolean canConsume()
	{
		if (this.isActive() && !this.isInversed)
		{
			return FortronHelper.getAmount(this.fortronTank) < this.fortronTank.getCapacity();
		}

		return false;
	}

	@Override
	public void onReceivePacket(int packetID, ByteArrayDataInput dataStream) throws IOException
	{
		super.onReceivePacket(packetID, dataStream);

		if (packetID == TilePacketType.TOGGLE_MODE.ordinal())
		{
			this.isInversed = !this.isInversed;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.processTime = nbt.getInteger("processTime");
		this.isInversed = nbt.getBoolean("isInversed");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setInteger("processTime", this.processTime);
		nbt.setBoolean("isInversed", this.isInversed);
	}

	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemStack)
	{
		if (itemStack != null)
		{
			if (slotID >= this.startModuleIndex)
			{
				return itemStack.getItem() instanceof IModule;
			}

			switch (slotID)
			{
				case SLOT_FREQUENCY:
					return itemStack.getItem() instanceof ItemCardFrequency;
				case SLOT_BATTERY:
					return itemStack.getItem() instanceof IItemElectric || itemStack.getItem() instanceof IElectricItem || itemStack.getItem() instanceof IEnergyContainerItem;
				case SLOT_FUEL:
					return itemStack.isItemEqual(new ItemStack(Item.dyePowder, 1, 4)) || itemStack.isItemEqual(new ItemStack(Item.netherQuartz));
			}
		}

		return false;
	}

	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		return true;
	}

	@Override
	public float getMaxEnergyStored()
	{
		return WATTAGE * 2;
	}

}