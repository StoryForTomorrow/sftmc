package master.of.the.tower;

import com.storyfortomorrow.commands.CommandFramework.Command;
import com.storyfortomorrow.commands.CommandFramework.CommandArgs;
import com.storyfortomorrow.commands.CommandFramework.CommandListener;

public class Commands implements CommandListener
{
	@Command(command = "start", permission = "masterofthetower.start")
	public void forceStart(CommandArgs info)
	{
		Manager.start();
	}
	
}
