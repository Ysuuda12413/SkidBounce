/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.other

import net.ccbluex.liquidbounce.event.events.MotionEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe
import net.ccbluex.liquidbounce.utils.extensions.jmp
import net.ccbluex.liquidbounce.utils.extensions.stopXZ

/**
 * @author SkidBounce/SkidBounce
 * @author ManInMyVan
 */
object Saturn : SpeedMode("Saturn") {
    override fun onMotion(event: MotionEvent) {
        if (!isMoving) {
            mc.thePlayer.stopXZ()
            return
        }

        strafe()
        mc.thePlayer.jumpMovementFactor = 0.028f
        mc.thePlayer.jmp(0.38)
    }
}
