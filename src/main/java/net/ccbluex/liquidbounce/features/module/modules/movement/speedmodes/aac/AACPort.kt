/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.aac

import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlock
import net.ccbluex.liquidbounce.utils.extensions.toRadians
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.init.Blocks.air
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.util.BlockPos
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author CCBlueX/LiquidBounce
 */
object AACPort : SpeedMode("AACPort", true) {
    private val portLength by FloatValue("PortLength", 1f, 1f..20f)

    override fun onUpdate() {
        val thePlayer = mc.thePlayer ?: return

        if (!isMoving)
            return

        val f = thePlayer.rotationYaw.toRadians()
        var d = 0.2

        while (d <= portLength) {
            val x = thePlayer.posX - sin(f) * d
            val z = thePlayer.posZ + cos(f) * d

            if (thePlayer.posY < thePlayer.posY.toInt() + 0.5 && getBlock(BlockPos(x, thePlayer.posY, z)) != air)
                break
            sendPacket(C04PacketPlayerPosition(x, thePlayer.posY, z, true))
            d += 0.2
        }
    }
}
