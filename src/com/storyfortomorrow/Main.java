/**
 * 
 */
package com.storyfortomorrow;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import com.storyfortomorrow.commands.CommandFramework;

/**
 * Main class. Where it all happens.
 * 
 * @author JohnnPM
 * @version development
 */
public class Main extends JavaPlugin
{
	public Main plugin;
	private CommandFramework commandFramework;

	@Override
	public void onLoad()
	{
		plugin = this;
		commandFramework = new CommandFramework(plugin);
	}

	@Override
	public void onEnable()
	{
		commandFramework.registerCommands();
		commandFramework.registerHelp();
		commandFramework.registerEvents(this);
	}

	@Override
	public void onDisable()
	{
		
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		return commandFramework.handleCommand(sender, label, cmd, args);
	}
}
