/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.command.shortcuts

import net.ccbluex.liquidbounce.features.command.Command

class Shortcut(val name: String, val script: List<Pair<Command, Array<String>>>) : Command(name) {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) = script.forEach { it.first.execute(it.second) }
}
