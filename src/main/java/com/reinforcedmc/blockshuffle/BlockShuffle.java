package com.reinforcedmc.blockshuffle;

import com.reinforcedmc.gameapi.GameAPI;
import com.reinforcedmc.gameapi.GameStatus;
import com.reinforcedmc.gameapi.events.GamePreStartEvent;
import com.reinforcedmc.gameapi.events.GameSetupEvent;
import com.reinforcedmc.gameapi.events.GameStartEvent;
import com.reinforcedmc.gameapi.scoreboard.UpdateScoreboardEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

public final class BlockShuffle extends JavaPlugin implements Listener {

    public static ArrayList<UUID> ingame = new ArrayList<>();
    private Shuffle swap;

    private static BlockShuffle instance;

    private World world;
    private Location spawn;
    private long maxRadius;

    @Override
    public void onEnable() {
        instance = this;

        log("Enabled!");
        Bukkit.getServer().getPluginManager().registerEvents(this, this);

    }

    @Override
    public void onDisable() {
        log("Disabled!");
    }

    @EventHandler
    public void onSetup(GameSetupEvent e) {
        createWorld();
        e.openServer();
    }

    public void createWorld() {

        if(Bukkit.getWorld("BlockShuffle") != null) {
            Bukkit.unloadWorld("BlockShuffle", false);
        }
        File folder = new File(Bukkit.getWorldContainer()+"/BlockShuffle");
        folder.delete();

        WorldCreator creator = new WorldCreator("BlockShuffle");
        creator.environment(World.Environment.NORMAL);
        creator.generateStructures(true);
        world = creator.createWorld();

        spawn = new Location(world, 0, 0, 0);
        maxRadius = 250;

    }

    @EventHandler
    public void onPreStart(GamePreStartEvent e) {

        for (UUID game : GameAPI.getInstance().ingame) {

            Player p = Bukkit.getServer().getPlayer(game);
            if (p == null || !p.isOnline()) continue;

            boolean notocean = false;

            Location location = Bukkit.getWorld("BlockShuffle").getSpawnLocation();

            while(!notocean) {
                location = new Location(world, 0, 0, 0); // New Location in the right World you want
                location.setX(spawn.getX() + Math.random() * maxRadius * 2 - maxRadius); // This get a Random with a MaxRange
                location.setZ(spawn.getZ() + Math.random() * maxRadius * 2 - maxRadius);

                Block highest = world.getHighestBlockAt(location.getBlockX(), location.getBlockZ());

                if(highest.isLiquid()) {
                    maxRadius += 100;
                    continue;
                }

                notocean = true;
                location.setY(highest.getY() + 1); // Get the Highest Block of the Location for Save Spawn.
            }

            p.teleport(location);

        }

        new com.reinforcedmc.gameapi.GamePostCountDown().start();

    }

    @EventHandler
    public void onStart(GameStartEvent e) {
        start();
    }


