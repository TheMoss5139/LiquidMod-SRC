package net.ccbluex.liquidbounce.features.module.modules.combat;

import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.UpdateEvent;
import net.ccbluex.liquidbounce.event.WorldEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.value.FloatValue;
import net.ccbluex.liquidbounce.value.IntegerValue;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import java.util.ArrayList;

@ModuleInfo(name = "AuraTeams", description = "Add Teams Near You", category = ModuleCategory.COMBAT)
public class AuraTeams extends Module {
    public FloatValue range = new FloatValue("Range", 3.5F, 1.0F, 6.0F);
    public IntegerValue maxplayer = new IntegerValue("MaxPlayers", 2, 1, 6);
    public ArrayList<EntityPlayer> players = new ArrayList<EntityPlayer>();
    public boolean isDead = false;
    @Override
    public void onEnable() {
        super.onEnable();
        players.clear();
        isDead = false;
        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (player instanceof EntityPlayerSP) {
                continue;
            }
            if (mc.thePlayer.getDistanceToEntity(player) <= range.get()) {
                players.add(player);
            }
            if (players.size() >= maxplayer.get()) {
                break;
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        players.clear();
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (!mc.thePlayer.isDead) return;
        isDead = true;
    }

    @EventTarget
    public void onWorld(WorldEvent event) {
        WorldClient world = event.getWorldClient();
        if (isDead) {
            isDead = false;
            return;
        }
        players.clear();
        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (player instanceof EntityPlayerSP) {
                continue;
            }
            if (mc.thePlayer.getDistanceToEntity(player) <= range.get()) {
                players.add(player);
            }
            if (players.size() >= maxplayer.get()) {
                break;
            }
        }
    }
}
