/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C0BPacketEntityAction

@ModuleInfo(
    name = "SuperKnockback",
    description = "Increases knockback dealt to other entities. Made by yorik100",
    category = ModuleCategory.COMBAT
)
class SuperKnockback : Module() {
    private val type = ListValue("Type", arrayOf("Always", "SprintOnly", "MoveOnly"), "Always")
    private val hurtTimeValue = IntegerValue("HurtTime", 2, 0, 10)
    private val delay = IntegerValue("Delay", 200, 0, 1000)
    private val noDouble = BoolValue("NoDoublePackets", true)
    private val alwaysStopSprint = BoolValue("AlwaysStopSprint", false)
    private val old = BoolValue("Useold", false)

    var buffer = false
    var noDoubleStart = false
    var noDoubleStop = false
    var timer = MSTimer()
    override fun onEnable() {
        buffer = false
        noDoubleStart = false
        noDoubleStop = false
        timer.reset()
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        if (!old.get())
            return
        if (event.targetEntity is EntityLivingBase) {
            if (event.targetEntity.hurtTime > hurtTimeValue.get())
                return

            if (mc.thePlayer.isSprinting)
                mc.netHandler.addToSendQueue(
                    C0BPacketEntityAction(
                        mc.thePlayer,
                        C0BPacketEntityAction.Action.STOP_SPRINTING
                    )
                )

            mc.netHandler.addToSendQueue(
                C0BPacketEntityAction(
                    mc.thePlayer,
                    C0BPacketEntityAction.Action.START_SPRINTING
                )
            )
            mc.netHandler.addToSendQueue(
                C0BPacketEntityAction(
                    mc.thePlayer,
                    C0BPacketEntityAction.Action.STOP_SPRINTING
                )
            )
            mc.netHandler.addToSendQueue(
                C0BPacketEntityAction(
                    mc.thePlayer,
                    C0BPacketEntityAction.Action.START_SPRINTING
                )
            )
            mc.thePlayer.isSprinting = true
            mc.thePlayer.serverSprintState = true
        }
    }

    @EventTarget
    fun onPacket(e: PacketEvent) {
        if (old.get())
            return
        if ((type.get() == "SprintOnly" && !mc.thePlayer.isSprinting) || (type.get() == "MoveOnly" && !MovementUtils.isMoving())) return
        if (e.packet is C02PacketUseEntity && e.packet
                .action == C02PacketUseEntity.Action.ATTACK
        ) {
            var newentity = e.packet.getEntityFromWorld(mc.theWorld)
            if (newentity !is EntityLivingBase) return
            if (newentity.hurtTime > hurtTimeValue.get() || !timer.hasTimePassed(delay.get().toLong())) return
            mc.thePlayer.sendQueue.addToSendQueue(
                C0BPacketEntityAction(
                    mc.thePlayer,
                    C0BPacketEntityAction.Action.START_SPRINTING
                )
            )
            buffer = !mc.thePlayer.isSprinting || alwaysStopSprint.get()
            timer.reset()
        } else if (noDouble.get() && e.packet is C0BPacketEntityAction) {
            if (e.packet.action == C0BPacketEntityAction.Action.START_SPRINTING) {
                if (noDoubleStart) {
                    e.cancelEvent()
                } else {
                    noDoubleStart = true
                    noDoubleStop = false
                }
            }

            if (e.packet.action == C0BPacketEntityAction.Action.STOP_SPRINTING) {
                if (noDoubleStop) {
                    e.cancelEvent()
                } else {
                    noDoubleStop = true
                    noDoubleStart = false
                }
            }
        }
    }

    @EventTarget
    fun onMotion(e: MotionEvent) {
        if (e.eventState == EventState.POST) {
            if (buffer) {
                buffer = false
                mc.thePlayer.sendQueue.addToSendQueue(
                    C0BPacketEntityAction(
                        mc.thePlayer,
                        C0BPacketEntityAction.Action.STOP_SPRINTING
                    )
                )
            }
            noDoubleStop = false
            noDoubleStart = noDoubleStop
        }
    }

    override val tag: String?
        get() = type.get()
}