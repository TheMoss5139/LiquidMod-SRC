package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.seksin;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.MoveEvent;
import net.ccbluex.liquidbounce.event.StrafeEvent;
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed;
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode;
import net.ccbluex.liquidbounce.utils.MovementUtils;
import net.ccbluex.liquidbounce.utils.Rotation;
import net.ccbluex.liquidbounce.utils.RotationUtils;
import net.ccbluex.liquidbounce.utils.timer.MSTimer;
import net.minecraft.util.MathHelper;

public class SeksinSpeed extends SpeedMode {

    public SeksinSpeed() {
        super("SeksinSpeed");
    }

    @Override
    public void onMotion() {

    }

    @Override
    public void onUpdate() {
        if (MovementUtils.isMoving()) {
            final Speed speed = LiquidBounce.moduleManager.getSpeed();
            if (mc.thePlayer.onGround) {
                mc.timer.timerSpeed = speed.Seksin2Timer.get();
                mc.thePlayer.jump();
            }else {
                mc.timer.timerSpeed = 0.5F;
            }
        }else {
            mc.timer.timerSpeed = 1F;
        }
    }

    @Override
    public void onMove(MoveEvent event) {

    }

    @Override
    public void onStrafe(StrafeEvent event) {
        float yaw = 0F;
        Rotation rotation = RotationUtils.targetRotation;
        if (rotation == null) {
            yaw = mc.thePlayer.rotationYaw;
        }else {
            yaw = rotation.getYaw();
        }
        float strafe = event.getStrafe();
        float forward = event.getForward();
        float friction = event.getFriction();

        float f = strafe * strafe + forward * forward;

        if (f >= 1.0E-4F) {
            final Speed speed = LiquidBounce.moduleManager.getSpeed();
            f = MathHelper.sqrt_float(f);

            if (f < 1.0F)
                f = 1.0F;

            f = friction / f;
            strafe *= f;
            forward *= f;
            forward += speed.Seksin2Speed.get() * (forward > 0 ? 1 : (forward < 0 ? -1 : 0));
            strafe += speed.Seksin2Speed.get() * (strafe > 0 ? 1 : (strafe < 0 ? -1 : 0));

            double yawSin = MathHelper.sin((float)(yaw * Math.PI / 180F));
            double yawCos = MathHelper.cos((float)(yaw * Math.PI / 180F));

            mc.thePlayer.motionX += strafe * yawCos - forward * yawSin;
            mc.thePlayer.motionZ += forward * yawCos + strafe * yawSin;
            event.cancelEvent();
        }
    }
}
