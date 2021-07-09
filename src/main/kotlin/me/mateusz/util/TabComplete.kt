package me.mateusz.util

import me.mateusz.data.Cuboid
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.io.File


class TabComplete : TabCompleter {
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        val completions : MutableList<String> = mutableListOf<String>()
        if(sender is Player) {
            val p : Player = sender
            if(args.size <= 1) {
                if(command.name == "cuboid") {
                    completions.addAll(mutableListOf("about", "info", "add", "remove", "delete", "leave"))
                    if(p.hasPermission("sc.admin")) {
                        completions.add("admin")
                    }
                }
            } else if(args.size <= 2) {
                if(command.name == "cuboid") {
                    if(args[0].lowercase() == "admin" && p.hasPermission("sc.admin")) {
                        completions.addAll(mutableListOf("reload", "delete", "destroy"))
                    } else if(args[0].lowercase() == "add" || args[0].lowercase() == "remove") {
                        completions.addAll(Bukkit.getOnlinePlayers().map { it.name })
                    }
                }
            } else if(args.size <= 3) {
            if(command.name == "cuboid") {
                if(args[0].lowercase() == "admin"  && p.hasPermission("sc.admin") ) {
                    if(args[1].lowercase() == "delete") {
                        completions.addAll(Bukkit.getOnlinePlayers().map { it.name })
                    } else if(args[1].lowercase() == "destroy") {
                        val CuboidDataFolder = File(Bukkit.getServer().pluginManager.getPlugin("SpawnCuboids")?.dataFolder!!, "CuboidData" + File.separator)
                        if(CuboidDataFolder.isDirectory) {
                            for (file in CuboidDataFolder.walk()) {
                                if(file.name != "CuboidData") completions.add(file.name.dropLast(4))
                            }
                        }
                    }
                }
            }
        }
        }
        return completions
    }
}