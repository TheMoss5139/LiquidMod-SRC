package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.aac;

import net.ccbluex.liquidbounce.event.MoveEvent;
import net.ccbluex.liquidbounce.event.StrafeEvent;
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode;

public class AACStrafe extends SpeedMode {
    public AACStrafe() {
        super("AACStrafe");
    }

    @Override
    public void onMotion() {

    }

    @Override
    public void onUpdate() {
        mc.timer.timerSpeed = 1f;
        mc.thePlayer.speedInAir = 0.02F;
        if (mc.thePlayer.onGround) mc.thePlayer.jump();
        if (mc.thePlayer.motionY >= 0) return;
        mc.timer.timerSpeed = 0.5f;
        mc.thePlayer.motionY += 0.042;
        mc.thePlayer.speedInAir = 0.021F;
    }

    @Override
    public void onMove(MoveEvent event) {

    }
}
