/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.ncp

import net.ccbluex.liquidbounce.event.events.MotionEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving

/**
 * @author CCBlueX/LiquidBounce
 */
object OnGround : SpeedMode("OnGround", true) {
    override fun onMotion(event: MotionEvent) {
        val thePlayer = mc.thePlayer ?: return

        if (!isMoving)
            return

        if (thePlayer.fallDistance > 3.994)
            return
        if (thePlayer.isCollidedHorizontally)
            return

        thePlayer.posY -= 0.3993000090122223
        thePlayer.motionY = -1000.0
        thePlayer.cameraPitch = 0.3f
        thePlayer.distanceWalkedModified = 44f
        mc.timer.timerSpeed = 1f

        if (thePlayer.onGround) {
            thePlayer.posY += 0.3993000090122223
            thePlayer.motionY = 0.3993000090122223
            thePlayer.distanceWalkedOnStepModified = 44f
            thePlayer.motionX *= 1.590000033378601
            thePlayer.motionZ *= 1.590000033378601
            thePlayer.cameraPitch = 0f
            mc.timer.timerSpeed = 1.199f
        }
    }
}
