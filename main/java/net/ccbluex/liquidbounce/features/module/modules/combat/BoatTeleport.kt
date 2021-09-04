package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.FloatValue

@ModuleInfo(name = "BoatTeleport", description = "Teleport You Position", category = ModuleCategory.COMBAT)
class BoatTeleport : Module() {
    val x = FloatValue("PosX", 5F, -100F, 100F)
    val y = FloatValue("PosY", 5F, -100F, 100F)
    val z = FloatValue("PosZ", 5F, -100F, 100F)
    var runfly = false

    override fun onEnable() {
        super.onEnable()
        runfly = false
    }

    override fun onDisable() {
        super.onDisable()
        runfly = false
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        if (mc.thePlayer.isRiding) {
            runfly = true
        }else if (runfly) {
            runfly = false
            mc.thePlayer.motionY = 0.42
            mc.thePlayer.motionX *= x.get()
            mc.thePlayer.motionY *= y.get()
            mc.thePlayer.motionZ *= z.get()
        }
    }
}