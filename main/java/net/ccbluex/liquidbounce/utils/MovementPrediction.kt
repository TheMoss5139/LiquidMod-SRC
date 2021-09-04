package net.ccbluex.liquidbounce.utils

import net.minecraft.entity.Entity
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3

class MovementPrediction {

    fun returnNewPredict(predictEntity : Entity, ticks : Int) : Vec3? {
        if (predictEntity == null || ticks <= 0) return null

//        calculateForTick(predictEntity.positionVector, predictEntity.)
        return null
    }

//    private fun calculateForTick(start : Vec3,strafe : Float, forward : Float, yaw : Float, ticks: Int) {
//        var startPos = start
//        for (i in 0..ticks) {
//            var v: Float = strafe * strafe + forward * forward
//            if (v >= 0.0001f) {
//                v = MathHelper.sqrt_float(v)
//                if (v < 1.0f) {
//                    v = 1.0f
//                }
//                v = MinecraftInstance.mc.thePlayer.jumpMovementFactor / v
//                var movestrafe = strafe * v
//                var moveforward = forward * v
//                val f1 = MathHelper.sin(yaw * Math.PI.toFloat() / 180.0f)
//                val f2 = MathHelper.cos(yaw * Math.PI.toFloat() / 180.0f)
//                startPos.xCoord += (movestrafe * f2 - moveforward * f1).toDouble()
//                startPos.zCoord += (moveforward * f2 + movestrafe * f1).toDouble()
//            }
//            motionY -= 0.08
//            motionX *= 0.91
//            motionY *= 0.9800000190734863
//            motionY *= 0.91
//            motionZ *= 0.91
//            x += motionX
//            y += motionY
//            z += motionZ
//        }
//    }
}