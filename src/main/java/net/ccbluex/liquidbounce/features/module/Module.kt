/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module

import net.ccbluex.liquidbounce.LiquidBounce.isStarting
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.features.module.modules.client.GameDetector
import net.ccbluex.liquidbounce.file.FileManager.modulesConfig
import net.ccbluex.liquidbounce.file.FileManager.saveConfig
import net.ccbluex.liquidbounce.lang.LanguageManager.getTranslation
import net.ccbluex.liquidbounce.ui.client.hud.HUD.addNotification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Arraylist
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notifications.Notification
import net.ccbluex.liquidbounce.utils.ClassUtils.getRawValues
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.extensions.plus
import net.ccbluex.liquidbounce.utils.extensions.toLowerCamelCase
import net.ccbluex.liquidbounce.utils.misc.RandomUtils.nextFloat
import net.ccbluex.liquidbounce.utils.timing.TickedActions.TickScheduler
import net.ccbluex.liquidbounce.value.BooleanValue
import net.ccbluex.liquidbounce.value.Value
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Keyboard

open class Module(
    val name: String,
    val category: Category,
    defaultKeyBind: Int = Keyboard.KEY_NONE,
    val defaultInArray: Boolean = true, // Used in HideCommand to reset modules visibility.
    private val canBeEnabled: Boolean = true,
    private val forcedDescription: String? = null,
    // Adds spaces between lowercase and uppercase letters (KillAura -> Kill Aura)
    val spacedName: String = name.split("(?<=[a-z])(?=[A-Z])".toRegex()).joinToString(separator = " "),
    val subjective: Boolean = category == Category.RENDER,
    val gameDetecting: Boolean = canBeEnabled,
    defaultEnabled: Boolean = false,
) : MinecraftInstance(), Listenable {

    // Value that determines whether the module should depend on GameDetector
    private val onlyInGameValue = BooleanValue("OnlyInGame", true, subjective = true) { GameDetector.state }

    private val hideModuleValue = object : BooleanValue("Hide", !defaultInArray, subjective = true) {
        override fun onUpdate(value: Boolean) {
            saveConfig(modulesConfig)
        }
    }

    protected val TickScheduler = TickScheduler(this)

    // Module information

    // Get normal or spaced name
    fun getName(spaced: Boolean = Arraylist.spacedModules) = if (spaced) spacedName else name

    var keyBind = defaultKeyBind
        set(keyBind) {
            field = keyBind

            saveConfig(modulesConfig)
        }

    var inArray
        get() = !hideModuleValue.get()
        set(value) {
            hideModuleValue.set(!value)
        }

    val description
        get() = forcedDescription ?: getTranslation("module.${name.toLowerCamelCase()}.description")

    var slideStep = 0F

    // Current state of module
    var state = false
        set(value) {
            if (field == value)
                return

            // Call toggle
            onToggle(value)

            TickScheduler.clear()

            // Play sound and add notification
            if (!isStarting) {
                mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("random.click"), 1F))
                addNotification(
                    Notification(
                        getTranslation(
                            "notification.module" + if (value) "Enabled" else "Disabled",
                            getName()
                        )
                    )
                )
            }

            // Call on enabled or disabled
            if (value) {
                onEnable()

                if (canBeEnabled)
                    field = true
            } else {
                onDisable()
                field = false
            }

            // Save module state
            saveConfig(modulesConfig)
        }

    // HUD
    val hue = nextFloat()
    var slide = 0F
    var yAnim = 0f

    // Tag
    open val tag: String?
        get() = null

    // AutoDisable
    @Suppress("PropertyName")
    val AutoDisable = AutoDisable()

    /**
     * Toggle module
     */
    fun toggle() {
        state = !state
    }

    /**
     * Called when module toggled
     */
    open fun onToggle(state: Boolean) {}

    /**
     * Called when module enabled
     */
    open fun onEnable() {}

    /**
     * Called when module disabled
     */
    open fun onDisable() {}

    /**
     * Get value by [valueName]
     */
    open fun getValue(valueName: String) = values.find { it.name.equals(valueName, ignoreCase = true) }

    /**
     * Get value via `module[valueName]`
     */
    operator fun get(valueName: String) = getValue(valueName)

    /**
     * Get all values of module with unique names
     */
    open val values
        get() = getValues0()

    private fun getValues0() = getRawValues(this)
        .toMutableList()
        .also {
            if (gameDetecting) it.add(onlyInGameValue)
            it.add(hideModuleValue)
        }
        .distinctBy { it.name }

    protected fun <M> getValuesWithModes(modes: List<M>, getModeName: (M) -> String, getValues: (M) -> List<Value<*>>, isEnabled: (M) -> Boolean, after: String? = null): MutableList<Value<*>> {
        val values = getValues0().toMutableList()

        values.addAll(
            after?.let { values.indexOfFirst { it.name == after } + 1 } ?: 0,
            modes.map { mode ->
            val modeName = getModeName(mode)
                getValues(mode).onEach { value ->
                    value.name = "$modeName-${value.name}"
                    value.isSupported += { isEnabled(mode) }
                }
            }.flatten()
        )

        return values
    }

    val isActive
        get() = !gameDetecting || !onlyInGameValue.get() || GameDetector.isInGame()

    /**
     * Events should be handled when module is enabled
     */
    override fun handleEvents() = state && isActive

    init {
        if (defaultEnabled) {
            state = true
        }
    }
}
