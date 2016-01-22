package com.storyfortomorrow.commands;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.help.GenericCommandHelpTopic;
import org.bukkit.help.HelpTopic;
import org.bukkit.help.HelpTopicComparator;
import org.bukkit.help.IndexHelpTopic;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Command framework so you don't have to go through the awful process of using
 * the plugin.yml file for command registration.
 *
 * @author minnymin3, modified by JohnnPM
 * @version 1.0-BETA
 */
public class CommandFramework
{
	private final Map<String, Entry<Method, Object>> commandMap = new HashMap<String, Entry<Method, Object>>();
	private CommandMap map;
	private Plugin plugin;

	/**
	 * Initializes the command framework and sets up the command maps
	 * 
	 * @param plugin
	 */
	public CommandFramework(Plugin plugin)
	{
		this.plugin = plugin;
		if (plugin.getServer().getPluginManager() instanceof SimplePluginManager)
		{
			SimplePluginManager manager = (SimplePluginManager) plugin.getServer().getPluginManager();
			try
			{
				Field field = SimplePluginManager.class.getDeclaredField("commandMap");
				field.setAccessible(true);
				map = (CommandMap) field.get(manager);
			} catch (IllegalArgumentException e)
			{
				e.printStackTrace();
			} catch (NoSuchFieldException e)
			{
				e.printStackTrace();
			} catch (IllegalAccessException e)
			{
				e.printStackTrace();
			} catch (SecurityException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Handles commands. Used in the onCommand method in your JavaPlugin class
	 * 
	 * @param sender
	 *            The {@link org.bukkit.command.CommandSender} parsed from
	 *            onCommand
	 * @param label
	 *            The label parsed from onCommand
	 * @param cmd
	 *            The {@link org.bukkit.command.Command} parsed from onCommand
	 * @param args
	 *            The arguments parsed from onCommand
	 * @return Always returns true for simplicity's sake in onCommand
	 */
	public boolean handleCommand(CommandSender sender, String label, org.bukkit.command.Command cmd, String[] args)
	{
		for (int i = args.length; i >= 0; i--)
		{
			StringBuilder buffer = new StringBuilder();
			buffer.append(label.toLowerCase());
			for (int x = 0; x < i; x++)
			{
				buffer.append(".").append(args[x].toLowerCase());
			}
			String cmdLabel = buffer.toString();
			if (commandMap.containsKey(cmdLabel))
			{
				Entry<Method, Object> entry = commandMap.get(cmdLabel);
				Command command = entry.getKey().getAnnotation(Command.class);
				if (!sender.hasPermission(command.permission()))
				{
					sender.sendMessage(command.noPerm());
					return true;
				}
				try
				{
					entry.getKey().invoke(entry.getValue(),
							new CommandArgs(sender, cmd, label, args, cmdLabel.split("\\.").length - 1));
				} catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e)
				{
					e.printStackTrace();
				}
				return true;
			}
		}
		defaultCommand(new CommandArgs(sender, cmd, label, args, 0));
		return true;
	}

	/**
	 * Registers all command and completer methods inside of the object. Similar
	 * to Bukkit's registerEvents method.
	 * 
	 * @param obj
	 *            The object to register the commands of
	 */
	public void registerCommands(Object obj)
	{
		for (Method m : obj.getClass().getMethods())
		{
			if (m.getAnnotation(Command.class) != null)
			{
				Command command = m.getAnnotation(Command.class);
				if (m.getParameterTypes().length > 1 || m.getParameterTypes()[0] != CommandArgs.class)
				{
					System.out.println("Unable to register command " + m.getName() + ". Unexpected method arguments");
					continue;
				}
				registerCommand(command, command.command(), m, obj);
				for (String alias : command.aliases())
				{
					registerCommand(command, alias, m, obj);
				}
			} else if (m.getAnnotation(Completer.class) != null)
			{
				Completer comp = m.getAnnotation(Completer.class);
				if (m.getParameterTypes().length > 1 || m.getParameterTypes().length == 0
						|| m.getParameterTypes()[0] != CommandArgs.class)
				{
					System.out.println(
							"Unable to register tab completer " + m.getName() + ". Unexpected method arguments");
					continue;
				}
				if (m.getReturnType() != List.class)
				{
					System.out.println("Unable to register tab completer " + m.getName() + ". Unexpected return type");
					continue;
				}
				registerCompleter(comp.command(), m, obj);
				for (String alias : comp.aliases())
				{
					registerCompleter(alias, m, obj);
				}
			}
		}
	}

	/**
	 * Dynamically registers all commands in project.
	 * 
	 * @author Not2EXceL
	 * @see Not2EXceL's CommandAPI
	 */
	public void registerCommands()
	{
		Class<?>[] classes = ClassEnumerator.getInstance().getClassesFromThisJar(plugin);
		if (classes == null || classes.length == 0)
		{
			return;
		}
		for (Class<?> c : classes)
		{
			try
			{
				if (CommandListener.class.isAssignableFrom(c) && !c.isInterface() && !c.isEnum() && !c.isAnnotation())
				{
					if (JavaPlugin.class.isAssignableFrom(c))
					{
						if (plugin.getClass().equals(c))
						{
							plugin.getLogger().log(Level.INFO, "Searching Class: " + c.getSimpleName());
							registerCommands(plugin);
						}
					} else
					{
						plugin.getLogger().log(Level.INFO, "Searching class: " + c.getSimpleName());
						registerCommands(c.newInstance());
					}
				}
			} catch (InstantiationException e)
			{
				plugin.getLogger().log(Level.INFO, c.getSimpleName() + " does not use the default constructor");

				e.printStackTrace();
			} catch (IllegalAccessException e)
			{
				plugin.getLogger().log(Level.INFO, c.getSimpleName() + " does not use the default constructor");

				e.printStackTrace();
			}
		}
	}

	public void registerEvents(Plugin plugin)
	{
		Class<?>[] classes = ClassEnumerator.getInstance().getClassesFromThisJar(plugin);
		if (classes == null || classes.length == 0)
		{
			return;
		}
		for (Class<?> c : classes)
		{
			try
			{
				if (Listener.class.isAssignableFrom(c) && !c.isInterface() && !c.isEnum() && !c.isAnnotation())
				{
					if (JavaPlugin.class.isAssignableFrom(c))
					{
						if (plugin.getClass().equals(c))
						{
							Bukkit.getPluginManager().registerEvents((Listener) plugin, plugin);
						}
					} else
					{
						Bukkit.getPluginManager().registerEvents((Listener) c.newInstance(), plugin);
					}
				}
			} catch (InstantiationException e)
			{
				plugin.getLogger().log(Level.INFO, c.getSimpleName() + " does not use the default constructor.");
				e.printStackTrace();
			} catch (IllegalAccessException e)
			{
				plugin.getLogger().log(Level.INFO, c.getSimpleName() + " does not use the default constructor.");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Registers all the commands under the plugin's help
	 */
	public void registerHelp()
	{
		Set<HelpTopic> help = new TreeSet<HelpTopic>(HelpTopicComparator.helpTopicComparatorInstance());
		for (String s : commandMap.keySet())
		{
			if (!s.contains("."))
			{
				org.bukkit.command.Command cmd = map.getCommand(s);
				HelpTopic topic = new GenericCommandHelpTopic(cmd);
				help.add(topic);
			}
		}
		IndexHelpTopic topic = new IndexHelpTopic(plugin.getName(), "All commands for " + plugin.getName(), null, help,
				"Below is a list of all " + plugin.getName() + " commands:");
		Bukkit.getServer().getHelpMap().addTopic(topic);
	}

	private void registerCommand(Command command, String label, Method m, Object obj)
	{
		Entry<Method, Object> entry = new AbstractMap.SimpleEntry<Method, Object>(m, obj);
		commandMap.put(label.toLowerCase(), entry);
		String cmdLabel = label.replace(".", ",").split(",")[0].toLowerCase();
		if (map.getCommand(cmdLabel) == null)
		{
			org.bukkit.command.Command cmd = new BukkitCommand(cmdLabel, plugin);
			map.register(plugin.getName(), cmd);
		}
		if (!command.description().equalsIgnoreCase("Much Description. So Wow.") && cmdLabel == label)
		{
			map.getCommand(cmdLabel).setDescription(command.description());
		}
		if (!command.usage().equalsIgnoreCase("Much Usage. So Wow.") && cmdLabel == label)
		{
			map.getCommand(cmdLabel).setUsage(command.usage());
		}
	}

	private void registerCompleter(String label, Method m, Object obj)
	{
		String cmdLabel = label.replace(".", ",").split(",")[0].toLowerCase();
		if (map.getCommand(cmdLabel) == null)
		{
			org.bukkit.command.Command command = new BukkitCommand(cmdLabel, plugin);
			map.register(plugin.getName(), command);
		}
		if (map.getCommand(cmdLabel) instanceof BukkitCommand)
		{
			BukkitCommand command = (BukkitCommand) map.getCommand(cmdLabel);
			if (command.completer == null)
			{
				command.completer = new BukkitCompleter();
			}
			command.completer.addCompleter(label, m, obj);
		} else if (map.getCommand(cmdLabel) instanceof PluginCommand)
		{
			try
			{
				Object command = map.getCommand(cmdLabel);
				Field field = command.getClass().getDeclaredField("completer");
				field.setAccessible(true);
				if (field.get(command) == null)
				{
					BukkitCompleter completer = new BukkitCompleter();
					completer.addCompleter(label, m, obj);
					field.set(command, completer);
				} else if (field.get(command) instanceof BukkitCompleter)
				{
					BukkitCompleter completer = (BukkitCompleter) field.get(command);
					completer.addCompleter(label, m, obj);
				} else
				{
					System.out.println("Unable to register tab completer " + m.getName()
							+ ". A tab completer is already registered for that command!");
				}
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

	private void defaultCommand(CommandArgs args)
	{
		args.getSender().sendMessage(args.getLabel() + " is not handled!");
	}

	/**
	 * Command Framework - Command <br>
	 * The command annotation used to designate methods as commands. All methods
	 * should have a single CommandArgs argument
	 * <p>
	 * Parameters
	 * <li>command() - the name of the command</li>
	 * <li>permission() - the commands permission ex. permission.me</li>
	 * <li>noPerm() - the no permission message</li>
	 * <li>aliases() - array of other names for the command</li>
	 * <li>description - pretty straightforward</li>
	 * <li>usage - the commands usage</li>
	 * 
	 * @author minnymin3
	 */
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Command
	{

		/**
		 * The name of the command. If it is a sub command then its values would
		 * be separated by periods. ie. a command that would be a subcommand of
		 * test would be 'test.subcommandname'
		 * 
		 * @return
		 */
		public String command();

		/**
		 * Gets the required permission of the command
		 * 
		 * @return
		 */
		public String permission() default "";

		/**
		 * The message sent to the player when they do not have permission to
		 * execute it
		 * 
		 * @return
		 */
		public String noPerm() default "Much Deny. So Wow.";

		/**
		 * A list of alternate names that the command is executed under. See
		 * name() for details on how names work
		 * 
		 * @return
		 */
		public String[] aliases() default
		{};

		/**
		 * The description that will appear in /help of the command
		 * 
		 * @return
		 */
		public String description() default "Much Description. So Wow.";

		/**
		 * The usage that will appear in /help (commandname)
		 * 
		 * @return
		 */
		public String usage() default "Much Usage. So Wow.";
	}

	/**
	 * Command Framework - Completer <br>
	 * The completer annotation used to designate methods as command completers.
	 * All methods should have a single CommandArgs argument and return a String
	 * List object
	 * 
	 * @author minnymin3
	 */
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Completer
	{

		/**
		 * The command that this completer completes. If it is a sub command
		 * then its values would be separated by periods. ie. a command that
		 * would be a subcommand of test would be 'test.subcommandname'
		 * 
		 * @return
		 */
		String command();

		/**
		 * A list of alternate names that the completer is executed under. See
		 * name() for details on how names work
		 * 
		 * @return
		 */
		String[] aliases() default
		{};

	}

	/**
	 * Command Framework - BukkitCommand <br>
	 * An implementation of Bukkit's Command class allowing for registering of
	 * commands without plugin.yml
	 * 
	 * @author minnymin3
	 */
	class BukkitCommand extends org.bukkit.command.Command
	{

		private final Plugin owningPlugin;
		protected BukkitCompleter completer;
		private CommandExecutor executor;

		/**
		 * A slimmed down PluginCommand
		 * 
		 * @param label
		 * @param owner
		 */
		protected BukkitCommand(String label, Plugin owner)
		{
			super(label);
			this.executor = owner;
			this.owningPlugin = owner;
			this.usageMessage = "";
		}

		@Override
		public boolean execute(CommandSender sender, String commandLabel, String[] args)
		{
			boolean success = false;

			if (!owningPlugin.isEnabled())
			{
				return false;
			}

			if (!testPermission(sender))
			{
				return true;
			}

			try
			{
				success = executor.onCommand(sender, this, commandLabel, args);
			} catch (Throwable ex)
			{
				throw new CommandException("Unhandled exception executing command '" + commandLabel + "' in plugin "
						+ owningPlugin.getDescription().getFullName(), ex);
			}

			if (!success && usageMessage.length() > 0)
			{
				for (String line : usageMessage.replace("<command>", commandLabel).split("\n"))
				{
					sender.sendMessage(line);
				}
			}

			return success;
		}

		@Override
		public List<String> tabComplete(CommandSender sender, String alias, String[] args)
				throws CommandException, IllegalArgumentException
		{
			Validate.notNull(sender, "Sender cannot be null");
			Validate.notNull(args, "Arguments cannot be null");
			Validate.notNull(alias, "Alias cannot be null");

			List<String> completions = null;
			try
			{
				if (completer != null)
				{
					completions = completer.onTabComplete(sender, this, alias, args);
				}
				if (completions == null && executor instanceof TabCompleter)
				{
					completions = ((TabCompleter) executor).onTabComplete(sender, this, alias, args);
				}
			} catch (Throwable ex)
			{
				StringBuilder message = new StringBuilder();
				message.append("Unhandled exception during tab completion for command '/").append(alias).append(' ');
				for (String arg : args)
				{
					message.append(arg).append(' ');
				}
				message.deleteCharAt(message.length() - 1).append("' in plugin ")
						.append(owningPlugin.getDescription().getFullName());
				throw new CommandException(message.toString(), ex);
			}

			if (completions == null)
			{
				return super.tabComplete(sender, alias, args);
			}
			return completions;
		}

		public CommandExecutor getExecutor()
		{
			return executor;
		}

		public void setExecutor(CommandExecutor executor)
		{
			this.executor = executor;
		}

	}

	/**
	 * Command Framework - BukkitCompleter <br>
	 * An implementation of the TabCompleter class allowing for multiple tab
	 * completers per command
	 * 
	 * @author minnymin3
	 */
	class BukkitCompleter implements TabCompleter
	{

		private final Map<String, Entry<Method, Object>> completers = new HashMap<String, Entry<Method, Object>>();

		public void addCompleter(String label, Method m, Object obj)
		{
			completers.put(label, new AbstractMap.SimpleEntry<Method, Object>(m, obj));
		}

		@SuppressWarnings("unchecked")
		@Override
		public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String label,
				String[] args)
		{
			for (int i = args.length; i >= 0; i--)
			{
				StringBuilder buffer = new StringBuilder();
				buffer.append(label.toLowerCase());
				for (int x = 0; x < i; x++)
				{
					if (!args[x].equals("") && !args[x].equals(" "))
					{
						buffer.append(".").append(args[x].toLowerCase());
					}
				}
				String cmdLabel = buffer.toString();
				if (completers.containsKey(cmdLabel))
				{
					Entry<Method, Object> entry = completers.get(cmdLabel);
					try
					{
						return (List<String>) entry.getKey().invoke(entry.getValue(),
								new CommandArgs(sender, command, label, args, cmdLabel.split("\\.").length - 1));
					} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e)
					{
						e.printStackTrace();
					}
				}
			}
			return null;
		}
	}

	/**
	 * Command Framework - CommandArgs <br>
	 * This class is passed to the command methods and contains various
	 * utilities as well as the command info.
	 * 
	 * @author minnymin3
	 */
	public class CommandArgs
	{

		private final CommandSender sender;
		private final org.bukkit.command.Command command;
		private final String label, description, permission, no_permission, usage, name;
		private final String[] args;

		protected CommandArgs(CommandSender sender, org.bukkit.command.Command command, String label, String[] args,
				int subCommand)
		{
			String[] modArgs = new String[args.length - subCommand];
			System.arraycopy(args, 0 + subCommand, modArgs, 0, args.length - subCommand);

			StringBuilder buffer = new StringBuilder();
			buffer.append(label);
			for (int x = 0; x < subCommand; x++)
			{
				buffer.append(".").append(args[x]);
			}
			String cmdLabel = buffer.toString();
			this.sender = sender;
			this.command = command;
			this.label = cmdLabel;
			this.args = modArgs;
			this.description = command.getDescription();
			this.permission = command.getPermission();
			this.no_permission = command.getPermissionMessage();
			this.usage = command.getUsage();
			this.name = command.getName();
		}

		/**
		 * Gets the command sender
		 * 
		 * @return sender
		 */
		public CommandSender getSender()
		{
			return sender;
		}

		/**
		 * Gets the original command object
		 * 
		 * @return
		 */
		public org.bukkit.command.Command getCommand()
		{
			return command;
		}

		/**
		 * Gets the label including sub command labels of this command
		 * 
		 * @return Something like 'test.subcommand'
		 */
		public String getLabel()
		{
			return label;
		}

		/**
		 * Gets all the arguments after the command's label. ie. if the command
		 * label was test.subcommand and the arguments were subcommand foo foo,
		 * it would only return 'foo foo' because 'subcommand' is part of the
		 * command
		 * 
		 * @return
		 */
		public String[] getArgs()
		{
			return args;
		}

		/**
		 * The description of the command
		 * 
		 * @return the description
		 */
		public String getDescription()
		{
			return description;
		}

		/**
		 * Permission of the command
		 * 
		 * @return the permission
		 */
		public String getPermission()
		{
			return permission;
		}

		/**
		 * No Permission Message of command
		 * 
		 * @return the no_permission
		 */
		public String getNoPermission()
		{
			return no_permission;
		}

		/**
		 * Usage of the command
		 * 
		 * @return the usage
		 */
		public String getUsage()
		{
			return usage;
		}

		/**
		 * Name of command
		 * 
		 * @return the name
		 */
		public String getName()
		{
			return name;
		}

		/**
		 * True if is player command sender
		 * 
		 * @return
		 */
		public boolean isPlayer()
		{
			return sender instanceof Player;
		}

		/**
		 * @return Player if commandSender is a player else returns false
		 */
		public Player getPlayer()
		{
			if (sender instanceof Player)
			{
				return (Player) sender;
			} else
			{
				return null;
			}
		}

		/**
		 * From Essentials
		 * 
		 * @param start
		 *            The number of where to start
		 * @return The string
		 */
		public String getFinalArg(final int start)
		{
			final StringBuilder bldr = new StringBuilder();
			for (int i = start; i < args.length; i++)
			{
				if (i != start)
				{
					bldr.append(" ");
				}
				bldr.append(args[i]);
			}
			return bldr.toString();
		}
	}

	/**
	 * CommandFramework - CommandListener <br>
	 * For a class to use @Command annotation it must implement CommandListener
	 * 
	 * @author Not2EXceL
	 */
	public interface CommandListener
	{

	}

	/**
	 * Command Framework - Util Class
	 * 
	 * @author Not2EXceL
	 */
	public static class ClassEnumerator
	{
		private static volatile ClassEnumerator instance;

		public static ClassEnumerator getInstance()
		{
			if (instance == null)
				;
			{
				instance = new ClassEnumerator();
				return instance;
			}
		}

		public List<Class<?>> getClassesFromLocation(File location)
		{
			final List<Class<?>> classes = new ArrayList<Class<?>>();
			if (location.isDirectory())
			{
				for (File file : Arrays.asList(location.listFiles()))
				{
					try
					{
						ClassLoader classLoader = new URLClassLoader(new URL[]
						{ file.toURI().toURL() }, this.getClass().getClassLoader());
						if (file.getName().toLowerCase().trim().endsWith(".class"))
						{
							classes.add(classLoader.loadClass(file.getName().replace(".class", "").replace("/", ".")));
						} else if (file.getName().toLowerCase().trim().endsWith(".jar"))
						{
							classes.addAll(getClassesFromJar(file, classLoader));
						} else if (file.isDirectory())
						{
							classes.addAll(getClassesFromLocation(file));
						}
					} catch (MalformedURLException e)
					{
						e.printStackTrace();
					} catch (ClassNotFoundException e)
					{
						e.printStackTrace();
					}
				}
			} else
			{
				try
				{
					ClassLoader classLoader = new URLClassLoader(new URL[]
					{ location.toURI().toURL() }, this.getClass().getClassLoader());
					if (location.getName().toLowerCase().trim().endsWith(".class"))
					{
						classes.add(classLoader.loadClass(location.getName().replace(".class", "").replace("/", ".")));
					} else if (location.getName().toLowerCase().trim().endsWith(".jar"))
					{
						classes.addAll(getClassesFromJar(location, classLoader));
					} else if (location.isDirectory())
					{
						classes.addAll(getClassesFromLocation(location));
					}
				} catch (MalformedURLException e)
				{
					e.printStackTrace();
				} catch (ClassNotFoundException e)
				{
					e.printStackTrace();
				}
			}
			return classes;
		}

		public Class<?>[] getClassesFromThisJar(Object object)
		{
			final List<Class<?>> classes = new ArrayList<Class<?>>();
			ClassLoader classLoader = null;
			URI uri = null;
			try
			{
				uri = object.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
				classLoader = new URLClassLoader(new URL[]
				{ uri.toURL() }, ClassEnumerator.class.getClassLoader());
			} catch (URISyntaxException e)
			{
				e.printStackTrace();
			} catch (MalformedURLException e)
			{
				e.printStackTrace();
			}
			if (uri == null)
			{
				throw new RuntimeException(
						"No uri for " + this.getClass().getProtectionDomain().getCodeSource().getLocation());
			}
			if (classLoader == null)
			{
				throw new RuntimeException(
						"No classLoader for " + this.getClass().getProtectionDomain().getCodeSource().getLocation());
			}
			File file = new File(uri);
			classes.addAll(getClassesFromLocation(file));
			return classes.toArray(new Class[classes.size()]);
		}

		public List<Class<?>> getClassesFromJar(File file, ClassLoader classLoader)
		{
			final List<Class<?>> classes = new ArrayList<Class<?>>();
			try
			{
				final JarFile jarFile = new JarFile(file);
				Enumeration<JarEntry> enumeration = jarFile.entries();
				while (enumeration.hasMoreElements())
				{
					final JarEntry jarEntry = enumeration.nextElement();
					if (jarEntry.isDirectory() || !jarEntry.getName().toLowerCase().trim().endsWith(".class"))
					{
						continue;
					}
					classes.add(classLoader.loadClass(jarEntry.getName().replace(".class", "").replace("/", ".")));
				}
				jarFile.close();
			} catch (IOException e)
			{
				e.printStackTrace();
			} catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}
			return classes;
		}
	}
}