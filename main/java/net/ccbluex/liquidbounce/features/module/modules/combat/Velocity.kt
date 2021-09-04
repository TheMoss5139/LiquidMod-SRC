/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.utils.extensions.getBlock
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.block.BlockAir
import net.minecraft.block.BlockSlab
import net.minecraft.block.BlockStairs
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S27PacketExplosion
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper

@ModuleInfo(
    name = "Velocity",
    description = "Allows you to modify the amount of knockback you take.",
    category = ModuleCategory.COMBAT
)
class Velocity : Module() {

    /**
     * OPTIONS
     */
    private val horizontalValue = FloatValue("Horizontal", 0F, 0F, 1F)
    private val verticalValue = FloatValue("Vertical", 0F, 0F, 1F)
    private val modeValue = ListValue(
        "Mode", arrayOf(
            "Simple", "AAC", "Lucky", "AACReduce", "AACReduce2","AACReduce3", "ByHurttime", "AACPush", "AACZero",
            "Reverse", "SmoothReverse", "Jump", "Glitch", "SeksinRekker", "AAC5.2.0", "Test", "Test2", "Test3"
        ), "Simple"
    )

    // Reverse
    private val reverseStrengthValue = FloatValue("ReverseStrength", 1F, 0.1F, 1F)
    private val reverse2StrengthValue = FloatValue("SmoothReverseStrength", 0.05F, 0.02F, 0.1F)

    // AAC Push
    val aacPushXZReducerValue = FloatValue("AACPushXZReducer", 2F, 1F, 3F)
    val aacPushYReducerValue = BoolValue("AACPushYReducer", true)
    val reduce_seksinrekker_value = FloatValue("reduce_seksinrekker_value", 0.6F, 0F, 1F)
    val luckyYReduceValue = FloatValue("LuckyYReduce", 0.42F, 0.1F, 0.42F)
    val onGroundOnly = BoolValue("SeksinRekkerOnGroundOnly", true)
    val seksinrekker_timer_value = IntegerValue("SeksinRekkerTimer", 80, 10, 1000)
    val aacreduce_nowater_value = BoolValue("AACReduceNoWater", false)
    val aacreduce_mincount_value = IntegerValue("MinAACReduceCount", 10, 1, 20)
    val aacreduce_maxcount_value = IntegerValue("MaxAACReduceCount", 14, 1, 20)
    val aacreduce_ground_value = FloatValue("AACReduceGroundMotion", 1.2F, 0F, 2F)
    val RemoveSuperKnockValue = BoolValue("RemoveSuperknockBack", false)
    val aacreduce2_state_value = IntegerValue("AACReduce2State", 2, 1 , 5)
    val aacreduce2_timer_value = IntegerValue("AACReduce2VanillaTimer", 500, 1 , 2000)
    val aacreduce3_min_value = IntegerValue("AACReduce3MinHurtTime", 1, 0 , 10)
    val aacreduce3_max_value = IntegerValue("AACReduce3MaxHurtTime", 5, 0 , 10)

    /**
     * VALUES
     */
    private var velocityTimer = MSTimer()
    private var velocityInput = false

    // SmoothReverse
    private var reverseHurt = false

    // AACPush
    private var jump = false

    //LiquidMod
    private var left = false
    private var ticks = 0
    var count = 0
    var hitstate = 0
    private var vanillaTimer = MSTimer()

    override val tag: String
        get() = modeValue.get()

    override fun onEnable() {
        hitstate = 0
        ticks = 0
        left = false
    }

    override fun onDisable() {
        mc.thePlayer?.speedInAir = 0.02F
        ticks = 0
        count = 0
        hitstate = 0
    }

