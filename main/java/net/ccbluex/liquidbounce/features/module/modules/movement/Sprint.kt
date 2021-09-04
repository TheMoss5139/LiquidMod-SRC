/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.potion.Potion
import net.minecraft.util.MathHelper

@ModuleInfo(name = "Sprint", description = "Automatically sprints all the time.", category = ModuleCategory.MOVEMENT)
class Sprint : Module() {
    @JvmField
    val allDirectionsValue = BoolValue("AllDirections", true)
    val blindnessValue = BoolValue("Blindness", true)
    @JvmField
    val foodValue = BoolValue("Food", true)
    @JvmField
    val checkServerSide = BoolValue("CheckServerSide", false)
    @JvmField
    val checkServerSideGround = BoolValue("CheckServerSideOnlyGround", false)
    override fun onEnable() {}
    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        if (!MovementUtils.isMoving() || mc.thePlayer.isSneaking ||
            blindnessValue.get() && mc.thePlayer.isPotionActive(Potion.blindness) ||
            foodValue.get() && !(mc.thePlayer.foodStats.foodLevel > 6.0f || mc.thePlayer.capabilities.allowFlying)
        ) {
            mc.thePlayer.isSprinting = false
            return
        }
        if (checkServerSide.get() && (mc.thePlayer.onGround || !checkServerSideGround.get())
            && !allDirectionsValue.get() && RotationUtils.targetRotation != null
        ) {
            val ka = LiquidBounce.moduleManager.killAura
            if (ka.state && ka.rotationStrafeValue.get()
                    .equals("Strict", ignoreCase = true) && RotationUtils.targetRotation != null
            ) {
                if (allDirectionsValue.get() || mc.thePlayer.movementInput.moveForward >= 0.8f) mc.thePlayer.isSprinting =
                    true
                if (!allDirectionsValue.get() && mc.thePlayer.movementInput.moveForward < 0.8f) {
                    mc.thePlayer.isSprinting = false
                }
                return
            } else if (ka.state && ka.rotationStrafeValue.get()
                    .equals("Silent", ignoreCase = true) && ka.target != null && RotationUtils.targetRotation != null
                ) {
                mc.thePlayer.isSprinting = strafeForwardCheck()
                return
            } else {
                var yaw = mc.thePlayer.rotationYaw
                var pitch = mc.thePlayer.rotationPitch
                val perspectiveMod = LiquidBounce.moduleManager.perspectiveMod
                if (perspectiveMod.state && perspectiveMod.Perspectiveyaw != null) {
                    yaw = perspectiveMod.Perspectiveyaw!!
                    pitch = perspectiveMod.Perspectivepitch!!
                }
                if (isRotationOver(Rotation(yaw, pitch))) {
                    mc.thePlayer.isSprinting = false
                    return
                }
            }
        }
        if (allDirectionsValue.get() || MovementUtils.isMoving(true)) mc.thePlayer.isSprinting = true
        if (allDirectionsValue.get()) {
            RotationUtils.setTargetRotation(Rotation((MovementUtils.getDirection().toFloat() * 180F) / Math.PI.toFloat(), mc.thePlayer.rotationPitch))
        }
    }

    open fun isRotationOver(rotation: Rotation):Boolean {
        val ka = LiquidBounce.moduleManager.killAura
        if (ka.state && ka.target != null && strafeForwardCheck() && ka.rotationStrafeValue.get().equals("Silent",true)) {
            return false
        }
        return RotationUtils.getRotationDifference2(rotation) > 30
    }

    open fun strafeForwardCheck() : Boolean{
        RotationUtils.targetRotation?: return false
        val player = mc.thePlayer
        val dif = ((MathHelper.wrapAngleTo180_float(player.rotationYaw - RotationUtils.targetRotation.yaw
                - 23.5f - 135)
                + 180) / 45).toInt()

        val strafe = mc.thePlayer.movementInput.moveStrafe
        val forward = mc.thePlayer.movementInput.moveForward

        var calcForward = 0f
        var calcStrafe = 0f

        when (dif) {
            0 -> {
                calcForward = forward
                calcStrafe = strafe
            }
            1 -> {
                calcForward += forward
                calcStrafe -= forward
                calcForward += strafe
                calcStrafe += strafe
            }
            2 -> {
                calcForward = strafe
                calcStrafe = -forward
            }
            3 -> {
                calcForward -= forward
                calcStrafe -= forward
                calcForward += strafe
                calcStrafe -= strafe
            }
            4 -> {
                calcForward = -forward
                calcStrafe = -strafe
            }
            5 -> {
                calcForward -= forward
                calcStrafe += forward
                calcForward -= strafe
                calcStrafe -= strafe
            }
            6 -> {
                calcForward = -strafe
                calcStrafe = forward
            }
            7 -> {
                calcForward += forward
                calcStrafe += forward
                calcForward -= strafe
                calcStrafe += strafe
            }
        }
        return calcForward > 0
    }
}