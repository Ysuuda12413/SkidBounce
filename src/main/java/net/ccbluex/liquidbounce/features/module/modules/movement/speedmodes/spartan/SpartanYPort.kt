/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.spartan

import net.ccbluex.liquidbounce.event.events.MotionEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.extensions.jmp
import net.ccbluex.liquidbounce.utils.misc.RandomUtils.nextDouble

/**
 * @author CCBlueX/LiquidBounce
 */
object SpartanYPort : SpeedMode("SpartanYPort") {
    private var airMoves = 0
    override fun onMotion(event: MotionEvent) {
        if (mc.gameSettings.keyBindForward.isKeyDown) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jmp()
                airMoves = 0
            } else {
                mc.timer.timerSpeed = 1.08f
                if (airMoves >= 3) mc.thePlayer.jumpMovementFactor = 0.0275f
                if (airMoves >= 4 && airMoves % 2 == 0) {
                    mc.thePlayer.motionY = -0.32 - nextDouble(endInclusive = 0.009)
                    mc.thePlayer.jumpMovementFactor = 0.0238f
                }
                airMoves++
            }
        }
    }
}
