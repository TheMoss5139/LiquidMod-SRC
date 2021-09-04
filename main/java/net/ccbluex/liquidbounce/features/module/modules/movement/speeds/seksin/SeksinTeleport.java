package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.seksin;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.MoveEvent;
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed;
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode;
import net.ccbluex.liquidbounce.utils.MovementUtils;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class SeksinTeleport extends SpeedMode {

    public SeksinTeleport() {
        super("SeksinTeleport");
    }

    @Override
    public void onMotion() {
        float f4 = 0.91F;
        if (mc.thePlayer.onGround) {
            f4 = mc.theWorld.getBlockState(new BlockPos(MathHelper.floor_double(mc.thePlayer.posX), MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minY) - 1, MathHelper.floor_double(mc.thePlayer.posZ))).getBlock().slipperiness * 0.91F;
        }

        float f = 0.16277136F / (f4 * f4 * f4);
        float f5;
        if (mc.thePlayer.onGround) {
            f5 = mc.thePlayer.getAIMoveSpeed() * f;
        } else {
            f5 = mc.thePlayer.jumpMovementFactor;
        }
        Speed speed = LiquidBounce.moduleManager.getSpeed();
        double posX = mc.thePlayer.posX;
        double posY = mc.thePlayer.posY;
        double posZ = mc.thePlayer.posZ;
        for (int i = 0; i < speed.SeksinTeleportTick.get(); i++) {
            Vec3 vec = test01(posX, posY, posZ, mc.thePlayer.moveForward, mc.thePlayer.moveStrafing, f5, mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ, mc.thePlayer.rotationYaw);
            posX = vec.xCoord;
            posY = vec.yCoord;
            posZ = vec.zCoord;
            mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(posX, posY, posZ, mc.thePlayer.onGround));
        }
        mc.thePlayer.setPosition(posX, posY, posZ);
    }

    @Override
    public void onUpdate() {
    }

    @Override
    public void onMove(MoveEvent event) {
    }

    public Vec3 test01(double x, double y, double z, float forward, float strafe, float friction, double motionX, double motionY, double motionZ, float yaw) {
        float v = strafe * strafe + forward * forward;

        if (v >= 0.0001f) {
            v = MathHelper.sqrt_float(v);

            if (v < 1.0F) {
                v = 1.0F;
            }
            v = friction / v;
            strafe = strafe * v;
            forward = forward * v;
            float f1 = MathHelper.sin(yaw * (float) Math.PI / 180.0F);
            float f2 = MathHelper.cos(yaw * (float) Math.PI / 180.0F);
            motionX += strafe * f2 - forward * f1;
            motionZ += forward * f2 + strafe * f1;
        }

        if (!mc.thePlayer.onGround) {
            motionY -= 0.08;
        }

        motionX *= 0.91;
        if (!mc.thePlayer.onGround) {
            motionY *= 0.9800000190734863D;
            motionY *= 0.91;
        }
        motionZ *= 0.91;

        x += motionX;
        if (!mc.thePlayer.onGround) {
            y += motionY;
        }
        z += motionZ;
        return new Vec3(x, y, z);
    }
}
