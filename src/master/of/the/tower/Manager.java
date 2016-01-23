package master.of.the.tower;

import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.storyfortomorrow.Chat;
import com.storyfortomorrow.Main;

public class Manager implements Listener
{
	public static final String NAME = "Tower Master";
	public static final String VERSION = "Development";
	public static final String AUTHOR = "JohnnPM, SugarBlood";

	public static final int MIN_PLAYERS = 5;
	public static final int MAX_PLAYERS = 100;

	/**
	 * x, y, z
	 */
	public static final int LOBBY_COORDS[] =
	{ 100, 60, 100 };

	/**
	 * x, y, z
	 */
	public static final int GAME_COORDS[] =
	{ 250, 60, 250 };

	public static ArrayList<UUID> players = new ArrayList<UUID>();
	
	public static master.of.the.tower.Level level;

	public Manager()
	{

	}

	public static void init()
	{
		level = new master.of.the.tower.Level();	
	}
	
	static int wait_time = 10;

	public static void startingTimer()
	{
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable()
		{
			public void run()
			{
				Chat.broadcast("<blue>[Tower Master] Beginning in %d", wait_time);
				wait_time--;

				if (wait_time == 0)
				{
					Bukkit.getServer().getScheduler().cancelTasks(Main.getPlugin());
					start();
				}
			}
		}, 0L, 20L);
	}

	public static void start()
	{
		for (UUID id : players)
		{
			Player p = Bukkit.getPlayer(id);
			p.teleport(new Location(p.getWorld(), GAME_COORDS[0], GAME_COORDS[1], GAME_COORDS[2]));
			p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 15, 5));

			
		}
	}

	public static void end(Player winner)
	{
		Chat.broadcast("<blue><bold>[Tower Master] <gold><bold>%s is the Master of the Tower!",
				winner.getDisplayName());
		Chat.send(winner, "<gold>You are the Tower Master!");

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable()
		{
			public void run()
			{
				for (UUID id : players)
				{
					Player p = Bukkit.getPlayer(id);
					p.teleport(new Location(p.getWorld(), LOBBY_COORDS[0], LOBBY_COORDS[1], LOBBY_COORDS[2]));
				}
			}
		}, 5 * 20L);

	}

	public static void addPlayer(Player p)
	{
		if (!players.contains(p.getUniqueId()))
		{
			level.setLevel(p, 0);
			players.add(p.getUniqueId());
			Main.log.log(Level.INFO, "{0} ({1}) added to game session.", new Object[]
			{ p.getName(), p.getUniqueId() });
		}

		if (players.size() == MIN_PLAYERS)
		{
			startingTimer();
		}
	}

	public static void removePlayer(Player p)
	{
		if (players.contains(p.getUniqueId()))
		{
			players.remove(p.getUniqueId());
			level.player_levels.remove(p.getUniqueId());
			Main.log.log(Level.INFO, "{0} ({1}) removed from game session.", new Object[]
			{ p.getName(), p.getUniqueId() });
		}
	}

}
