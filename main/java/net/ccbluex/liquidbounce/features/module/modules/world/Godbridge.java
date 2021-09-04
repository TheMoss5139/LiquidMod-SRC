package net.ccbluex.liquidbounce.features.module.modules.world;

import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.UpdateEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.utils.InventoryUtils;
import net.ccbluex.liquidbounce.utils.timer.TimeUtils;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

@ModuleInfo(name = "Godbridge",  description = "You know What it's", category = ModuleCategory.WORLD)
public class Godbridge extends Module {
    private final BoolValue swingValue = new BoolValue("Swing", true);
    @EventTarget
    public void onUpdate(final UpdateEvent event) {
        if (!mc.gameSettings.keyBindUseItem.isKeyDown())
            return;
        MovingObjectPosition movingObjectPosition = mc.objectMouseOver;
        if (movingObjectPosition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            BlockPos pos = movingObjectPosition.getBlockPos();
            if ((int)mc.thePlayer.posY - 1 == pos.getY()) {
                EnumFacing facing = movingObjectPosition.sideHit;
                Vec3 vec = movingObjectPosition.hitVec;
                if (facing != EnumFacing.UP && checkhelditem()) {
                    place(pos, facing, vec);
                }
            }
        }
    }

    private boolean checkhelditem() {
        if (mc.thePlayer.getHeldItem() != null) {
            ItemStack itemStack = mc.thePlayer.getHeldItem();
            Item item = itemStack.getItem();
            if (!(item instanceof ItemBlock)) {
                return false;
            }
            final Block block = ((ItemBlock) itemStack.getItem()).getBlock();
            boolean isblacklist = InventoryUtils.BLOCK_BLACKLIST.contains(block);
            if (!isblacklist) {
                return true;
            }
        }
        return false;
    }

    private void place(BlockPos blockPos, EnumFacing facing, Vec3 vec) {
        ItemStack itemStack = mc.thePlayer.getHeldItem();
        if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, itemStack, blockPos, facing, vec)) {
            if (swingValue.get())
                mc.thePlayer.swingItem();
            else
                mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
        }
    }
}
