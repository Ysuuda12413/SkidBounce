/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventState.RECEIVE
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.events.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.player.Blink
import net.ccbluex.liquidbounce.features.module.modules.targets.AntiBot.isBot
import net.ccbluex.liquidbounce.features.module.modules.targets.Friends
import net.ccbluex.liquidbounce.features.module.modules.targets.Teams
import net.ccbluex.liquidbounce.injection.implementations.IMixinEntity
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.blink.FakePlayer
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.misc.StringUtils.contains
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBacktrackBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils.glColor
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.value.BooleanValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.Packet
import net.minecraft.network.handshake.client.C00Handshake
import net.minecraft.network.play.server.*
import net.minecraft.network.status.client.C00PacketServerQuery
import net.minecraft.network.status.server.S01PacketPong
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.util.*
import java.util.concurrent.*

object Backtrack : Module("Backtrack", Category.COMBAT) {
    private val delay by object : IntValue("Delay", 80, 0..700) {
        override fun onChange(oldValue: Int, newValue: Int): Int {
            if (mode == "Modern")
                clearPackets()

            return newValue
        }
    }

    val mode by object : ListValue("Mode", arrayOf("Legacy", "Modern"), "Modern") {
        override fun onChanged(oldValue: String, newValue: String) {
            clearPackets()
            backtrackedPlayer.clear()
        }
    }

    // Legacy
    private val legacyPos by ListValue(
        "CachingMode",
        arrayOf("ClientPos", "ServerPos"),
        "ClientPos"
    ) { mode == "Legacy" }

    // Modern
    private val style by ListValue("Style", arrayOf("Pulse", "Smooth"), "Smooth") { mode == "Modern" }

    private val maxDistanceValue: FloatValue = object : FloatValue("MaxDistance", 3.0f, 0.0f..6.0f) {
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtLeast(minDistance)
        override fun isSupported() = mode == "Modern"
    }
    private val maxDistance by maxDistanceValue
    private val minDistance by object : FloatValue("MinDistance", 2.0f, 0.0f..6.0f) {
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceIn(minimum, maxDistance)
        override fun isSupported() = mode == "Modern"
    }
    private val smart by BooleanValue("Smart", true) { mode == "Modern" }

    // ESP
    val espMode by ListValue("ESP-Mode", arrayOf("None", "Box", "Player"), "Box", subjective = true) { mode == "Modern" }
    private val rainbow by BooleanValue("Rainbow", true, subjective = true) { mode == "Modern" && espMode == "Box" }
    private val red by IntValue("R", 0, 0..255, subjective = true) { !rainbow && mode == "Modern" && espMode == "Box" }
    private val green by IntValue("G", 255, 0..255, subjective = true) { !rainbow && mode == "Modern" && espMode == "Box" }
    private val blue by IntValue("B", 0, 0..255, subjective = true) { !rainbow && mode == "Modern" && espMode == "Box" }

    private val packetQueue = LinkedHashMap<Packet<*>, Long>()
    private val positions = mutableListOf<Pair<Vec3, Long>>()

    var target: Entity? = null

    private var globalTimer = MSTimer()

    var shouldRender = true

    private var ignoreWholeTick = false

    // Legacy
    private val maximumCachedPositions by IntValue("MaxCachedPositions", 10, 1..20) { mode == "Legacy" }

    private val backtrackedPlayer = ConcurrentHashMap<UUID, MutableList<BacktrackData>>()

    private val nonDelayedSoundSubstrings = arrayOf("game.player.hurt", "game.player.die")

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (Blink.blinkingReceive())
            return

        if (event.isCancelled)
            return

