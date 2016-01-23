package master.of.the.tower;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.storyfortomorrow.Chat.C;

public class Events implements Listener
{
	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent e)
	{
		Player p = e.getPlayer();
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		Player p = e.getPlayer();
		e.setJoinMessage(C.formatString("<yellow>%s joined.", p.getDisplayName()));
		if (!p.getName().equals("ChaBoiTyron"))
			p.teleport(p.getWorld().getSpawnLocation());

		Manager.addPlayer(p);
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e)
	{
		Player p = e.getPlayer();
		e.setQuitMessage(C.formatString("<yellow>%s left.", p.getDisplayName()));

		Manager.removePlayer(p);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e)
	{
		if (e.getAction().equals(Action.PHYSICAL))
			if (e.getClickedBlock().getType().equals(Material.STONE_PLATE))
				if (e.getClickedBlock().getRelative(BlockFace.DOWN).getType().equals(Material.BEDROCK))
				{
					Player player = e.getPlayer();
					Manager.end(player);
				}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e)
	{
		Player p = e.getEntity();
		p.getInventory().clear();

		Manager.level.levelUp(p);

		if (p.getKiller() == null)
			e.setDeathMessage(C.formatString("<red>[Tower Master] %s fell to their death.", p.getDisplayName()));
		else
			e.setDeathMessage(C.formatString("<red>[Tower Master] %s died because of %s", p.getDisplayName(),
					p.getKiller().getDisplayName()));
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		//Player p = event.getPlayer();
		//if (p.getName() != "ChaBoiTyron" || p.getName() != "SugarBlood")
		//	event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e)
	{
		Player p = e.getPlayer();
		Location spawn = new Location(p.getWorld(), Manager.GAME_COORDS[0], Manager.GAME_COORDS[1],
				Manager.GAME_COORDS[2]);
		e.setRespawnLocation(spawn);
		Manager.level.updateLevelItems(p);
	}
}
