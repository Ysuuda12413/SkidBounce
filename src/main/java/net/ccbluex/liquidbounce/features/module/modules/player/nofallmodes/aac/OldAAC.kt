/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.aac

import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.minecraft.network.play.client.C03PacketPlayer

/**
 * @author CCBlueX/LiquidBounce
 */
object OldAAC : NoFallMode("OldAAC") {

    private var currentState = 0

    override fun onUpdate() {
        if (mc.thePlayer.fallDistance > 2f) {
            sendPacket(C03PacketPlayer(true))
            currentState = 2
        } else if (currentState == 2 && mc.thePlayer.fallDistance < 2) {
            mc.thePlayer.motionY = 0.1
            currentState = 3
            return
        }

        when (currentState) {
            3 -> {
                mc.thePlayer.motionY = 0.1
                currentState = 4
            }

            4 -> {
                mc.thePlayer.motionY = 0.1
                currentState = 5
            }

            5 -> {
                mc.thePlayer.motionY = 0.1
                currentState = 1
            }
        }
    }
}
