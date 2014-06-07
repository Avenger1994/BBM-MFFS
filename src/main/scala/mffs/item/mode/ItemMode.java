package mffs.item.mode;

import mffs.base.ItemMFFS;
import universalelectricity.api.vector.Vector3;
import calclavia.api.mffs.IFieldInteraction;
import calclavia.api.mffs.IProjector;
import calclavia.api.mffs.modules.IProjectorMode;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class ItemMode extends ItemMFFS implements IProjectorMode
{
	public ItemMode(int i, String name)
	{
		super(i, name);
		this.setMaxStackSize(1);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void render(IProjector projector, double x, double y, double z, float f, long ticks)
	{

	}

	@Override
	public boolean isInField(IFieldInteraction projector, Vector3 position)
	{
		return false;
	}

	@Override
	public float getFortronCost(float amplifier)
	{
		return 8;
	}
}