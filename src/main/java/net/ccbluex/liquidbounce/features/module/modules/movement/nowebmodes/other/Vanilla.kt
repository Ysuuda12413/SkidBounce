/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.other

import net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.NoWebMode

/**
 * @author CCBlueX/LiquidBounce
 */
object Vanilla : NoWebMode("Vanilla") {
    override fun onUpdate() {
        mc.thePlayer.isInWeb = false
    }
}
