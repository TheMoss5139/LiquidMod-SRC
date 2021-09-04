package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.block.BlockChest
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.Slot
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement

@ModuleInfo(
    name = "SlientChestStealer",
    description = "Automatically steals all items from a chest.",
    category = ModuleCategory.WORLD
)
class SlientChestStealer : Module() {
    val mindelay = IntegerValue("MinDelay", 100, 0, 2000)
    val maxdelay = IntegerValue("MaxDelay", 200, 0, 2000)
    var listStack = arrayListOf<Slot>()
    val timer = MSTimer()
    var randomdelay = 0
    var isopenchest = false
    var currentscreen: GuiScreen? = null

    override fun onEnable() {
        setNewTimer()
        isopenchest = false
        listStack.clear()
        currentscreen = null
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C08PacketPlayerBlockPlacement) {
            if (mc.theWorld.getBlockState(packet.position).block is BlockChest) {
                isopenchest = true
                ClientUtils.displayChatMessage("Opened Chest by Packet")
            }
            return
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (mc.currentScreen is GuiChest && isopenchest) {
            listStack.clear()
            // inventory cleaner
            val inventoryCleaner = LiquidBounce.moduleManager.inventoryCleaner
            val screen = (mc.currentScreen as GuiChest)
            for (slot in 0 until screen.inventoryRows * 9) {
                val slotstack = screen.inventorySlots.inventorySlots[slot]
                if (slotstack.stack != null && (!inventoryCleaner.state || inventoryCleaner.isUseful(slotstack.stack, -1))) {
                    listStack.add(slotstack)
                }
            }
            setNewTimer()
            currentscreen = mc.currentScreen
            mc.displayGuiScreen(null)
            if (listStack.isEmpty())
                mc.thePlayer.closeScreen()
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        isopenchest = false
    }

    @EventTarget
    fun onUpdate(event : UpdateEvent) {
        if (!isopenchest) return
        if (listStack.isNotEmpty()) {
            if (fullInventory) {
                val msg = "Your Inventory is full."
                ClientUtils.displayChatMessage("§8[§9§l${LiquidBounce.CLIENT_NAME}§8] §3$msg")
                listStack.clear()
                if (isopenchest)
                    mc.thePlayer.closeScreen()
                isopenchest = false
                return
            }
            if (timer.hasTimePassed(randomdelay.toLong())) {
                if (randomdelay == 0) {
                    for (list in listStack) {
                        click(currentscreen as GuiChest, list)
                    }
                } else {
                    val slot = listStack[0]
                    click(currentscreen as GuiChest, slot)
                    try {
                        listStack.removeAt(0)
                    } catch (e: Exception) {
                        listStack.clear()
                        mc.thePlayer.closeScreen()
                        isopenchest = false
                        ClientUtils.displayChatMessage("Error Remove Slot -> $slot")
                        return
                    }
                }
                setNewTimer()
                isopenchest = listStack.isNotEmpty()
                if (randomdelay == 0) {
                    listStack.clear()
                    isopenchest = false
                }
                if (!isopenchest) mc.thePlayer.closeScreen()
            }
        }
    }

    private fun setNewTimer() {
        timer.reset()
        randomdelay = RandomUtils.nextInt(mindelay.get(), maxdelay.get())
    }

    private val fullInventory: Boolean
        get() = mc.thePlayer.inventory.mainInventory.none { it == null }

    fun click(screen : GuiChest, slot : Slot) {
        screen.handleMouseClick(slot, slot.slotNumber, 0, 1)
    }
}