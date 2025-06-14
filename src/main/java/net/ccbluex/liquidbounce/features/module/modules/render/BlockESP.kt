/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.events.Render3DEvent
import net.ccbluex.liquidbounce.event.events.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlockName
import net.ccbluex.liquidbounce.utils.block.BlockUtils.searchBlocks
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.render.RenderUtils.draw2D
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBlockBox
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.value.BlockValue
import net.ccbluex.liquidbounce.value.BooleanValue
import net.ccbluex.liquidbounce.value.IntValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.block.Block
import net.minecraft.init.Blocks.air
import net.minecraft.util.BlockPos
import java.awt.Color

object BlockESP : Module("BlockESP", Category.RENDER) {
    private val mode by ListValue("Mode", arrayOf("Box", "2D"), "Box")
    private val block by BlockValue("Block", 168)
    private val radius by IntValue("Radius", 40, 5..120)
    private val blockLimit by IntValue("BlockLimit", 256, 0..2056)

    private val colorRainbow by BooleanValue("Rainbow", false)
    private val colorRed by IntValue("R", 255, 0..255) { !colorRainbow }
    private val colorGreen by IntValue("G", 179, 0..255) { !colorRainbow }
    private val colorBlue by IntValue("B", 72, 0..255) { !colorRainbow }

    private val searchTimer = MSTimer()
    private val posList = mutableListOf<BlockPos>()
    private var thread: Thread? = null

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (searchTimer.hasTimePassed(1000) && (thread?.isAlive != true)) {
            val radius = radius
            val selectedBlock = Block.getBlockById(block)
            val blockLimit = blockLimit

            if (selectedBlock == null || selectedBlock == air)
                return

            thread = Thread({
                                val blocks = searchBlocks(radius, setOf(selectedBlock), blockLimit)
                                searchTimer.reset()

                                synchronized(posList) {
                                    posList.clear()
                                    posList += blocks.keys
                                }
                            }, "BlockESP-BlockFinder")

            thread!!.start()
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        synchronized(posList) {
            val color = if (colorRainbow) rainbow() else Color(colorRed, colorGreen, colorBlue)
            for (blockPos in posList) {
                when (mode.lowercase()) {
                    "box" -> drawBlockBox(blockPos, color, true)
                    "2d" -> draw2D(blockPos, color.rgb, Color.BLACK.rgb)
                }
            }
        }
    }

    override val tag
        get() = getBlockName(block)
}
