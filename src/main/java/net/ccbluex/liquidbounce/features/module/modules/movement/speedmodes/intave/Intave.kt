/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.intave

import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.extensions.jmp

object Intave : SpeedMode("Intave") {
    override fun onUpdate() {
        if (isMoving) {
            if (mc.thePlayer.onGround)
                mc.thePlayer.jmp()
            when {
                mc.thePlayer.fallDistance >= 1.3 -> mc.timer.timerSpeed = 1f
                mc.thePlayer.fallDistance > 0.1 && mc.thePlayer.fallDistance < 1.3 -> mc.timer.timerSpeed = 0.7f
                !mc.thePlayer.onGround && mc.thePlayer.fallDistance <= 0.1 -> mc.timer.timerSpeed = 1.4f
            }
        }
    }
}
