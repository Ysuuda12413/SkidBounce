/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.event.events

import net.ccbluex.liquidbounce.event.Event
import net.minecraft.client.gui.GuiScreen

/**
 * Called when the screen changes
 */
class ScreenEvent(val guiScreen: GuiScreen?) : Event()
