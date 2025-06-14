package net.ccbluex.liquidbounce.features.module.modules.combat.velocitymodes.grim
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitymodes.VelocityMode
import net.minecraft.network.play.server.S27PacketExplosion
import kotlin.math.cos
import kotlin.math.sin

object GrimZeroVelocityMode : VelocityMode("GrimZeroDefault")
{
    @EventTarget
    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S12PacketEntityVelocity && packet.entityID == mc.thePlayer.entityId) {
            packet.motionX = 0
            packet.motionY = 0
            packet.motionZ = 0
        }
        if (packet is S27PacketExplosion) {
            packet.field_149152_f = 0f
            packet.field_149153_g = 0f
            packet.field_149159_h = 0f
        }
    }

    fun onMotionUpdate() {
        if (mc.thePlayer != null && mc.theWorld != null) {
            if (mc.gameSettings.keyBindForward.isKeyDown ||
                mc.gameSettings.keyBindBack.isKeyDown ||
                mc.gameSettings.keyBindLeft.isKeyDown ||
                mc.gameSettings.keyBindRight.isKeyDown
            ) {
                val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
                var moveX = 0.0
                var moveZ = 0.0
                if (mc.gameSettings.keyBindForward.isKeyDown) {
                    moveX -= sin(yaw) * 0.08
                    moveZ += cos(yaw) * 0.08
                }
                if (mc.gameSettings.keyBindBack.isKeyDown) {
                    moveX += sin(yaw) * 0.08
                    moveZ -= cos(yaw) * 0.08
                }
                if (mc.gameSettings.keyBindLeft.isKeyDown) {
                    moveX -= cos(yaw) * 0.08
                    moveZ -= sin(yaw) * 0.08
                }
                if (mc.gameSettings.keyBindRight.isKeyDown) {
                    moveX += cos(yaw) * 0.08
                    moveZ += sin(yaw) * 0.08
                }
                mc.thePlayer.motionX = moveX
                mc.thePlayer.motionZ = moveZ
            }
        }
    }
}