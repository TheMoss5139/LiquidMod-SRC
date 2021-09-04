package net.ccbluex.liquidbounce.features.module.modules.`fun`

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.minecraft.network.play.client.C02PacketUseEntity

@ModuleInfo(name = "TestModule", description = "TestModule TestModule", category = ModuleCategory.FUN)
class TestModule : Module() {
    override fun onEnable() {
        mc.thePlayer.sendQueue.addToSendQueue(C02PacketUseEntity(mc.thePlayer, C02PacketUseEntity.Action.ATTACK))
        state = false
    }
}