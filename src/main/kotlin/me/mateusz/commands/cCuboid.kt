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
import me.mateusz.util.GetConfig
import org.bukkit.Material
import org.bukkit.block.Sign
import java.util.*
import kotlin.math.roundToInt


class cCuboid(override var name: String, jplugin : JavaPlugin) : ICommand {

    private val SpawnCuboids = jplugin
    private val CuboidData = CuboidData(SpawnCuboids)

    private val PlayerData : PlayerData = PlayerData(SpawnCuboids)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        val prefix = GetConfig("prefix")
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
                        p.sendMessage("$prefix §cYou don't own a cuboid${color}§l!")
                    }
                }
                "add" -> {
                    val playerCuboidId : String = PlayerData.get(p, "cuboid") as String
                    val playerStatusString : String = PlayerData.get(p, "status") as String
                    if(playerStatusString == Status.OWNER.toString()) {
                        if(args[1].isNotEmpty()) {
                            val foundPlayer : Player? = SpawnCuboids.server.getPlayer(args[1])
                            if(foundPlayer != null) {
                                if(PlayerData.get(foundPlayer, "status") == Status.NONE.toString()) {
                                    val cuboid : Cuboid? = CuboidData.GetCuboid(playerCuboidId)
                                    if(cuboid != null) {
                                        if(cuboid.members.size == GetConfig("maxMemberCount").toInt()) {
                                            p.sendMessage("$prefix §cYou've hit a max member limit of $color${GetConfig("maxMemberCount")}§c members$color§l!")
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
                                        p.sendMessage("$prefix §7Successfully added$color ${foundPlayer.name}§7 to your cuboid$color§l!")
                                        foundPlayer.sendMessage("$prefix §7You've been added to$color cuboid §7with id $color$playerCuboidId§7 by $color${p.name}§l!§7 Hooray$color§l!!")
                                        foundPlayer.sendMessage("$prefix §7Remember you can always leave with §f/${color}cuboid §fleave$color§l!")
                                    } else {
                                        p.sendMessage("$prefix §cThere has been an error! I couldn't find any cuboid with id $playerCuboidId$color§l! §cPlease contact the administrator$color§l!")
                                        return true
                                    }
                                } else {
                                    p.sendMessage("$prefix §cThis player already owns or is a member of a cuboid$color§l!")
                                }
                            } else {
                                p.sendMessage("$prefix §cInvalid player$color§l!")
                            }
                        } else {
                            p.sendMessage("$prefix §cProvide a player to add$color§l!")
                        }
                    } else {
                        p.sendMessage("$prefix §cYou don't own a cuboid${color}§l!")
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
                                        p.sendMessage("$prefix §7Successfully removed$color ${foundPlayer.name}§7 from your cuboid$color§l!")
                                        foundPlayer.sendMessage("$prefix §7You've been removed from$color cuboid §7with id $color$playerCuboidId§7 by $color${p.name}§l!")
                                    } else {
                                        p.sendMessage("$prefix §cThere has been an error! I couldn't find any cuboid with id $playerCuboidId$color§l! §cPlease contact the administrator$color§l!")
                                        return true
                                    }
                                } else {
                                    p.sendMessage("$prefix §cThis player does not belong to your cuboid$color§l!")
                                }
                            } else {
                                p.sendMessage("$prefix §cInvalid player$color§l!")
                            }
                        } else {
                            p.sendMessage("$prefix §cProvide a player to remove$color§l!")
                        }
                    } else {
                        p.sendMessage("$prefix §cYou don't own a cuboid${color}§l!")
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
                            p.sendMessage("$prefix §7Successfully left$color cuboid §7with id $color$playerCuboidId§l!")
                            val owner = SpawnCuboids.server.getPlayer(UUID.fromString(cuboid.owner))
                            owner?.sendMessage("$prefix §7$color${p.name} §7has left your cuboid$color§l!")
                        } else {
                            p.sendMessage("$prefix §cThere has been an error! I couldn't find any cuboid with id $playerCuboidId$color§l! §cPlease contact the administrator$color§l!")
                            return true
                        }
                    } else {
                        p.sendMessage("$prefix §cYou're not a member of a cuboid'${color}§l!")
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
                                    p.sendMessage(
                                        """ 
                                        §8§k|
                                        §8[${color}SpawnCuboids§8] §8- §7Cuboid ${color}${region.id} §7info
                                          §8- ${color}Id§8: §f${cuboid.id}
                                          §8- ${color}Owner§8: §f${cuboidOwner}
                                          §8- ${color}Members§8: §f${members.toString().replace("[", "§8[§f").replace("]", "§8]§f").replace(",", "§8,§f")}
                                          §8- ${color}Last Price§8: §f${cuboid.price}§7$
                                          §8- ${color}Center§8: §f${cuboid.centerLocation.blockX} §8| §f${cuboid.centerLocation.blockY} §8| §f${cuboid.centerLocation.blockZ}
                                          §8- ${color}Radius§8: §f${cuboid.radius}
                                          §8- ${color}Size§8: §f${size}§7x§f${size}
                                        §8§k|
                                        """.trimIndent()
                                    )
                                } else {
                                    p.sendMessage("$prefix §7There's no (more) cuboids where you stand${color}§l!")
                                }
                            } else {
                                p.sendMessage("$prefix §7There's no (more) cuboids where you stand${color}§l!")
                            }
                        }
                    } else {
                        p.sendMessage("$prefix §7There's no cuboids where you stand${color}§l!")
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
                                p.sendMessage("§8[${color}SpawnCuboids§8] §7Reloaded ${color}config${color}§l!")
                                p.playSound(p.location, Sound.BLOCK_SWEET_BERRY_BUSH_PICK_BERRIES, 1F, 1F)
                            }
                            "delete" -> {
                                if(args[2].isNotEmpty()) {
                                    val foundPlayer : Player? = SpawnCuboids.server.getPlayer(args[2])
                                    if(foundPlayer == null) {
                                        p.sendMessage("$prefix §cInvalid player${color}§l!")
                                        return true
                                    }
                                    val playerData = PlayerData.CreateOrGetPlayer(foundPlayer)
                                    val cuboid = playerData["cuboid"] as String
                                    if(playerData["status"] == Status.OWNER.toString())
                                        delete(p, foundPlayer, cuboid, true)
                                    else p.sendMessage("$prefix §cPlayer does not own a cuboid${color}§l!")
                                }
                            }
                            "destroy" -> {
                                if(args[2].isNotEmpty()) {
                                    val CuboidDataFile =
                                        File(SpawnCuboids.dataFolder, "CuboidData" + File.separator + args[2] + ".yml")
                                    if (!CuboidDataFile.exists()) {
                                        p.sendMessage("$prefix §cInvalid cuboid id${color}§l!")
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
                                            p.sendMessage("$prefix §cCould not find the cuboid owner§l!")
                                        }
                                    } else {
                                        destroy(p, CuboidDataFile, null)
                                    }
                                } else {
                                    p.sendMessage("$prefix §cProvide a cuboid id $color§l!")
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

        val prefix = GetConfig("prefix")
        val color = GetConfig("mainColor")

        val cuboid : Cuboid? = CuboidData.GetCuboid(playerCuboidId)
        if (cuboid == null) {
            invoker.sendMessage("$prefix §cThere has been an error! I couldn't find any cuboid with id $playerCuboidId$color§l! §cPlease contact the administrator$color§l!")
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
        invoker.sendMessage("$prefix §7Regenerating terrain...§l!")
        WorldEdit.getInstance().newEditSession(weWorld).use { editSession ->
            weWorld.regenerate(region, editSession)
        }
        WorldEdit.getInstance().newEditSession(weWorld).use { editSession ->
            editSession.setBlocks(region2, BukkitAdapter.adapt(Material.AIR.createBlockData()))
            editSession.setBlocks(region3, BukkitAdapter.adapt(baseBlock.createBlockData()))
        }

        invoker.sendMessage("$prefix §7Successfully deleted$color cuboid§7 with id $color$playerCuboidId§l!")
        if(sign) {
            WorldEdit.getInstance().newEditSession(weWorld).use { editSession ->
                editSession.setBlock(weSignLocation, BukkitAdapter.adapt(Material.SPRUCE_SIGN.createBlockData()))
            }
            val signBlock : Sign = cuboid.centerLocation.block.state as Sign
            signBlock.setLine(0,"$color§lRMB §fto §l§nbuy")
            signBlock.setLine(1, "$newPrice$")
            signBlock.setLine(2,"$color§l^ §fPrice $color§l^")
            signBlock.setLine(3,"$color§lId§8: §f$playerCuboidId")
            signBlock.update()
        }
    }

    private fun destroy(p : Player, data : File, cuboid : Cuboid?) {

        val prefix = GetConfig("prefix")
        val color = GetConfig("mainColor")

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
        p.sendMessage("$prefix §7Successfully destroyed$color cuboid§7 with id $color${cuboid!!.id}§l!")
    }

    private fun about(p : Player) {

        val color = GetConfig("mainColor")

        if(p.hasPermission("sc.admin")) {
            p.sendMessage(
                """ 
                    §8§k|
                    §8[${color}SpawnCuboids§8] §8- §7Commands
                      §8- §f/${color}cuboid §8[§7about§f/§7info§f/§7delete§f/§7add§f/§7remove/§7leave§f/§7admin§8]
                      §8- §f/${color}cuboid §fdelete §8- §4§l(!) §cDoes not refund the money! §4§l(!)
                      §8- §f/${color}cuboid §fadmin §8[§7reload§f/§7delete§f/§7destroy§8]
                    §8§k|
                    §8[${color}SpawnCuboids§8] §8- §7Info
                      §8- ${color}Version§8: §f${SpawnCuboids.description.version} §8- §f${SpawnCuboids.description.apiVersion}
                      §8- ${color}Creator§8: §fIru21 §c- §7https://github.com/Iru21
                    §8§k|
                """.trimIndent()
            )
        } else {
            p.sendMessage(
                """ 
                    §8[${color}SpawnCuboids§8] §8- §7Commands
                      §8- §f/${color}cuboid §8[§7about§f/§7info§f/§7delete§f/§7add§f/§7remove/§7leave§8]
                      §8- §f/${color}cuboid §fdelete §8- §4§l(!) §cDoes not refund the money! §4§l(!)
                    §8§k|
                    §8[${color}SpawnCuboids§8] §8- §7Info
                      §8- ${color}Version§8: §f${SpawnCuboids.description.version} §8- §f${SpawnCuboids.description.apiVersion}
                      §8- ${color}Creator§8: §fIru21 §c- §7https://github.com/Iru21
                """.trimIndent()
            )
        }
    }
}