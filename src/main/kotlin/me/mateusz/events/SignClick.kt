package me.mateusz.events

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion
import me.mateusz.Economy
import me.mateusz.SpawnCuboids
import me.mateusz.data.Cuboid
import me.mateusz.data.CuboidData
import me.mateusz.data.PlayerData
import me.mateusz.data.Status
import me.mateusz.translation.Translation
import me.mateusz.util.GetConfig
import net.md_5.bungee.api.ChatColor
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.BlockVector

class SignClick(sc : JavaPlugin) : Listener {

    val SpawnCuboids = sc
    val economy = Economy(SpawnCuboids)

    private val CuboidData : CuboidData = CuboidData(SpawnCuboids)
    private val PlayerData : PlayerData = PlayerData(SpawnCuboids)

    val translation = Translation(SpawnCuboids)

    @EventHandler
    fun onSignClick(e : PlayerInteractEvent) {

        val p : Player = e.player
        if(e.clickedBlock?.type == Material.SPRUCE_SIGN) {
            if(e.action == Action.RIGHT_CLICK_BLOCK) {
                val sign : Sign = e.clickedBlock!!.state as Sign
                SpawnCuboids.server.consoleSender.sendMessage(sign.getLine(0).replace("§", "*"))
                SpawnCuboids.server.consoleSender.sendMessage(translation.get("sing_line_1").replace("§", "*"))
                if(sign.getLine(0) == translation.get("sign_line_1")) {
                    val op : OfflinePlayer = e.player
                    val price = sign.getLine(1).dropLast(1).toDouble()
                    val id = sign.getLine(3).drop(12)
                    if (p.hasPermission("sc.buy")) {
                        if(economy.econ?.has(op, price) == true) {
                            val cuboid: Cuboid? = CuboidData.GetCuboid(id)
                            if (cuboid != null) {
                                CuboidData.set(cuboid, "owner", p.uniqueId.toString())
                            } else {
                                p.sendMessage(String.format(translation.get("signclick_errornocuboid"), id))
                                return
                            }
                            economy.econ?.withdrawPlayer(op, price)
                            val container = WorldGuard.getInstance().platform.regionContainer
                            val weWorld = BukkitAdapter.adapt(cuboid.centerLocation.world)
                            val regions = container[weWorld]
                            val rg = regions?.getRegion(id)
                            rg?.owners?.addPlayer(p.uniqueId)
                            WorldEdit.getInstance().newEditSession(weWorld).use { editSession ->
                                editSession.setBlock(
                                    BukkitAdapter.asBlockVector(cuboid.centerLocation),
                                    BukkitAdapter.adapt(Material.AIR.createBlockData())
                                )
                            }
                            PlayerData.setCuboid(p, cuboid, Status.OWNER)
                            p.sendMessage(String.format(translation.get("signclick_success"), id))
                        } else {
                            p.sendMessage(translation.get("signclick_notenoughmoney"))
                        }
                    }
                }
            }
        }
    }
}