/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.aac

import net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.NoWebMode
import net.ccbluex.liquidbounce.utils.MovementUtils

/**
 * @author liquidbounceplusreborn/LiquidbouncePlus-Reborn
 */
object AACv4 : NoWebMode("AACv4") {
    override fun onUpdate() {
        if (!mc.thePlayer.isInWeb) return
        mc.gameSettings.keyBindRight.pressed = false
        mc.gameSettings.keyBindBack.pressed = false
        mc.gameSettings.keyBindLeft.pressed = false

        if (mc.thePlayer.onGround) MovementUtils.strafe(0.25F)
        else {
            MovementUtils.strafe(0.12F)
            mc.thePlayer.motionY = 0.0
        }
    }
}
