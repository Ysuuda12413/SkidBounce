/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.lang

import net.ccbluex.liquidbounce.LiquidBounce.CLIENT_NAME
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MinecraftInstance

object LanguageManager : MinecraftInstance() {

    // Current language
    private val language: String
        get() = overrideLanguage.ifBlank { mc.gameSettings.language }

    // The game language can be overridden by the user
    var overrideLanguage = ""

    // Common language
    private const val COMMON_UNDERSTOOD_LANGUAGE = "en_US"

    // List of all languages
    val knownLanguages = arrayOf(
        "en_US",
        "pt_BR",
        "pt_PT",
        "zh_CN",
        "zh_TW",
        "bg_BG",
        "ru_RU"
    )
    private val languageMap = mutableMapOf<String, Language>()

    /**
     * Load all languages which are pre-defined in [knownLanguages] and stored in assets.
     * If a language is not found, it will be logged as error.
     *
     * Languages are stored in assets/minecraft/[CLIENT_NAME]/lang and when loaded will be stored in [languageMap]
     */
    fun loadLanguages() {
        for (language in knownLanguages) {
            runCatching {
                val languageFile = javaClass.getResourceAsStream("/assets/minecraft/${CLIENT_NAME.lowercase()}/lang/$language.json")
                val languageJson = FileManager.PRETTY_GSON.fromJson(languageFile!!.bufferedReader(), Language::class.java)
                languageMap[language] = languageJson
            }.onFailure {
                ClientUtils.LOGGER.error("Failed to load language $language", it)
            }
        }
        ClientUtils.LOGGER.info("Loaded ${knownLanguages.size} languages")
    }

    /**
     * Get translation from language
     */
    fun getTranslation(key: String, vararg args: Any)
        = languageMap[language]?.getTranslation(key, *args)
        ?: languageMap[COMMON_UNDERSTOOD_LANGUAGE]?.getTranslation(key, *args)
        ?: key

    fun translationMenu(key: String, vararg args: Any) = getTranslation("menu.$key", *args)
}
