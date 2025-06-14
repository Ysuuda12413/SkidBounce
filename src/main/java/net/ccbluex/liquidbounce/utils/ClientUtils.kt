/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.utils

import com.google.gson.JsonObject
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.LiquidBounce.CLIENT_NAME
import net.minecraft.client.gui.GuiNewChat
import net.minecraft.client.settings.GameSettings
import net.minecraft.network.NetworkManager
import net.minecraft.network.login.client.C01PacketEncryptionResponse
import net.minecraft.network.login.server.S01PacketEncryptionRequest
import net.minecraft.util.*
import net.minecraftforge.fml.relauncher.*
import org.apache.logging.log4j.*
import java.lang.reflect.Field
import java.security.PublicKey
import javax.crypto.SecretKey

@SideOnly(Side.CLIENT)
object ClientUtils : MinecraftInstance() {
    private var fastRenderField: Field? = null
    var runTimeTicks = 0

    init {
        try {
            val declaredField = GameSettings::class.java.getDeclaredField("ofFastRender")

            fastRenderField = declaredField
        } catch (ignored: NoSuchFieldException) { }
    }

    val LOGGER: Logger = LogManager.getLogger("SkidBounce")

    fun disableFastRender() {
        try {
            fastRenderField?.let {
                if (!it.isAccessible)
                    it.isAccessible = true

                it.setBoolean(mc.gameSettings, false)
            }
        } catch (ignored: IllegalAccessException) {}
    }

    fun sendEncryption(
        networkManager: NetworkManager,
        secretKey: SecretKey?,
        publicKey: PublicKey?,
        encryptionRequest: S01PacketEncryptionRequest,
    ) {
        networkManager.sendPacket(C01PacketEncryptionResponse(secretKey, publicKey, encryptionRequest.verifyToken),
            { networkManager.enableEncryption(secretKey) }
        )
    }

    @JvmStatic
    fun displayChatMessage(message: Any? = "") {
        val jsonObject = JsonObject()
        jsonObject.addProperty("text", message.toString())
        val chatComponent = IChatComponent.Serializer.jsonToComponent(jsonObject.toString())
        try {
            mc.ingameGUI.chatGUI.printChatMessage(chatComponent)
        } catch (e: Throwable) {
            LOGGER.error(e)
        }
    }
    @JvmStatic
    fun displayClientMessage(message: Any? = "") = displayChatMessage("§8[§9$CLIENT_NAME§8] §r$message")

    fun resource(directory: String) = ResourceLocation("${CLIENT_NAME.lowercase()}/$directory")
}
