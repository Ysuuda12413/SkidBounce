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
import net.ccbluex.liquidbounce.features.module.modules.exploit.Phase
import net.ccbluex.liquidbounce.utils.MovementUtils.direction
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.extensions.fakeJump
import net.ccbluex.liquidbounce.utils.extensions.jmp
import net.ccbluex.liquidbounce.utils.extensions.resetSpeed
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin

object Step : Module("Step", Category.MOVEMENT, gameDetecting = false) {

    /**
     * OPTIONS
     */

    private val mode by ListValue(
        "Mode",
        arrayOf(
            "Vanilla",
            "Jump",
            "NCP",
            "MotionNCP",
            "OldNCP",
            "AAC",
            "LAAC",
            "AAC3.3.4",
            "Spartan",
            "Rewinside",
            "Verus",
        ).sortedArray(), "Vanilla"
    )

    private val height by FloatValue("Height", 1F, 0.6F..10F) { mode !in arrayOf("Jump", "MotionNCP", "LAAC", "AAC3.3.4") }
    private val jumpHeight by FloatValue("JumpHeight", 0.42F, 0.37F..0.42F) { mode == "Jump" }

    private val delay by IntValue("Delay", 0, 0..500)

    /**
     * VALUES
     */

    private var wasTimer = false
    private var isStep = false
    private var stepX = 0.0
    private var stepY = 0.0
    private var stepZ = 0.0

    private var ncpNextStep = 0
    private var spartanSwitch = false
    private var isAACStep = false

    private val timer = MSTimer()

    override fun onDisable() {
        val thePlayer = mc.thePlayer ?: return

        // Change step height back to default (0.6 is default)
        thePlayer.stepHeight = 0.6F
    }

    @Suppress("UNUSED_PARAMETER")
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (wasTimer) {
            mc.timer.resetSpeed()
            wasTimer = false
        }

        val mode = mode
        val thePlayer = mc.thePlayer ?: return

        // Motion steps
        when (mode) {
            "Jump" ->
                if (thePlayer.isCollidedHorizontally && thePlayer.onGround && !mc.gameSettings.keyBindJump.isKeyDown) {
                    mc.thePlayer.fakeJump()
                    thePlayer.motionY = jumpHeight.toDouble()
                }

            "LAAC" ->
                if (thePlayer.isCollidedHorizontally && !thePlayer.isOnLadder && !thePlayer.isInWater && !thePlayer.isInLava && !thePlayer.isInWeb) {
                    if (thePlayer.onGround && timer.hasTimePassed(delay)) {
                        isStep = true

                        mc.thePlayer.fakeJump()
                        thePlayer.motionY += 0.620000001490116

                        val yaw = direction
                        thePlayer.motionX -= sin(yaw) * 0.2
                        thePlayer.motionZ += cos(yaw) * 0.2
                        timer.reset()
                    }

                    thePlayer.onGround = true
                } else isStep = false

            "AAC3.3.4" ->
                if (thePlayer.isCollidedHorizontally && isMoving) {
                    if (thePlayer.onGround && couldStep()) {
                        thePlayer.motionX *= 1.26
                        thePlayer.motionZ *= 1.26
                        thePlayer.jmp()
                        isAACStep = true
                    }

                    if (isAACStep) {
                        thePlayer.motionY -= 0.015

                        if (!thePlayer.isUsingItem && thePlayer.movementInput.moveStrafe == 0F)
                            thePlayer.jumpMovementFactor = 0.3F
                    }
                } else isAACStep = false
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        val thePlayer = mc.thePlayer ?: return

        if (mode != "MotionNCP" || !thePlayer.isCollidedHorizontally || mc.gameSettings.keyBindJump.isKeyDown)
            return

        // Motion steps
        when {
            thePlayer.onGround && couldStep() -> {
                mc.thePlayer.fakeJump()
                thePlayer.motionY = 0.0
                event.y = 0.41999998688698
                ncpNextStep = 1
            }

            ncpNextStep == 1 -> {
                event.y = 0.7531999805212 - 0.41999998688698
                ncpNextStep = 2
            }

            ncpNextStep == 2 -> {
                val yaw = direction

                event.y = 1.001335979112147 - 0.7531999805212
                event.x = -sin(yaw) * 0.7
                event.z = cos(yaw) * 0.7

                ncpNextStep = 0
            }
        }
    }

