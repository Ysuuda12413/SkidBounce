/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.events.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.misc.RandomUtils.nextInt
import net.ccbluex.liquidbounce.utils.timing.TimeUtils.randomClickDelay
import net.ccbluex.liquidbounce.value.BooleanValue
import net.ccbluex.liquidbounce.value.IntValue
import net.minecraft.client.settings.KeyBinding

object TriggerBot : Module("TriggerBot", Category.COMBAT) {

    private val simulateDoubleClicking by BooleanValue("SimulateDoubleClicking", false)

    private val maxCPSValue : IntValue =
        object : IntValue("MaxCPS", 8, 1..20) {
            override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtLeast(minCPS)

            override fun onChanged(oldValue: Int, newValue: Int) {
                delay = randomClickDelay(minCPS, get())
            }
        }
    private val maxCPS by maxCPSValue

    private val minCPS: Int by object : IntValue("MinCPS", 5, 1..20) {
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtMost(maxCPS)

        override fun onChanged(oldValue: Int, newValue: Int) {
            delay = randomClickDelay(get(), maxCPS)
        }

        override fun isSupported() = !maxCPSValue.isMinimal
    }

    private var delay = randomClickDelay(minCPS, maxCPS)
    private var lastSwing = 0L

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val objectMouseOver = mc.objectMouseOver
        val doubleClick = if (simulateDoubleClicking) nextInt(-1, 1) else 0

        if (objectMouseOver != null && System.currentTimeMillis() - lastSwing >= delay &&
            isSelected(objectMouseOver.entityHit, true)
        ) repeat(1 + doubleClick) {
            KeyBinding.onTick(mc.gameSettings.keyBindAttack.keyCode) // Minecraft Click handling

            lastSwing = System.currentTimeMillis()
            delay = randomClickDelay(minCPS, maxCPS)
        }
    }
}
