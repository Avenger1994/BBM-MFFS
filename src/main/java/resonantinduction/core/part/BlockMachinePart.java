package resonantinduction.core.part;

import resonantinduction.core.Reference;
import resonantinduction.core.prefab.block.BlockBase;

/**
 * A block used to build machines.
 * 
 * @author Calclavia
 * 
 */
public class BlockMachinePart extends BlockBase
{
	public BlockMachinePart()
	{
		super("machinePart");
		setTextureName(Reference.PREFIX + "blockSteel");
	}
}
