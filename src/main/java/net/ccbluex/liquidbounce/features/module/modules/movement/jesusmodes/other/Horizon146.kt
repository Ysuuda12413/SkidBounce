/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.jesusmodes.other

import net.ccbluex.liquidbounce.features.module.modules.movement.jesusmodes.JesusMode
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe
import net.ccbluex.liquidbounce.utils.extensions.inLiquid

object Horizon146 : JesusMode("Horizon1.4.6", false) {
    private var wasJesus = false
    override fun onUpdate() {
        if (!mc.thePlayer.inLiquid) {
            if (wasJesus)
                mc.gameSettings.keyBindJump.pressed = false
            wasJesus = false
            return
        }

        mc.gameSettings.keyBindJump.pressed = true
        strafe()
        if (isMoving && !mc.thePlayer.onGround)
            mc.thePlayer.motionY += 0.13

        wasJesus = mc.thePlayer.inLiquid
    }
}
