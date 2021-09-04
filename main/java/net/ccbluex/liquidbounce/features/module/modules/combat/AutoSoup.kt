/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.InventoryUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.init.Items
import net.minecraft.network.play.client.*
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

@ModuleInfo(name = "AutoSoup", description = "Makes you automatically eat soup whenever your health is low.", category = ModuleCategory.COMBAT)
class AutoSoup : Module() {

    private val healthValue = FloatValue("Health", 15f, 0f, 20f)
    private val addhealthValue = FloatValue("AddedHealth", 15f, 0f, 20f)
    private val delayValue = IntegerValue("Delay", 150, 0, 500)
    private val openInventoryValue = BoolValue("OpenInv", false)
    private val simulateInventoryValue = BoolValue("SimulateInventory", true)
    private val bowlValue = ListValue("Bowl", arrayOf("Drop", "Move", "Stay"), "Drop")
    private val ignoreValue = BoolValue("Ignore", false)
    private val ignoreslotValue = IntegerValue("IgnoreSlot", 37, 9, 44)

    private val timer = MSTimer()

    override val tag: String
        get() = healthValue.get().toString()

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        if (!timer.hasTimePassed(delayValue.get().toLong()))
            return
        var health =  mc.thePlayer.health
        if (health <= healthValue.get()) {
            var soupInHotbar = InventoryUtils.findItem(36, 45, Items.mushroom_stew)
            val ignoreslot = arrayListOf<Int>()
            var isChanged = false
            while (health <= healthValue.get() && soupInHotbar != -1) {
                isChanged = true
                if (soupInHotbar - 36 != mc.thePlayer.inventory.currentItem)
                    mc.netHandler.addToSendQueue(C09PacketHeldItemChange(soupInHotbar - 36))
                mc.netHandler.addToSendQueue(
                    C08PacketPlayerBlockPlacement(
                        mc.thePlayer.inventoryContainer
                            .getSlot(soupInHotbar).stack
                    )
                )
                if (bowlValue.get().equals("Drop", true))
                    mc.netHandler.addToSendQueue(
                        C07PacketPlayerDigging(
                            C07PacketPlayerDigging.Action.DROP_ITEM,
                            BlockPos.ORIGIN, EnumFacing.DOWN
                        )
                    )
                health += addhealthValue.get()
                ignoreslot.add(soupInHotbar)
                soupInHotbar = InventoryUtils.findItem(36, 45, Items.mushroom_stew, ignoreslot.toIntArray())
            }
            if (isChanged) {
                if (soupInHotbar - 36 != mc.thePlayer.inventory.currentItem)
                    mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                timer.reset()
            }
            return
        }

        //NomalBowl
        val bowlInHotbar = InventoryUtils.findItem(36, 45, Items.bowl, if (ignoreValue.get()) getIgnoreSlot() else -1)
        if (bowlValue.get().equals("Move", true) && bowlInHotbar != -1) {
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
                mc.playerController.windowClick(0, bowlInHotbar, 0, 1, mc.thePlayer)

                if (openInventory)
                    mc.netHandler.addToSendQueue(C0DPacketCloseWindow())
            }
        }

        //SoupBowl
        val soupInInventory = InventoryUtils.findItem(9, 36, Items.mushroom_stew)
        if (soupInInventory != -1 && InventoryUtils.hasSpaceHotbar(if (ignoreValue.get()) getIgnoreSlot() else -1)) {
            if (openInventoryValue.get() && mc.currentScreen !is GuiInventory)
                return

            val openInventory = mc.currentScreen !is GuiInventory && simulateInventoryValue.get()
            if (openInventory)
                mc.netHandler.addToSendQueue(C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT))
            if (ignoreValue.get()) {
                val spaceslot = InventoryUtils.findSpaceHotbar(getIgnoreSlot())
                if (spaceslot != -1) {
                    mc.playerController.windowClick(0, soupInInventory, spaceslot, 2, mc.thePlayer)
                }else {
                    mc.playerController.windowClick(0, soupInInventory, 0, 1, mc.thePlayer)
                }
            }else {
                mc.playerController.windowClick(0, soupInInventory, 0, 1, mc.thePlayer)
            }
            if (openInventory)
                mc.netHandler.addToSendQueue(C0DPacketCloseWindow())

            timer.reset()
        }
    }

    fun getIgnoreSlot(): Int {
        return ignoreslotValue.get()
    }
}