package resonantinduction.electrical.armbot.task;

import resonantinduction.electrical.armbot.TaskBaseProcess;

public class TaskReturn extends TaskRotateTo
{
	public TaskReturn()
	{
		super("Return", 0, 0);
	}

	@Override
	public TaskBaseProcess clone()
	{
		return new TaskReturn();
	}

}
