/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat.velocitymodes.aac

import net.ccbluex.liquidbounce.event.events.JumpEvent
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitymodes.VelocityMode

/**
 * @author CCBlueX/LiquidBounce
 */
object AACZero : VelocityMode("AACZero") {
    private var hasVelocity = false

    override fun onEnable() {
        hasVelocity = false
    }

    override fun onVelocityPacket(event: PacketEvent) {
        if (!mc.thePlayer.isInWater && !mc.thePlayer.isInLava && !mc.thePlayer.isInWeb)
            hasVelocity = true
    }

    override fun onUpdate() {
        if (mc.thePlayer.isInWater || mc.thePlayer.isInLava || mc.thePlayer.isInWeb) return
        if (mc.thePlayer.hurtTime > 0) {
            if (!hasVelocity || mc.thePlayer.onGround || mc.thePlayer.fallDistance > 2f) return
            mc.thePlayer.motionY -= 1.0
            mc.thePlayer.isAirBorne = true
            mc.thePlayer.onGround = true
        } else hasVelocity = false
    }

    override fun onJump(event: JumpEvent) {
        if (mc.thePlayer.hurtTime > 0)
            event.cancelEvent()
    }
}
