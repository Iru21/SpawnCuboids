package me.mateusz.events

import  com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion
import me.mateusz.data.Cuboid
import me.mateusz.data.CuboidData
import me.mateusz.util.GetConfig
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import kotlin.math.roundToInt


class SignCreate(SpawnCuboids : JavaPlugin) : Listener {


    private val CuboidData : CuboidData = CuboidData(SpawnCuboids)

    @EventHandler
    fun onSignCreate(e : SignChangeEvent) {

        val prefix = GetConfig("prefix")
        val color = GetConfig("mainColor")
        val parentRegion = GetConfig("parentRegion")

        val p : Player = e.player
        if(p.hasPermission("sc.create")) {
            if(e.getLine(0)?.lowercase() == "[cuboid]" && e.getLine(1)!!.isNotEmpty() && e.getLine(2)!!.isNotEmpty()) {
                var price = e.getLine(1)!!.toDouble()
                price = (price * 100.0).roundToInt() / 100.0
                val radius = e.getLine(2)!!.toInt()
                var id = UUID.randomUUID().toString()
                id = id.substring(0, id.length.coerceAtMost(4))
                e.setLine(0,"$color§lRMB §fto §l§nbuy")
                e.setLine(1, "$price$")
                e.setLine(2,"$color§l^ §fPrice $color§l^")
                e.setLine(3,"$color§lId§8: §f$id")
                val signLocation = e.block.location
                val weSignLocation = BukkitAdapter.asBlockVector(signLocation)
                val weWorld = BukkitAdapter.adapt(signLocation.world)
                val region = CuboidRegion(weWorld, weSignLocation, weSignLocation)
                region.expand(BlockVector3.at(radius, weWorld.maxY, radius), BlockVector3.at(-radius, -weWorld.maxY, -radius))

                val rg = ProtectedCuboidRegion(id, region.pos1, region.pos2)

                val container = WorldGuard.getInstance().platform.regionContainer
                val regions = container[weWorld]
                regions!!.addRegion(rg)

                if(parentRegion != "_none_") {
                    val parent = regions.getRegion(parentRegion)
                    rg.parent = parent
                }

                val cuboid = Cuboid(id, "none", price, signLocation, radius, arrayListOf())
                CuboidData.CreateCuboid(cuboid)

                p.sendMessage("$prefix §7Successfully created a cuboid$color§l!")
            }
        }
    }
}