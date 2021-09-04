/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.SlowDownEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.item.*
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

@ModuleInfo(name = "NoSlow", description = "Cancels slowness effects caused by soulsand and using items.",
        category = ModuleCategory.MOVEMENT)
class NoSlow : Module() {

    val mode = ListValue("Mode", arrayOf("Normal", "AAC4.4.0", "MixAAC"), "Normal")
    private val blockForwardMultiplier = FloatValue("BlockForwardMultiplier", 1.0F, 0.2F, 1.0F)
    private val blockStrafeMultiplier = FloatValue("BlockStrafeMultiplier", 1.0F, 0.2F, 1.0F)

    private val consumeForwardMultiplier = FloatValue("ConsumeForwardMultiplier", 1.0F, 0.2F, 1.0F)
    private val consumeStrafeMultiplier = FloatValue("ConsumeStrafeMultiplier", 1.0F, 0.2F, 1.0F)

    private val bowForwardMultiplier = FloatValue("BowForwardMultiplier", 1.0F, 0.2F, 1.0F)
    private val bowStrafeMultiplier = FloatValue("BowStrafeMultiplier", 1.0F, 0.2F, 1.0F)
    private val minhurtTimeValue = IntegerValue("MinHurtTime", 2, 0, 10)
    private val maxhurtTimeValue = IntegerValue("MaxHurtTime", 5, 0, 10)

    private val packet = BoolValue("Packet", true)

    // Soulsand
    val soulsandValue = BoolValue("Soulsand", true)
    val timer = MSTimer()
    var noSlowhasBlocked = false

    @EventTarget
    fun onMotion(event: MotionEvent) {
        val heldItem = mc.thePlayer.heldItem
        if (heldItem == null || heldItem.item !is ItemSword || !MovementUtils.isMoving()) {
            return
        }
        val killAura = LiquidBounce.moduleManager.killAura
        if (!mc.thePlayer.isBlocking && !killAura.blockingStatus) {
            return
        }
        if (this.packet.get()) {
            when (mode.get().toLowerCase()) {
                "mixaac" -> {
                    when (event.eventState) {
                        EventState.PRE -> {
                            val digging = C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.UP)
                            mc.netHandler.addToSendQueue(digging)
                        }
                        EventState.POST -> {
                            val blockPlace = C08PacketPlayerBlockPlacement(BlockPos(69, 40, 1337), 255, mc.thePlayer.inventory.getCurrentItem(), 0F, 0F,0F)
                            mc.netHandler.addToSendQueue(blockPlace)
                        }
                    }
                }

                "aac4.4.0" -> {
                    noSlowhasBlocked = when (event.eventState) {
                        EventState.PRE -> {
                            if (!noSlowhasBlocked) return
                            val digging = C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.UP)
                            mc.netHandler.addToSendQueue(digging)
                            false
                        }
                        EventState.POST -> {
                            if (noSlowhasBlocked) return
                            if (minhurtTimeValue.get() > mc.thePlayer.hurtTime || mc.thePlayer.hurtTime > maxhurtTimeValue.get()) return
                            val blockPlace = C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem())
                            mc.netHandler.addToSendQueue(blockPlace)
                            true
                        }
                    }
                }

                "normal" -> {
                    when (event.eventState) {
                        EventState.PRE -> {
                            val digging = C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN)
                            mc.netHandler.addToSendQueue(digging)
                        }
                        EventState.POST -> {
                            val blockPlace = C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem())
                            mc.netHandler.addToSendQueue(blockPlace)
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    fun onSlowDown(event: SlowDownEvent) {
        val heldItem = mc.thePlayer.heldItem?.item

        event.forward = getMultiplier(heldItem, true)
        event.strafe = getMultiplier(heldItem, false)
    }

    private fun getMultiplier(item: Item?, isForward: Boolean) = when (item) {
        is ItemFood, is ItemPotion, is ItemBucketMilk -> {
            if (isForward) this.consumeForwardMultiplier.get() else this.consumeStrafeMultiplier.get()
        }
        is ItemSword -> {
            if (isForward) this.blockForwardMultiplier.get() else this.blockStrafeMultiplier.get()
        }
        is ItemBow -> {
            if (isForward) this.bowForwardMultiplier.get() else this.bowStrafeMultiplier.get()
        }
        else -> 0.2F
    }

}
