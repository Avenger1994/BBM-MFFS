// Date: 8/27/2012 3:20:21 PM
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
public class ModelGenerator extends ModelBase
{
    //fields
    ModelRenderer BasePlate;
    ModelRenderer LeftConnection;
    ModelRenderer RightConnection;
    ModelRenderer Mid;
    ModelRenderer Mid2;
    ModelRenderer front;
    ModelRenderer front2;
    ModelRenderer front3;
    ModelRenderer Mid3;
    ModelRenderer FrontConnector;

    public ModelGenerator()
    {
        textureWidth = 128;
        textureHeight = 128;

        BasePlate = new ModelRenderer(this, 0, 0);
        BasePlate.addBox(-7F, 0F, -7F, 14, 1, 14);
        BasePlate.setRotationPoint(0F, 23F, 0F);
        BasePlate.setTextureSize(128, 128);
        BasePlate.mirror = true;
        setRotation(BasePlate, 0F, 0F, 0F);
        LeftConnection = new ModelRenderer(this, 0, 112);
        LeftConnection.addBox(-2F, -2F, -2F, 2, 4, 4);
        LeftConnection.setRotationPoint(-6F, 16F, 0F);
        LeftConnection.setTextureSize(128, 128);
        LeftConnection.mirror = true;
        setRotation(LeftConnection, 0F, 0F, 0F);
        RightConnection = new ModelRenderer(this, 12, 112);
        RightConnection.addBox(0F, -2F, -2F, 2, 4, 4);
        RightConnection.setRotationPoint(6F, 16F, 0F);
        RightConnection.setTextureSize(128, 128);
        RightConnection.mirror = true;
        setRotation(RightConnection, 0F, 0F, 0F);
        Mid = new ModelRenderer(this, 0, 29);
        Mid.addBox(-4F, 0F, -6F, 8, 12, 12);
        Mid.setRotationPoint(0F, 10F, 0F);
        Mid.setTextureSize(128, 128);
        Mid.mirror = true;
        setRotation(Mid, 0F, 0F, 0F);
        Mid2 = new ModelRenderer(this, 0, 53);
        Mid2.addBox(-6F, 0F, -6F, 12, 8, 12);
        Mid2.setRotationPoint(0F, 12F, 0F);
        Mid2.setTextureSize(128, 128);
        Mid2.mirror = true;
        setRotation(Mid2, 0F, 0F, 0F);
        front = new ModelRenderer(this, 20, 15);
        front.addBox(-2F, -4F, 0F, 4, 8, 1);
        front.setRotationPoint(0F, 16F, -7F);
        front.setTextureSize(128, 128);
        front.mirror = true;
        setRotation(front, 0F, 0F, 0F);
        front2 = new ModelRenderer(this, 0, 24);
        front2.addBox(-4F, -2F, 0F, 8, 4, 1);
        front2.setRotationPoint(0F, 16F, -7F);
        front2.setTextureSize(128, 128);
        front2.mirror = true;
        setRotation(front2, 0F, 0F, 0F);
        front3 = new ModelRenderer(this, 0, 16);
        front3.addBox(-3F, -3F, 0F, 6, 6, 1);
        front3.setRotationPoint(0F, 16F, -7F);
        front3.setTextureSize(128, 128);
        front3.mirror = true;
        setRotation(front3, 0F, 0F, 0F);
        Mid3 = new ModelRenderer(this, 40, 29);
        Mid3.addBox(-5F, -1F, -6F, 10, 10, 12);
        Mid3.setRotationPoint(0F, 12F, 0F);
        Mid3.setTextureSize(128, 128);
        Mid3.mirror = true;
        setRotation(Mid3, 0F, 0F, 0F);
        FrontConnector = new ModelRenderer(this, 0, 120);
        FrontConnector.addBox(-2F, 0F, -2F, 4, 4, 4);
        FrontConnector.setRotationPoint(0F, 14F, -6F);
        FrontConnector.setTextureSize(128, 128);
        FrontConnector.mirror = true;
        setRotation(FrontConnector, 0F, 0F, 0F);
    }

    public void RenderMain(float f5)
    {
        BasePlate.render(f5);
        Mid.render(f5);
        Mid2.render(f5);
        front.render(f5);
        front2.render(f5);
        front3.render(f5);
        Mid3.render(f5);
        FrontConnector.render(f5);
    }

    public void RenderLeft(float f5)
    {
        LeftConnection.render(f5);
    }

    public void RenderRight(float f5)
    {
        RightConnection.render(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

}
