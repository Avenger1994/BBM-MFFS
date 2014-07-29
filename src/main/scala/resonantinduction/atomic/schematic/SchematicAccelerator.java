package resonantinduction.atomic.schematic;

import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraftforge.common.util.ForgeDirection;
import resonant.lib.schematic.Schematic;
import resonant.lib.type.Pair;
import resonantinduction.atomic.Atomic;
import universalelectricity.core.transform.vector.Vector3;

public class SchematicAccelerator extends Schematic
{
    @Override
    public String getName()
    {
        return "schematic.accelerator.name";
    }

    @Override
    public HashMap<Vector3, Pair<Block, Integer>> getStructure(ForgeDirection dir, int size)
    {
        HashMap<Vector3, Pair<Block, Integer>> returnMap = new HashMap<Vector3, Pair<Block, Integer>>();

        int r = size;

        for (int x = -r; x < r; x++)
        {
            for (int z = -r; z < r; z++)
            {
                for (int y = -1; y <= 1; y++)
                {
                    if (x == -r || x == r - 1 || z == -r || z == r - 1)
                    {
                        returnMap.put(new Vector3(x, y, z), new Pair(Atomic.blockElectromagnet, 0));
                    }
                }
            }
        }

        r = size - 2;

        for (int x = -r; x < r; x++)
        {
            for (int z = -r; z < r; z++)
            {
                for (int y = -1; y <= 1; y++)
                {
                    if (x == -r || x == r - 1 || z == -r || z == r - 1)
                    {
                        returnMap.put(new Vector3(x, y, z), new Pair(Atomic.blockElectromagnet, 0));
                    }
                }
            }
        }

        r = size - 1;

        for (int x = -r; x < r; x++)
        {
            for (int z = -r; z < r; z++)
            {
                for (int y = -1; y <= 1; y++)
                {
                    if (x == -r || x == r - 1 || z == -r || z == r - 1)
                    {
                        if (y == -1 || y == 1)
                        {
                            returnMap.put(new Vector3(x, y, z), new Pair(Atomic.blockElectromagnet, 1));
                        }
                        else if (y == 0)
                        {
                            returnMap.put(new Vector3(x, y, z), new Pair(0, 0));
                        }
                    }
                }
            }
        }

        return returnMap;
    }
}
