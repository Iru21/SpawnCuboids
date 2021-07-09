package me.mateusz.data

import org.bukkit.Location

class Cuboid(id_: String, owner_: String, price_: Double, centerLocation_: Location, radius_: Int, members_ : ArrayList<String>) {
    val owner : String = owner_
    val id : String = id_
    val price : Double = price_
    val centerLocation : Location = centerLocation_
    val radius : Int = radius_
    val members : ArrayList<String> = members_
}