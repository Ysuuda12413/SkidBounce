/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.aac

import net.ccbluex.liquidbounce.event.events.JumpEvent
import net.ccbluex.liquidbounce.event.events.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode

/**
 * @author CCBlueX/LiquidBounce
 */
object LAAC : NoFallMode("LAAC") {
    private var jumped = false

    override fun onUpdate() {
        if (mc.thePlayer.onGround) jumped = false

        if (mc.thePlayer.motionY > 0) jumped = true

        if (!jumped && mc.thePlayer.onGround && !mc.thePlayer.isOnLadder && !mc.thePlayer.isInWater && !mc.thePlayer.isInWeb)
            mc.thePlayer.motionY = -6.0
    }

    override fun onJump(event: JumpEvent) {
        jumped = true
    }

    override fun onMove(event: MoveEvent) {
        if (!jumped && !mc.thePlayer.onGround && !mc.thePlayer.isOnLadder && !mc.thePlayer.isInWater && !mc.thePlayer.isInWeb && mc.thePlayer.motionY < 0.0)
            event.zeroXZ()
    }
}
