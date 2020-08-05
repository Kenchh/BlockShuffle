package com.reinforcedmc.blockshuffle;

import com.reinforcedmc.gameapi.GameAPI;
import com.reinforcedmc.gameapi.GameStatus;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Shuffle extends BukkitRunnable {

    public long interval;
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

            BlockShuffle.getInstance().shuffle();
            remaining = interval;
        } else {


            if(remaining == 60) {
                Bukkit.broadcastMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Blocks will shuffle in " + remaining + " seconds!");
            }

            if(remaining == 30) {
                Bukkit.broadcastMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Blocks will shuffle in " + remaining + " seconds!");
            }

            if(remaining == 15) {
                Bukkit.broadcastMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Blocks will shuffle in " + remaining + " seconds!");
            }

            if(remaining == 5) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for(Player p : Bukkit.getOnlinePlayers()) {
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1F, 1.2F);
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1F, 0.8F);
                                }
                            }.runTaskLater(BlockShuffle.getInstance(), 3L);
                        }

                        if(remaining > 5 || GameAPI.getInstance().status == GameStatus.ENDING) {
                            this.cancel();
                        }

                    }
                }.runTaskTimer(BlockShuffle.getInstance(), 0L, 3L);

            }

            if(remaining <= 5) {
                Bukkit.broadcastMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Blocks will shuffle in " + remaining + " seconds!");
            }

            remaining--;
        }
    }

}
