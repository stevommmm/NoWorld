package com.c45y.NoWorld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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

import com.c45y.NoWorld.SLAPI;


public class NoWorld extends JavaPlugin implements Listener
{
	public void onEnable() {
		this.getConfig().options().copyDefaults(true);
		this.getConfig().addDefault("player.total.count", 1);
		this.getConfig().addDefault("spawns", new ArrayList<String>());
		getServer().getPluginManager().registerEvents(this, this);
		getLogger().info(this.getName() + " enabled.");
		totalPlayerCount = getConfig().getInt("player.total.count");
		playerSpawns = slapi.load();
	}

	public void onDisable()	{
		getConfig().set("player.total.count", totalPlayerCount);
		saveConfig();
		slapi.save(playerSpawns);
		getLogger().info(this.getName() + " disabled.");
	}

	@Override
	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id)	{
		return new NoWorldGen();
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onWorldInit(WorldInitEvent event) {
		defaultSpawn = new Location(event.getWorld(), 0.5, 17, 0.5);
		getLogger().info("Spawn location set.");
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(!event.getPlayer().hasPlayedBefore()) {
			totalPlayerCount++;
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		if(event.getPlayer().hasPermission("NoWorld.op")) {
			return;
		}
		if(event.getBlock().getType() == Material.BEDROCK) {
			event.setCancelled(true);
		}
		if( playerSpawns.containsKey(event.getPlayer().getName())) {
			if(distance(event.getPlayer().getLocation(),playerSpawns.get(event.getPlayer().getName())) <= 15) {
				event.setCancelled(false);
				return;
			}
		}
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		if(event.getPlayer().hasPermission("NoWorld.op")) {
			return;
		}
		if(event.getBlock().getType() == Material.BEDROCK) {
			event.setCancelled(true);
			return;
		}
		System.out.println("Not permissions or bedrock");
		if( playerSpawns.containsKey(event.getPlayer().getName())) {
			System.out.println("Has home");
			if(distance(event.getBlock().getLocation(),playerSpawns.get(event.getPlayer().getName())) <= 15) {
				System.out.println("In own sphere");
				event.setCancelled(false);
				return;
			}
			System.out.println("Over distance");
		}
		event.setCancelled(true);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("get")) {
			if (args.length >= 1){
				if(args[0].equals("this")) {
					if (sender instanceof Player) {
						Player pSender = (Player) sender;
						String player = pSender.getName();
						if( args.length == 2 && !args[1].isEmpty() && pSender.hasPermission("NoWorld.op")) {
							pSender.sendMessage("op override");
							Location totp = getCenterSphere(pSender.getLocation());
							if (playerSpawns.containsValue(totp)) {
								pSender.sendMessage("Sorry somebody already claimed that sphere");
							} else {
								playerSpawns.put(args[1], totp);
								pSender.sendMessage("You have claimed this sphere for " + args[1]);
								pSender.teleport(totp);
							}
							return true;
						}

						if( playerSpawns.containsKey(player) && !pSender.hasPermission("NoWorld.op")) {
							pSender.sendMessage("You already own a sphere of land");
						} else {
							Location totp = getCenterSphere(pSender.getLocation());
							if (playerSpawns.containsValue(totp)) {
								pSender.sendMessage("Sorry somebody already claimed that sphere");
							} else {
								playerSpawns.put(player, totp);
								pSender.sendMessage("You have claimed this sphere");
								pSender.teleport(totp);
							}
						}
						return true;
					}
				}
				if(args[0].equals("unthis")) {
					if (sender instanceof Player) {
						Player pSender = (Player) sender;
						if (pSender.hasPermission("NoWorld.op")) {
							Location totp = getCenterSphere(pSender.getLocation());
							if (playerSpawns.containsValue(totp)) {
								for (Entry<String, Location> entry : playerSpawns.entrySet()) {
									if (entry.getValue().equals(totp)) {
										playerSpawns.remove(entry.getKey());
										pSender.sendMessage("Unclaimed this sphere");
									}
								}
							}
						}
						return true;
					}
				}
				if(args[0].equals("tp")) {
					if (sender instanceof Player) {
						Player pSender = (Player) sender;
						String player = pSender.getName();
						if( playerSpawns.containsKey(player)) {
							pSender.teleport(playerSpawns.get(player));
						} else {
							pSender.sendMessage("Sorry you don't seem to own a sphere");
						}
						return true;
					}
				}
				if(args[0].equals("list")) {
					Player pSender = (Player) sender;
					for (Entry<String, Location> entry : playerSpawns.entrySet()) {
						String key = entry.getKey();
						Object value = entry.getValue();
						pSender.sendMessage(key + " = " + value);
					}
					return true;
				}
				if(args[0].equals("who")) {
					Player pSender = (Player) sender;
					Location totp = getCenterSphere(pSender.getLocation());
					for (Entry<String, Location> entry : playerSpawns.entrySet()) {
						if (entry.getValue().equals(totp)) {
							pSender.sendMessage("This sphere owned by: " + entry.getKey());
							return true;
						}
					}
					pSender.sendMessage("This sphere is not owned ");
					return true;
				}
			}
		}
		return false;
	}

	public Location getCenterSphere(Location location) {
		Chunk c = location.getChunk();
		World w = location.getWorld();
		int x = c.getX() % 2;
		int z = c.getZ() % 2;
		if (x == -1 && z == 0) { // Bottom Right
			return c.getBlock(15, 20, 0).getLocation();
		}
		if (x == -1 && z == -1) { // Bottom Left
			Location l = c.getBlock(15, 20, 15).getLocation();
			return w.getBlockAt(l.getBlockX(), 20, l.getBlockZ() + 1).getLocation();
		}
		if (x == 0 && z == -1) { // Top Left
			Location l = c.getBlock(0, 20, 15).getLocation();
			return w.getBlockAt(l.getBlockX() -1, 20, l.getBlockZ() + 1).getLocation();
		}
		if (x == 0 && z == 0) { // Top Right
			Location l = c.getBlock(0, 20, 0).getLocation();
			return w.getBlockAt(l.getBlockX() -1, 20, l.getBlockZ()).getLocation();
		}
		return defaultSpawn;
	}

	public double distance(Location pl, Location wo) {
		return distance((int)wo.getX(), 10,(int)wo.getZ(), (int)pl.getX(), 10, (int)pl.getZ());
	}

	public double distance(int sx, int sy, int sz, int x, int y, int z)	{
		return Math.sqrt(Math.pow(Math.abs(sx) - x, 2.0D) + Math.pow(Math.abs(sy) - y, 2.0D) + Math.pow(Math.abs(sz) - z, 2.0D));
	}

	private int totalPlayerCount;
	private HashMap<String, Location> playerSpawns = new HashMap<String, Location>();
	private Location defaultSpawn;
	private SLAPI slapi = new SLAPI(this);
}