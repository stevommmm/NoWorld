package com.c45y.NoWorld;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;


public class NoWorld extends JavaPlugin implements Listener
{
	public void onEnable()	{
		getServer().getPluginManager().registerEvents(this, this);
		getLogger().info(this.getName() + " enabled.");
		totalPlayerCount = getConfig().getInt("player.total.count");
	}

	public void onDisable()	{
		getConfig().set("player.total.count", totalPlayerCount);
		getConfig().set("player.spawn", playerSpawns);
		getLogger().info(this.getName() + " disabled.");
	}

	@Override
	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id)	{
		return new NoWorldGen();
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onWorldInit(WorldInitEvent event) {
		getLogger().info("Spawn location set.");
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(!event.getPlayer().hasPlayedBefore()) {
			totalPlayerCount++;
			event.getPlayer().teleport(getPlayerSpawn(event.getPlayer().getName()));
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		if(event.getBlock().getType() == Material.BEDROCK) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		if(event.getBlock().getType() == Material.BEDROCK) {
			event.setCancelled(true);
		}
	}

	private Location getPlayerSpawn(String player) {
		if (playerSpawns.containsKey(player)) {
			return playerSpawns.get(player);
		} else {
			return new Location(getServer().getWorld("world"), 0.5, 17, 0.5);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("get")) {
			if (args.length == 1){
				if(args[0].equals("this")) {
					if (sender instanceof Player) {
						Player pSender = (Player) sender;
						Location l = pSender.getLocation();
						Chunk c = l.getChunk();
						sender.sendMessage("bl -> " + c.getBlock(0, 0, 0).toString());  //bottom left
						sender.sendMessage("br -> " + c.getBlock(0, 0, 15).toString()); //bottom right
						sender.sendMessage("tr -> " + c.getBlock(15, 0, 15).toString());//top right
						sender.sendMessage("tl -> " + c.getBlock(15, 0, 0).toString()); //top left
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public double distance(int sx, int sz, int x, int z)	{
		return Math.sqrt(Math.pow(Math.abs(sx) - x, 2.0D) + Math.pow(Math.abs(sz) - z, 2.0D));
	}

	private int totalPlayerCount;
	private HashMap<String, Location> playerSpawns;
}