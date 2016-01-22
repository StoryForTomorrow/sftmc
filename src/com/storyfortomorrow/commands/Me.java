package com.storyfortomorrow.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.storyfortomorrow.commands.CommandFramework.Command;
import com.storyfortomorrow.commands.CommandFramework.CommandArgs;
import com.storyfortomorrow.commands.CommandFramework.CommandListener;

public class Me implements CommandListener
{
	@Command(command = "me")
	public void me(CommandArgs info)
	{
		Player player = info.getPlayer();
		Bukkit.broadcastMessage("* " + player.getDisplayName() + ":");
	}
}
