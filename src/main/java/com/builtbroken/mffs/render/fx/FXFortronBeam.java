package com.builtbroken.mffs.render.fx;

import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mffs.Reference;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

/**
 * Based off Thaumcraft's Beam Renderer.
 *
 * @author Calclavia, Azanor
 */

public class FXFortronBeam extends FXBeam
{
    private static final ResourceLocation FXBEAM = new ResourceLocation(Reference.domain, Reference.blockDirectory + "fortron.png");

    public FXFortronBeam(World world, Pos position, Pos target, float red, float green, float blue, int age)
    {
        super(FXBEAM, world, position, target, red, green, blue, age);
        noClip = true;
    }
}