package net.ccbluex.liquidbounce.features.module.modules.combat.velocitymodes.grim

import net.ccbluex.liquidbounce.features.module.modules.combat.Velocity.grimVerticalMode
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitymodes.VelocityMode
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.util.EnumFacing
import net.ccbluex.liquidbounce.features.module.modules.combat.Velocity
import net.minecraft.util.BlockPos

object GrimVertical : VelocityMode("GrimVertical") {
    private var attack = false
    private var motionXZ = 0.0
    private var velocityInput = false
    private var canCancel = false
    private var canSpoof = false

    override fun onEnable() {
        var attack = false
        var motionXZ = 0.0
        var velocityInput = false
        var canCancel = false
        var canSpoof = false
    }
    override fun onUpdate() {
            when (grimVerticalMode.lowercase()) {
                "1.17" -> {
                    if (canSpoof) {
                        sendPacket(
                            C03PacketPlayer.C06PacketPlayerPosLook(
                            mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ,
                            mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, mc.thePlayer.onGround
                        ))
                        sendPacket(C07PacketPlayerDigging(
                            C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                            BlockPos(mc.thePlayer).down(), EnumFacing.DOWN
                        ))
                        canSpoof = false
                    }
                }
                "vertical" -> {
                    if (attack) {
                        val entity = mc.thePlayer.entityId
                        val target = mc.theWorld.getEntityByID(entity)

                        if (Velocity.via) {
                            sendPacket(C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK))
                            if (Velocity.callEvent) sendPacket(C0APacketAnimation())
                        } else {
                            if (Velocity.callEvent) sendPacket(C0APacketAnimation())
                            sendPacket(C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK))
                        }

                        if (Velocity.smartVelo && mc.thePlayer.onGround) {
                            mc.thePlayer.motionX *= motionXZ
                            mc.thePlayer.motionZ *= motionXZ
                        } else {
                            mc.thePlayer.motionX *= 0.07776
                            mc.thePlayer.motionZ *= 0.07776
                        }
                        velocityInput = false
                        attack = false
                    }
                }
            }
    }
}