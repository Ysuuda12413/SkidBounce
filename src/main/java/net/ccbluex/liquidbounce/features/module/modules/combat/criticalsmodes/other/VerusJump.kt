/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat.criticalsmodes.other

import net.ccbluex.liquidbounce.features.module.modules.combat.criticalsmodes.CriticalsMode
import net.ccbluex.liquidbounce.utils.extensions.jmp
import net.minecraft.entity.Entity

/**
 * @author CCBlueX/LiquidBounce
 */
object VerusJump : CriticalsMode("VerusJump") {
    override fun onAttack(entity: Entity) {
        mc.thePlayer.motionY = 0.11
        mc.thePlayer.onGround = false
        mc.thePlayer.posY = mc.thePlayer.prevPosY
        mc.thePlayer.isInWeb = true
        mc.thePlayer.jmp(ignoreGround = true)
        mc.thePlayer.prevPosY = mc.thePlayer.posY
        mc.thePlayer.isInWeb = false
    }
}
