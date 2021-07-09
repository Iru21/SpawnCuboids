package me.mateusz

import me.mateusz.commands.cCuboid
import me.mateusz.events.SignClick
import org.bukkit.ChatColor
import org.bukkit.plugin.java.JavaPlugin
import me.mateusz.events.SignCreate
import me.mateusz.util.registerCommand


class SpawnCuboids : JavaPlugin() {
    private val version = this.description.version
    private val pluginName: String = this.description.name

    override fun onEnable() {
        server.consoleSender.sendMessage("${ChatColor.DARK_GRAY}[${ChatColor.GOLD}$pluginName${ChatColor.DARK_GRAY}] ${ChatColor.GREEN}Enabling $version")

        saveDefaultConfig()

        server.pluginManager.registerEvents(SignCreate(this), this)
        server.pluginManager.registerEvents(SignClick(this), this)

        registerCommand(this, cCuboid("cuboid", this))
    }

    override fun onDisable() {
        server.consoleSender.sendMessage("${ChatColor.DARK_GRAY}[${ChatColor.GOLD}$pluginName${ChatColor.DARK_GRAY}] ${ChatColor.RED}Disabling $version")
    }
}