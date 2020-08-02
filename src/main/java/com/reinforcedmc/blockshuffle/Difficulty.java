package com.reinforcedmc.blockshuffle;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public enum Difficulty {

    EASY("&a&lEASY"), MEDIUM("&e&lMEDIUM"), HARD("&c&lHARD"), HARDCORE("&4&lHARDCORE");

    private String prefix;

    Difficulty(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

}
