/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.other

import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.LongJumpMode

/**
 * @author CCBlueX/LiquidBounce
 */
object Hycraft : LongJumpMode("Hycraft") {
    override fun onUpdate() {
        if (mc.thePlayer.motionY < 0) {
            mc.thePlayer.motionY *= 0.75f
            mc.thePlayer.jumpMovementFactor = 0.055f
        } else {
            mc.thePlayer.motionY += 0.02f
            mc.thePlayer.jumpMovementFactor = 0.08f
        }
    }
}
