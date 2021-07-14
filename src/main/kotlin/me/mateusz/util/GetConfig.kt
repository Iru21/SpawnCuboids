package me.mateusz.util

import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit

fun GetConfig(key : String) : String {
    val config = Bukkit.getServer().pluginManager.getPlugin("SpawnCuboids")?.config!!
    when(key) {
        "mainColor" -> {
            return '§' + config.getString("mainColor").toString()
        }
        "prefix" -> {
            return ChatColor.translateAlternateColorCodes('&', config.getString("prefix"))
        }
        "parentRegion" -> {
            return config.getString("parentRegion").toString()
        }
        "maxMemberCount" -> {
            return config.getString("maxMemberCount").toString()
        }
        "lang" -> {
            return config.getString("lang").toString().lowercase()
        }
    }

    return "none"
}