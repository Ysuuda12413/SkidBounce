/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.flymodes

import net.ccbluex.liquidbounce.event.events.*
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.extensions.isActuallyPressed

open class FlyMode(val modeName: String) : MinecraftInstance() {
    open fun onMove(event: MoveEvent) {}
    open fun onPacket(event: PacketEvent) {}
    open fun onRender3D(event: Render3DEvent) {}
    open fun onBB(event: BlockBBEvent) {}
    open fun onJump(event: JumpEvent) {}
    open fun onStep(event: StepEvent) {}
    open fun onMotion(event: MotionEvent) {}
    open fun onUpdate() {}

    open fun onEnable() {}
    open fun onDisable() {}

    protected val yDirection: Int
        get() {
            var i = 0
            if (mc.gameSettings.keyBindJump.isActuallyPressed) i++
            if (mc.gameSettings.keyBindSneak.isActuallyPressed) i--
            return i
        }
}
