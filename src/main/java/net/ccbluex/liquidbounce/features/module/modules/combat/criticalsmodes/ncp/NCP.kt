/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat.criticalsmodes.ncp

import net.ccbluex.liquidbounce.features.module.modules.combat.criticalsmodes.CriticalsMode
import net.minecraft.entity.Entity

/**
 * @author CCBlueX/LiquidBounce
 */
object NCP : CriticalsMode("NCP") {
    override fun onAttack(entity: Entity) {
        sendPacket(0.11, false)
        sendPacket(0.1100013579, false)
        sendPacket(0.0000013579, false)
        crit(entity)
    }
}
