/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes

import net.ccbluex.liquidbounce.event.events.JumpEvent
import net.ccbluex.liquidbounce.event.events.MoveEvent
import net.ccbluex.liquidbounce.utils.MinecraftInstance

open class LongJumpMode(val modeName: String) : MinecraftInstance() {
    open fun onUpdate() {}
    open fun onMove(event: MoveEvent) {}
    open fun onJump(event: JumpEvent) {}
    open fun onDisable() {}
}