    @EventTarget
    fun onStep(event: StepEvent) {
        val thePlayer = mc.thePlayer ?: return

        // Phase should disable step
        if (Phase.handleEvents()) {
            event.stepHeight = 0F
            return
        }

        // Some fly modes should disable step
        if (Fly.handleEvents() && Fly.mode in arrayOf(
                "Hypixel",
                "OtherHypixel",
                "LatestHypixel",
                "Rewinside",
                "Mineplex"
            )
            && thePlayer.inventory.getCurrentItem() == null
        ) {
            event.stepHeight = 0F
            return
        }

        val mode = mode

        // Set step to default in some cases
        if (!thePlayer.onGround || !timer.hasTimePassed(delay) ||
            mode in arrayOf("Jump", "MotionNCP", "LAAC", "AAC3.3.4")
        ) {
            thePlayer.stepHeight = 0.6F
            event.stepHeight = 0.6F
            return
        }

        // Set step height
        val height = height
        thePlayer.stepHeight = height
        event.stepHeight = height

        // Detect possible step
        if (event.stepHeight > 0.6F) {
            isStep = true
            stepX = thePlayer.posX
            stepY = thePlayer.posY
            stepZ = thePlayer.posZ
        }
    }

    @EventTarget(ignoreCondition = true)
    fun onStepConfirm(event: StepConfirmEvent) {
        val thePlayer = mc.thePlayer

        if (thePlayer == null || !isStep) // Check if step
            return

        if (thePlayer.entityBoundingBox.minY - stepY > 0.6) { // Check if full block step

            when (mode) {
                "NCP", "AAC" -> {
                    mc.thePlayer.fakeJump()

                    // Half legit step (1 packet missing) [COULD TRIGGER TOO MANY PACKETS]
                    send(0.41999998688698, false)
                    send(0.7531999805212, false)
                    timer.reset()
                }

                "Spartan" -> {
                    mc.thePlayer.fakeJump()

                    if (spartanSwitch) {
                        // Vanilla step (3 packets) [COULD TRIGGER TOO MANY PACKETS]
                        send(0.41999998688698, false)
                        send(0.7531999805212, false)
                        send(1.001335979112147, false)
                    } else // Force step
                        send(0.6, false)

                    // Spartan allows one unlegit step so just swap between legit and unlegit
                    spartanSwitch = !spartanSwitch

                    // Reset timer
                    timer.reset()
                }

                "Rewinside" -> {
                    mc.thePlayer.fakeJump()

                    // Vanilla step (3 packets) [COULD TRIGGER TOO MANY PACKETS]
                    send(0.41999998688698, false)
                    send(0.7531999805212, false)
                    send(1.001335979112147, false)

                    // Reset timer
                    timer.reset()
                }

                "Verus" -> {
                    val stepHeight = mc.thePlayer.entityBoundingBox.minY - stepY

                    mc.timer.timerSpeed = 1 / ceil(stepHeight * 2).toFloat()
                    mc.thePlayer.fakeJump()
                    wasTimer = true

                    var height = 0.0
                    repeat ((ceil(stepHeight * 2) - 1).toInt()) {
                        height += 0.5
                        send(height, true)
                    }
                }
            }
        }

        isStep = false
        stepX = 0.0
        stepY = 0.0
        stepZ = 0.0
    }

    @EventTarget(ignoreCondition = true)
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer && isStep && mode == "OldNCP") {
            packet.y += 0.07
            isStep = false
        }
    }

    private fun couldStep(): Boolean {
        val yaw = direction
        val x = -sin(yaw) * 0.4
        val z = cos(yaw) * 0.4

        return mc.theWorld.getCollisionBoxes(mc.thePlayer.entityBoundingBox.offset(x, 1.001335979112147, z))
            .isEmpty()
    }

    override val tag
        get() = mode

    private fun send(y: Double, ground: Boolean) {
        sendPacket(C04PacketPlayerPosition(stepX, stepY + y, stepZ, ground))
    }
}
