package com.reinforcedmc.blockshuffle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
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

        for(int i=0;i<3;i++)
            player.getPlayer().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 1F, 0.8F);

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
        name = name.substring(0, name.length() - 1);

        ItemStack item = new ItemStack(block);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.RESET + ChatColor.GOLD.toString() + name);
        meta.setLore(Arrays.asList(ChatColor.RESET + "Your Block"));

        item.setItemMeta(meta);

        player.getInventory().setItem(4, item);
        player.sendMessage(ChatColor.YELLOW + "Your block is " + ChatColor.GOLD + name + "!");
        player.sendTitle(ChatColor.GOLD + name, "Your Block", 0, 100, 0);
    }

}
