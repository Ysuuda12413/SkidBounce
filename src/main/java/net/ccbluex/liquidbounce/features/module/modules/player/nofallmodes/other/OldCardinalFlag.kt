/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.other

import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer

/**
 * @author SkidBounce/SkidBounce
 * @author ManInMyVan
 */
object OldCardinalFlag : NoFallMode("OldCardinalFlag") {
    private var falling = false

    override fun onEnable() {
        falling = false
    }

    override fun onUpdate() {
        if (mc.thePlayer.fallDistance > 3)
            falling = true
    }

    override fun onPacket(event: PacketEvent) {
        if (event.packet is C03PacketPlayer && falling && event.packet.onGround && mc.thePlayer.onGround) {
            falling = false
            mc.thePlayer.onGround = false
            event.packet.y = Double.POSITIVE_INFINITY
            event.packet.onGround = false
        }
    }
}
