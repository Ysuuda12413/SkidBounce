/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.other

import net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.NoWebMode
import net.ccbluex.liquidbounce.utils.extensions.jmp

/**
 * @author CCBlueX/LiquidBounce
 */
object Rewinside : NoWebMode("Rewinside") {
    override fun onUpdate() {
        if (!mc.thePlayer.isInWeb)
            return
        mc.thePlayer.jumpMovementFactor = 0.42f
        mc.thePlayer.jmp()
    }
}
