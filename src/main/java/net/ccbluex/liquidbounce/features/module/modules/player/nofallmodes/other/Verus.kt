/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.other

import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.ccbluex.liquidbounce.value.DoubleValue
import net.minecraft.network.play.client.C03PacketPlayer

/**
 * @author SkidderMC/FDPClient
 * @author Aspw-w/NightX-Client
 */
object Verus : NoFallMode("Verus") {
    private val multi by DoubleValue("XZMulti", 0.6, 0.0..1.0)
    private var spoof = false

    override fun onEnable() {
        spoof = false
    }

    override fun onPacket(event: PacketEvent) {
        if (event.packet is C03PacketPlayer && spoof) {
            event.packet.onGround = true
            spoof = false
        }
    }

    override fun onUpdate() {
        if (mc.thePlayer.fallDistance - mc.thePlayer.motionY > 3) {
            mc.thePlayer.motionY = 0.0
            mc.thePlayer.fallDistance = 0.0f
            mc.thePlayer.motionX *= multi
            mc.thePlayer.motionZ *= multi
            spoof = true
        }
    }
}
