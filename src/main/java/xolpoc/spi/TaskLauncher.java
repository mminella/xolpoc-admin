package xolpoc.spi;

import xolpoc.model.TaskDescriptor;
import xolpoc.model.TaskStatus;

/**
 * @author Michael Minella
 */
public interface TaskLauncher {

	void launch(String taskName, String[] args);

	public TaskStatus getStatus(TaskDescriptor descriptor);
}
