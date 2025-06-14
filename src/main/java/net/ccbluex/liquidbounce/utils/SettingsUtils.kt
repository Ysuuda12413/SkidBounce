/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.LiquidBounce.moduleManager
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.utils.ClientUtils.displayClientMessage
import net.ccbluex.liquidbounce.utils.misc.StringUtils
import net.ccbluex.liquidbounce.value.*
import org.lwjgl.input.Keyboard

/**
 * Utility class for handling settings and scripts in LiquidBounce.
 */
object SettingsUtils {

    /**
     * Execute settings script.
     * @param script The script to apply.
     */
    fun applyScript(script: String) {
        script.lines().forEachIndexed { index, s ->
            if (s.isEmpty() || s.startsWith('#')) {
                return@forEachIndexed
            }

            val args = s.split(" ").toTypedArray()

            if (args.size <= 1) {
                displayClientMessage("§cSyntax error at line '$index' in setting script.\n§8§lLine: §7$s")
                return@forEachIndexed
            }

            if (args.size < 3) {
                displayClientMessage("§cSyntax error at line '$index' in setting script.\n§8§lLine: §7$s")
                return@forEachIndexed
            }

            val moduleName = args[0]
            val valueName = args[1]
            val value = args[2]
            val module = moduleManager[moduleName]

            if (module == null) {
                displayClientMessage("§cModule §a$moduleName§c does not exist!")
                return@forEachIndexed
            }

            when (valueName) {
                "toggle" -> setToggle(module, value)
                "bind" -> setBind(module, value)
                else -> setValue(module, valueName, value, args)
            }
        }

        FileManager.saveConfig(FileManager.valuesConfig)
    }
    private fun setToggle(module: Module, value: String) {
        module.state = value.equals("true", ignoreCase = true)
    }
    private fun setBind(module: Module, value: String) {
        module.keyBind = Keyboard.getKeyIndex(value)
    }

    // Utility functions for setting values
    private fun setValue(module: Module, valueName: String, value: String, args: Array<String>) {
        val moduleValue = module[valueName]

        if (moduleValue == null) {
            displayClientMessage("§cValue §a§l$valueName§c wasn't found in module §a§l${module.getName()}§c.")
            return
        }

        try {
            when (moduleValue) {
                is BooleanValue -> moduleValue.changeValue(value.toBoolean())
                is FloatValue -> moduleValue.changeValue(value.toFloat())
                is IntValue -> moduleValue.changeValue(value.toInt())
                is TextValue -> moduleValue.changeValue(StringUtils.toCompleteString(args, 2))
                is ListValue -> moduleValue.changeValue(value)
            }
        } catch (e: Exception) {
            displayClientMessage("§a§l${e.javaClass.name}§7(${e.message}) §cAn Exception occurred while setting §a§l$value§c to §a§l${moduleValue.name}§c in §a§l${module.getName()}§c.")
        }
    }

    /**
     * Generate settings script.
     * @return The generated script.
     */
    fun generateScript(): String {
        return moduleManager.modules
            .filter { !it.subjective }
            .joinToString("\n") { module ->
                buildString {
                    val vals = module.values.filter { !it.subjective }
                    if (vals.isNotEmpty()) {
                        vals.joinTo(this, separator = "\n") { "${module.name} ${it.name} ${it.get()}" }
                        appendLine()
                    }
                    appendLine("${module.name} toggle ${module.state}")
                    appendLine("${module.name} bind ${Keyboard.getKeyName(module.keyBind)}")
                }
            }.lines().filter { it.isNotBlank() }.joinToString("\n")
    }
}
