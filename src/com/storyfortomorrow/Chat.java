package com.storyfortomorrow;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Anything chat related will be here.
 * 
 * @author JohnnPM
 *
 */
public class Chat implements Listener
{
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e)
	{
		e.setFormat("%s: %s");
	}

	/**
	 * Broadcasts a message to all the players online.
	 * 
	 * @param message
	 *            message
	 * @param args
	 *            any arguments (optional)
	 */
	public static void broadcast(String message, Object... args)
	{
		String msg = C.formatString(message, args);
		for (Player p : Bukkit.getOnlinePlayers())
			p.sendMessage(msg);
		Main.log.info(msg);
	}

	/**
	 * Sends a message to a certain player
	 * 
	 * @param p
	 *            player to send
	 * @param message
	 *            message to send
	 * @param args
	 *            anything else
	 */
	public static void send(Player p, String message, Object... args)
	{
		String msg = C.formatString(message, args);
		p.sendMessage(msg);
	}

	/**
	 * @author Richmond Steele
	 * @since 12/16/13 All rights Reserved Please read included LICENSE file
	 */
	public static class C
	{
		private static final int MAX_SIZE = 1000;
		private final static Map<String, String> colorizedStrings = new ConcurrentHashMap<String, String>();
		private final static Map<String, ChatColor> customColors = new ConcurrentHashMap<String, ChatColor>();

		{
			addColor("aqua", ChatColor.AQUA);
			addColor("black", ChatColor.BLACK);
			addColor("blue", ChatColor.BLUE);
			addColor("bold", ChatColor.BOLD);
			addColor("dark_aqua", ChatColor.DARK_AQUA);
			addColor("dark_blue", ChatColor.DARK_BLUE);
			addColor("dark_gray", ChatColor.DARK_GRAY);
			addColor("dark_green", ChatColor.DARK_GREEN);
			addColor("dark_purple", ChatColor.DARK_PURPLE);
			addColor("dark_red", ChatColor.DARK_RED);
			addColor("gold", ChatColor.GOLD);
			addColor("gray", ChatColor.GRAY);
			addColor("green", ChatColor.GREEN);
			addColor("italic", ChatColor.ITALIC);
			addColor("light_purple", ChatColor.LIGHT_PURPLE);
			addColor("magic", ChatColor.MAGIC);
			addColor("red", ChatColor.RED);
			addColor("reset", ChatColor.RESET);
			addColor("strikethrough", ChatColor.STRIKETHROUGH);
			addColor("underline", ChatColor.UNDERLINE);
			addColor("white", ChatColor.WHITE);
			addColor("yellow", ChatColor.YELLOW);
		}

		/**
		 * Converts simple colors into ChatColor values eg. <blue>test => §9test
		 * (actually its technically ChatColor.BLUE, not §9. despite them being
		 * the same)
		 * 
		 * @param string
		 *            input string
		 * @return string with proper ChatColor inputted
		 */
		public static String formatColors(String string)
		{
			synchronized (colorizedStrings)
			{
				if (colorizedStrings.containsKey(string))
				{
					return colorizedStrings.get(string);
				} else
				{
					Pattern p = Pattern.compile("<([a-zA-Z_]*)>");
					Matcher m = p.matcher(string);
					String colorized = string;
					while (m.find())
					{
						colorized = colorized.replaceFirst(p.pattern(), convertToColorCode(m.group(1)));
					}
					colorizedStrings.put(string, colorized);
					if (colorizedStrings.size() > MAX_SIZE)
					{
						reduceSize();
					}
					return colorized;
				}
			}
		}

		/**
		 * Formats string and colorizes it
		 * 
		 * @param string
		 *            String containing colors and %s %d etc.
		 * @param objects
		 *            Objects to be formatted into the string
		 * @return formatted and colorized String
		 */
		public static String formatString(String string, Object... objects)
		{
			string = String.format(string, objects);
			return formatColors(string);
		}

		public void send(CommandSender sender, String string, Object... objects)
		{
			sender.sendMessage(formatString(string, objects));
		}

		public void addColor(String s, ChatColor color)
		{
			synchronized (customColors)
			{
				if (!customColors.containsKey(s.toUpperCase()))
				{
					customColors.put(s.toUpperCase(), color);
				}
			}
		}

		public void removeColor(String s)
		{
			synchronized (customColors)
			{
				if (customColors.containsKey(s.toUpperCase()))
				{
					customColors.remove(s.toUpperCase());
				}
			}
		}

		/**
		 * Wrapper for <code>ChatColor.valueOf()</code>
		 * 
		 * @param s
		 *            string to get color of
		 * @return ChatColor char
		 */
		private static String convertToColorCode(String s)
		{
			synchronized (customColors)
			{
				if (customColors.containsKey(s.toUpperCase()))
				{
					return customColors.get(s.toUpperCase()).toString();
				}
			}
			try
			{
				return ChatColor.valueOf(s.toUpperCase()).toString();
			} catch (Exception e)
			{
				return "<" + s + ">";
			}
		}

		private static void reduceSize()
		{
			synchronized (colorizedStrings)
			{
				Iterator<String> iterator = colorizedStrings.values().iterator();
				for (int i = colorizedStrings.size() / 10; i >= 0; --i)
				{
					if (!iterator.hasNext())
					{
						break;
					}
					iterator.next();
					iterator.remove();
				}
			}
		}

	}
}
