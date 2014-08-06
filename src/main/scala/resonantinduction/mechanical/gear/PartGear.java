package resonantinduction.mechanical.gear;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import resonant.lib.multiblock.reference.IMultiBlockStructure;
import resonant.lib.utility.WrenchUtility;
import resonantinduction.core.Reference;
import resonantinduction.mechanical.Mechanical;
import resonantinduction.mechanical.energy.grid.PartMechanical;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.FaceMicroClass;
import codechicken.multipart.ControlKeyModifer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import universalelectricity.api.core.grid.INode;
import universalelectricity.core.transform.rotation.EulerAngle;
import universalelectricity.core.transform.vector.VectorWorld;

/** We assume all the force acting on the gear is 90 degrees.
 * 
 * @author Calclavia */
public class PartGear extends PartMechanical implements IMultiBlockStructure<PartGear>
{
    public static Cuboid6[][] oBoxes = new Cuboid6[6][2];

    static
    {
        oBoxes[0][0] = new Cuboid6(1 / 8D, 0, 0, 7 / 8D, 1 / 8D, 1);
        oBoxes[0][1] = new Cuboid6(0, 0, 1 / 8D, 1, 1 / 8D, 7 / 8D);
        for (int s = 1; s < 6; s++)
        {
            Transformation t = Rotation.sideRotations[s].at(Vector3.center);
            oBoxes[s][0] = oBoxes[0][0].copy().apply(t);
            oBoxes[s][1] = oBoxes[0][1].copy().apply(t);
        }
    }

    private boolean isClockwiseCrank = true;
    private int manualCrankTime = 0;
    private int multiBlockRadius = 1;

    public PartGear()
    {
        super();
        node = new GearNode(this);
       
    }

    @Override
    public void update()
    {
        super.update();

        if (!this.world().isRemote)
        {
            if (manualCrankTime > 0)
            {
                node.apply(this, isClockwiseCrank ? 15 : -15, isClockwiseCrank ? 0.025f : -0.025f);
                manualCrankTime--;
            }
        }
        getMultiBlock().update();
    }

    @Override
    public void checkClientUpdate()
    {
        if (getMultiBlock().isPrimary())
            super.checkClientUpdate();
    }

    @Override
    public boolean activate(EntityPlayer player, MovingObjectPosition hit, ItemStack itemStack)
    {
        if (itemStack != null && itemStack.getItem() instanceof ItemHandCrank)
        {
            if (!world().isRemote && ControlKeyModifer.isControlDown(player))
            {
                getMultiBlock().get().node.torque = -getMultiBlock().get().node.torque;
                getMultiBlock().get().node.angularVelocity = -getMultiBlock().get().node.angularVelocity;
                return true;
            }

            isClockwiseCrank = player.isSneaking();
            getMultiBlock().get().manualCrankTime = 20;
            world().playSoundEffect(x() + 0.5, y() + 0.5, z() + 0.5, Reference.prefix() + "gearCrank", 0.5f, 0.9f + world().rand.nextFloat() * 0.2f);
            player.addExhaustion(0.01f);
            return true;
        }

        if (WrenchUtility.isWrench(itemStack))
        {
            getMultiBlock().toggleConstruct();
            return true;
        }

        return super.activate(player, hit, itemStack);
    }

    @Override
    public void preRemove()
    {
        super.preRemove();
        getMultiBlock().deconstruct();
    }

    /** Is this gear block the one in the center-edge of the multiblock that can interact with other
     * gears?
     * 
     * @return */
    public boolean isCenterMultiBlock()
    {
        if (!getMultiBlock().isConstructed())
        {
            return true;
        }

        universalelectricity.core.transform.vector.Vector3 primaryPos = getMultiBlock().getPrimary().getPosition();

        if (primaryPos.xi() == x() && placementSide.offsetX == 0)
        {
            return true;
        }

        if (primaryPos.yi() == y() && placementSide.offsetY == 0)
        {
            return true;
        }

        if (primaryPos.zi() == z() && placementSide.offsetZ == 0)
        {
            return true;
        }

        return false;
    }

    @Override
    protected ItemStack getItem()
    {
        return new ItemStack(Mechanical.itemGear, 1, tier);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderDynamic(Vector3 pos, float frame, int pass)
    {
        if (pass == 0)
        {
            RenderGear.INSTANCE.renderDynamic(this, pos.x, pos.y, pos.z, tier);
        }
    }

    @Override
    public String getType()
    {
        return "resonant_induction_gear";
    }

    @Override
    public void load(NBTTagCompound nbt)
    {
        super.load(nbt);
        getMultiBlock().load(nbt);
    }

    @Override
    public void save(NBTTagCompound nbt)
    {
        super.save(nbt);
        getMultiBlock().save(nbt);
    }

    /** Multiblock */
    private GearMultiBlockHandler multiBlock;

    @Override
    public universalelectricity.core.transform.vector.Vector3[] getMultiBlockVectors()
    {
        return (universalelectricity.core.transform.vector.Vector3[])new universalelectricity.core.transform.vector.Vector3(this.x(), this.y(), this.z()).getAround(this.world(), placementSide, 1).toArray();
    }

    @Override
    public World getWorld()
    {
        return world();
    }

    @Override
    public void onMultiBlockChanged()
    {
        if (world() != null)
        {
            tile().notifyPartChange(this);

            if (!world().isRemote)
            {
                sendDescUpdate();
            }
        }
    }

    @Override
    public GearMultiBlockHandler getMultiBlock()
    {
        if (multiBlock == null)
            multiBlock = new GearMultiBlockHandler(this);

        return multiBlock;
    }

    @Override
    public INode getNode(Class<? extends INode> nodeType, ForgeDirection from)
    {
        if (nodeType.isAssignableFrom(node.getClass()))
            return getMultiBlock().get().node;
        return null;
    }

    /** Multipart Bounds */
    @Override
    public Iterable<Cuboid6> getOcclusionBoxes()
    {
        return Arrays.asList(oBoxes[this.placementSide.ordinal()]);
    }

    @Override
    public int getSlotMask()
    {
        return 1 << this.placementSide.ordinal();
    }

    @Override
    public Cuboid6 getBounds()
    {
        return FaceMicroClass.aBounds()[0x10 | this.placementSide.ordinal()];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Cuboid6 getRenderBounds()
    {
        return Cuboid6.full.copy().expand(multiBlockRadius);
    }

    @Override
    public String toString()
    {
        return "[PartGear]" + x() + "x " + y() + "y " + z() + "z " + getSlotMask() + "s ";
    }
}