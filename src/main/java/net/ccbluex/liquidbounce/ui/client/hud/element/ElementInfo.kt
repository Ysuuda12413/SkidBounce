/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element

/**
 * Element info
 */
annotation class ElementInfo(
    val name: String,
    val single: Boolean = false,
    val force: Boolean = false,
    val disableScale: Boolean = false,
    val priority: Int = 0
)
