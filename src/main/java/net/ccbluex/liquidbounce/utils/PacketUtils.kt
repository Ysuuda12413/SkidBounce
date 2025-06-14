/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.utils

import io.netty.buffer.Unpooled.buffer
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.event.events.GameLoopEvent
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.event.events.TickEvent
import net.ccbluex.liquidbounce.event.events.WorldEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.Velocity
import net.ccbluex.liquidbounce.features.module.modules.client.PacketDebugger
import net.ccbluex.liquidbounce.features.module.modules.combat.FakeLag
import net.ccbluex.liquidbounce.injection.implementations.IMixinEntity
import net.ccbluex.liquidbounce.utils.PPSCounter.PacketType.SEND
import net.ccbluex.liquidbounce.utils.PacketType.*
import net.ccbluex.liquidbounce.utils.extensions.*
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.NetworkManager
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayClient
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.network.play.client.C0CPacketInput
import net.minecraft.network.play.server.S0CPacketSpawnPlayer
import net.minecraft.network.play.server.S0FPacketSpawnMob
import net.minecraft.network.play.server.S14PacketEntity
import net.minecraft.network.play.server.S18PacketEntityTeleport
import net.minecraft.util.MovementInput

object PacketUtils : MinecraftInstance(), Listenable {

    val queuedPackets = mutableListOf<Packet<*>>()

    @EventTarget(priority = 2)
    fun onTick(event: TickEvent) {
        for (entity in mc.theWorld.loadedEntityList) {
            if (entity is EntityLivingBase) {
                (entity as? IMixinEntity)?.apply {
                    if (!truePos) {
                        trueX = entity.posX
                        trueY = entity.posY
                        trueZ = entity.posZ
                        truePos = true
                    }
                }
            }
        }
    }
    @EventTarget(priority = 2)
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        val world = mc.theWorld ?: return

        when (packet) {
            is S0CPacketSpawnPlayer ->
                (world.getEntityByID(packet.entityID) as? IMixinEntity)?.apply {
                    trueX = packet.realX
                    trueY = packet.realY
                    trueZ = packet.realZ
                    truePos = true
                }

            is S0FPacketSpawnMob ->
                (world.getEntityByID(packet.entityID) as? IMixinEntity)?.apply {
                    trueX = packet.realX
                    trueY = packet.realY
                    trueZ = packet.realZ
                    truePos = true
                }

            is S14PacketEntity -> {
                val entity = packet.getEntity(world)
                val mixinEntity = entity as? IMixinEntity

                mixinEntity?.apply {
                    if (!truePos) {
                        trueX = entity.posX
                        trueY = entity.posY
                        trueZ = entity.posZ
                        truePos = true
                    }

                    trueX += packet.realMotionX
                    trueY += packet.realMotionY
                    trueZ += packet.realMotionZ
                }
            }

            is S18PacketEntityTeleport ->
                (world.getEntityByID(packet.entityId) as? IMixinEntity)?.apply {
                    trueX = packet.realX
                    trueY = packet.realY
                    trueZ = packet.realZ
                    truePos = true
                }
        }
    }
    @EventTarget(priority = -5)
    fun onGameLoop(event: GameLoopEvent) {
        synchronized(queuedPackets) {
            queuedPackets.forEach {
                handlePacket(it)
                val packetEvent = PacketEvent(it, EventState.RECEIVE)
                FakeLag.onPacket(packetEvent)
                Velocity.onPacket(packetEvent)
            }

            queuedPackets.clear()
        }
    }

    @EventTarget(priority = -1)
    fun onDisconnect(event: WorldEvent) {
        if (event.worldClient == null) {
            synchronized(queuedPackets) {
                queuedPackets.clear()
            }
        }
    }

    override fun handleEvents() = true

    @JvmStatic
    fun sendPacket(packet: Packet<*>, triggerEvent: Boolean = true) {
        if (triggerEvent) {
            if (packet is C03PacketPlayer && packet !is C04PacketPlayerPosition && packet !is C06PacketPlayerPosLook)
                mc.thePlayer.positionUpdateTicks++
            mc.netHandler?.addToSendQueue(packet)
            return
        }
        val netManager = mc.netHandler?.networkManager ?: return
        PacketDebugger.onPacket(PacketEvent(packet, EventState.SEND))
        PPSCounter.registerType(SEND)
        if (packet is C03PacketPlayer && packet !is C04PacketPlayerPosition && packet !is C06PacketPlayerPosLook)
            mc.thePlayer.positionUpdateTicks++
        if (netManager.isChannelOpen) {
            netManager.flushOutboundQueue()
            netManager.dispatchPacket(packet, null)
        } else {
            netManager.readWriteLock.writeLock().lock()
            try {
                netManager.outboundPacketsQueue += NetworkManager.InboundHandlerTuplePacketListener(packet, null)
            } finally {
                netManager.readWriteLock.writeLock().unlock()
            }
        }
    }

    @JvmStatic
    fun C0CPacketInput(input: MovementInput, inputMultiplier: Float = 0.98f) = C0CPacketInput(
        input.moveStrafe * inputMultiplier,
        input.moveForward * inputMultiplier,
        input.jump,
        input.sneak
    )

    fun PacketBuffer() = net.minecraft.network.PacketBuffer(buffer())

    @JvmStatic
    fun sendPackets(vararg packets: Packet<*>, triggerEvents: Boolean = true) =
        packets.forEach { sendPacket(it, triggerEvents) }

    fun handlePackets(vararg packets: Packet<*>) =
        packets.forEach { handlePacket(it) }

    fun handlePacket(packet: Packet<*>?) {
        runCatching {
            (packet as Packet<INetHandlerPlayClient>).processPacket(mc.netHandler)
        }.onSuccess {
            PPSCounter.registerType(PPSCounter.PacketType.RECEIVED)
        }
    }

    val Packet<*>.type
        get() = if (javaClass.simpleName[0] == 'C')
            CLIENT else SERVER
}
