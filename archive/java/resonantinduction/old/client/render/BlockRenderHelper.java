package resonantinduction.old.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import resonantinduction.old.client.model.ModelConstructionPump;
import resonantinduction.old.client.model.ModelGearRod;
import resonantinduction.old.client.model.ModelGenerator;
import resonantinduction.old.client.model.ModelPump;
import resonantinduction.old.client.model.ModelSink;
import resonantinduction.old.core.recipe.RecipeLoader;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BlockRenderHelper implements ISimpleBlockRenderingHandler
{
	public static BlockRenderHelper instance = new BlockRenderHelper();
	public static int renderID = RenderingRegistry.getNextAvailableRenderId();
	private ModelPump modelPump = new ModelPump();
	private ModelGearRod modelRod = new ModelGearRod();
	private ModelGenerator modelGen = new ModelGenerator();
	private ModelSink sink = new ModelSink();
	private ModelConstructionPump conPump = new ModelConstructionPump();

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer)
	{
		GL11.glPushMatrix();
		if (RecipeLoader.blockPumpMachine != null && block.blockID == RecipeLoader.blockPumpMachine.blockID && metadata < 4)
		{
			GL11.glTranslatef(0.0F, 1.1F, 0.0F);
			GL11.glRotatef(180f, 0f, 0f, 1f);

			FMLClientHandler.instance().getClient().renderEngine.bindTexture(new ResourceLocation(Reference.DOMAIN, Reference.MODEL_DIRECTORY + "pumps/WaterPump.png"));
			modelPump.render(0.0725F);
			modelPump.renderMotion(0.0725F, 0);
		}
		else if (RecipeLoader.blockSink != null && block.blockID == RecipeLoader.blockSink.blockID)
		{
			GL11.glTranslatef(0.0F, .8F, 0.0F);
			GL11.glRotatef(180f, 0f, 0f, 1f);
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(new ResourceLocation(Reference.DOMAIN, Reference.MODEL_DIRECTORY + "Sink.png"));
			sink.render(0.0565F);
		}
		else if (RecipeLoader.blockRod != null && block.blockID == RecipeLoader.blockRod.blockID)
		{
			GL11.glTranslatef(0.0F, 1.5F, 0.0F);
			GL11.glRotatef(180f, 0f, 0f, 1f);
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(new ResourceLocation(Reference.DOMAIN, Reference.MODEL_DIRECTORY + "resonantinduction/mechanical/GearRod.png"));
			modelRod.render(0.0825F, 0);
		}
		else if (RecipeLoader.blockConPump != null && block.blockID == RecipeLoader.blockConPump.blockID && metadata < 4)
		{
			GL11.glTranslatef(0.0F, 1.2F, 0.0F);
			GL11.glRotatef(180f, 0f, 0f, 1f);
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(new ResourceLocation(Reference.DOMAIN, Reference.MODEL_DIRECTORY + "ConstructionPump.png"));
			conPump.render(0.0725F);
			conPump.renderMotor(0.0725F);

		}
		else if (RecipeLoader.frackingPipe != null && block.blockID == RecipeLoader.frackingPipe.blockID)
		{
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(RenderFrackingPipe.TEXTURE);
			GL11.glTranslatef(0, 1F, 0);
			GL11.glScalef(1.0F, -1F, -1F);
			RenderFrackingPipe.model.renderAll();
		}
		GL11.glPopMatrix();
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		return false;
	}

	@Override
	public boolean shouldRender3DInInventory()
	{

		return true;
	}

	@Override
	public int getRenderId()
	{
		return renderID;
	}
}
