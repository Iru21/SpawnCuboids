package me.mateusz.data

import org.bukkit.Location
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class CuboidData (sc : JavaPlugin) {
    val plugin : JavaPlugin = sc
    val CuboidDataFolder = File(plugin.dataFolder, "CuboidData" + File.separator)

    init {
        if(!CuboidDataFolder.exists()) CuboidDataFolder.mkdirs()
    }

    fun CreateCuboid(cuboid : Cuboid) : FileConfiguration {
        val CuboidDataFile = File(plugin.dataFolder, "CuboidData" + File.separator + cuboid.id + ".yml")
        CuboidDataFile.createNewFile()
        val config = YamlConfiguration.loadConfiguration(CuboidDataFile)
        config.set("id", cuboid.id)
        config.set("owner", cuboid.owner)
        config.set("centerLocation", cuboid.centerLocation)
        config.set("radius", cuboid.radius)
        config.set("price", cuboid.price)
        config.set("members", arrayListOf<String>())
        UpdateOrSaveCuboid(cuboid, config)
        return config
    }

    fun GetCuboid(id : String) : Cuboid? {
        val CuboidDataFile = File(plugin.dataFolder, "CuboidData" + File.separator + id + ".yml")
        if(!CuboidDataFile.exists()) return null
        return construct(YamlConfiguration.loadConfiguration(CuboidDataFile))
    }

    @Suppress("UNCHECKED_CAST")
    private fun construct(conf : YamlConfiguration) : Cuboid {
        return Cuboid(
            conf.get("id") as String,
            conf.get("owner") as String,
            conf.get("price") as Double,
            conf.get("centerLocation") as Location,
            conf.get("radius") as Int,
            conf.get("members") as ArrayList<String>
        )
    }

    fun UpdateOrSaveCuboid(cuboid : Cuboid, CuboidData : FileConfiguration) {
        val CuboidDataFile = File(plugin.dataFolder, "CuboidData" + File.separator + cuboid.id + ".yml")
        CuboidData.save(CuboidDataFile)
    }

    fun set(cuboid : Cuboid, key : String, value : Any) {
        val CuboidDataFile = File(plugin.dataFolder, "CuboidData" + File.separator + cuboid.id + ".yml")
        val config = YamlConfiguration.loadConfiguration(CuboidDataFile)
        config.set(key, value)
        UpdateOrSaveCuboid(cuboid, config)
    }

//    fun updateIfOld(p : Player, key : String, default : Any) {
//        val UserDataFile = File(plugin.dataFolder, "userdata" + File.separator + p.uniqueId + ".yml")
//        val config = YamlConfiguration.loadConfiguration(UserDataFile)
//        if(!config.contains(key)) {
//            config.set(key, default.toString())
//            UpdateOrSaveUser(p, config)
//        }
//        return
//    }

//    fun CheckIfExists(p : Player) : Boolean {
//        val UserDataFile = File(plugin.dataFolder, "userdata" + File.separator + p.uniqueId + ".yml")
//        return UserDataFile.exists()
//    }
//
//    fun Validate(p : Player, pass : String) : Boolean {
//        val UserDataFile = File(plugin.dataFolder, "userdata" + File.separator + p.uniqueId + ".yml")
//        val config = YamlConfiguration.loadConfiguration(UserDataFile)
//        if(HashUtil.toSHA256(pass) == config.get("pass")) return true
//        return false
//    }
//
//    fun PasswordMatchesRules(pass : String) : Boolean {
//        return (pass.length >= 6 && pass.contains(Regex("[A-Z]")) && pass.contains(Regex("[0-9]")))
//    }
//
//    fun DeleteUser(p : Player) : Boolean {
//        try {
//            val UserDataFile = File(plugin.dataFolder, "userdata" + File.separator + p.uniqueId + ".yml")
//            UserDataFile.delete()
//            return true
//        } catch(e : Exception) {
//            return false
//        }
//    }
//
//    fun DeleteUser(p : OfflinePlayer) : Boolean {
//        try {
//            val UserDataFile = File(plugin.dataFolder, "userdata" + File.separator + p.uniqueId + ".yml")
//            UserDataFile.delete()
//            return true
//        } catch(e : Exception) {
//            return false
//        }
//    }
//
//    fun get(p : Player, key : String) : Any? {
//        val UserDataFile = File(plugin.dataFolder, "userdata" + File.separator + p.uniqueId + ".yml")
//        val config = YamlConfiguration.loadConfiguration(UserDataFile)
//        return config.get(key)
//    }
}