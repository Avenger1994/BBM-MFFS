package mffs.gui;

import cpw.mods.fml.common.network.PacketDispatcher;
import mffs.ModularForceFieldSystem;
import mffs.base.GuiMFFS;
import mffs.base.TileMFFS.TilePacketType;
import mffs.field.ContainerForceFieldProjector;
import mffs.gui.button.GuiIcon;
import mffs.field.TileElectromagnetProjector;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;
import resonant.lib.prefab.vector.Rectangle;
import resonant.lib.utility.LanguageUtility;
import universalelectricity.api.energy.UnitDisplay;
import universalelectricity.api.energy.UnitDisplay.Unit;
import universalelectricity.api.energy.UnitDisplay.UnitPrefix;
import universalelectricity.api.vector.Vector2;

public class GuiForceFieldProjector extends GuiMFFS
{
	private TileElectromagnetProjector tileEntity;

	public GuiForceFieldProjector(EntityPlayer player, TileElectromagnetProjector tileEntity)
	{
		super(new ContainerForceFieldProjector(player, tileEntity), tileEntity);
		this.tileEntity = tileEntity;
	}

	@Override
	public void initGui()
	{
		this.textFieldPos = new Vector2(48, 91);
		super.initGui();
		this.buttonList.add(new GuiIcon(1, this.width / 2 - 82, this.height / 2 - 82, null, new ItemStack(Item.compass)));

		this.tooltips.put(new Rectangle(new Vector2(117, 44), new Vector2(117, 44).add(18)), LanguageUtility.getLocal("gui.projector.mode"));

		this.tooltips.put(new Rectangle(new Vector2(90, 17), new Vector2(90, 17).add(18)), LanguageUtility.getLocal("gui.projector.up"));
		this.tooltips.put(new Rectangle(new Vector2(90 + 18 * 3, 17), new Vector2(90 + 18 * 3, 17).add(18)), LanguageUtility.getLocal("gui.projector.up"));

		this.tooltips.put(new Rectangle(new Vector2(90, 17 + 18 * 3), new Vector2(90, 17 + 18 * 3).add(18)), LanguageUtility.getLocal("gui.projector.down"));
		this.tooltips.put(new Rectangle(new Vector2(90 + 18 * 3, 17 + 18 * 3), new Vector2(90 + 18 * 3, 17 + 18 * 3).add(18)), LanguageUtility.getLocal("gui.projector.down"));

		String north = LanguageUtility.getLocal("gui.projector.north");
		String south = LanguageUtility.getLocal("gui.projector.south");
		String west = LanguageUtility.getLocal("gui.projector.west");
		String east = LanguageUtility.getLocal("gui.projector.east");

		if (!this.tileEntity.isAbsolute)
		{
			north = LanguageUtility.getLocal("gui.projector.front");
			south = LanguageUtility.getLocal("gui.projector.back");
			west = LanguageUtility.getLocal("gui.projector.left");
			east = LanguageUtility.getLocal("gui.projector.right");
		}

		this.tooltips.put(new Rectangle(new Vector2(90 + 18 * 1, 17), new Vector2(90 + 18 * 1, 17).add(18)), north);
		this.tooltips.put(new Rectangle(new Vector2(90 + 18 * 2, 17), new Vector2(90 + 18 * 2, 17).add(18)), north);

		this.tooltips.put(new Rectangle(new Vector2(90 + 18 * 1, 17 + 18 * 3), new Vector2(90 + 18 * 1, 17 + 18 * 3).add(18)), south);
		this.tooltips.put(new Rectangle(new Vector2(90 + 18 * 2, 17 + 18 * 3), new Vector2(90 + 18 * 2, 17 + 18 * 3).add(18)), south);

		this.tooltips.put(new Rectangle(new Vector2(90, 17 + 18 * 1), new Vector2(90 + 18 * 1, 17 + 18 * 1).add(18)), west);
		this.tooltips.put(new Rectangle(new Vector2(90, 17 + 18 * 2), new Vector2(90 + 18 * 1, 17 + 18 * 2).add(18)), west);

		this.tooltips.put(new Rectangle(new Vector2(90 + 18 * 3, 17 + 18 * 1), new Vector2(90 + 18 * 3, 17 + 18 * 1).add(18)), east);
		this.tooltips.put(new Rectangle(new Vector2(90 + 18 * 3, 17 + 18 * 2), new Vector2(90 + 18 * 3, 17 + 18 * 2).add(18)), east);
	}

