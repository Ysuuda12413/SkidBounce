/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat.velocitymodes.other

import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitymodes.VelocityMode
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitymodes.vanilla.Vanilla
import net.minecraft.network.play.client.C0FPacketConfirmTransaction

/**
 * @author SkidderMC/FDPClient
 */
object OldVulcan : VelocityMode("OldVulcan") {
    override fun onPacket(event: PacketEvent) {
        if (event.packet is C0FPacketConfirmTransaction && event.packet.uid <= -30769 && event.packet.uid >= -31767)
            event.cancelEvent()
    }

    override fun onVelocityPacket(event: PacketEvent) {
        Vanilla.onVelocityPacket(event)
    }
}
