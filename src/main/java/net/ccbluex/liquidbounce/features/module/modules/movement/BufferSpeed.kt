/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.event.events.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlock
import net.ccbluex.liquidbounce.utils.extensions.jmp
import net.ccbluex.liquidbounce.value.BooleanValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.block.BlockSlab
import net.minecraft.block.BlockSlime
import net.minecraft.block.BlockStairs
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.BlockPos

object BufferSpeed : Module("BufferSpeed", Category.MOVEMENT) {
    private val speedLimit by BooleanValue("SpeedLimit", true)
    private val maxSpeed by FloatValue("MaxSpeed", 2f, 1f..5f) { speedLimit }

    private val buffer by BooleanValue("Buffer", true)

    private val stairs by BooleanValue("Stairs", true)
    private val stairsMode by ListValue("StairsMode", arrayOf("Old", "New"), "New") { stairs }
    private val stairsBoost by FloatValue("StairsBoost", 1.87f, 1f..2f) { stairs && stairsMode == "Old" }

    private val slabs by BooleanValue("Slabs", true)
    private val slabsMode by ListValue("SlabsMode", arrayOf("Old", "New"), "New") { slabs }
    private val slabsBoost by FloatValue("SlabsBoost", 1.87f, 1f..2f) { slabs && slabsMode == "Old" }

    private val ice by BooleanValue("Ice", false)
    private val iceBoost by FloatValue("IceBoost", 1.342f, 1f..2f) { ice }

    private val snow by BooleanValue("Snow", true)
    private val snowBoost by FloatValue("SnowBoost", 1.87f, 1f..2f) { snow }
    private val snowPort by BooleanValue("SnowPort", true) { snow }

    private val wall by BooleanValue("Wall", true)
    private val wallMode by ListValue("WallMode", arrayOf("Old", "New"), "New") { wall }
    private val wallBoost by FloatValue("WallBoost", 1.87f, 1f..2f) { wall && wallMode == "Old" }

    private val headBlock by BooleanValue("HeadBlock", true)
    private val headBlockBoost by FloatValue("HeadBlockBoost", 1.87f, 1f..2f) { headBlock }

    private val slime by BooleanValue("Slime", true)
    private val airStrafe by BooleanValue("AirStrafe", false)
    private val noHurt by BooleanValue("NoHurt", true)

    private var speed = 0.0
    private var down = false
    private var forceDown = false
    private var fastHop = false
    private var hadFastHop = false
    private var legitHop = false

    @Suppress("UNUSED_PARAMETER")
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val thePlayer = mc.thePlayer ?: return

        if (Speed.handleEvents() || noHurt && thePlayer.hurtTime > 0) {
            reset()
            return
        }

        val blockPos = BlockPos(thePlayer)

        if (forceDown || down && thePlayer.motionY == 0.0) {
            thePlayer.motionY = -1.0
            down = false
            forceDown = false
        }

        if (fastHop) {
            thePlayer.speedInAir = 0.0211f
            hadFastHop = true
        } else if (hadFastHop) {
            thePlayer.speedInAir = 0.02f
            hadFastHop = false
        }

        if (!isMoving || thePlayer.isSneaking || thePlayer.isInWater || mc.gameSettings.keyBindJump.isKeyDown) {
            reset()
            return
        }

