package resonantinduction.electrical.encoder.coding.args;

import resonantinduction.core.ArgumentData;

/**
 * Used to store lists of data that the user can scroll threw to select one.
 * 
 * @author DarkGuardsman
 */
public class ArgumentListData<O> extends ArgumentData
{
	protected O[] options;

	public ArgumentListData(String name, Object defaultvalue, O... object)
	{
		super(name, defaultvalue);
		this.options = object;
	}

	public O[] getOptions()
	{
		return options;
	}
}
