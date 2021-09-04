package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.network.play.server.S03PacketTimeUpdate

@ModuleInfo(name = "Ambience", description = "Change Time", category = ModuleCategory.WORLD)
class Ambience : Module() {

    val timeValue = IntegerValue("Time", 24000, 1000, 25565)

    @EventTarget
    fun onPacket(event : PacketEvent) {
        var packet = event.packet
        if (packet is S03PacketTimeUpdate) {
            event.cancelEvent()
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        mc.theWorld.worldTime = timeValue.get().toLong()
    }
}