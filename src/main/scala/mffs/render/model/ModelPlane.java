package mffs.render.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * A plane model.
 * 
 * @author Calclavia
 * 
 */
@SideOnly(Side.CLIENT)
public class ModelPlane extends ModelBase
{
	public static final ModelPlane INSTNACE = new ModelPlane();
	private ModelRenderer cube;

	public ModelPlane()
	{
		this.cube = new ModelRenderer(this, 0, 0);
		int size = 16;
		this.cube.addBox(-size / 8, -size / 2, -size / 2, size / 6, size, size);
		this.cube.setTextureSize(112, 70);
		this.cube.mirror = true;
	}

	public void render()
	{
		float f = 0.0625f;
		this.cube.render(f);
	}
}
