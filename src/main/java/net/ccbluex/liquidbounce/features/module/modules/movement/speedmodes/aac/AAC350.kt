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
object AAC350 : SpeedMode("AAC3.5.0") {

    override fun onMotion(event: MotionEvent) {
        val thePlayer = mc.thePlayer ?: return

        if (isMoving) {
            thePlayer.jumpMovementFactor += 0.00208f
            if (thePlayer.fallDistance <= 1f) {
                if (thePlayer.onGround) {
                    thePlayer.jmp()
                    thePlayer.motionX *= 1.0118f
                    thePlayer.motionZ *= 1.0118f
                } else {
                    thePlayer.motionY -= 0.0147f
                    thePlayer.motionX *= 1.00138f
                    thePlayer.motionZ *= 1.00138f
                }
            }
        }
    }

    override fun onEnable() {
        val thePlayer = mc.thePlayer ?: return

        if (thePlayer.onGround) {
            thePlayer.motionX = 0.0
            thePlayer.motionZ = 0.0
        }
    }

    override fun onDisable() {
        mc.thePlayer?.jumpMovementFactor = 0.02f
    }
}
