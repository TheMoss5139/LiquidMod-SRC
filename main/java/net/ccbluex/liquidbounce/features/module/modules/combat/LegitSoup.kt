package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.ccbluex.liquidbounce.utils.InventoryUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.init.Items
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.client.C16PacketClientStatus

@ModuleInfo(
    name = "LegitSoup",
    description = "Makes you automatically eat soup whenever your health is low.",
    category = ModuleCategory.COMBAT
)
class LegitSoup : Module() {
    var healthValue = FloatValue("Health", 13f, 1f, 20f)
    var delayValue = IntegerValue("Delay", 150, 1, 1000)
    var swapbackValue = IntegerValue("SwapbackDelay", 250, 1, 1000)
    val openInventoryValue = BoolValue("OpenInv", false)
    val simulateInventoryValue = BoolValue("SimulateInventory", true)
    var delaytimer = MSTimer()
    var swapbacktimer = MSTimer()
    var soupslot = -1
    var setbackslot = -1
    override fun onEnable() {
        reset()
    }

    override fun onDisable() {
        reset()
    }

    fun reset() {
        soupslot = -1
        setbackslot = -1
        swapbacktimer.reset()
        delaytimer.reset()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        //Swap hotbar back
        if (swapbacktimer.hasTimePassed(swapbackValue.get().toLong()) && setbackslot != -1) {
            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(setbackslot))
            mc.thePlayer.inventory.currentItem = setbackslot
            setbackslot = -1
            return
        }
        //Check Timer
        if (!delaytimer.hasTimePassed(delayValue.get().toLong())) {
            return
        }

        //CheckHealth
        if (!lowhealth()) {
            //Move Soup
            MoveSoup()
            NoSoupInHotbar()
            return
        }
        //FindSoup
        findSoup()
        if (soupslot == -1) {
            return
        } //Run souping
        if (swapbacktimer.hasTimePassed(swapbackValue.get().toLong()) || setbackslot == -1)
            setbackslot = mc.thePlayer.inventory.currentItem
        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(soupslot - 36))
        mc.thePlayer.inventory.currentItem = soupslot - 36
        mc.netHandler.addToSendQueue(
            C08PacketPlayerBlockPlacement(
                mc.thePlayer.inventoryContainer
                    .getSlot(soupslot).stack
            )
        )
        swapbacktimer.reset()
        delaytimer.reset()
    }

    fun lowhealth(): Boolean {
        return mc.thePlayer.health <= healthValue.get()
    }

    fun findSoup() {
        soupslot = InventoryUtils.findItem(36, 45, Items.mushroom_stew)
    }

    fun MoveSoup() {
        val bowlInHotbar = InventoryUtils.findItem(36, 45, Items.bowl)
        if (bowlInHotbar != -1) {
            if (openInventoryValue.get() && mc.currentScreen !is GuiInventory)
                return

            var bowlMovable = false

            for (i in 9..36) {
                val itemStack = mc.thePlayer.inventoryContainer.getSlot(i).stack

                if (itemStack == null) {
                    bowlMovable = true
                    break
                } else if (itemStack.item == Items.bowl && itemStack.stackSize < 64) {
                    bowlMovable = true
                    break
                }
            }

            if (bowlMovable) {
                val openInventory = mc.currentScreen !is GuiInventory && simulateInventoryValue.get()

                if (openInventory)
                    mc.netHandler.addToSendQueue(C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT))
                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, bowlInHotbar, 0, 1, mc.thePlayer)
                delaytimer.reset()
                if (openInventory)
                    mc.netHandler.addToSendQueue(C0DPacketCloseWindow())
            }
        }
    }

    fun NoSoupInHotbar() {
        val soupInInventory = InventoryUtils.findItem(9, 36, Items.mushroom_stew)
        if (soupInInventory != -1 && InventoryUtils.hasSpaceHotbar()) {
            if (openInventoryValue.get() && mc.currentScreen !is GuiInventory)
                return

            val openInventory = mc.currentScreen !is GuiInventory && simulateInventoryValue.get()
            if (openInventory)
                mc.netHandler.addToSendQueue(C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT))

            mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, soupInInventory, 0, 1, mc.thePlayer)

            if (openInventory)
                mc.netHandler.addToSendQueue(C0DPacketCloseWindow())

            delaytimer.reset()
        }
    }
}