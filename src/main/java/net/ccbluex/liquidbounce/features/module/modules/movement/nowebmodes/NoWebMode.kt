/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes

import net.ccbluex.liquidbounce.event.events.BlockCollideEvent
import net.ccbluex.liquidbounce.utils.MinecraftInstance

open class NoWebMode(val modeName: String) : MinecraftInstance() {
    open fun onUpdate() {}
    open fun onCollide(event: BlockCollideEvent) {}
}
