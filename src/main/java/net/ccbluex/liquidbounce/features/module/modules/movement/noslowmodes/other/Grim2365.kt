/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.other

import net.ccbluex.liquidbounce.event.EventState.PRE
import net.ccbluex.liquidbounce.event.events.MotionEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.NoSlowMode
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.extensions.canUse
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.serverSlot
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement

class Grim2365 : NoSlowMode("Grim2.3.65") {
    override var canNoSlow = true

    override fun onMotion(event: MotionEvent) {
        if (event.eventState != PRE) return
        for (i in 0..8) {
            if (mc.thePlayer.inventory?.mainInventory?.get(i).canUse) continue

            serverSlot = i
            sendPacket(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
            serverSlot = mc.thePlayer.inventory.currentItem

            canNoSlow = true
            return
        }
        canNoSlow = false
    }
}
