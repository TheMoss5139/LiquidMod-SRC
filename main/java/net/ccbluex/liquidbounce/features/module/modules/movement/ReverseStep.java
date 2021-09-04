/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement;

import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.JumpEvent;
import net.ccbluex.liquidbounce.event.UpdateEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.utils.block.BlockUtils;
import net.ccbluex.liquidbounce.value.FloatValue;
import net.ccbluex.liquidbounce.value.ListValue;
import net.minecraft.block.BlockLiquid;
import net.minecraft.util.AxisAlignedBB;

@ModuleInfo(name = "ReverseStep", description = "Allows you to step down blocks faster.", category = ModuleCategory.MOVEMENT)
public class ReverseStep extends Module {

    private final ListValue modeValue = new ListValue("Mode", new String[] {"Motion", "Timer"}, "Motion");
    private final FloatValue motionValue = new FloatValue("Motion", 1F, 0.21F, 1F);

    private boolean jumped;

    @EventTarget(ignoreCondition = true)
    public void onUpdate(UpdateEvent event) {
        if (!getState()) return;
        if(mc.thePlayer.onGround) {
            switch (modeValue.get().toLowerCase()) {
                case "Timer" :
                    mc.timer.timerSpeed = 1F;
                    break;
            }
            jumped = false;
        }

        if(mc.thePlayer.motionY > 0) {
            jumped = true;
        }

        if(BlockUtils.collideBlock(mc.thePlayer.getEntityBoundingBox(), block -> block instanceof BlockLiquid) || BlockUtils.collideBlock(new AxisAlignedBB(mc.thePlayer.getEntityBoundingBox().maxX, mc.thePlayer.getEntityBoundingBox().maxY, mc.thePlayer.getEntityBoundingBox().maxZ, mc.thePlayer.getEntityBoundingBox().minX, mc.thePlayer.getEntityBoundingBox().minY - 0.01D, mc.thePlayer.getEntityBoundingBox().minZ), block -> block instanceof BlockLiquid)) {
            if (modeValue.get().equalsIgnoreCase("Timer"))
                mc.timer.timerSpeed = 1F;
            return;
        }

        if (mc.thePlayer.fallDistance > 0 && !jumped && !mc.thePlayer.onGround) {
            switch (modeValue.get().toLowerCase()) {
                case "Timer" :
                    mc.timer.timerSpeed = 3.5F;
                    break;
            }
        }

        if(!mc.gameSettings.keyBindJump.isKeyDown() && !mc.thePlayer.onGround && !mc.thePlayer.movementInput.jump && mc.thePlayer.motionY <= 0D && mc.thePlayer.fallDistance <= 1F && !jumped) {
            switch (modeValue.get().toLowerCase()) {
                case "motion" :
                    mc.thePlayer.motionY = -motionValue.get();
                    break;
            }
        }
    }

    @EventTarget(ignoreCondition = true)
    public void onJump(JumpEvent event) {
        jumped = true;
    }
}