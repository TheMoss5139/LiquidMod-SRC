package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import org.apache.logging.log4j.LogManager

@ModuleInfo(name = "MemoryRemover", description = "Remove useless Memory", category = ModuleCategory.MISC)
class MemoryRemover : Module() {
    val time = IntegerValue("TimeToRemove", 16000, 10000, 240000)
    val loggertest = BoolValue("LoggerTest", false)
    val timer = MSTimer()
    override fun onEnable() {
        timer.reset()
    }
    override fun onDisable() {
        timer.reset()
    }

    @EventTarget
    fun onEvent2D(render2DEvent: Render2DEvent) {
        if (timer.hasTimePassed(time.get().toLong())) {
            val i = Runtime.getRuntime().maxMemory()
            val j = Runtime.getRuntime().totalMemory()
            val k = Runtime.getRuntime().freeMemory()
            val l = j - k
            ClientUtils.displayChatMessage("MaxMemory:$i")
            ClientUtils.displayChatMessage("TotalMemory:$j")
            ClientUtils.displayChatMessage("FreeMemory:$k")
            ClientUtils.displayChatMessage("CurrentMemory:$l")
            Runtime.getRuntime().gc()
            if (loggertest.get()) {
                LogManager.getLogger().exit()
            }
            timer.reset()
        }
    }
}