package com.c45y.NoWorld;

import java.util.ArrayList;
import java.util.HashMap;
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
		playerSpawns = slapi.load();
		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
            public void run() {
                slapi.save(playerSpawns);
            }
        }, 1200, 1200); // 10 Minutes
	}

	public void onDisable()	{
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

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		if(event.getPlayer().hasPermission("World.op")) {
			return;
		}
		if(event.getBlock().getType() == Material.BEDROCK) {
			event.setCancelled(true);
		}
		if( playerSpawns.containsKey(event.getPlayer().getName())) {
			if(distance(playerSpawns.get(event.getPlayer().getName()), event.getBlock().getLocation()) <= 15.5) {
				return;
			}
		}
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		if(event.getPlayer().hasPermission("World.op")) {
			return;
		}
		if(event.getBlock().getType() == Material.BEDROCK) {
			event.setCancelled(true);
			return;
		}
		if( playerSpawns.containsKey(event.getPlayer().getName())) {
			System.out.println(distance(playerSpawns.get(event.getPlayer().getName()), event.getBlock().getLocation()));
			if(distance(playerSpawns.get(event.getPlayer().getName()), event.getBlock().getLocation()) <= 15.5) {
				return;
			}
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
						if( args.length == 2 && !args[1].isEmpty() && pSender.hasPermission("World.op")) {
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

						if( playerSpawns.containsKey(player)) {
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
						if (pSender.hasPermission("World.op")) {
							Location totp = getCenterSphere(pSender.getLocation());
							if (playerSpawns.containsValue(totp)) {
								for (Entry<String, Location> entry : playerSpawns.entrySet()) {
									if (entry.getValue().equals(totp)) {
										playerSpawns.remove(entry.getKey());
										pSender.sendMessage("Unclaimed this sphere");
										return true;
									}
								}
							}
						}
						return true;
					}
				}
				if(args[0].equals("home")) {
					if (sender instanceof Player) {
						Player pSender = (Player) sender;
						String player = pSender.getName();
						if( args.length == 2 && !args[1].isEmpty() ) {
							player = args[1];
						}
						if( playerSpawns.containsKey(player)) {
							pSender.teleport(playerSpawns.get(player));
						} else {
							pSender.sendMessage("Sorry can't find that home");
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
		World w = this.getServer().getWorlds().get(0);
		int x = Math.abs(c.getX() % 2);
		int z = Math.abs(c.getZ() % 2);
		//int distance = (int) distance(c.getX() % 2 * 16,c.getZ() % 2 * 16 ,location.getBlockX() , 17, location.getBlockZ());
		//System.out.println("x: " + x + "; z " + z + "; d: " + distance);
		if (x == 1 && z == 0) { //SW
			System.out.println("SW");
			Location l = c.getBlock(15, 20, 0).getLocation();
			l.setX(l.getX() + 1);
			return l;
		}
		if (x == 1 && z == 1) { //NW
			System.out.println("NW");
			Location l = c.getBlock(15, 20, 15).getLocation();
			l.setX(l.getX() + 1);
			l.setZ(l.getZ() + 1);
			return l;
		}
		if (x == 0 && z == 1) { //NE
			System.out.println("NE");
			Location l = c.getBlock(0, 20, 15).getLocation();
			l.setZ(l.getZ() + 1);
			return l;
		}
		if (x == 0 && z == 0) { //SE -
			System.out.println("SE");
			return c.getBlock(0, 20, 0).getLocation();
		}
		return defaultSpawn;
	}

	public double distance(Location pl, Location wo) {
		wo.setY(pl.getY());
		return pl.distance(wo);
		//return distance((int)wo.getX(), (int)wo.getY(),(int)wo.getZ(), (int)pl.getX(), (int)wo.getY(), (int)pl.getZ());
	}
	
	//public double distance(int sx, int sz, int x, int y, int z)	{
	//	return Math.sqrt(Math.pow(Math.abs(sx) - x, 2.0D) + Math.pow(15 - y, 2.0D) + Math.pow(Math.abs(sz) - z, 2.0D));
	//}

	private int totalPlayerCount;
	private HashMap<String, Location> playerSpawns = new HashMap<String, Location>();
	private Location defaultSpawn;
	private SLAPI slapi = new SLAPI(this);
}