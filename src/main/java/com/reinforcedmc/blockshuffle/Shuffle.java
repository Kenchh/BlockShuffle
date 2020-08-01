package com.reinforcedmc.blockshuffle;

import com.reinforcedmc.gameapi.GameAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

public class Shuffle extends BukkitRunnable {

    public final long interval;
    public long remaining;

    public Shuffle(long interval) {
        this.interval = interval;
        this.remaining = interval;
    }

    public void start() {
        this.runTaskTimer(BlockShuffle.getInstance(), 0, 20);
    }

    @Override
    public void run() {
        if (remaining <= 0) {
            Bukkit.broadcastMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Players have been swapped!");
            BlockShuffle.swap();
            Bukkit.getOnlinePlayers().forEach((p) -> p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 0.8F));

            remaining = interval;
        } else {

            if (remaining == 30) {
                String text = String.format(ChatColor.RED + ChatColor.BOLD.toString() + "Death Swap will ocurr in %s seconds.", remaining);
                Bukkit.broadcastMessage(text);
                Bukkit.getOnlinePlayers().forEach((p) -> p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1F));
            }

            if (remaining <= 5) {
                String text = String.format(ChatColor.RED + ChatColor.BOLD.toString() + "Death Swap will ocurr in %s seconds.", remaining);
                Bukkit.broadcastMessage(text);
                for(int i=0;i<=3;i++) {
                    Bukkit.getOnlinePlayers().forEach((p) -> p.playSound(p.getLocation(), Sound.BLOCK_STONE_PLACE, 1F, 0.6F));
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Bukkit.getOnlinePlayers().forEach((p) -> p.playSound(p.getLocation(), Sound.BLOCK_STONE_PLACE, 1F, 0.1F));
                        }
                    }.runTaskLater(GameAPI.getInstance(), 5L);
                }
            }

            remaining--;
        }
    }

}