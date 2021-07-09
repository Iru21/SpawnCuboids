package me.mateusz.commands

import org.bukkit.command.CommandExecutor

interface ICommand : CommandExecutor {
    var name : String
}