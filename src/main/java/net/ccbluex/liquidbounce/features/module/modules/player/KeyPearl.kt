/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.events.TickEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.ClientUtils.displayClientMessage
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.serverSlot
import net.ccbluex.liquidbounce.value.BooleanValue
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.TextValue
import net.minecraft.init.Items
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.world.WorldSettings
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

object KeyPearl : Module("KeyPearl", Category.PLAYER, gameDetecting = false) {

    private val delayedSlotSwitch by BooleanValue("DelayedSlotSwitch", true)
    private val mouse by BooleanValue("Mouse", false, subjective = true)
    private val mouseButtonValue = ListValue(
        "MouseButton",
        arrayOf("Left", "Right", "Middle", "MouseButton4", "MouseButton5"), "Middle", subjective = true
    ) { mouse }

    private val keyName by TextValue("KeyName", "X", subjective = true) { !mouse }

    private val noEnderPearlsMessage by BooleanValue("NoEnderPearlsMessage", true, subjective = true)

    private var wasMouseDown = false
    private var wasKeyDown = false
    private var hasThrown = false

    private fun throwEnderPearl() {
        val pearlInHotbar = InventoryUtils.findItem(36, 44, Items.ender_pearl) ?: run {
            if (noEnderPearlsMessage) {
                displayClientMessage("§6§lWarning: §aThere are no ender pearls in your hotbar.")
            }
            return
        }

        val click = C08PacketPlayerBlockPlacement(mc.thePlayer.openContainer.getSlot(pearlInHotbar).stack)

        serverSlot = pearlInHotbar - 36
        sendPacket(click)

        if (delayedSlotSwitch) hasThrown = true
        else serverSlot = mc.thePlayer.inventory.currentItem
    }

    @EventTarget
    fun onTick(event: TickEvent) {
        if (hasThrown) {
            serverSlot = mc.thePlayer.inventory.currentItem
            hasThrown = false
        }

        if (mc.currentScreen != null || mc.playerController.currentGameType == WorldSettings.GameType.SPECTATOR
            || mc.playerController.currentGameType == WorldSettings.GameType.CREATIVE
        ) return

        val isMouseDown = Mouse.isButtonDown(mouseButtonValue.values.indexOf(mouseButtonValue.get()))
        val isKeyDown = Keyboard.isKeyDown(Keyboard.getKeyIndex(keyName.uppercase()))

        if (mouse && !wasMouseDown && isMouseDown) {
            throwEnderPearl()
        } else if (!mouse && !wasKeyDown && isKeyDown) {
            throwEnderPearl()
        }

        wasMouseDown = isMouseDown
        wasKeyDown = isKeyDown
    }

    override fun onEnable() {
        hasThrown = false
    }
}
