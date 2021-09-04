/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.*;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.utils.MovementUtils;
import net.ccbluex.liquidbounce.utils.Rotation;
import net.ccbluex.liquidbounce.utils.RotationUtils;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.ccbluex.liquidbounce.value.FloatValue;
import net.ccbluex.liquidbounce.value.ListValue;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;

@ModuleInfo(name = "LongJump", description = "Allows you to jump further.", category = ModuleCategory.MOVEMENT)
public class LongJump extends Module {

    private final ListValue modeValue = new ListValue("Mode", new String[] {"NCP", "AACv1", "AACv2", "AACv3", "AACSeksin", "Seksin2", "Seksin3", "Mineplex", "Mineplex2", "Mineplex3"}, "NCP");
    private final FloatValue ncpBoostValue = new FloatValue("NCPBoost", 4.25F, 1F, 10F);
    private final FloatValue ss_speedBoost = new FloatValue("SS_SpeedBoost", 0.6F, 0.1F, 5F);
    private final FloatValue ss_timerBoost = new FloatValue("SS_TimerBoost", 2.75F, 0.1F, 5F);
    private final FloatValue ss_speed = new FloatValue("SS_Speed", 0.75F, 0.1F, 5F);
    private final FloatValue ss_timer = new FloatValue("SS_Timer", 0.75F, 0.1F, 5F);
    private final FloatValue ss_motionY = new FloatValue("SS_MotionY", 0.75F, 0.1F, 5F);
    private final FloatValue ss3_motionY = new FloatValue("SS3_MotionY", 0.75F, 0.1F, 5F);
    private final FloatValue ss3_speed = new FloatValue("SS3_Speed", 0.05F, 0F, 1F);
    private final FloatValue ss3_timerBoost = new FloatValue("SS3_TimerBoost", 2.75F, 0.1F, 5F);

    private final BoolValue autoJumpValue = new BoolValue("AutoJump", false);
    private final BoolValue autoDisableValue = new BoolValue("AutoDisable", false);

    private boolean jumped;
    private boolean canBoost;
    private boolean teleported;
    private boolean canMineplexBoost;

    @Override
    public void onEnable() {
        super.onEnable();
        jumped = false;
        canMineplexBoost = false;
        canBoost = false;
        teleported = false;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        mc.timer.timerSpeed = 1F;
    }

