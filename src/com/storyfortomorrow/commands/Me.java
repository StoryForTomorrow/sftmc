package com.storyfortomorrow.commands;

import org.bukkit.entity.Player;

import com.storyfortomorrow.Chat;
import com.storyfortomorrow.commands.CommandFramework.Command;
import com.storyfortomorrow.commands.CommandFramework.CommandArgs;
import com.storyfortomorrow.commands.CommandFramework.CommandListener;

public class Me implements CommandListener
{
	@Command(command = "me")
	public void me(CommandArgs info)
	{
		if (info.isPlayer())
		{
			Player player = info.getPlayer();
			if (info.getArgs().length == 0)
				Chat.send(player, "<red>You did not provide me a message! (/me <message>)");
			else
				Chat.broadcast("* %s<reset> %s", player.getDisplayName(), info.getFinalArg(0));
		} else
		{
			if (info.getArgs().length == 0)
				info.getSender().sendMessage("You did not provide me a message! (/me <message>)");
			else
				Chat.broadcast("* Console %s", info.getFinalArg(0));
		}
	}
}
