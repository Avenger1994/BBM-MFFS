package mffs.item.module.interdiction;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import calclavia.api.mffs.security.IInterdictionMatrix;
import resonant.lib.utility.LanguageUtility;

public class ItemModuleWarn extends ItemModuleInterdictionMatrix
{
	public ItemModuleWarn(int i)
	{
		super(i, "moduleWarn");
	}

	@Override
	public boolean onDefend(IInterdictionMatrix interdictionMatrix, EntityLivingBase entityLiving)
	{
		boolean hasPermission = false;
		if (!hasPermission && entityLiving instanceof EntityPlayer)
		{
			((EntityPlayer) entityLiving).addChatMessage("[" + interdictionMatrix.getInvName() + "] " + LanguageUtility.getLocal("message.moduleWarn.warn"));
		}

		return false;
	}
}