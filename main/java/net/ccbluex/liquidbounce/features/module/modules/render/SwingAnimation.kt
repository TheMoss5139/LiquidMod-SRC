/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue

@ModuleInfo(name = "SwingAnimation", description = "Changes swing animation.", category = ModuleCategory.RENDER)
class SwingAnimation : Module() {
    val mode = ListValue(
        "Mode",
        arrayOf(
            "Liquidbounce",
            "None",
            "Tap",
            "Tap2",
            "Vanilla",
            "Slide",
            "Sigma",
            "Exhibition",
            "Remix",
            "1.8Slide",
            "1.8Slide2",
            "Jello"
        ),
        "Liquidbounce"
    )
    val swing = BoolValue("Swing", false)
    val swordonly = BoolValue("OnlySword", true)
    val itemSize = FloatValue("ItemSize", 1F, 0.1F, 2F)
    val itemx = FloatValue("X", 0F, -2F, 2F)
    val itemy = FloatValue("Y", 0F, -2F, 2F)
    val itemz = FloatValue("Z", 0F, -2F, 2F)

    override val tag: String?
        get() = mode.get()
}