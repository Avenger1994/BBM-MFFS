// Date: 8/14/2012 1:48:41 AM
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
public class ModelCornerTank extends ModelBase
{
    //Corner
    ModelRenderer Shape1;
    ModelRenderer Shape2;
    ModelRenderer Shape3;
    ModelRenderer Shape6;
    ModelRenderer Shape7;
    ModelRenderer Shape4;

    public ModelCornerTank(float par1)
    {
        textureWidth = 128;
        textureHeight = 128;

        //corner
        Shape1 = new ModelRenderer(this, 0, 1);
        Shape1.addBox(0F, 0F, 0F, 1, 16, 20);
        Shape1.setRotationPoint(7F, 8F, -7F);
        Shape1.setTextureSize(128, 128);
        Shape1.mirror = true;
        setRotation(Shape1, 0F, -0.7853982F, 0F);
        Shape2 = new ModelRenderer(this, 44, 0);
        Shape2.addBox(0F, 0F, 0F, 2, 16, 2);
        Shape2.setRotationPoint(-8F, 8F, 6F);
        Shape2.setTextureSize(128, 128);
        Shape2.mirror = true;
        setRotation(Shape2, 0F, 0F, 0F);
        Shape3 = new ModelRenderer(this, 44, 0);
        Shape3.addBox(0F, 0F, 0F, 2, 16, 2);
        Shape3.setRotationPoint(6F, 8F, -8F);
        Shape3.setTextureSize(128, 128);
        Shape3.mirror = true;
        setRotation(Shape3, 0F, 0F, 0F);
        Shape6 = new ModelRenderer(this, 0, 44);
        Shape6.addBox(0F, 0F, 0F, 1, 15, 13);
        Shape6.setRotationPoint(-8F, 9F, -7F);
        Shape6.setTextureSize(128, 128);
        Shape6.mirror = true;
        setRotation(Shape6, 0F, 0F, 0F);
        Shape7 = new ModelRenderer(this, 0, 73);
        Shape7.addBox(0F, 0F, 0F, 14, 15, 1);
        Shape7.setRotationPoint(-8F, 9F, -8F);
        Shape7.setTextureSize(128, 128);
        Shape7.mirror = true;
        setRotation(Shape7, 0F, 0F, 0F);
        Shape4 = new ModelRenderer(this, 0, 92);
        Shape4.addBox(0F, 0F, 0F, 16, 1, 16);
        Shape4.setRotationPoint(-8F, 8F, -8F);
        Shape4.setTextureSize(128, 128);
        Shape4.mirror = true;
        setRotation(Shape4, 0F, 0F, 0F);
    }

    public void renderCorner(float f5)
    {
        Shape1.render(f5);
        Shape2.render(f5);
        Shape3.render(f5);
        Shape6.render(f5);
        Shape7.render(f5);
        Shape4.render(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

}
