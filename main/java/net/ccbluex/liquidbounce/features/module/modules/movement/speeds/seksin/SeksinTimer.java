/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.seksin;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.MoveEvent;
import net.ccbluex.liquidbounce.event.StrafeEvent;
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed;
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode;
import net.ccbluex.liquidbounce.utils.MovementUtils;
import net.ccbluex.liquidbounce.utils.timer.MSTimer;

public class SeksinTimer extends SpeedMode {

    public MSTimer timer = new MSTimer();

    public SeksinTimer() {
        super("SeksinTimer");
    }

    @Override
    public void onMotion() {

    }

    @Override
    public void onUpdate() {
        if (MovementUtils.isMoving()) {
            final Speed speed = LiquidBounce.moduleManager.getSpeed();
            if (timer.hasTimePassed(speed.SeksinDelayValue.get())) {
                //Toggle Timer
                timer.reset();
                mc.timer.timerSpeed = speed.SeksinTimerValue.get();
            }else {
                mc.timer.timerSpeed = 1F;
            }
        }else {
            timer.reset();
            mc.timer.timerSpeed = 1F;
        }
    }

    @Override
    public void onMove(MoveEvent event) {

    }

    @Override
    public void onStrafe(StrafeEvent event) {

    }
}