    @EventTarget
    fun onTick(event: TickEvent) {
        if (mc.thePlayer == null)
            return
        if ((mc.thePlayer.isInWater && aacreduce_nowater_value.get()) || mc.thePlayer.isInLava || mc.thePlayer.isInWeb) {
            ticks = 0
            return
        }
        when (modeValue.get().toLowerCase()) {
            "aacreduce3" -> {
                if (velocityInput && velocityTimer.hasTimePassed(80L)) {
                    if (mc.thePlayer.hurtTime in aacreduce3_min_value.get()..aacreduce3_max_value.get()) {
                        mc.thePlayer.movementInput.moveForward = 0F
                    }
                }
                if (velocityTimer.hasTimePassed(550L)) {
                    count = 0
                    velocityInput = false
                }
            }

            "test2" -> {
                if (velocityInput && velocityTimer.hasTimePassed(80L)) {
                    if (mc.thePlayer.hurtTime in aacreduce3_min_value.get()..aacreduce3_max_value.get()) {
                        mc.thePlayer.movementInput.moveForward = 0F
                    }
                }
                if (velocityTimer.hasTimePassed(550L)) {
                    count = 0
                    velocityInput = false
                }
            }

            "test3" -> {
                if (mc.thePlayer.hurtTime != 0) {
                    ticks++
                }else {
                    ticks = 0
                }
                if (ticks >= 9) {
                    mc.thePlayer.motionX *= 0.6
                    mc.thePlayer.motionZ *= 0.6
                    ticks = 0
                }
            }
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if ((mc.thePlayer.isInWater && aacreduce_nowater_value.get()) || mc.thePlayer.isInLava || mc.thePlayer.isInWeb)
            return

        when (modeValue.get().toLowerCase()) {
            "byhurttime" -> {
                if (mc.thePlayer.hurtTime > 0) {
                    mc.thePlayer.motionX *= (mc.thePlayer.hurtTime / 100).toDouble()
                    mc.thePlayer.motionZ *= (mc.thePlayer.hurtTime / 100).toDouble()
                }
            }
            "seksinrekker" -> {
                if (velocityInput && velocityTimer.hasTimePassed(seksinrekker_timer_value.get().toLong())) {
                    if (onGroundOnly.get() && mc.thePlayer.onGround) {
                        mc.thePlayer.motionX *= reduce_seksinrekker_value.get()
                        mc.thePlayer.motionZ *= reduce_seksinrekker_value.get()
                        mc.thePlayer.motionY *= verticalValue.get()
                    } else if (!onGroundOnly.get()) {
                        mc.thePlayer.motionX *= reduce_seksinrekker_value.get()
                        mc.thePlayer.motionZ *= reduce_seksinrekker_value.get()
                        mc.thePlayer.motionY *= verticalValue.get()
                    }
                    velocityInput = false
                }
            }

            "lucky" -> {
                if (mc.thePlayer.onGround && mc.thePlayer.hurtTime > 0) {
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionY = luckyYReduceValue.get().toDouble()
                    mc.thePlayer.motionZ = 0.0
                }
            }

            "aacreduce" -> {
                if (mc.thePlayer.isCollidedHorizontally || !MovementUtils.isMoving()) {
                    count = 0
                    return
                }
                if (count > 0) {
                    if (velocityInput && velocityTimer.hasTimePassed(80L)) {
                        if (mc.thePlayer.hurtTime in aacreduce3_min_value.get()..aacreduce3_max_value.get()) {
                            count--
                            return
                        }
                    }
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.motionX *= aacreduce_ground_value.get()
                        mc.thePlayer.motionZ *= aacreduce_ground_value.get()
                    }else {
                        mc.thePlayer.motionX *= 0.6
                        mc.thePlayer.motionZ *= 0.6
                    }
                    count--
                }
            }

            "aacreduce2" -> {
                if (mc.thePlayer.isCollidedHorizontally || !MovementUtils.isMoving()) {
                    count = 0
                    return
                }
                if (count > 0) {
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.motionX *= aacreduce_ground_value.get()
                        mc.thePlayer.motionZ *= aacreduce_ground_value.get()
                    }else {
                        mc.thePlayer.motionX *= 0.6
                        mc.thePlayer.motionZ *= 0.6
                    }
                    count--
                }
            }

            "aacreduce3" -> {
                if (!MovementUtils.isMoving(true)) return
                if (mc.thePlayer.isCollidedHorizontally) {
                    count = 0
                    return
                }
                if (mc.thePlayer.hurtTime in aacreduce3_min_value.get()..aacreduce3_max_value.get()) {
                    return
                }

                if (count > 0) {
                    if (!mc.thePlayer.onGround) {
                        mc.thePlayer.motionX *= 0.6
                        mc.thePlayer.motionZ *= 0.6
                    }
                    count--
                }
            }

            "test2" -> {
                if (!MovementUtils.isMoving(true)) return
                if (mc.thePlayer.isCollidedHorizontally) {
                    count = 0
                    return
                }
                if (mc.thePlayer.hurtTime in aacreduce3_min_value.get()..aacreduce3_max_value.get()) {
                    return
                }
                if (count > 0) {
                    if (!isOnGround(0.2) && !left) {
                        if (mc.thePlayer.ticksExisted % 3 == 0) {
                            mc.thePlayer.motionY -= 0.0169 * mc.thePlayer.fallDistance
                        }
                        left = true
                    }
                    if (!mc.thePlayer.onGround) {
                        mc.thePlayer.motionX *= 0.6
                        mc.thePlayer.motionZ *= 0.6
                    }
                    count--
                }
            }

            "jump" -> {
                if (mc.thePlayer.hurtTime <= 0) {
                    return
                }
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.jump()
                    mc.thePlayer.jumpMovementFactor = 0F
                    val yaw = mc.thePlayer.rotationYaw * 0.017453292F
                    mc.thePlayer.motionX -= MathHelper.sin(yaw) * 0.2
                    mc.thePlayer.motionZ += MathHelper.cos(yaw) * 0.2
                } else {
                    mc.thePlayer.motionX *= horizontalValue.get()
                    mc.thePlayer.motionZ *= verticalValue.get()
                }
            }

