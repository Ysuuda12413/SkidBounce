/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat.criticalsmodes.other

import net.ccbluex.liquidbounce.features.module.modules.combat.criticalsmodes.CriticalsMode
import net.minecraft.entity.Entity

/**
 * @author SkidderMC/FDPClient
 */
object Vulcan : CriticalsMode("Vulcan") {
    override fun onAttack(entity: Entity) {
        sendPacket(0.16477328182606651, false)
        sendPacket(0.08307781780646721, false)
        sendPacket(0.0030162615090425808, false)
        crit(entity)
    }
}
