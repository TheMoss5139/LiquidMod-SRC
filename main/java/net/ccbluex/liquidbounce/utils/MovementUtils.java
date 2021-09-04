/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.utils;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.player.PerspectiveMod;
import net.minecraft.util.MathHelper;

public final class MovementUtils extends MinecraftInstance {

    public static float getSpeed() {
        return (float) Math.sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX + mc.thePlayer.motionZ * mc.thePlayer.motionZ);
    }

    public static void strafe() {
        strafe(getSpeed());
    }

    public static boolean isMoving() {
        return mc.thePlayer != null && (mc.thePlayer.movementInput.moveForward != 0F || mc.thePlayer.movementInput.moveStrafe != 0F);
    }

    public static boolean isForward() {
        return mc.thePlayer != null && (mc.thePlayer.movementInput.moveForward != 0F);
    }

    public static boolean isStrafe() {
        return mc.thePlayer != null && (mc.thePlayer.movementInput.moveStrafe != 0F);
    }

    public static boolean isMoving(boolean motioncheck) {
        if (mc.thePlayer == null) {
            return false;
        }

        if (Math.abs(mc.thePlayer.movementInput.moveForward) < 0.8F && Math.abs(mc.thePlayer.movementInput.moveStrafe) < 0.8F) {
            return false;
        }
        return true;
    }

    public static boolean isMoving2() {
        return mc.thePlayer != null && getSpeed() > 0;
    }

    public static boolean hasMotion() {
        return mc.thePlayer.motionX != 0D && mc.thePlayer.motionZ != 0D && mc.thePlayer.motionY != 0D;
    }

    public static void strafe(final float speed) {
        if (!isMoving())
            return;

        final double yaw = getDirection();
        mc.thePlayer.motionX = -Math.sin(yaw) * speed;
        mc.thePlayer.motionZ = Math.cos(yaw) * speed;
    }

    public static void forward(final double length) {
        final double yaw = Math.toRadians(mc.thePlayer.rotationYaw);
        mc.thePlayer.setPosition(mc.thePlayer.posX + (-Math.sin(yaw) * length), mc.thePlayer.posY, mc.thePlayer.posZ + (Math.cos(yaw) * length));
    }

    public static double getDirection() {
        PerspectiveMod perspectiveMod = LiquidBounce.moduleManager.getPerspectiveMod();
        float rotationYaw = perspectiveMod.getState() && perspectiveMod.getPerspectiveyaw() != null ? perspectiveMod.getPerspectiveyaw() : mc.thePlayer.rotationYaw;

        if (mc.thePlayer.moveForward < 0F)
            rotationYaw += 180F;

        float forward = 1F;
        if (mc.thePlayer.moveForward < 0F)
            forward = -0.5F;
        else if (mc.thePlayer.moveForward > 0F)
            forward = 0.5F;

        if (mc.thePlayer.moveStrafing > 0F)
            rotationYaw -= 90F * forward;

        if (mc.thePlayer.moveStrafing < 0F)
            rotationYaw += 90F * forward;

        return Math.toRadians(rotationYaw);
    }

    public static double getDirection(float addedYaw) {
        PerspectiveMod perspectiveMod = LiquidBounce.moduleManager.getPerspectiveMod();
        float rotationYaw = perspectiveMod.getState() && perspectiveMod.getPerspectiveyaw() != null ? perspectiveMod.getPerspectiveyaw() : mc.thePlayer.rotationYaw;
        rotationYaw += addedYaw;
        if (mc.thePlayer.moveForward < 0F)
            rotationYaw += 180F;

        float forward = 1F;
        if (mc.thePlayer.moveForward < 0F)
            forward = -0.5F;
        else if (mc.thePlayer.moveForward > 0F)
            forward = 0.5F;

        if (mc.thePlayer.moveStrafing > 0F)
            rotationYaw -= 90F * forward;

        if (mc.thePlayer.moveStrafing < 0F)
            rotationYaw += 90F * forward;

        return Math.toRadians(rotationYaw);
    }

    public static double StrafeRotation(float addedYaw) {
        float yaw = (float)(getDirection(addedYaw) * 180f / Math.PI);
        int dif = MathHelper.floor_float((MathHelper.wrapAngleTo180_float(0 - yaw
                - 23.5f - 135)
                + 180) / 45);
        float rotationYaw = 0;
        rotationYaw += dif * -45;
        rotationYaw = MathHelper.wrapAngleTo180_float(rotationYaw);
        return Math.toRadians(rotationYaw);
    }

    public static double CustomRotation(float yaw) {
        int dif = MathHelper.floor_float((MathHelper.wrapAngleTo180_float(0 - yaw
                - 23.5f - 135)
                + 180) / 45);
        float rotationYaw = 0;
        rotationYaw += dif * -15;
        rotationYaw = MathHelper.wrapAngleTo180_float(rotationYaw);
        return Math.toRadians(rotationYaw);
    }
}