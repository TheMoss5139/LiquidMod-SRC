package net.ccbluex.liquidbounce.injection.forge.mixins.network;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(NetHandlerPlayServer.class)
public abstract class MixinNetHandlerPlayServer {
    @Shadow
    @Final
    private MinecraftServer serverController;
    @Shadow
    public EntityPlayerMP playerEntity;

    @Overwrite
    public void processUseEntity(C02PacketUseEntity p_processUseEntity_1_) {
        PacketThreadUtil.checkThreadAndEnqueue(p_processUseEntity_1_, (NetHandlerPlayServer)((Object)this), this.playerEntity.getServerForPlayer());
        WorldServer worldserver = this.serverController.worldServerForDimension(this.playerEntity.dimension);
        Entity entity = p_processUseEntity_1_.getEntityFromWorld(worldserver);
        this.playerEntity.markPlayerActive();
        if (entity != null) {
            boolean flag = this.playerEntity.canEntityBeSeen(entity);
            double d0 = 36.0D;
            if (!flag) {
                d0 = 9.0D;
            }

            if (this.playerEntity.getDistanceSqToEntity(entity) < d0) {
                if (p_processUseEntity_1_.getAction() == net.minecraft.network.play.client.C02PacketUseEntity.Action.INTERACT) {
                    this.playerEntity.interactWith(entity);
                } else if (p_processUseEntity_1_.getAction() == net.minecraft.network.play.client.C02PacketUseEntity.Action.INTERACT_AT) {
                    entity.interactAt(this.playerEntity, p_processUseEntity_1_.getHitVec());
                } else if (p_processUseEntity_1_.getAction() == net.minecraft.network.play.client.C02PacketUseEntity.Action.ATTACK) {
                    if (entity instanceof EntityItem || entity instanceof EntityXPOrb || entity instanceof EntityArrow) {
                        this.kickPlayerFromServer("Attempting to attack an invalid entity");
                        this.serverController.logWarning("Player " + this.playerEntity.getName() + " tried to attack an invalid entity");
                        return;
                    }

                    this.playerEntity.attackTargetEntityWithCurrentItem(entity);
                }
            }
        }
    }

    @Shadow
    public abstract void kickPlayerFromServer(String p_kickPlayerFromServer_1_);
}
