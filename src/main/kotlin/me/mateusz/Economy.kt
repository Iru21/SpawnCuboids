package me.mateusz

import net.milkbowl.vault.chat.Chat
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.permission.Permission
import org.bukkit.ChatColor
import org.bukkit.plugin.java.JavaPlugin

class Economy(pl : JavaPlugin) {
    val plugin = pl

    var econ: Economy? = null
    var perms: Permission? = null
    var chat: Chat? = null

    init {
        if (!setupEconomy() ) {
            plugin.server.consoleSender.sendMessage("${ChatColor.DARK_GRAY}[${ChatColor.GOLD}${plugin.description.name}${ChatColor.DARK_GRAY}] ${ChatColor.RED}- Disabled due to no Vault dependency found!")
            plugin.server.pluginManager.disablePlugin(plugin)
        } else {
            setupPermissions()
            setupChat()
        }
    }

    private fun setupEconomy(): Boolean {
        if (plugin.server.pluginManager.getPlugin("Vault") == null) {
            return false
        }
        val rsp = plugin.server.servicesManager.getRegistration(Economy::class.java)
        if (rsp != null) {
            econ = rsp.provider
        }
        return econ != null
    }

    private fun setupChat(): Boolean {
        val rsp = plugin.server.servicesManager.getRegistration(Chat::class.java)
        if (rsp != null) {
            chat = rsp.provider
        }
        return chat != null
    }

    private fun setupPermissions(): Boolean {
        val rsp = plugin.server.servicesManager.getRegistration(Permission::class.java)
        if (rsp != null) {
            perms = rsp.provider
        }
        return perms != null
    }
}