            "glitch" -> {
                mc.thePlayer.noClip = velocityInput
                if (mc.thePlayer.hurtTime == 7)
                    mc.thePlayer.motionY = 0.4

                velocityInput = false
            }

            "reverse" -> {
                if (!velocityInput)
                    return

                if (!mc.thePlayer.onGround) {
                    MovementUtils.strafe(MovementUtils.getSpeed() * reverseStrengthValue.get())
                } else if (velocityTimer.hasTimePassed(80L))
                    velocityInput = false
            }

            "smoothreverse" -> {
                if (!velocityInput) {
                    mc.thePlayer.speedInAir = 0.02F
                    return
                }

                if (mc.thePlayer.hurtTime > 0)
                    reverseHurt = true

                if (!mc.thePlayer.onGround) {
                    if (reverseHurt)
                        mc.thePlayer.speedInAir = reverse2StrengthValue.get()
                } else if (velocityTimer.hasTimePassed(80L)) {
                    velocityInput = false
                    reverseHurt = false
                }
            }

            "aac" -> if (velocityInput && velocityTimer.hasTimePassed(80L)) {
                mc.thePlayer.motionX *= horizontalValue.get()
                mc.thePlayer.motionZ *= horizontalValue.get()
                //mc.thePlayer.motionY *= verticalValue.get() ?
                velocityInput = false
            }

            "aacpush" -> {
                if (jump) {
                    if (mc.thePlayer.onGround)
                        jump = false
                } else {
                    // Strafe
                    if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.motionX != 0.0 && mc.thePlayer.motionZ != 0.0)
                        mc.thePlayer.onGround = true

                    // Reduce Y
                    if (mc.thePlayer.hurtResistantTime > 0 && aacPushYReducerValue.get()
                        && !LiquidBounce.moduleManager.speed.state
                    )
                        mc.thePlayer.motionY -= 0.014999993
                }