        when (mode.lowercase()) {
            "legacy" -> {
                when (packet) {
                    // Check if packet is a spawn player packet
                    is S0CPacketSpawnPlayer -> {
                        // Insert first backtrack data
                        addBacktrackData(
                            packet.player,
                            packet.realX,
                            packet.realY,
                            packet.realZ,
                            System.currentTimeMillis()
                        )
                    }

                    is S14PacketEntity -> {
                        if (legacyPos == "ServerPos") {
                            val entity = mc.theWorld?.getEntityByID(packet.entityId)
                            val entityMixin = entity as? IMixinEntity
                            if (entityMixin != null) {
                                addBacktrackData(
                                    entity.uniqueID,
                                    entityMixin.trueX,
                                    entityMixin.trueY,
                                    entityMixin.trueZ,
                                    System.currentTimeMillis()
                                )
                            }
                        }
                    }

                    is S18PacketEntityTeleport -> {
                        if (legacyPos == "ServerPos") {
                            val entity = mc.theWorld?.getEntityByID(packet.entityId)
                            val entityMixin = entity as? IMixinEntity
                            if (entityMixin != null) {
                                addBacktrackData(
                                    entity.uniqueID,
                                    entityMixin.trueX,
                                    entityMixin.trueY,
                                    entityMixin.trueZ,
                                    System.currentTimeMillis()
                                )
                            }
                        }
                    }
                }
            }

            "modern" -> {
                // Prevent cancelling packets when not needed
                if (packetQueue.isEmpty() && !shouldBacktrack())
                    return

                when (packet) {
                    // Ignore chat & pong packets
                    is S02PacketChat, is S01PacketPong -> return

                    // Ignore server related packets
                    is C00Handshake, is C00PacketServerQuery, is S21PacketChunkData, is S26PacketMapChunkBulk -> return

                    // Flush on teleport or disconnect
                    is S08PacketPlayerPosLook, is S40PacketDisconnect -> {
                        clearPackets()
                        return
                    }

                    is S29PacketSoundEffect ->
                        if (nonDelayedSoundSubstrings in packet.soundName)
                            return

                    // Flush on own death
                    is S06PacketUpdateHealth ->
                        if (packet.health <= 0) {
                            clearPackets()
                            return
                        }

                    is S13PacketDestroyEntities ->
                        if (target != null && target!!.entityId in packet.entityIDs) {
                            clearPackets()
                            reset()
                            return
                        }

                    is S1CPacketEntityMetadata ->
                        if (target?.entityId == packet.entityId) {
                            val metadata = packet.func_149376_c() ?: return

                            metadata.forEach {
                                if (it.dataValueId == 6) {
                                    val objectValue = it.getObject().toString().toDoubleOrNull()
                                    if (objectValue != null && !objectValue.isNaN() && objectValue <= 0.0) {
                                        clearPackets()
                                        reset()
                                        return
                                    }
                                }
                            }

                            return
                        }

                    is S19PacketEntityStatus ->
                        if (packet.entityId == target?.entityId)
                            return
                }

                // Cancel every received packet to avoid possible server synchronization issues from random causes.
                if (event.eventType == RECEIVE) {
                    when (packet) {
                        is S14PacketEntity ->
                            if (packet.entityId == target?.entityId)
                                (target as? IMixinEntity)?.run {
                                    synchronized(positions) {
                                        positions += Pair(Vec3(trueX, trueY, trueZ), System.currentTimeMillis())
                                    }
                                }

                        is S18PacketEntityTeleport ->
                            if (packet.entityId == target?.entityId)
                                (target as? IMixinEntity)?.run {
                                    synchronized(positions) {
                                        positions += Pair(Vec3(trueX, trueY, trueZ), System.currentTimeMillis())
                                    }
                                }
                    }

                    event.cancelEvent()
                    synchronized(packetQueue) {
                        packetQueue[packet] = System.currentTimeMillis()
                    }
                }
            }
        }
    }

    @EventTarget
    fun onGameLoop(event: GameLoopEvent) {
        if (mode == "Legacy") {
            backtrackedPlayer.forEach { (key, backtrackData) ->
                // Remove old data
                backtrackData.removeAll { it.time + delay < System.currentTimeMillis() }

                // Remove player if there is no data left. This prevents memory leaks.
                if (backtrackData.isEmpty())
                    removeBacktrackData(key)
            }
        }

        val target = target as? EntityLivingBase
        val targetMixin = target as? IMixinEntity

        if (mode == "Modern" && targetMixin != null && !Blink.blinkingReceive() && shouldBacktrack() && targetMixin.truePos) {
            val trueDist = mc.thePlayer.getDistance(targetMixin.trueX, targetMixin.trueY, targetMixin.trueZ)
            val dist = mc.thePlayer.getDistance(target.posX, target.posY, target.posZ)

            if (trueDist <= 6f && (!smart || trueDist >= dist) && (style == "Smooth" || !globalTimer.hasTimePassed(delay))) {
                shouldRender = true

                if (mc.thePlayer.getDistanceToEntityBox(target) in minDistance..maxDistance)
                    handlePackets()
                else
                    handlePacketsRange()
            } else {
                clearPackets()
                globalTimer.reset()
            }
        }

        ignoreWholeTick = false
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        if (!isEnemy(event.targetEntity))
            return

        // Clear all packets, start again on enemy change
        if (target != event.targetEntity) {
            clearPackets()
            reset()
        }

        target = event.targetEntity
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        when (mode.lowercase()) {
            "legacy" -> {
                val color = Color.RED

                for (entity in mc.theWorld.loadedEntityList) {
                    if (entity is EntityPlayer) {
                        glPushMatrix()
                        glDisable(GL_TEXTURE_2D)
                        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
                        glEnable(GL_LINE_SMOOTH)
                        glEnable(GL_BLEND)
                        glDisable(GL_DEPTH_TEST)

                        mc.entityRenderer.disableLightmap()

                        glBegin(GL_LINE_STRIP)
                        glColor(color)

                        val renderPosX = mc.renderManager.viewerPosX
                        val renderPosY = mc.renderManager.viewerPosY
                        val renderPosZ = mc.renderManager.viewerPosZ

                        loopThroughBacktrackData(entity) {
                            glVertex3d(entity.posX - renderPosX, entity.posY - renderPosY, entity.posZ - renderPosZ)
                            false
                        }

                        glColor4d(1.0, 1.0, 1.0, 1.0)
                        glEnd()
                        glEnable(GL_DEPTH_TEST)
                        glDisable(GL_LINE_SMOOTH)
                        glDisable(GL_BLEND)
                        glEnable(GL_TEXTURE_2D)
                        glPopMatrix()
                    }
                }
            }

            "modern" -> {
                if (!shouldBacktrack() || packetQueue.isEmpty() || !shouldRender)
                    return

                if (espMode != "Box") return

                val renderManager = mc.renderManager

                target?.run {
                    val targetEntity = target as IMixinEntity

                    if (targetEntity.truePos) {
                        val x =
                            targetEntity.trueX - renderManager.renderPosX
                        val y =
                            targetEntity.trueY - renderManager.renderPosY
                        val z =
                            targetEntity.trueZ - renderManager.renderPosZ

                        val axisAlignedBB = entityBoundingBox.offset(-posX, -posY, -posZ).offset(x, y, z)

                        drawBacktrackBox(
                            AxisAlignedBB.fromBounds(
                                axisAlignedBB.minX,
                                axisAlignedBB.minY,
                                axisAlignedBB.minZ,
                                axisAlignedBB.maxX,
                                axisAlignedBB.maxY,
                                axisAlignedBB.maxZ
                            ), color
                        )
                    }
                }
            }
        }
    }

    @EventTarget
    fun onEntityMove(event: EntityMovementEvent) {
        if (mode == "Legacy" && legacyPos == "ClientPos") {
            val entity = event.movedEntity

            // Check if entity is a player
            if (entity is EntityPlayer) {
                // Add new data
                addBacktrackData(entity.uniqueID, entity.posX, entity.posY, entity.posZ, System.currentTimeMillis())
            }
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        // Clear packets on disconnect only
        if (mode == "Modern" && event.worldClient == null)
            clearPackets(false)
    }

    override fun onEnable() =
        reset()

    override fun onDisable() {
        clearPackets()
        backtrackedPlayer.clear()
    }

    private fun handlePackets() {
        synchronized(packetQueue) {
            packetQueue.entries.removeAll { (packet, timestamp) ->
                if (timestamp <= System.currentTimeMillis() - delay) {
                    PacketUtils.queuedPackets.add(packet)
                    true
                } else false
            }
        }
        synchronized(positions) {
            positions.removeAll { (_, timestamp) -> timestamp < System.currentTimeMillis() - delay }
        }
    }

    private fun handlePacketsRange() {
        val time = getRangeTime()
        if (time == -1L) {
            clearPackets()
            return
        }
        synchronized(packetQueue) {
            packetQueue.entries.removeAll { (packet, timestamp) ->
                if (timestamp <= time) {
                    PacketUtils.queuedPackets.add(packet)
                    true
                } else false
            }
        }
        synchronized(positions) {
            positions.removeAll { (_, timestamp) -> timestamp < time }
        }
    }

    private fun getRangeTime(): Long {
        if (target == null) return 0L
        var time = 0L
        var found = false
        synchronized(positions) {
            for (data in positions) {
                time = data.second
                val targetPos = Vec3(target!!.posX, target!!.posY, target!!.posZ)
                val (dx, dy, dz) = data.first - targetPos
                val targetBox = target!!.hitBox.offset(dx, dy, dz)
                if (mc.thePlayer.getDistanceToBox(targetBox) in minDistance..maxDistance) {
                    found = true
                    break
                }
            }
        }
        return if (found) time else -1L
    }

    private fun clearPackets(handlePackets: Boolean = true) {
        synchronized(packetQueue) {
            if (handlePackets)
                PacketUtils.queuedPackets.addAll(packetQueue.keys)

            packetQueue.clear()
        }
        positions.clear()
        shouldRender = false
        ignoreWholeTick = true
    }

    private fun addBacktrackData(id: UUID, x: Double, y: Double, z: Double, time: Long) {
        // Get backtrack data of player
        val backtrackData = getBacktrackData(id)

        // Check if there is already data of the player
        if (backtrackData != null) {
            // Check if there is already enough data of the player
            if (backtrackData.size >= maximumCachedPositions) {
                // Remove first data
                backtrackData.removeFirst()
            }

            // Insert new data
            backtrackData += BacktrackData(x, y, z, time)
        } else {
            // Create new list
            backtrackedPlayer[id] = mutableListOf(BacktrackData(x, y, z, time))
        }
    }

    private fun getBacktrackData(id: UUID) = backtrackedPlayer[id]

    private fun removeBacktrackData(id: UUID) = backtrackedPlayer.remove(id)

    private fun isEnemy(entity: Entity?): Boolean {
        return when {
            mc.netHandler == null -> false
            entity !is EntityLivingBase || entity == mc.thePlayer -> false
            entity !is EntityPlayer -> true
            entity.isSpectator || isBot(entity) -> false
            entity.isClientFriend && !Friends.handleEvents() -> false
            else -> !Teams.handleEvents() || !Teams.isInYourTeam(entity)
        }
    }

    /**
     * This function will return the nearest tracked range of an entity.
     */
    fun getNearestTrackedDistance(entity: Entity): Double {
        var nearestRange = 0.0

        loopThroughBacktrackData(entity) {
            val range = entity.getDistanceToEntityBox(mc.thePlayer)

            if (range < nearestRange || nearestRange == 0.0) {
                nearestRange = range
            }

            false
        }

        return nearestRange
    }

    /**
     * This function will loop through the backtrack data of an entity.
     */
    fun loopThroughBacktrackData(entity: Entity, action: () -> Boolean) {
        if (!Backtrack.state || entity !is EntityPlayer || mode == "Modern")
            return

        val backtrackDataArray = getBacktrackData(entity.uniqueID) ?: return
        val currPos = entity.currPos
        val prevPos = entity.prevPos

        // This will loop through the backtrack data. We are using reversed() to loop through the data from the newest to the oldest.
        for ((x, y, z, _) in backtrackDataArray.reversed()) {
            entity.setPosAndPrevPos(Vec3(x, y, z))

            if (action())
                break
        }

        // Reset position
        entity.setPosAndPrevPos(currPos, prevPos)
    }

    fun runWithNearestTrackedDistance(entity: Entity, f: () -> Unit) {
        if (entity !is EntityPlayer || !handleEvents() || mode == "Modern") {
            f()

            return
        }

        var backtrackDataArray = getBacktrackData(entity.uniqueID)?.toMutableList()

        if (backtrackDataArray == null) {
            f()

            return
        }

        backtrackDataArray = backtrackDataArray.sortedBy { (x, y, z, _) ->
            runWithSimulatedPastPosition(entity, Vec3(x, y, z)) {
                mc.thePlayer.getDistanceToBox(entity.hitBox)
            }
        }.toMutableList()

        val (x, y, z, _) = backtrackDataArray.first()

        runWithSimulatedPastPosition(entity, Vec3(x, y, z)) {
            f()

            null
        }
    }

    private fun runWithSimulatedPastPosition(entity: Entity, vec3: Vec3, f: () -> Double?): Double? {
        val currPos = entity.currPos
        val prevPos = entity.prevPos

        entity.setPosAndPrevPos(vec3)

        val result = f()

        // Reset position
        entity.setPosAndPrevPos(currPos, prevPos)

        return result
    }

    val color
        get() = if (rainbow) rainbow() else Color(red, green, blue)

    fun shouldBacktrack() =
        target?.let {
            !it.isDead && isEnemy(it) && (mc.thePlayer?.ticksExisted ?: 0) > 20 && !ignoreWholeTick
        } ?: false

    private fun reset() {
        target = null
        globalTimer.reset()
    }

    data class BacktrackData(val x: Double, val y: Double, val z: Double, val time: Long)
}
