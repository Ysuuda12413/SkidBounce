/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.aac

import net.ccbluex.liquidbounce.event.events.MotionEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.extensions.jmp

/**
 * @author CCBlueX/LiquidBounce
 */
object AAC2BHop : SpeedMode("AAC2BHop") {
    override fun onMotion(event: MotionEvent) {
        val thePlayer = mc.thePlayer

        if (isMoving) {
            if (thePlayer.onGround) {
                thePlayer.jmp()
                thePlayer.motionX *= 1.02
                thePlayer.motionZ *= 1.02
            } else if (thePlayer.motionY > -0.2) {
                thePlayer.jumpMovementFactor = 0.08f
                thePlayer.motionY += 0.01430999999999999
                thePlayer.jumpMovementFactor = 0.07f
            }
        } else {
            thePlayer.motionX = 0.0
            thePlayer.motionZ = 0.0
        }
    }
}
