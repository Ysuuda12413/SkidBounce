/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.jesusmodes

import net.ccbluex.liquidbounce.event.events.MoveEvent
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.utils.MinecraftInstance

open class JesusMode(val modeName: String, val solid: Boolean) : MinecraftInstance() {
    open fun onMove(event: MoveEvent) {}
    open fun onPacket(event: PacketEvent) {}
    open fun onUpdate() {}
}
