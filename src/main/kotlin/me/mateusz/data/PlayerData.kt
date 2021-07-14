package me.mateusz.data

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.lang.IllegalStateException

class PlayerData(jplugin : JavaPlugin) {
    val SpawnCuboids : JavaPlugin = jplugin
    val PlayerDataFolder = File(jplugin.dataFolder, "PlayerData" + File.separator)

    init {
        if(!PlayerDataFolder.exists()) PlayerDataFolder.mkdirs()
    }

    fun CreateOrGetPlayer(p : Player) : FileConfiguration {
        val PlayerDataFile = File(SpawnCuboids.dataFolder, "PlayerData" + File.separator + p.uniqueId + ".yml")
        if(!PlayerDataFile.exists()) {
            PlayerDataFile.createNewFile()
            val config = YamlConfiguration.loadConfiguration(PlayerDataFile)
            config.set("usr", p.name)
            config.set("cuboid", "__none__")
            config.set("status", Status.NONE.toString())
            UpdateOrSavePlayer(p, config)
            return config
        }
        return YamlConfiguration.loadConfiguration(PlayerDataFile)
    }

    fun setCuboid(p : Player, cuboid : Cuboid?, status : Status) {
        this.CreateOrGetPlayer(p)
        if(cuboid == null) {
            this.set(p, "cuboid", "__none__")
            this.set(p, "status", Status.NONE.toString())
        } else {
            this.set(p, "cuboid", cuboid.id)
            when(status) {
                Status.OWNER -> {
                    this.set(p, "status", Status.OWNER.toString())
                }
                Status.MEMBER -> {
                    this.set(p, "status", Status.MEMBER.toString())
                }
                Status.NONE -> {
                    throw IllegalStateException("Can't set cuboid status to NONE when the cuboid object is not null!")
                }
            }
        }
    }

    fun set(p : Player, key : String, value : Any) {
        val PlayerDataFile = File(SpawnCuboids.dataFolder, "PlayerData" + File.separator + p.uniqueId + ".yml")
        val config = YamlConfiguration.loadConfiguration(PlayerDataFile)
        config.set(key, value)
        UpdateOrSavePlayer(p, config)
    }

    fun UpdateOrSavePlayer(p : Player, PlayerData : FileConfiguration) {
        val PlayerDataFile = File(SpawnCuboids.dataFolder, "PlayerData" + File.separator + p.uniqueId + ".yml")
        PlayerData.save(PlayerDataFile)
    }

    fun get(p : Player, key : String) : Any? {
        val PlayerDataFile = File(SpawnCuboids.dataFolder, "PlayerData" + File.separator + p.uniqueId + ".yml")
        val config = YamlConfiguration.loadConfiguration(PlayerDataFile)
        return config.get(key)
    }
}