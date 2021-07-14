package me.mateusz.translation

import me.mateusz.util.GetConfig
import net.md_5.bungee.api.ChatColor
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class Translation(jplugin : JavaPlugin) {
    private val plugin = jplugin
    private val langFolder = File(plugin.dataFolder, "lang" + File.separator + "defaults" + File.separator)
    private val defaults = arrayListOf("en-us", "pl-pl")

    init {
        if(!langFolder.exists()) {
            langFolder.mkdirs()
        }
        for(default in defaults) {
            val langFile = File(plugin.dataFolder, "lang" + File.separator + default + ".yml")
            if(!langFile.exists()) {
                plugin.saveResource("lang/$default.yml", false)
            }
            plugin.saveResource("lang/defaults/$default.yml", true)
            if(hasOldVersion(default)) {
                plugin.server.consoleSender.sendMessage("${org.bukkit.ChatColor.DARK_GRAY}[${org.bukkit.ChatColor.GOLD}${plugin.description.name}${org.bukkit.ChatColor.DARK_GRAY}] ${org.bukkit.ChatColor.AQUA}It looks like you have an older vesrion of lang file $default.yml! You can go to ${plugin.dataFolder.name}/lang/defaults/$default.yml to see the changes and update/replace your main lang files. If not you may experience some issues")
            }
        }
    }

    fun get(key : String) : String {
        val lang = GetConfig("lang")
        val langFile = File(plugin.dataFolder, "lang" + File.separator + lang + ".yml")
        val config = YamlConfiguration.loadConfiguration(langFile)
        val value = config.getString(key)
            ?: return "§cThere has been a translation error! Couldn't find key $key in $lang.yml§l!"
        return ChatColor.translateAlternateColorCodes('&', value).replace("{{color}}", GetConfig("mainColor")).replace("{{prefix}}", GetConfig("prefix"))
    }

    private fun hasOldVersion(l : String) : Boolean{
        val langFile = File(plugin.dataFolder, "lang" + File.separator + l + ".yml")
        val defaultLangFile = File(plugin.dataFolder, "lang" + File.separator + "defaults" + File.separator + l + ".yml")
        val config = YamlConfiguration.loadConfiguration(langFile)
        val defaultconfig = YamlConfiguration.loadConfiguration(defaultLangFile)
        return defaultconfig.getInt("ver") > config.getInt("ver")
    }
}