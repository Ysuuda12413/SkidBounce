/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat.velocitymodes.grim

import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitymodes.VelocityMode
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitymodes.vanilla.Vanilla
import net.minecraft.network.play.server.S32PacketConfirmTransaction

/**
 * @author SkidderMC/FDPClient
 */
object Grim2343 : VelocityMode("Grim2.3.43") {
    private var cancel = 0

    override fun onEnable() {
        cancel = 0
    }

    override fun onVelocityPacket(event: PacketEvent) {
        Vanilla.onVelocityPacket(event)
        cancel = 6
    }

    override fun onPacket(event: PacketEvent) {
        if (event.packet is S32PacketConfirmTransaction && cancel > 0) {
            event.cancelEvent()
            cancel--
        }
    }

    override fun onUpdate() {
        if (cancel > 0) cancel--
    }
}