	@Override
	public void updateScreen()
	{
		super.updateScreen();
		((GuiIcon) this.buttonList.get(1)).setIndex(this.tileEntity.isAbsolute ? 1 : 0);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y)
	{
		this.fontRenderer.drawString(this.tileEntity.getInvName(), this.xSize / 2 - this.fontRenderer.getStringWidth(this.tileEntity.getInvName()) / 2, 6, 4210752);

		GL11.glPushMatrix();
		GL11.glRotatef(-90, 0, 0, 1);
		this.fontRenderer.drawString(this.tileEntity.getDirection().name(), -82, 10, 4210752);
		GL11.glPopMatrix();

		this.drawTextWithTooltip("matrix", 40, 20, x, y);
		this.textFieldFrequency.drawTextBox();

		this.drawTextWithTooltip("fortron", "%1: " + UnitDisplay.getDisplayShort(this.tileEntity.getFortronEnergy(), Unit.LITER) + "/" + UnitDisplay.getDisplay(this.tileEntity.getFortronCapacity(), Unit.LITER, UnitPrefix.MILLI), 8, 110, x, y);
		this.fontRenderer.drawString("\u00a74-" + UnitDisplay.getDisplayShort(this.tileEntity.getFortronCost() * 20, Unit.LITER) + "/s", 118, 121, 4210752);
		super.drawGuiContainerForegroundLayer(x, y);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y)
	{
		super.drawGuiContainerBackgroundLayer(f, x, y);

		// Frequency Card Slot
		this.drawSlot(9, 88);
		this.drawSlot(9 + 18, 88);

		/**
		 * Matrix Slots
		 */

		// Mode
		this.drawSlot(117, 44, SlotType.NONE, 1f, 0.4f, 0.4f);

		// Directional Modules
		for (int xSlot = 0; xSlot < 4; xSlot++)
		{
			for (int ySlot = 0; ySlot < 4; ySlot++)
			{
				if (!(xSlot == 1 && ySlot == 1) && !(xSlot == 2 && ySlot == 2) && !(xSlot == 1 && ySlot == 2) && !(xSlot == 2 && ySlot == 1))
				{
					SlotType type = SlotType.NONE;

					if (xSlot == 0 && ySlot == 0)
					{
						type = SlotType.ARR_UP_LEFT;
					}
					else if (xSlot == 0 && ySlot == 3)
					{
						type = SlotType.ARR_DOWN_LEFT;
					}
					else if (xSlot == 3 && ySlot == 0)
					{
						type = SlotType.ARR_UP_RIGHT;
					}
					else if (xSlot == 3 && ySlot == 3)
					{
						type = SlotType.ARR_DOWN_RIGHT;
					}
					else if (ySlot == 0)
					{
						type = SlotType.ARR_UP;
					}
					else if (ySlot == 3)
					{
						type = SlotType.ARR_DOWN;
					}
					else if (xSlot == 0)
					{
						type = SlotType.ARR_LEFT;
					}
					else if (xSlot == 3)
					{
						type = SlotType.ARR_RIGHT;
					}

					this.drawSlot(90 + 18 * xSlot, 17 + 18 * ySlot, type);
				}
			}
		}

		// General
		for (int xSlot = 0; xSlot < 3; xSlot++)
		{
			for (int ySlot = 0; ySlot < 2; ySlot++)
			{
				this.drawSlot(30 + 18 * xSlot, 35 + 18 * ySlot);
			}
		}

		// Fortron Bar
		this.drawForce(8, 120, Math.min((float) this.tileEntity.getFortronEnergy() / (float) this.tileEntity.getFortronCapacity(), 1));
	}

	@Override
	protected void actionPerformed(GuiButton guiButton)
	{
		super.actionPerformed(guiButton);

		if (guiButton.id == 1)
		{
			PacketDispatcher.sendPacketToServer(ModularForceFieldSystem.PACKET_TILE.getPacket((TileEntity) this.frequencyTile, TilePacketType.TOGGLE_MODE_4.ordinal()));
		}
	}
}