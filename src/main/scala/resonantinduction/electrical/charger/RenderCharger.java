package resonantinduction.electrical.charger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

import resonant.api.items.ISimpleItemRenderer;
import resonant.lib.render.RenderItemOverlayUtility;
import resonant.lib.render.RenderUtility;
import resonantinduction.core.Reference;
import universalelectricity.api.CompatibilityModule;
import universalelectricity.api.energy.UnitDisplay;
import universalelectricity.api.energy.UnitDisplay.Unit;
import universalelectricity.api.vector.Vector3;

/**
 * Renderer for electric item charger
 * 
 * @author DarkGuardsman
 */
public class RenderCharger implements ISimpleItemRenderer
{
	public static final RenderCharger INSTANCE = new RenderCharger();

	public static final IModelCustom MODEL = AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "charger.tcn");
	public static final ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "charger.png");

	public void render(PartCharger part, double x, double y, double z)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);

		RenderUtility.rotateFaceToSideNoTranslate(part.placementSide);
		RenderUtility.rotateBlockBasedOnDirection(part.getFacing());

		RenderUtility.bind(TEXTURE);
		GL11.glPushMatrix();
		GL11.glTranslatef(0, 0.12f, 0);
		MODEL.renderAll();
		GL11.glPopMatrix();
		GL11.glPopMatrix();

		if (part.getStackInSlot(0) != null)
		{
			GL11.glPushMatrix();
			GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
			RenderUtility.rotateFaceToSideNoTranslate(part.placementSide);
			RenderUtility.rotateBlockBasedOnDirection(part.getFacing());
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
			RenderItemOverlayUtility.renderItem(part.world(), part.placementSide, part.getStackInSlot(0), new Vector3(0.00, -0.3, -0.00), 0, 4);
			GL11.glPopMatrix();

			EntityPlayer player = Minecraft.getMinecraft().thePlayer;
			boolean isLooking = false;

			MovingObjectPosition objectPosition = player.rayTrace(8, 1);

			if (objectPosition != null)
			{
				if (objectPosition.blockX == part.x() && objectPosition.blockY == part.y() && objectPosition.blockZ == part.z())
				{
					/**
					 * Render item and tool tip
					 */
					if (CompatibilityModule.getMaxEnergyItem(part.getStackInSlot(0)) > 0)
					{
						long energy = CompatibilityModule.getEnergyItem(part.getStackInSlot(0));
						long maxEnergy = CompatibilityModule.getMaxEnergyItem(part.getStackInSlot(0));
						RenderUtility.renderFloatingText(UnitDisplay.getDisplay(energy, Unit.JOULES, 2, true) + "/" + UnitDisplay.getDisplay(maxEnergy, Unit.JOULES, 2, true), new Vector3((float) x + 0.5F, (float) y + 0.8F, (float) z + 0.5F));
					}
				}
			}
		}

	}

	@Override
	public void renderInventoryItem(ItemStack itemStack)
	{
		GL11.glTranslatef(0f, 0.5f, 0f);
		RenderUtility.bind(TEXTURE);
		MODEL.renderAll();
	}
}
