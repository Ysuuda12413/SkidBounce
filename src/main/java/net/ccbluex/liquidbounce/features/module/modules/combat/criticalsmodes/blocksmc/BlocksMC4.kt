/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat.criticalsmodes.blocksmc

import net.ccbluex.liquidbounce.features.module.modules.combat.criticalsmodes.CriticalsMode
import net.minecraft.entity.Entity

/**
 * @author CCBlueX/LiquidBounce
 * @author EclipsesDev
 */
object BlocksMC4 : CriticalsMode("BlocksMC4") {
    override fun onAttack(entity: Entity) {
        sendPacket(0.001, false)
        sendPacket(0.0010353, false)
        sendPacket(0.0, false)
        crit(entity)
    }
}
