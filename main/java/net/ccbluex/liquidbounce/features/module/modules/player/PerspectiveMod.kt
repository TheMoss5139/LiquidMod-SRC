package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.TextValue
import net.minecraft.util.MathHelper
import org.lwjgl.input.Keyboard

@ModuleInfo(name = "PerspectiveMod", description = "The ThridPerson Mod From Cynosphere https://github.com/BackportProjectMC/PerspectiveModRedux", category = ModuleCategory.PLAYER)
class PerspectiveMod : Module() {
    private val keybind = TextValue("KeyBind", "LCONTROL")
    private val mode = ListValue("Mode", arrayOf("Hold", "Toggle"), "Toggle")
    private val thridpersonvalue = IntegerValue("ThridPersonValue", 1, 0, 2)
    open var Perspectiveyaw : Float? = null
    open var Perspectivepitch : Float? = null
    //Toggle_Enabled = 0, Toggle_Disabled = 1
    var toggle_hold_int = 1
    var perspectiveview = -1

    override fun onEnable() {
        super.onEnable()
        when (mode.get().toLowerCase()) {
            "toggle" -> {
                toggle_hold_int = 1
            }
        }
    }

    override fun onDisable() {
        super.onDisable()
        if (Perspectiveyaw != null) {
            mc.thePlayer.rotationYaw = Perspectiveyaw!!.toFloat()
            Perspectiveyaw = null
        }

        if (Perspectivepitch != null) {
            mc.thePlayer.rotationPitch = Perspectivepitch!!.toFloat()
            Perspectivepitch = null
        }
        toggle_hold_int = 1
        //SetViewBack
        if (perspectiveview != -1) {
            mc.gameSettings.thirdPersonView = perspectiveview
        }
        perspectiveview = -1
    }

    @EventTarget
    fun onKey(event: KeyEvent) {
        if (Keyboard.getKeyIndex(keybind.get()) == event.key) {
            when (mode.get().toLowerCase()) {
                "toggle" -> {
                    when (toggle_hold_int) {
                        0 -> {
                            //Disabled
                            if (Perspectiveyaw != null) {
                                mc.thePlayer.rotationYaw = Perspectiveyaw!!.toFloat()
                                Perspectiveyaw = null
                            }

                            if (Perspectivepitch != null) {
                                mc.thePlayer.rotationPitch = Perspectivepitch!!.toFloat()
                                Perspectivepitch = null
                            }
                            toggle_hold_int = 1
                            //SetViewBack
                            if (perspectiveview != -1) {
                                mc.gameSettings.thirdPersonView = perspectiveview
                            }
                            perspectiveview = -1
                        }

                        1 -> {
                            //Enabled
                            if (Perspectiveyaw == null || Perspectivepitch == null) {
                                Perspectiveyaw = mc.thePlayer.rotationYaw
                                Perspectivepitch = mc.thePlayer.rotationPitch
                            }
                            toggle_hold_int = 0
                            //GetViewValue
                            perspectiveview = mc.gameSettings.thirdPersonView
                            //SetToPersonView
                            mc.gameSettings.thirdPersonView = thridpersonvalue.get()
                        }
                    }
                }

                "hold" -> {
                    //onHold
                    if (Perspectiveyaw == null || Perspectivepitch == null) {
                        Perspectiveyaw = mc.thePlayer.rotationYaw
                        Perspectivepitch = mc.thePlayer.rotationPitch
                    }
                    //GetView(onHold) -> onetime
                    if (perspectiveview == -1) {
                        perspectiveview = mc.gameSettings.thirdPersonView
                    }
                }
            }
        }
    }

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        if (Perspectiveyaw == null)
            return
        val killAura = LiquidBounce.moduleManager.killAura
        if (killAura.state && killAura.target != null) return

        val yaw = Perspectiveyaw!!.toFloat()
        var strafe = event.strafe
        var forward = event.forward
        val friction = event.friction

        var f = strafe * strafe + forward * forward

        if (f >= 1.0E-4F) {
            f = MathHelper.sqrt_float(f)

            if (f < 1.0F)
                f = 1.0F

            f = friction / f
            strafe *= f
            forward *= f

            val yawSin = MathHelper.sin((yaw * Math.PI / 180F).toFloat())
            val yawCos = MathHelper.cos((yaw * Math.PI / 180F).toFloat())

            mc.thePlayer.motionX += strafe * yawCos - forward * yawSin
            mc.thePlayer.motionZ += forward * yawCos + strafe * yawSin
        }
        event.cancelEvent()
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        if (Perspectiveyaw == null)
            return

        val killAura = LiquidBounce.moduleManager.killAura
        if (killAura.state) return
        event.yaw = Perspectiveyaw!!.toFloat()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (Perspectiveyaw == null)
            return
        val killAura = LiquidBounce.moduleManager.killAura
        if (toggle_hold_int == 0) {
            if (killAura.state && killAura.target != null) return
            RotationUtils.targetRotation = Rotation(Perspectiveyaw!!.toFloat(), Perspectivepitch!!.toFloat())
        }else if (mode.get().equals("Hold", ignoreCase = true)) {
            if (Keyboard.isKeyDown(Keyboard.getKeyIndex(keybind.get()))) {
                //Holding Key
                mc.gameSettings.thirdPersonView = thridpersonvalue.get()
                if (killAura.state && killAura.target != null) return
                RotationUtils.targetRotation = Rotation(Perspectiveyaw!!.toFloat(), Perspectivepitch!!.toFloat())
            }else {
                //NotHolding Key
                if (Perspectiveyaw != null) {
                    mc.thePlayer.rotationYaw = Perspectiveyaw!!.toFloat()
                    Perspectiveyaw = null
                }

                if (Perspectivepitch != null) {
                    mc.thePlayer.rotationPitch = Perspectivepitch!!.toFloat()
                    Perspectivepitch = null
                }
                toggle_hold_int = 1
                //SetViewBack
                if (perspectiveview != -1) {
                    mc.gameSettings.thirdPersonView = perspectiveview
                }
                perspectiveview = -1
            }
        }
    }
}