/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.ncp

import net.ccbluex.liquidbounce.event.events.MotionEvent
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.MovementUtils.speed
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe
import net.ccbluex.liquidbounce.utils.extensions.jmp
import net.ccbluex.liquidbounce.value.BooleanValue
import net.minecraft.network.play.server.S12PacketEntityVelocity

/**
 * @author SkidBounce/SkidBounce
 * @author ManInMyVan
 */
object UNCPYPort : SpeedMode("UNCPYPort") {
    private val damageBoost by BooleanValue("DamageBoost", true)

    override fun onMotion(event: MotionEvent) {
        mc.thePlayer.jumpMovementFactor = 0.0254f
        mc.timer.timerSpeed = if (mc.thePlayer.motionY < 0.0 && !mc.thePlayer.onGround && isMoving) 1.1675f else 1f

        if (mc.thePlayer.motionY < 0.0 && mc.thePlayer.motionY > -0.1 && isMoving)
            mc.thePlayer.motionY -= 0.16

        if (mc.thePlayer.onGround && isMoving) {
            mc.thePlayer.jmp(0.3993535)
        }
        speed = speed.coerceAtMost(1.75f)
        strafe(speed, true)
    }

    override fun onPacket(event: PacketEvent) {
        if (damageBoost && event.packet is S12PacketEntityVelocity && event.packet.entityID == mc.thePlayer.entityId)
            speed *= 2f
    }
}
