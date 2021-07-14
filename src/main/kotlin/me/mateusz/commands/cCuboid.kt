package me.mateusz.commands

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldguard.WorldGuard
import me.mateusz.data.CuboidData
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import com.sk89q.worldguard.protection.ApplicableRegionSet
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import me.mateusz.data.Cuboid
import me.mateusz.data.PlayerData
import me.mateusz.data.Status
import me.mateusz.translation.Translation
import me.mateusz.util.GetConfig
import org.bukkit.Material
import org.bukkit.block.Sign
import java.util.*
import kotlin.math.roundToInt


class cCuboid(override var name: String, jplugin : JavaPlugin) : ICommand {

    private val SpawnCuboids = jplugin
    private val CuboidData = CuboidData(SpawnCuboids)

    private val PlayerData : PlayerData = PlayerData(SpawnCuboids)

    private val translation = Translation(SpawnCuboids)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        val color = GetConfig("mainColor")

        if(sender is Player) {
            val p : Player = sender
            if(args.isEmpty()) {
                about(p)
                return true
            }
            when (args[0].lowercase()) {
                "about" -> {
                    about(p)
                }
                "delete" -> {
                    val playerCuboidId : String = PlayerData.get(p, "cuboid") as String
                    val playerStatusString : String = PlayerData.get(p, "status") as String
                    if(playerStatusString == Status.OWNER.toString()) {
                        delete(p, p, playerCuboidId, true)
                    } else {
                        p.sendMessage(translation.get("cCuboid_delete_noowncuboid"))
                    }
                }
                "add" -> {
                    val playerCuboidId : String = PlayerData.get(p, "cuboid") as String
                    val playerStatusString : String = PlayerData.get(p, "status") as String
                    if(playerStatusString == Status.OWNER.toString()) {
                        if(args[1].isNotEmpty()) {
                            val foundPlayer : Player? = SpawnCuboids.server.getPlayer(args[1])
                            if(foundPlayer != null) {
                                PlayerData.CreateOrGetPlayer(foundPlayer)
                                if(PlayerData.get(foundPlayer, "status") == Status.NONE.toString()) {
                                    val cuboid : Cuboid? = CuboidData.GetCuboid(playerCuboidId)
                                    if(cuboid != null) {
                                        if(cuboid.members.size == GetConfig("maxMemberCount").toInt()) {
                                            p.sendMessage(String.format(translation.get("cCuboid_add_maxmembers"),GetConfig("maxMemberCount")))
                                            return true
                                        }
                                        PlayerData.setCuboid(foundPlayer, cuboid, Status.MEMBER)
                                        cuboid.members.add(foundPlayer.uniqueId.toString())
                                        CuboidData.set(cuboid, "members", cuboid.members)
                                        val container = WorldGuard.getInstance().platform.regionContainer
                                        val weWorld = BukkitAdapter.adapt(cuboid.centerLocation.world)
                                        val regions = container[weWorld]
                                        val rg = regions?.getRegion(playerCuboidId)
                                        rg?.owners?.addPlayer(foundPlayer.uniqueId)
                                        p.sendMessage(String.format(translation.get("cCuboid_add_success"),foundPlayer.name))
                                        foundPlayer.sendMessage(String.format(translation.get("cCuboid_add_added"), playerCuboidId, p.name))
                                        foundPlayer.sendMessage(translation.get("cCuboid_add_leave"))
                                    } else {
                                        p.sendMessage(String.format(translation.get("cCuboid_add_errornocuboid"), playerCuboidId))
                                        return true
                                    }
                                } else {
                                    p.sendMessage(translation.get("cCuboid_add_alreadyhas"))
                                }
                            } else {
                                p.sendMessage(translation.get("cCuboid_add_invalidplayer"))
                            }
                        } else {
                            p.sendMessage(translation.get("cCuboid_add_noplayer"))
                        }
                    } else {
                        p.sendMessage(translation.get("cCuboid_add_noowncuboid"))
                    }
                }
                "remove" -> {
                    val playerCuboidId : String = PlayerData.get(p, "cuboid") as String
                    val playerStatusString : String = PlayerData.get(p, "status") as String
                    if(playerStatusString == Status.OWNER.toString()) {
                        if(args[1].isNotEmpty()) {
                            val foundPlayer : Player? = SpawnCuboids.server.getPlayer(args[1])
                            if(foundPlayer != null) {
                                if(PlayerData.get(foundPlayer, "status") == Status.MEMBER.toString() && PlayerData.get(foundPlayer, "cuboid") == playerCuboidId) {
                                    val cuboid : Cuboid? = CuboidData.GetCuboid(playerCuboidId)
                                    if(cuboid != null) {
                                        PlayerData.setCuboid(foundPlayer, null, Status.NONE)
                                        cuboid.members.remove(foundPlayer.uniqueId.toString())
                                        CuboidData.set(cuboid, "members", cuboid.members)
                                        val container = WorldGuard.getInstance().platform.regionContainer
                                        val weWorld = BukkitAdapter.adapt(cuboid.centerLocation.world)
                                        val regions = container[weWorld]
                                        val rg = regions?.getRegion(playerCuboidId)
                                        rg?.owners?.removePlayer(foundPlayer.uniqueId)
                                        p.sendMessage(String.format(translation.get("cCuboid_remove_success"), foundPlayer.name))
                                        foundPlayer.sendMessage(String.format(translation.get("cCuboid_remove_removed"),playerCuboidId, p.name))
                                    } else {
                                        p.sendMessage(String.format(translation.get("cCuboid_remove_errornocuboid"), playerCuboidId))
                                        return true
                                    }
                                } else {
                                    p.sendMessage(translation.get("cCuboid_remove_notamember"))
                                }
                            } else {
                                p.sendMessage(translation.get("cCuboid_remove_invalidplayer"))
                            }
                        } else {
                            p.sendMessage(translation.get("cCuboid_remove_noplayer"))
                        }
                    } else {
                        p.sendMessage(translation.get("cCuboid_remove_noowncuboid"))
                    }
                }
                "leave" -> {
                    val playerCuboidId : String = PlayerData.get(p, "cuboid") as String
                    val playerStatusString : String = PlayerData.get(p, "status") as String
                    if(playerStatusString == Status.MEMBER.toString()) {
                        val cuboid : Cuboid? = CuboidData.GetCuboid(playerCuboidId)
                        if(cuboid != null) {
                            PlayerData.setCuboid(p, null, Status.NONE)
                            cuboid.members.remove(p.uniqueId.toString())
                            CuboidData.set(cuboid, "members", cuboid.members)
                            val container = WorldGuard.getInstance().platform.regionContainer
                            val weWorld = BukkitAdapter.adapt(cuboid.centerLocation.world)
                            val regions = container[weWorld]
                            val rg = regions?.getRegion(playerCuboidId)
                            rg?.owners?.removePlayer(p.uniqueId)
                            p.sendMessage(String.format(translation.get("cCuboid_leave_success"), playerCuboidId))
                            val owner = SpawnCuboids.server.getPlayer(UUID.fromString(cuboid.owner))
                            owner?.sendMessage(String.format(translation.get("cCuboid_leave_hasleft"), p.name))
                        } else {
                            p.sendMessage(String.format(translation.get("cCuboid_leave_errornocuboid"), playerCuboidId))
                            return true
                        }
                    } else {
                        p.sendMessage(translation.get("cCuboid_leave_notincuboid"))
                    }
                }
                "info" -> {
                    val container = WorldGuard.getInstance().platform.regionContainer
                    val regions = container[BukkitAdapter.adapt(p.world)]
                    val set: ApplicableRegionSet = regions!!.getApplicableRegions(BukkitAdapter.asBlockVector(p.location))
                    if(set.size() > 0) {
                        for(region : ProtectedRegion in set) {
                            if(region.id.length == 4) {
                                val cuboid : Cuboid? = CuboidData.GetCuboid(region.id)
                                if(cuboid != null) {
                                    var cuboidOwner : Any = "none"
                                    if(cuboid.owner != "none") {
                                        val foundPlayer : Player? = SpawnCuboids.server.getPlayer(UUID.fromString(cuboid.owner))
                                        if (foundPlayer != null) {
                                            cuboidOwner = foundPlayer.name
                                        }
                                    }
                                    val size : Int = cuboid.radius * 2 + 3
                                    val members = arrayListOf<String>()
                                    for(member in cuboid.members) {
                                        members.add(SpawnCuboids.server.getOfflinePlayer(UUID.fromString(member)).name as String)
                                    }
                                    val strmembers = members.toString().replace("[", "§8[§f").replace("]", "§8]§f").replace(",", "§8,§f")
                                    p.sendMessage(
                                        """ 
                                        §8§k|
                                        §8[${color}SpawnCuboids§8] §8- §7Cuboid ${color}${region.id} §7info
                                          §8- ${String.format(translation.get("cCuboid_info_id"), cuboid.id)}
                                          §8- ${String.format(translation.get("cCuboid_info_owner"), cuboidOwner)}
                                          §8- ${String.format(translation.get("cCuboid_info_members"), strmembers)}
                                          §8- ${String.format(translation.get("cCuboid_info_center"), cuboid.centerLocation.blockX, cuboid.centerLocation.blockY, cuboid.centerLocation.blockZ)}
                                          §8- ${String.format(translation.get("cCuboid_info_radius"), cuboid.radius)}
                                          §8- ${String.format(translation.get("cCuboid_info_size"), size, size)}
                                        §8§k|
                                        """.trimIndent()
                                    )
                                    return true
                                }
                            }
                        }
                    } else {
                        p.sendMessage(translation.get("cCuboid_info_nocuboids"))
                    }
                }
                "admin" -> {
                    if(p.hasPermission("sc.admin")) {
                        if(args.size < 2) {
                            about(p)
                            return true
                        }

                        when (args[1].lowercase()) {
                            "reload" -> {
                                val file = File(SpawnCuboids.dataFolder.absolutePath + "/config.yml")
                                SpawnCuboids.config.load(file)
                                p.sendMessage(translation.get("cCuboid_adminreload_reloaded"))
                                p.playSound(p.location, Sound.BLOCK_SWEET_BERRY_BUSH_PICK_BERRIES, 1F, 1F)
                            }
                            "delete" -> {
                                if(args[2].isNotEmpty()) {
                                    val foundPlayer : Player? = SpawnCuboids.server.getPlayer(args[2])
                                    if(foundPlayer == null) {
                                        p.sendMessage(translation.get("cCuboid_admindelete_invalidplayer"))
                                        return true
                                    }
                                    val playerData = PlayerData.CreateOrGetPlayer(foundPlayer)
                                    val cuboid = playerData["cuboid"] as String
                                    if(playerData["status"] == Status.OWNER.toString())
                                        delete(p, foundPlayer, cuboid, true)
                                    else p.sendMessage(translation.get("cCuboid_admindelete_noowncuboid"))
                                }
                            }
                            "destroy" -> {
                                if(args[2].isNotEmpty()) {
                                    val CuboidDataFile =
                                        File(SpawnCuboids.dataFolder, "CuboidData" + File.separator + args[2] + ".yml")
                                    if (!CuboidDataFile.exists()) {
                                        p.sendMessage(translation.get("cCuboid_admindestroy_invalidcuboid"))
                                        return true
                                    }
                                    val cuboidObject = CuboidData.GetCuboid(args[2])
                                    if (cuboidObject != null) {
                                        if (cuboidObject.owner == "none") {
                                            destroy(p, CuboidDataFile, cuboidObject)
                                            return true
                                        }
                                        val foundPlayer: Player? =
                                            SpawnCuboids.server.getOfflinePlayer(UUID.fromString(cuboidObject.owner)) as Player?
                                        if (foundPlayer != null) {
                                            PlayerData.setCuboid(foundPlayer, null, Status.NONE)
                                            val members = cuboidObject.members
                                            for (member: String in members) {
                                                val asPlayer: Player? =
                                                    SpawnCuboids.server.getOfflinePlayer(UUID.fromString(member)) as Player?
                                                if (asPlayer != null) {
                                                    PlayerData.setCuboid(asPlayer, null, Status.NONE)
                                                }
                                            }
                                            destroy(p, CuboidDataFile, cuboidObject)
                                        } else {
                                            p.sendMessage(translation.get("cCuboid_admindestroy_noowner"))
                                        }
                                    } else {
                                        destroy(p, CuboidDataFile, null)
                                    }
                                } else {
                                    p.sendMessage(translation.get("cCuboid_admindestroy_nocuboid"))
                                    return true
                                }
                            }
                            else -> {
                                about(p)
                            }
                        }
                    }
                }
                else -> {
                    about(p)
                }
            }
        }
        return true
    }

    private fun delete(invoker : Player, p : Player?, playerCuboidId : String, sign : Boolean) {

        val cuboid : Cuboid? = CuboidData.GetCuboid(playerCuboidId)
        if (cuboid == null) {
            invoker.sendMessage(String.format(translation.get("cCuboid_delete_errornocuboid"), playerCuboidId))
            return
        }
        if (p != null) {
            PlayerData.setCuboid(p, null, Status.NONE)
            CuboidData.set(cuboid, "owner", "none")
            CuboidData.set(cuboid, "members", arrayListOf<String>())
            val members = cuboid.members
            for(member : String in members) {
                val asPlayer : Player? = SpawnCuboids.server.getOfflinePlayer(UUID.fromString(member)) as Player?
                if(asPlayer != null) {
                    PlayerData.setCuboid(asPlayer, null, Status.NONE)
                }
            }
        }
        var newPrice : Double = if(cuboid.price == 0.0) 0.25 else cuboid.price * 1.25
        newPrice = (newPrice * 100.0).roundToInt() / 100.0
        CuboidData.set(cuboid, "price", newPrice)
        val weWorld = BukkitAdapter.adapt(cuboid.centerLocation.world)
        if (p != null) {
            val container = WorldGuard.getInstance().platform.regionContainer
            val regions = container[weWorld]
            val rg = regions?.getRegion(playerCuboidId)
            rg?.owners?.removePlayer(p.uniqueId)
        }
        val weSignLocation = BukkitAdapter.asBlockVector(cuboid.centerLocation)
        val region = CuboidRegion(weWorld, weSignLocation, weSignLocation)
        val region2 = CuboidRegion(weWorld, weSignLocation, weSignLocation)
        val region3 = CuboidRegion(weWorld, weSignLocation, weSignLocation)
        region.expand(BlockVector3.at(cuboid.radius, weWorld.maxY, cuboid.radius), BlockVector3.at(-cuboid.radius, -weWorld.maxY, -cuboid.radius))
        region2.expand(BlockVector3.at(cuboid.radius, weWorld.maxY, cuboid.radius), BlockVector3.at(-cuboid.radius, 0, -cuboid.radius))
        region3.expand(BlockVector3.at(cuboid.radius, 0, cuboid.radius), BlockVector3.at(-cuboid.radius, -1, -cuboid.radius))
        region3.contract(BlockVector3.at(0, -1, 0))
        var baseBlock : Material? = Material.matchMaterial(SpawnCuboids.config.get("baseBlock") as String)
        if(baseBlock == null) baseBlock = Material.GRASS_BLOCK
        invoker.sendMessage(translation.get("cCuboid_delete_regen"))
        WorldEdit.getInstance().newEditSession(weWorld).use { editSession ->
            weWorld.regenerate(region, editSession)
        }
        WorldEdit.getInstance().newEditSession(weWorld).use { editSession ->
            editSession.setBlocks(region2, BukkitAdapter.adapt(Material.AIR.createBlockData()))
            editSession.setBlocks(region3, BukkitAdapter.adapt(baseBlock.createBlockData()))
        }

        invoker.sendMessage(String.format(translation.get("cCuboid_delete_success"),playerCuboidId))
        if(sign) {
            WorldEdit.getInstance().newEditSession(weWorld).use { editSession ->
                editSession.setBlock(weSignLocation, BukkitAdapter.adapt(Material.SPRUCE_SIGN.createBlockData()))
            }
            val signBlock : Sign = cuboid.centerLocation.block.state as Sign
            signBlock.setLine(0,translation.get("sign_line_1"))
            signBlock.setLine(1, String.format(translation.get("sign_line_2"), newPrice))
            signBlock.setLine(2,translation.get("sign_line_3"))
            signBlock.setLine(3,String.format(translation.get("sign_line_4"), playerCuboidId))
            signBlock.update()
        }
    }

    private fun destroy(p : Player, data : File, cuboid : Cuboid?) {

        if(cuboid != null) {
            var owner : Player? = null
            if(cuboid.owner != "none") {
                owner = SpawnCuboids.server.getOfflinePlayer(UUID.fromString(cuboid.owner)) as Player
            }
            delete(p, owner, cuboid.id, false)
            val weWorld = BukkitAdapter.adapt(cuboid.centerLocation.world)
            val container = WorldGuard.getInstance().platform.regionContainer
            val regions = container[weWorld]
            regions!!.removeRegion(cuboid.id)
        }
        data.delete()
        p.sendMessage(String.format(translation.get("cCuboid_destroy_success"), cuboid!!.id))
    }

    private fun about(p : Player) {

        val color = GetConfig("mainColor")

        if(p.hasPermission("sc.admin")) {
            p.sendMessage(
                """ 
                    §8§k|
                    §8[${color}SpawnCuboids§8] §8- ${translation.get("cCuboid_about_commands")}
                      §8- §f/${color}cuboid §8[§7about§f/§7info§f/§7delete§f/§7add§f/§7remove/§7leave§f/§7admin§8]
                      §8- §f/${color}cuboid §fdelete §8- ${translation.get("cCuboid_about_norefund")}
                      §8- §f/${color}cuboid §fadmin §8[§7reload§f/§7delete§f/§7destroy§8]
                    §8§k|
                    §8[${color}SpawnCuboids§8] §8- ${translation.get("cCuboid_about_info")}
                      §8- ${color}${translation.get("cCuboid_about_version")}§8: §f${SpawnCuboids.description.version} §8- §f${SpawnCuboids.description.apiVersion}
                      §8- ${color}${translation.get("cCuboid_about_creator")}§8: §fIru21 §c- §7https://github.com/Iru21
                    §8§k|
                """.trimIndent()
            )
        } else {
            p.sendMessage(
                """ 
                    §8[${color}SpawnCuboids§8] §8- ${translation.get("cCuboid_about_commands")}
                      §8- §f/${color}cuboid §8[§7about§f/§7info§f/§7delete§f/§7add§f/§7remove/§7leave§8]
                      §8- §f/${color}cuboid §fdelete §8- ${translation.get("cCuboid_about_norefund")}
                    §8§k|
                    §8[${color}SpawnCuboids§8] §8- ${translation.get("cCuboid_about_info")}
                      §8- ${translation.get("cCuboid_about_version")}§8: §f${SpawnCuboids.description.version} §8- §f${SpawnCuboids.description.apiVersion}
                      §8- ${translation.get("cCuboid_about_creator")}§8: §fIru21 §c- §7https://github.com/Iru21
                    §8§k|
                """.trimIndent()
            )
        }
    }
}