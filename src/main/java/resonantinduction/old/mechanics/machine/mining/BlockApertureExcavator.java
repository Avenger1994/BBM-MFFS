package resonantinduction.old.mechanics.machine.mining;

import java.util.Set;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import resonantinduction.core.Settings;
import universalelectricity.api.UniversalElectricity;
import calclavia.lib.prefab.block.BlockMachine;

import com.builtbroken.common.Pair;

/** @author Archadia */
public class BlockApertureExcavator extends BlockMachine
{

    public BlockApertureExcavator()
    {
        super(Settings.CONFIGURATION, "Machine_ApertureExcavator", UniversalElectricity.machine);
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata)
    {
        return new TileApertureExcavator();
    }

    @Override
    public void getTileEntities(int blockID, Set<Pair<String, Class<? extends TileEntity>>> list)
    {
        list.add(new Pair<String, Class<? extends TileEntity>>("TileApertureExcavator", TileApertureExcavator.class));
    }
}