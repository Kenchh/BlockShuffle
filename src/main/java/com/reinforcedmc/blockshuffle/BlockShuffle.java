package com.reinforcedmc.blockshuffle;

import com.reinforcedmc.gameapi.GameAPI;
import com.reinforcedmc.gameapi.GameStatus;
import com.reinforcedmc.gameapi.events.GamePreStartEvent;
import com.reinforcedmc.gameapi.events.GameSetupEvent;
import com.reinforcedmc.gameapi.events.GameStartEvent;
import com.reinforcedmc.gameapi.scoreboard.UpdateScoreboardEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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

public class BlockShuffle extends JavaPlugin implements Listener {

    public static HashMap<UUID, ShuffleProfile> shuffleProfiles = new HashMap<>();
    private Shuffle shuffle;

    public static Difficulty difficulty = Difficulty.EASY;
    public static int round = 1;

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

        difficulty = Difficulty.EASY;
        shuffleProfiles.clear();
        round = 0;

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
        new BukkitRunnable() {
            @Override
            public void run() {
                if (GameAPI.getInstance().ingame.size() > 1) {

                    for(UUID uuid : GameAPI.getInstance().ingame) {
                        shuffleProfiles.put(uuid, new ShuffleProfile(uuid));
                    }

                    shuffle = new Shuffle(30);
                    shuffle.start();

                    Bukkit.broadcastMessage(GameAPI.getInstance().currentGame.getPrefix() + ChatColor.GRAY + " has started. " + ChatColor.YELLOW + "The player who finds the most blocks wins!");
                    assignBlocks();

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

    public void assignBlocks() {

        if(getAlive().size() <= 1) {
            return;
        }

        round++;

        if(round == 3) difficulty = Difficulty.MEDIUM;
        if(round == 5) difficulty = Difficulty.HARD;
        if(round == 8) difficulty = Difficulty.HARDCORE;

        if(difficulty != Difficulty.EASY) {
            shuffle.interval = 5*60;
        }

        shuffle.remaining = shuffle.interval;

        for(ShuffleProfile sp : shuffleProfiles.values()) {
            sp.setRandomBlock();
        }
    }

    public void shuffle() {

        for(Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1F, 0.5F);
        }

        int deadplayers = 0;

        for(ShuffleProfile sp : shuffleProfiles.values()) {
            if(!sp.foundBlock) {
                deadplayers++;
            }
        }

        if(deadplayers >= getAlive().size()) {
            shuffleProfiles.clear();
            update();
        } else {

            for(ShuffleProfile sp : shuffleProfiles.values()) {
                if(!sp.foundBlock) {
                    sp.getPlayer().setHealth(0);
                }
            }

            assignBlocks();
        }
    }

    public static ArrayList<Player> getAlive() {
        ArrayList<Player> alive = new ArrayList<>();
        for (UUID uuid : shuffleProfiles.keySet()) {
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
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if(shuffleProfiles.containsKey(e.getPlayer().getUniqueId())) {
            ShuffleProfile sp = shuffleProfiles.get(e.getPlayer().getUniqueId());
            if(!sp.foundBlock) {
                if(e.getTo().getBlock().getType() == sp.getBlock() || e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == sp.getBlock()) {

                    Bukkit.broadcastMessage(ChatColor.GRAY + ChatColor.BOLD.toString() + e.getPlayer().getName() + " has found their block!");

                    for(int i=0;i<3;i++) {
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1F, 1.2F);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1F, 1.6F);
                            }
                        }.runTaskLater(BlockShuffle.getInstance(), 3L);
                    }

                    sp.foundBlock = true;

                    boolean allfound = true;
                    for(ShuffleProfile spp : shuffleProfiles.values()) {
                        if(!spp.foundBlock) {
                            allfound = false;
                            break;
                        }
                    }

                    if(allfound) {
                        Bukkit.broadcastMessage(ChatColor.GOLD + "Everyone has found their block!");
                        shuffle.remaining = shuffle.interval;
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                shuffle();
                            }
                        }.runTaskLater(this, 40L);
                    }

                }
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

        if(shuffleProfiles.containsKey(p.getUniqueId())) {

            int old = shuffleProfiles.size();

            shuffleProfiles.remove(p.getUniqueId());
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> p.spigot().respawn(), 1L);

            if(getAlive().size() > 1) {
                e.setDeathMessage(ChatColor.RED.toString() + (old - getAlive().size()) + " players haven't found their block in time!");
                e.setDeathMessage(ChatColor.RED.toString() + getAlive() + " players remain!");
            } else {
                e.setDeathMessage(null);
            }

            GameAPI.getInstance().getAPI().putInSpectator(p);

            update();
        }
    }

    private void update() {

        if(GameAPI.getInstance().status != GameStatus.INGAME) return;

        if (getAlive().size() == 1) {

            Player winner = getAlive().get(0);
            shuffle.cancel();
            GameAPI.getInstance().getAPI().endGame(winner);

        }

        if(shuffleProfiles.isEmpty()) {

            shuffle.cancel();
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
        if (!shuffleProfiles.containsKey(e.getPlayer().getUniqueId())) return;
        shuffleProfiles.remove(e.getPlayer().getUniqueId());
        update();
    }

    @EventHandler
    public void onSBUpdate(UpdateScoreboardEvent e) {

        if (GameAPI.getInstance().currentGame.getName() != "BlockShuffle" || GameAPI.getInstance().status != GameStatus.INGAME) {
            return;
        }

        String timestring = "";

        if(shuffle.remaining >= 60) {
            timestring = ((int) shuffle.remaining/60) + "m ";
            timestring += shuffle.remaining - ((int) shuffle.remaining/60) * 60 + "s";
        }

        String[] scoreboard = {
                "",
                "&bMode: " + difficulty.getPrefix(),
                "",
                "&bTime left: &f" + timestring + " seconds",
                "",
                String.format("&bPlayers remaining: &f%s", shuffleProfiles.size()),
                "",
                "&3play.reinforced.com"
        };

        e.getScoreboard().setLines(scoreboard);
    }
}
