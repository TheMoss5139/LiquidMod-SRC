package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue
import java.lang.StringBuilder

@ModuleInfo(name = "ChatBypass", description = "use &i to set the invisible character", category = ModuleCategory.MISC)
class ChatBypass : Module() {
    val disablecommand = BoolValue("DisabledCommand", false)
    fun getReplace(obj : String) : String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("\uF8FF")
        var obj2 = obj.replace("&i", stringBuilder.toString())
        return obj2
    }
}