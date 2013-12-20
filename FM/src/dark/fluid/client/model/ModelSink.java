// Date: 1/22/2013 12:21:32 AM
// Template version 1.1
// Java generated by Techne
// Keep in mind that you still need to fill in some blanks
// - ZeuX

package dark.fluid.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelSink extends ModelBase
{
    // fields
    ModelRenderer Base;
    ModelRenderer FrontLip;
    ModelRenderer BottomLip;
    ModelRenderer RightLip;
    ModelRenderer LeftLip;
    ModelRenderer BLip;
    ModelRenderer Edge;
    ModelRenderer Edge2;
    ModelRenderer Water;

    public ModelSink()
    {
        textureWidth = 128;
        textureHeight = 128;

        Base = new ModelRenderer(this, 0, 0);
        Base.addBox(-7F, 0F, -7F, 14, 12, 14);
        Base.setRotationPoint(0F, 12F, 0F);
        Base.setTextureSize(128, 128);
        Base.mirror = true;
        setRotation(Base, 0F, 0F, 0F);
        FrontLip = new ModelRenderer(this, 10, 62);
        FrontLip.addBox(-8F, -4F, -8F, 16, 4, 2);
        FrontLip.setRotationPoint(0F, 12F, 0F);
        FrontLip.setTextureSize(128, 128);
        FrontLip.mirror = true;
        setRotation(FrontLip, 0F, 0F, 0F);
        BottomLip = new ModelRenderer(this, 5, 37);
        BottomLip.addBox(-8F, -4F, 4F, 16, 4, 4);
        BottomLip.setRotationPoint(0F, 12F, 0F);
        BottomLip.setTextureSize(128, 128);
        BottomLip.mirror = true;
        setRotation(BottomLip, 0F, 0F, 0F);
        RightLip = new ModelRenderer(this, 0, 47);
        RightLip.addBox(-8F, -4F, -6F, 2, 4, 10);
        RightLip.setRotationPoint(0F, 12F, 0F);
        RightLip.setTextureSize(128, 128);
        RightLip.mirror = true;
        setRotation(RightLip, 0F, 0F, 0F);
        LeftLip = new ModelRenderer(this, 25, 47);
        LeftLip.addBox(6F, -4F, -6F, 2, 4, 10);
        LeftLip.setRotationPoint(0F, 12F, 0F);
        LeftLip.setTextureSize(128, 128);
        LeftLip.mirror = true;
        setRotation(LeftLip, 0F, 0F, 0F);
        BLip = new ModelRenderer(this, 9, 32);
        BLip.addBox(-1F, -1F, 4F, 2, 2, 2);
        BLip.setRotationPoint(0F, 12F, 0F);
        BLip.setTextureSize(128, 128);
        BLip.mirror = true;
        setRotation(BLip, 0.5061455F, 0F, 0F);
        Edge = new ModelRenderer(this, 5, 64);
        Edge.addBox(0F, 0F, 0F, 1, 12, 1);
        Edge.setRotationPoint(7F, 12F, 7F);
        Edge.setTextureSize(128, 128);
        Edge.mirror = true;
        setRotation(Edge, 0F, 0F, 0F);
        Edge2 = new ModelRenderer(this, 0, 64);
        Edge2.addBox(0F, 0F, 0F, 1, 12, 1);
        Edge2.setRotationPoint(-8F, 12F, 7F);
        Edge2.setTextureSize(128, 128);
        Edge2.mirror = true;
        setRotation(Edge2, 0F, 0F, 0F);
        Water = new ModelRenderer(this, 0, 0);
        Water.addBox(-6F, 0F, -6F, 12, 0, 10);
        Water.setRotationPoint(0F, 12F, 0F);
        Water.setTextureSize(128, 128);
        Water.mirror = true;
        setRotation(Water, 0F, 0F, 0F);
    }

    public void render(float f5)
    {
        Base.render(f5);
        FrontLip.render(f5);
        BottomLip.render(f5);
        RightLip.render(f5);
        LeftLip.render(f5);
        BLip.render(f5);
        Edge.render(f5);
        Edge2.render(f5);

    }

    public void renderLiquid(float f5, float level)
    {
        Water.setRotationPoint(0F, 12F - level, 0F);
        Water.render(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
