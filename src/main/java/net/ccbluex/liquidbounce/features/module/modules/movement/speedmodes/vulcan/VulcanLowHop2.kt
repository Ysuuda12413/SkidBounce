/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.vulcan

import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe
import net.ccbluex.liquidbounce.utils.extensions.jmp

object VulcanLowHop2 : SpeedMode("VulcanLowHop2") {
    override fun onUpdate() {
        val player = mc.thePlayer ?: return
        if (player.isInWater || player.isInLava || player.isInWeb || player.isOnLadder) return

        if (isMoving) {
            if (!player.onGround && player.fallDistance > 1.1) {
                mc.timer.timerSpeed = 1f
                player.motionY = -0.25
                return
            }

            if (player.onGround) {
                player.jmp()
                strafe(0.4815f)
                mc.timer.timerSpeed = 1.263f
            } else if (player.ticksExisted % 4 == 0) {
                if (player.ticksExisted % 3 == 0) player.motionY = -0.01 / player.motionY
                else player.motionY = -player.motionY / player.posY
                mc.timer.timerSpeed = 0.8985f
            }

        } else mc.timer.timerSpeed = 1f
    }
}
