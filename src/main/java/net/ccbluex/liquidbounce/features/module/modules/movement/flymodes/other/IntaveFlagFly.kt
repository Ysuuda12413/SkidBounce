package net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.other

import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.FlyMode
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.event.events.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Fly.boostTimes2
import net.ccbluex.liquidbounce.features.module.modules.movement.Fly.strafeSpeed
import net.ccbluex.liquidbounce.features.module.modules.movement.Fly.boostDuration
import net.ccbluex.liquidbounce.features.module.modules.movement.Fly.multiTickBoost
import net.ccbluex.liquidbounce.features.module.modules.movement.Fly.velocityMultiplier
import net.ccbluex.liquidbounce.utils.ClientUtils.displayClientMessage
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S27PacketExplosion

object IntaveFlagFly : FlyMode("IntaveFlagFly") {
    private var lastKnockbackX = 0.0
    private var lastKnockbackY = 0.0
    private var lastKnockbackZ = 0.0
    private var hasKnockback = false
    private var tickKnockbackLeft = 0

    override fun onEnable() {
        displayClientMessage("IntaveFlagExtremeFly enabled!")
        tickKnockbackLeft = 0
        hasKnockback = false
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if ((packet is S12PacketEntityVelocity || packet is S27PacketExplosion) && mc.thePlayer != null && (packet as? S12PacketEntityVelocity)?.entityID == mc.thePlayer.entityId) {
            event.cancelEvent()
            lastKnockbackX = when (packet) {
                is S12PacketEntityVelocity -> packet.motionX / 8000.0 * velocityMultiplier
                is S27PacketExplosion -> packet.func_149149_c().toDouble() * velocityMultiplier
                else -> 0.0
            }
            lastKnockbackY = when (packet) {
                is S12PacketEntityVelocity -> packet.motionY / 8000.0 * velocityMultiplier * 0.98
                is S27PacketExplosion -> packet.func_149144_d().toDouble() * velocityMultiplier * 0.98
                else -> 0.0
            }
            lastKnockbackZ = when (packet) {
                is S12PacketEntityVelocity -> packet.motionZ / 8000.0 * velocityMultiplier
                is S27PacketExplosion -> packet.func_149147_e().toDouble() * velocityMultiplier
                else -> 0.0
            }
            hasKnockback = true
            tickKnockbackLeft = boostDuration
        }
    }

    override fun onUpdate() {
        val player = mc.thePlayer ?: return
        if (hasKnockback) {
            if (multiTickBoost) {
                if (tickKnockbackLeft > 0) {
                    repeat(boostTimes2) {
                        player.motionX += lastKnockbackX
                        player.motionY += lastKnockbackY
                        player.motionZ += lastKnockbackZ
                    }
                    tickKnockbackLeft--
                } else {
                    hasKnockback = false
                }
            } else {
                repeat(boostTimes2) {
                    player.motionX += lastKnockbackX
                    player.motionY += lastKnockbackY
                    player.motionZ += lastKnockbackZ
                }
                hasKnockback = false
            }
        }
        strafe(strafeSpeed)
    }

    override fun onDisable() {
        val player = mc.thePlayer ?: return
        player.motionX += 25
        player.motionZ += 25
    }
}