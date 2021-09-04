package net.ccbluex.liquidbounce.features.module.modules.`fun`

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.IntegerValue

@ModuleInfo(name = "ArmorFun", description = "Put Your Armor and have fun.", category = ModuleCategory.FUN)
class ArmorFun : Module() {
    val nomovedelay : IntegerValue = IntegerValue("Delay-NoMove", 1000, 50, 5000)
    val movedelay : IntegerValue = IntegerValue("Delay-Move", 1000, 50, 5000)
    var type : ArmorType = ArmorType.Helmet
    var timer = MSTimer()

    override fun onEnable() {
        timer.reset()
        type = ArmorType.Helmet
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val delay = if (MovementUtils.isMoving()) movedelay.get() else nomovedelay.get()
        if (timer.hasTimePassed(delay.toLong())) {
            timer.reset()
            when (type) {
                ArmorType.Helmet -> {
                    if (mc.thePlayer.inventory.armorItemInSlot(0) == null) {
                    }else {
                        mc.playerController.windowClick(0, 5,0,1,mc.thePlayer)
                    }
                    type = ArmorType.Chestplate
                    return
                }
                ArmorType.Chestplate -> {
                    if (mc.thePlayer.inventory.armorItemInSlot(1) == null) {
                    }else {
                        mc.playerController.windowClick(0, 6,0,1,mc.thePlayer)
                    }
                    type = ArmorType.Leggings
                    return
                }
                ArmorType.Leggings -> {
                    if (mc.thePlayer.inventory.armorItemInSlot(2) == null) {
                    }else {
                        mc.playerController.windowClick(0, 7,0,1,mc.thePlayer)
                    }
                    type = ArmorType.Boots
                    return
                }
                ArmorType.Boots -> {
                    if (mc.thePlayer.inventory.armorItemInSlot(3) == null) {
                    }else {
                        mc.playerController.windowClick(0, 8,0,1,mc.thePlayer)
                    }
                    type = ArmorType.Helmet
                    return
                }
            }
        }
    }

    enum class ArmorType() {
        Helmet(),
        Chestplate(),
        Leggings(),
        Boots()
    }
}