                // Reduce XZ
                if (mc.thePlayer.hurtResistantTime >= 19) {
                    val reduce = aacPushXZReducerValue.get()

                    mc.thePlayer.motionX /= reduce
                    mc.thePlayer.motionZ /= reduce
                }
            }

            "aaczero" -> if (mc.thePlayer.hurtTime > 0) {
                if (!velocityInput || mc.thePlayer.onGround || mc.thePlayer.fallDistance > 2F)
                    return

                    mc.thePlayer.addVelocity(0.0, -1.0, 0.0)
                    mc.thePlayer.onGround = true
                } else {
                    velocityInput = false
                }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is S12PacketEntityVelocity) {
            if (mc.thePlayer == null || (mc.theWorld?.getEntityByID(packet.entityID) ?: return) != mc.thePlayer)
                return

            velocityTimer.reset()

            when (modeValue.get().toLowerCase()) {
                "simple" -> {
                    val horizontal = horizontalValue.get()
                    val vertical = verticalValue.get()

                    if (horizontal == 0F && vertical == 0F)
                        event.cancelEvent()

                    packet.motionX = (packet.getMotionX() * horizontal).toInt()
                    packet.motionY = (packet.getMotionY() * vertical).toInt()
                    packet.motionZ = (packet.getMotionZ() * horizontal).toInt()
                }

                "aac5.2.0" -> {
                    event.cancelEvent()
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX,1.7976931348623157E+308,mc.thePlayer.posZ,true))
                }


                "aac", "seksinrekker", "reverse", "smoothreverse", "aaczero" -> velocityInput = true

                "glitch" -> {
                    if (!mc.thePlayer.onGround)
                        return

                    velocityInput = true
                    event.cancelEvent()
                }
                "aacreduce" -> {
                    count = RandomUtils.nextInt(aacreduce_mincount_value.get(), aacreduce_maxcount_value.get())
                }

                "aacreduce2" -> {
                    if (!vanillaTimer.hasTimePassed(aacreduce2_timer_value.get().toLong())) {

                    }else {
                        hitstate = 0
                    }
                    if (hitstate >= aacreduce2_state_value.get()) {
                        hitstate = 0
                        count = 0
                        event.cancelEvent()
                    }else {
                        count = RandomUtils.nextInt(aacreduce_mincount_value.get(), aacreduce_maxcount_value.get())
                    }
                    hitstate++
                    vanillaTimer.reset()
                }
                "aacreduce3" -> {
                    count = RandomUtils.nextInt(aacreduce_mincount_value.get(), aacreduce_maxcount_value.get())
                    velocityInput = true
                }
                "test" -> {
                    velocityInput = true
                }

                "test2" -> {
                    count = RandomUtils.nextInt(aacreduce_mincount_value.get(), aacreduce_maxcount_value.get())
                    velocityInput = true
                    left = false
                }
                "test3" -> {
                    velocityInput = true
                    ticks = 0
                }
            }
        }

        if (packet is S27PacketExplosion) {
            // TODO: Support velocity for explosions
            event.cancelEvent()
        }
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        if (mc.thePlayer == null || mc.thePlayer.isInWater || mc.thePlayer.isInLava || mc.thePlayer.isInWeb)
            return

        when (modeValue.get().toLowerCase()) {
            "aacpush" -> {
                jump = true

                if (!mc.thePlayer.isCollidedVertically)
                    event.cancelEvent()
            }
            "aaczero" -> if (mc.thePlayer.hurtTime > 0)
                event.cancelEvent()
        }
    }

    private fun isOnGround(height : Double) : Boolean{
        val blockPos = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - height, mc.thePlayer.posZ)
        val block = BlockUtils.getBlock(blockPos) ?: return false
        return block !is BlockAir
    }

    fun onVelocityReduce() {
        when (modeValue.get().toLowerCase()) {
            "test" -> {
                if (!velocityInput) {
                    return
                }
                if (mc.thePlayer.hurtTime <= 2) {
                    velocityInput = false
                    return
                }
                if (mc.thePlayer.onGround) return
                mc.thePlayer.motionX *= 0.6
                mc.thePlayer.motionZ *= 0.6
            }
        }
    }
}
