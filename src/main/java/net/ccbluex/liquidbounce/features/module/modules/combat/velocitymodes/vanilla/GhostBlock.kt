/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat.velocitymodes.vanilla

import net.ccbluex.liquidbounce.event.events.BlockBBEvent
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.Velocity
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitymodes.VelocityMode
import net.minecraft.block.BlockAir
import net.minecraft.util.AxisAlignedBB

/**
 * @author CCBlueX/LiquidBounce
 */
object GhostBlock : VelocityMode("GhostBlock") {
    private var hasVelocity = false
    override fun onEnable() {
        hasVelocity = false
    }

    override fun onVelocityPacket(event: PacketEvent) {
        hasVelocity = true
    }

    override fun onBlockBB(event: BlockBBEvent) {
        if (!hasVelocity)
            return
        if (mc.thePlayer.hurtTime in Velocity.minHurtTime.get()..Velocity.maxHurtTime.get()) {
            // Check if there is air exactly 1 level above the player's Y position
            if (event.block is BlockAir && event.y == mc.thePlayer.posY.toInt() + 1) {
                event.boundingBox = AxisAlignedBB(
                    event.x.toDouble(),
                    event.y.toDouble(),
                    event.z.toDouble(),
                    event.x + 1.0,
                    event.y + 1.0,
                    event.z + 1.0
                )
            }
        } else if (mc.thePlayer.hurtTime == 0)
            hasVelocity = false
    }
}