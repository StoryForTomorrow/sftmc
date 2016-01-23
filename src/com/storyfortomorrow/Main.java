/**
 * 
 */
package com.storyfortomorrow;

import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import com.storyfortomorrow.commands.CommandFramework;

import master.of.the.tower.Manager;

/**
 * Main class. Where it all happens.
 * 
 * @author JohnnPM
 * @version development
 */
public class Main extends JavaPlugin
{
	public static Main plugin;
	public static Main getPlugin()
	{
		return plugin;
	}
	private CommandFramework commandFramework;
	
	public static Logger log;

	@Override
	public void onLoad()
	{
		plugin = this;
		log = plugin.getLogger();
		commandFramework = new CommandFramework(plugin);
	}

	@Override
	public void onEnable()
	{
		commandFramework.registerCommands();
		commandFramework.registerHelp();
		commandFramework.registerEvents(this);
		
		Manager.init();
		
		log.info("Enabled");
	}

	@Override
	public void onDisable()
	{
		log.info("Disabled");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		return commandFramework.handleCommand(sender, label, cmd, args);
	}
	
}