    public void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (GameAPI.getInstance().ingame.size() > 1) {

                    ingame.clear();

                    for(UUID uuid : GameAPI.getInstance().ingame) {
                        ingame.add(uuid);
                    }

                    swap = new Shuffle(15);
                    swap.start();

                    Bukkit.broadcastMessage(GameAPI.getInstance().currentGame.getPrefix() + ChatColor.GRAY + " has started. " + ChatColor.YELLOW + "Last one to survive wins!");

                    this.cancel();
                } else {
                    GameAPI.getInstance().getAPI().endGame(null);
                }
            }
        }.runTaskTimer(this, 0, 1);
    }

    public static void log(Object object) {
        System.out.println("[BlockShuffle] " + object.toString());
    }

    public static BlockShuffle getInstance() {
        return instance;
    }

    static HashMap<Player, Player> players = new HashMap<>();

    public static void swap() {

        HashMap<Player, Location> plocs = new HashMap<>();
        ArrayList<UUID> reserved = new ArrayList<>();

        for(UUID uuid : ingame) {
            Player p = Bukkit.getPlayer(uuid);

            int maxtries = 3;
            UUID tp = p.getUniqueId();
            if(ingame.size() >= 3) {
                /* Loop, that searches for a valid player to teleport to. */
                while ((tp.equals(uuid) || reserved.contains(tp) || (players.containsKey(Bukkit.getPlayer(tp)) && players.get(Bukkit.getPlayer(tp)).getUniqueId() == uuid)) && maxtries > 0) {
                    tp = ingame.get(new Random().nextInt(ingame.size()));
                    maxtries--;
                }
            } else {
                for(Player pp : Bukkit.getOnlinePlayers()) {
                    if(!p.getUniqueId().equals(pp.getUniqueId())) {
                        tp = pp.getUniqueId();
                        break;
                    }
                }
            }
            players.put(p, Bukkit.getPlayer(tp));
            reserved.add(tp);
            plocs.put(p, Bukkit.getPlayer(tp).getLocation());
        }

        for(Player p : plocs.keySet()) {
            p.teleport(plocs.get(p));
        }

    }

    public static ArrayList<Player> getAlive() {
        ArrayList<Player> alive = new ArrayList<>();
        for (UUID uuid : ingame){
            Player player = Bukkit.getServer().getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;
            alive.add(player);
        }

        return alive;
    }

    /*
    NETHER CANCEL
     */
    @EventHandler
    public void onPortal(PlayerPortalEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {

        if (e.getCause().equals(PlayerTeleportEvent.TeleportCause.END_PORTAL))
            e.setCancelled(true);
        if (e.getCause().equals(PlayerTeleportEvent.TeleportCause.NETHER_PORTAL))
            e.setCancelled(true);
        if (e.getCause().equals(PlayerTeleportEvent.TeleportCause.END_GATEWAY))
            e.setCancelled(true);

    }

    /*
    PVP CANCEL
     */
    @EventHandler
    public void onPvP(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (!(e.getDamager() instanceof Player)) return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if(GameAPI.getInstance().status == GameStatus.POSTCOUNTDOWN || GameAPI.getInstance().status == GameStatus.ENDING) {
            if(GameAPI.getInstance().ingame.contains(e.getPlayer().getUniqueId())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if(GameAPI.getInstance().status == GameStatus.POSTCOUNTDOWN || GameAPI.getInstance().status == GameStatus.ENDING) {
            if(GameAPI.getInstance().ingame.contains(e.getPlayer().getUniqueId())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {

        if (!getAlive().isEmpty()) {
            Player toTeleport = getAlive().get(new Random().nextInt(getAlive().size()));
            e.setRespawnLocation(toTeleport.getLocation());
        }

    }

    @EventHandler
    public void onDie(PlayerDeathEvent e) {
        Player p = e.getEntity();

        if(ingame.contains(p.getUniqueId())) {

            ingame.remove(p.getUniqueId());
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> p.spigot().respawn(), 1L);

            if(getAlive().size() > 1) {
                if (swap.interval - swap.remaining > 60) {
                    e.setDeathMessage(ChatColor.RED + ChatColor.BOLD.toString() + p.getName() + " has died! " + ingame.size() + " remaining.");
                } else {
                    e.setDeathMessage(ChatColor.RED + ChatColor.BOLD.toString() + p.getName() + " died to " + players.get(p).getName() + "'s trap.");
                }
            }

            e.setDeathMessage(null);

            GameAPI.getInstance().getAPI().putInSpectator(p);

            update();
        }
    }

    private void update() {

        if(GameAPI.getInstance().status != GameStatus.INGAME) return;

        if (getAlive().size() <= 1) {

            Player winner = getAlive().get(0);
            swap.cancel();
            GameAPI.getInstance().getAPI().endGame(winner);

        }

        if(ingame.isEmpty()) {

            swap.cancel();
            GameAPI.getInstance().getAPI().endGame(null);

        }

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        if (!getAlive().isEmpty()) {

            GameAPI.getInstance().getAPI().putInSpectator(p);

            Player toTeleport = getAlive().get(new Random().nextInt(getAlive().size()));
            p.teleport(toTeleport);
        }

    }

    @EventHandler
    public void onLog(PlayerQuitEvent e) {
        if (!ingame.contains(e.getPlayer().getUniqueId())) return;
        ingame.remove(e.getPlayer().getUniqueId());
        update();
    }

    @EventHandler
    public void onSBUpdate(UpdateScoreboardEvent e) {

        if (GameAPI.getInstance().currentGame.getName() != "BlockShuffle" || GameAPI.getInstance().status != GameStatus.INGAME) {
            return;
        }

        String[] scoreboard = {
                "",
                String.format("&bPlayers remaining: &f%s", ingame.size()),
                ""
        };

        e.getScoreboard().setLines(scoreboard);
    }
}