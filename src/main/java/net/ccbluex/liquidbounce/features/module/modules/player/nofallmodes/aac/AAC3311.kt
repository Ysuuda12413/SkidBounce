/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.aac

import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.ccbluex.liquidbounce.utils.MovementUtils.serverOnGround
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPackets
import net.ccbluex.liquidbounce.utils.extensions.stopXZ
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition

/**
 * @author CCBlueX/LiquidBounce
 */
object AAC3311 : NoFallMode("AAC3.3.11") {
    override fun onUpdate() {
        if (mc.thePlayer.fallDistance > 2) {
            mc.thePlayer.stopXZ()

            sendPackets(
                C04PacketPlayerPosition(
                    mc.thePlayer.posX,
                    mc.thePlayer.posY - 10E-4,
                    mc.thePlayer.posZ,
                    serverOnGround
                ),
                C03PacketPlayer(true)
            )
        }
    }
}