    @EventTarget
    public void onUpdate(final UpdateEvent event) {
        if(LadderJump.jumped)
            MovementUtils.strafe(MovementUtils.getSpeed() * 1.08F);

        if (autoDisableValue.get() && jumped && mc.thePlayer.onGround) {
            this.toggle();
            return;
        }
        if(jumped) {
            final String mode = modeValue.get();

            if (mc.thePlayer.onGround || mc.thePlayer.capabilities.isFlying) {
                jumped = false;
                canMineplexBoost = false;

                if (mode.equalsIgnoreCase("NCP")) {
                    mc.thePlayer.motionX = 0;
                    mc.thePlayer.motionZ = 0;
                }
                return;
            }

            switch (mode.toLowerCase()) {
                case "ncp":
                    MovementUtils.strafe(MovementUtils.getSpeed() * (canBoost ? ncpBoostValue.get() : 1F));
                    canBoost = false;
                    break;
                case "seksin2":
                    if (MovementUtils.isMoving()) {
                        mc.thePlayer.motionY += ss_motionY.get();
                    }
                    MovementUtils.strafe(canBoost ? ss_speedBoost.get() : ss_speed.get());
                    mc.timer.timerSpeed = canBoost ? ss_timerBoost.get() : ss_timer.get();
                    canBoost = false;
                    break;
                case "seksin3":
                    if (MovementUtils.isMoving()) {
                        mc.thePlayer.motionY += ss3_motionY.get();
                    }
                    mc.timer.timerSpeed = canBoost ? ss3_timerBoost.get() : 1F;
                    canBoost = false;
                    break;
                case "aacseksin":
                    if (MovementUtils.isMoving()) {
                        mc.thePlayer.motionY += 0.042;
                    }
                    MovementUtils.strafe(canBoost ? 0.6F : 0.75F);
                    mc.timer.timerSpeed = canBoost ? 1.75F : 0.5F;
                    canBoost = false;
                    break;
                case "aacv1":
                    mc.thePlayer.motionY += 0.05999D;
                    MovementUtils.strafe(MovementUtils.getSpeed() * 1.08F);
                    break;
                case "aacv2":
                case "mineplex3":
                    mc.thePlayer.jumpMovementFactor = 0.09F;
                    mc.thePlayer.motionY += 0.0132099999999999999999999999999;
                    mc.thePlayer.jumpMovementFactor = 0.08F;
                    MovementUtils.strafe();
                    break;
                case "aacv3":
                    final EntityPlayerSP player = mc.thePlayer;

                    if(player.fallDistance > 0.5F && !teleported) {
                        double value = 3;
                        EnumFacing horizontalFacing = player.getHorizontalFacing();
                        double x = 0;
                        double z = 0;
                        switch(horizontalFacing) {
                            case NORTH:
                                z = -value;
                                break;
                            case EAST:
                                x = +value;
                                break;
                            case SOUTH:
                                z = +value;
                                break;
                            case WEST:
                                x = -value;
                                break;
                        }

                        player.setPosition(player.posX + x, player.posY, player.posZ + z);
                        teleported = true;
                    }
                    break;
                case "mineplex":
                    mc.thePlayer.motionY += 0.0132099999999999999999999999999;
                    mc.thePlayer.jumpMovementFactor = 0.08F;
                    MovementUtils.strafe();
                    break;
                case "mineplex2":
                    if(!canMineplexBoost)
                        break;

                    mc.thePlayer.jumpMovementFactor = 0.1F;

                    if(mc.thePlayer.fallDistance > 1.5F) {
                        mc.thePlayer.jumpMovementFactor = 0F;
                        mc.thePlayer.motionY = -10F;
                    }
                    MovementUtils.strafe();
                    break;
            }
        }

        if(autoJumpValue.get() && mc.thePlayer.onGround && MovementUtils.isMoving()) {
            jumped = true;
            mc.thePlayer.jump();
        }
    }

    @EventTarget
    public void onMove(final MoveEvent event) {
        final String mode = modeValue.get();

        if (mode.equalsIgnoreCase("mineplex3")) {
            if(mc.thePlayer.fallDistance != 0)
                mc.thePlayer.motionY += 0.037;
        } else if (mode.equalsIgnoreCase("ncp") && !MovementUtils.isMoving() && jumped) {
            mc.thePlayer.motionX = 0;
            mc.thePlayer.motionZ = 0;
            event.zeroXZ();
        }
    }

    @EventTarget
    public void onStrafe(StrafeEvent event) {
        if (!modeValue.get().equalsIgnoreCase("Seksin3"))
            return;
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
            f = MathHelper.sqrt_float(f);

            if (f < 1.0F)
                f = 1.0F;

            f = friction / f;
            strafe *= f;
            forward *= f;
            forward += ss3_speed.get();

            double yawSin = MathHelper.sin((float)(yaw * Math.PI / 180F));
            double yawCos = MathHelper.cos((float)(yaw * Math.PI / 180F));

            mc.thePlayer.motionX += strafe * yawCos - forward * yawSin;
            mc.thePlayer.motionZ += forward * yawCos + strafe * yawSin;
            event.cancelEvent();
        }
    }

    @EventTarget()
    public void onJump(final JumpEvent event) {
        jumped = true;
        canBoost = true;
        teleported = false;

        if(getState()) {
            switch(modeValue.get().toLowerCase()) {
                case "mineplex":
                    event.setMotion(event.getMotion() * 4.08f);
                    break;
                case "mineplex2":
                    if(mc.thePlayer.isCollidedHorizontally) {
                        event.setMotion(2.31f);
                        canMineplexBoost = true;
                        mc.thePlayer.onGround = false;
                    }
                    break;
            }
        }

    }

    @Override
    public String getTag() {
        return modeValue.get();
    }
}
