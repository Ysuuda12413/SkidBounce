/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat.criticalsmodes.vanilla

import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.criticalsmodes.CriticalsMode
import net.minecraft.network.play.client.C03PacketPlayer

/**
 * @author CCBlueX/LiquidBounce
 */
object NoGround : CriticalsMode("NoGround") {
    override fun onPacket(event: PacketEvent) {
        if (event.packet is C03PacketPlayer)
            event.packet.onGround = false
    }
}
