/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat.criticalsmodes.ncp

import net.ccbluex.liquidbounce.features.module.modules.combat.criticalsmodes.CriticalsMode
import net.minecraft.entity.Entity

/**
 * @author SkidderMC/FDPClient
 * @author Koitoyuu
 */
object UNCP : CriticalsMode("UNCP") {
    override fun onAttack(entity: Entity) {
        sendPacket(0.00001058293536, false)
        sendPacket(0.00000916580235, false)
        sendPacket(0.00000010371854, false)
        crit(entity)
    }
}