        if (thePlayer.onGround) {
            fastHop = false

            if (slime && (getBlock(blockPos.down()) is BlockSlime || getBlock(blockPos) is BlockSlime)) {
                thePlayer.jmp()
                // TODO: motX & motZ will be different?
                thePlayer.motionX = thePlayer.motionY * 1.132
                thePlayer.motionY = 0.08
                thePlayer.motionZ = thePlayer.motionY * 1.132

                down = true
                return
            }
            if (slabs && getBlock(blockPos) is BlockSlab) {
                when (slabsMode.lowercase()) {
                    "old" -> {
                        boost(slabsBoost)
                        return
                    }

                    "new" -> {
                        fastHop = true
                        if (legitHop) {
                            thePlayer.jmp()
                            thePlayer.onGround = false
                            legitHop = false
                            return
                        }
                        thePlayer.onGround = false

                        strafe(0.375f)

                        thePlayer.jmp(0.41, ignoreJumpBoost = true)
                        return
                    }
                }
            }
            if (stairs && (getBlock(blockPos.down()) is BlockStairs || getBlock(blockPos) is BlockStairs)) {
                when (stairsMode.lowercase()) {
                    "old" -> {
                        boost(stairsBoost)
                        return
                    }

                    "new" -> {
                        fastHop = true

                        if (legitHop) {
                            thePlayer.jmp()
                            thePlayer.onGround = false
                            legitHop = false
                            return
                        }

                        thePlayer.onGround = false
                        strafe(0.375f)
                        thePlayer.jmp(0.41, ignoreJumpBoost = true)
                        return
                    }
                }
            }
            legitHop = true

            if (headBlock && getBlock(blockPos.up(2)) == Blocks.air) {
                boost(headBlockBoost)
                return
            }

            if (ice && (getBlock(blockPos.down()) == Blocks.ice || getBlock(blockPos.down()) == Blocks.packed_ice)) {
                boost(iceBoost)
                return
            }

            if (snow && getBlock(blockPos) == Blocks.snow_layer && (snowPort || thePlayer.posY - thePlayer.posY.toInt() >= 0.12500)) {
                if (thePlayer.posY - thePlayer.posY.toInt() >= 0.12500) {
                    boost(snowBoost)
                } else {
                    thePlayer.jmp()
                    forceDown = true
                }
                return
            }

            if (wall) {
                when (wallMode.lowercase()) {
                    "old" -> if (thePlayer.isCollidedVertically && isNearBlock || getBlock(BlockPos(thePlayer).up(2)) != Blocks.air) {
                        boost(wallBoost)
                        return
                    }

                    "new" ->
                        if (isNearBlock && !thePlayer.movementInput.jump) {
                            thePlayer.jmp(0.08, ignoreJumpBoost = true)
                            thePlayer.motionX *= 0.99
                            thePlayer.motionZ *= 0.99
                            down = true
                            return
                        }
                }
            }
            val currentSpeed = speed

            if (speed < currentSpeed)
                speed = currentSpeed

            if (buffer && speed > 0.2) {
                speed /= 1.0199999809265137
                strafe()
            }
        } else {
            speed = 0.0

            if (airStrafe)
                strafe()
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S08PacketPlayerPosLook)
            speed = 0.0
    }

    override fun onEnable() = reset()

    override fun onDisable() = reset()

    private fun reset() {
        val thePlayer = mc.thePlayer ?: return
        legitHop = true
        speed = 0.0

        if (hadFastHop) {
            thePlayer.speedInAir = 0.02f
            hadFastHop = false
        }
    }

    private fun boost(boost: Float) {
        val thePlayer = mc.thePlayer

        thePlayer.motionX *= boost
        thePlayer.motionZ *= boost

        speed = MovementUtils.speed.toDouble()

        if (speedLimit && speed > maxSpeed)
            speed = maxSpeed.toDouble()
    }

    private val isNearBlock: Boolean
        get() {
            val thePlayer = mc.thePlayer
            val theWorld = mc.theWorld
            val blocks = mutableListOf<BlockPos>()
            blocks += BlockPos(thePlayer.posX, thePlayer.posY + 1, thePlayer.posZ - 0.7)
            blocks += BlockPos(thePlayer.posX + 0.7, thePlayer.posY + 1, thePlayer.posZ)
            blocks += BlockPos(thePlayer.posX, thePlayer.posY + 1, thePlayer.posZ + 0.7)
            blocks += BlockPos(thePlayer.posX - 0.7, thePlayer.posY + 1, thePlayer.posZ)
            for (blockPos in blocks) {
                val blockState = theWorld.getBlockState(blockPos)

                val collisionBoundingBox = blockState.block.getCollisionBoundingBox(theWorld, blockPos, blockState)

                if ((collisionBoundingBox == null || collisionBoundingBox.maxX ==
                            collisionBoundingBox.minY + 1) &&
                    !blockState.block.isTranslucent && blockState.block == Blocks.water &&
                    blockState.block !is BlockSlab || blockState.block == Blocks.barrier
                ) return true
            }
            return false
        }
}
