package master.of.the.tower;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Level
{
	public Level()
	{

	}

	public final int MAX_LEVEL = 9;
	
	public HashMap<UUID, Integer> player_levels = new HashMap<UUID, Integer>();

	public void levelUp(Player p)
	{
		if (p != null && player_levels.containsKey(p.getUniqueId()))
		{
			if (player_levels.get(p.getUniqueId()) != MAX_LEVEL)
				player_levels.put(p.getUniqueId(), player_levels.get(p.getUniqueId()) + 1);
		}
	}

	public void levelDown(Player p)
	{
		if (p != null && player_levels.containsKey(p.getUniqueId()))
		{
			player_levels.put(p.getUniqueId(), player_levels.get(p.getUniqueId()) - 1);
		}
	}

	public void setLevel(Player p, int level)
	{
		if (p != null && player_levels.containsKey(p.getUniqueId()))
		{
			player_levels.put(p.getUniqueId(), level);
		} else
		{
			player_levels.put(p.getUniqueId(), level);
		}
	}

	public int getLevel(Player p)
	{
		if (p != null &&  player_levels.containsKey(p.getUniqueId()))
		{
			player_levels.get(p.getUniqueId());
		} else
		{
			player_levels.put(p.getUniqueId(), 0);
		}
		return player_levels.get(p.getUniqueId());
	}

	public void updateLevelItems(Player p)
	{
		p.getInventory().clear();
		switch (getLevel(p))
		{
		case 0:
			p.getInventory().addItem(new ItemStack(Material.BOW, 1), new ItemStack(Material.ARROW, 16));
			ItemStack boots = new ItemStack(Material.IRON_BOOTS);
			boots.addUnsafeEnchantment(Enchantment.PROTECTION_FALL, 2);
			p.getInventory().setBoots(boots);
			break;
		case 1:
			p.getInventory().addItem(new ItemStack(Material.FISHING_ROD, 1));
			ItemStack boots2 = new ItemStack(Material.GOLD_BOOTS);
			boots2.addUnsafeEnchantment(Enchantment.PROTECTION_FALL, 2);
			p.getInventory().setBoots(boots2);
			break;
		case 2:
			p.getInventory().addItem(new ItemStack(Material.DIAMOND_AXE, 1));
			ItemStack boots3 = new ItemStack(Material.LEATHER_BOOTS);
			boots3.addUnsafeEnchantment(Enchantment.PROTECTION_FALL, 2);
			p.getInventory().setBoots(boots3);
			break;
		case 3:
			p.getInventory().addItem(new ItemStack(Material.IRON_AXE, 1));
			break;
		case 4:
			p.getInventory().addItem(new ItemStack(Material.DIAMOND_SPADE, 1));
			break;
		case 5:
			p.getInventory().addItem(new ItemStack(Material.STONE_AXE, 1));
			break;
		case 6:
			p.getInventory().addItem(new ItemStack(Material.IRON_SPADE, 1));
			break;
		case 7:
			p.getInventory().addItem(new ItemStack(Material.GOLD_AXE, 1));
			break;
		case 8:
			p.getInventory().addItem(new ItemStack(Material.WOOD_AXE, 1));
			break;
		case 9:
			p.getInventory().clear();
			break;
		}
	}
}
