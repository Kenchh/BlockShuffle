package com.reinforcedmc.blockshuffle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Random;
import java.util.UUID;

public class ShuffleProfile {

    private Player player;

    private Material block;
    public boolean foundBlock = false;

    public ShuffleProfile(UUID uuid) {
        player = Bukkit.getPlayer(uuid);
    }

    public Player getPlayer() {
        return player;
    }

    public Material getBlock() {
        return block;
    }

    public void setRandomBlock() {

        foundBlock = false;

        Material[] blocks = BlockRarity.valueOf(BlockShuffle.difficulty.toString()).getBlocks();
        block = blocks[new Random().nextInt(blocks.length)];

        String name = block.toString().toLowerCase();

        String words[] = name.split("_");
        name = "";
        for(String word : words) {
            String firstletter = word.substring(0, 1).toUpperCase();
            String other = word.substring(1);

            String uppercaseWord = firstletter + other;
            name += uppercaseWord + " ";
        }

        player.sendMessage(ChatColor.YELLOW + "Your block is " + ChatColor.GOLD + name + "!");
        player.sendTitle(ChatColor.GOLD + name, "Your Block", 0, 100, 0);
    }

}
