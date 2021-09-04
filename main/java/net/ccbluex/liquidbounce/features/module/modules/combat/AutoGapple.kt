package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.item.EnumRarity
import net.minecraft.item.ItemAppleGold
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

@ModuleInfo(name = "AutoGapple", description = "Eat Apple Auto.", category = ModuleCategory.COMBAT)
class AutoGapple : Module() {
    val healthValue = FloatValue("Health", 12.5F, 1F, 20F)
    val modeValue = ListValue("Mode", arrayOf("Fast", "Coming Soon Normal"), "Fast")
    val delayValue = IntegerValue("Delay", 1000, 0, 5000)
    val packetValue = IntegerValue("Packet", 35, 1, 50)
    val noairValue = BoolValue("NoAir", true)
    val delayTimer = MSTimer()
    override fun onEnable() {
        delayTimer.reset()
    }

    override fun onDisable() {
        delayTimer.reset()
    }

    @EventTarget
    fun onUpdate(updateEvent: UpdateEvent) {
        if (mc.thePlayer.health > healthValue.get()) return
        if (noairValue.get() && !mc.thePlayer.onGround) {
            return
        }
        if (!delayTimer.hasTimePassed(delayValue.get().toLong())) return
        //Find Normal
        var slot = findGapple(false)
        //Find Notch if slot = -1
        if (slot == -1)
            slot = findGapple(true)
        //if no gapple return
        if (slot == -1)
            return
        //Eating
        val currentslot = mc.thePlayer.inventory.currentItem
        val issameslot = (currentslot == slot)
        if (!issameslot) {
            mc.thePlayer.inventory.currentItem = slot
            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(slot));
        }
        if (!mc.thePlayer.isUsingItem)
            mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.inventoryContainer.getSlot(slot).stack))
        //Normal 35
        repeat(packetValue.get()) {
            mc.netHandler.addToSendQueue(C03PacketPlayer(mc.thePlayer.onGround))
        }
        mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
        if (!issameslot) {
            mc.thePlayer.inventory.currentItem = slot
        }
        mc.thePlayer.stopUsingItem()
        if (!issameslot) {
            mc.thePlayer.inventory.currentItem = currentslot
        }
        if (!issameslot) {
            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(currentslot));
        }
        //Finish
        delayTimer.reset()
    }
    fun findGapple(isNotch : Boolean) : Int {
        for (i in 36..44) {
            val itemStack = mc.thePlayer.inventoryContainer.getSlot(i).stack ?: continue
            if (itemStack.item is ItemAppleGold) {
                val itemAppleGold = itemStack.item as ItemAppleGold
                var howrare = EnumRarity.RARE
                if (isNotch) {
                    howrare = EnumRarity.EPIC
                }
                if (itemAppleGold.getRarity(itemStack) == howrare) {
                    return i - 36
                }
            }
        }
        return -1
    }
}