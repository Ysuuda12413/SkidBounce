/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.events.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.clickgui.ClickGui
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPackets
import net.ccbluex.liquidbounce.utils.extensions.updateKeys
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.canClickInventory
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.noMove
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.noMoveAir
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.noMoveAirValue
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.noMoveGround
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.noMoveGroundValue
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.noMoveValue
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.serverOpenInventory
import net.ccbluex.liquidbounce.value.BooleanValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.GuiIngameMenu
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.client.C16PacketClientStatus
import net.minecraft.network.play.client.C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT

object InventoryMove : Module("InventoryMove", Category.MOVEMENT, gameDetecting = false) {

    private val notInChests by BooleanValue("NotInChests", false)
    private val mode by ListValue("Mode", arrayOf("Normal", "Silent", "Packet"), "Normal")
    private val intave by BooleanValue("Intave", false)

    private val isIntave = (mc.currentScreen is GuiInventory || mc.currentScreen is GuiChest) && intave

    // NoMove values are inserted here
    private val undetectable by BooleanValue("Undetectable", false)

    // If player violates nomove check and inventory is open, close inventory and reopen it when still
    private val silentlyCloseAndReopen by BooleanValue("SilentlyCloseAndReopen", false) { noMove && (noMoveAir || noMoveGround) && mode == "Normal" }

    // Reopen closed inventory just before a click (could flag for clicking too fast after opening inventory)
    private val reopenOnClick by BooleanValue("ReopenOnClick", false) { silentlyCloseAndReopen && noMove && (noMoveAir || noMoveGround) && mode == "Normal" }

    val affectedBindings = arrayOf(
        mc.gameSettings.keyBindForward,
        mc.gameSettings.keyBindBack,
        mc.gameSettings.keyBindRight,
        mc.gameSettings.keyBindLeft,
        mc.gameSettings.keyBindJump,
        mc.gameSettings.keyBindSprint
    )

    private var clicking = false

    val canMove: Boolean
        get() {
            val screen = mc.currentScreen

            if (!handleEvents())
                return screen == null

            // Don't make player move when chat or ESC menu are open
            if (screen is GuiChat || screen is GuiIngameMenu)
                return false

            if (undetectable && (screen != null && screen !is GuiHudDesigner && screen !is ClickGui))
                return false

            if (notInChests && screen is GuiChest)
                return false

            return true
        }

    @EventTarget(priority = 999)
    fun onUpdate(event: UpdateEvent) {
        if (!canMove) return

        val screen = mc.currentScreen

        if (silentlyCloseAndReopen && screen is GuiInventory && noMove && (noMoveAir || noMoveGround) && mode == "Normal") {
            if (canClickInventory(closeWhenViolating = true) && !reopenOnClick)
                serverOpenInventory = true
        }

        mc.gameSettings.updateKeys(*affectedBindings)
    }

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        if (isIntave) {
            mc.gameSettings.keyBindSneak.pressed = true
        }
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        if (isIntave) {
            event.cancelEvent()
        }
    }

    @EventTarget
    fun onClick(event: ClickWindowEvent) {
        if (!canClickInventory()) event.cancelEvent()
        else if (reopenOnClick && silentlyCloseAndReopen && noMove && (noMoveAir || noMoveGround) && mode == "Normal") serverOpenInventory = true
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (mode == "Packet") {
            if (event.packet is C0EPacketClickWindow && event.packet.windowId == 0 && !clicking) {
                clicking = true
                event.cancelEvent()

                sendPackets(
                    C16PacketClientStatus(OPEN_INVENTORY_ACHIEVEMENT),
                    event.packet,
                    C0DPacketCloseWindow(0)
                )

                clicking = false
            }
        }

        if (mode == "Silent" || mode == "Packet") {
            if (event.packet is C0DPacketCloseWindow && event.packet.windowId == 0
                || event.packet is C16PacketClientStatus && event.packet.status == OPEN_INVENTORY_ACHIEVEMENT) {
                if (mode != "Packet" || !clicking) {
                    event.cancelEvent()
                }
            }
        }
    }

    override fun onDisable() {
        mc.gameSettings.updateKeys(*affectedBindings)
    }

    override val tag
        get() = mode

    override val values = super.values.toMutableList().apply {
        addAll(indexOfFirst { it.name == "Undetectable" },
               listOf(
                   noMoveValue,
                   noMoveAirValue,
                   noMoveGroundValue,
               )
        )
    }
}
