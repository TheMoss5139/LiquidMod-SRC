package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.misc.AntiBot
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.client.C02PacketUseEntity

@ModuleInfo(name = "MacroPacket", description = "Spamming Packet.", category = ModuleCategory.COMBAT)
class MacroPacket : Module() {
    val rangeValue = FloatValue("Range", 3.7f, 1f, 8f)
    val packetcount = IntegerValue("PacketCount", 40, 1, 100)
    val onetime = BoolValue("OneTime", false)
    var target: EntityLivingBase? = null
    var domore = false;
    override fun onEnable() {
        super.onEnable()
        domore = true
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        target = updateTarget()
        //Update Rotations
        if (target != null && domore) {
            var boundingBox = target!!.entityBoundingBox
            val (vec, rotation) = RotationUtils.searchCenter(
                    boundingBox,
                    false,
                    false,
                    false,
                    mc.thePlayer.getDistanceToEntityBox(target!!) < rangeValue.get(),
                    rangeValue.get()
            )
            RotationUtils.setTargetRotation(rotation, 15)

            // Attack target
            mc.thePlayer.swingItem()
            var i = 0
            while (i < packetcount.get()) {
                mc.netHandler.addToSendQueue(C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK))
                i++
            }
            if (onetime.get())
            domore = false
        }
    }

    fun updateTarget() : EntityLivingBase? {
        // Find possible targets
        val targets = mutableListOf<EntityLivingBase>()

        for (entity in mc.theWorld.loadedEntityList) {
            if (entity !is EntityLivingBase || !isEnemy(entity))
                continue

            val distance = mc.thePlayer.getDistanceToEntityBox(entity)
            val entityFov = RotationUtils.getRotationDifference(entity)

            if (distance <= rangeValue.get())
                targets.add(entity)
        }
        if (targets.isEmpty()) {
            return null
        }
        targets.sortBy { mc.thePlayer.getDistanceToEntityBox(it)}
        return targets.get(0)
    }

    /**
     * Check if [entity] is selected as enemy with current target options and other modules
     */
    private fun isEnemy(entity: Entity?): Boolean {
        if (entity is EntityLivingBase && (EntityUtils.targetDead || isAlive(entity)) && entity != mc.thePlayer) {
            if (!EntityUtils.targetInvisible && entity.isInvisible())
                return false

            if (EntityUtils.targetPlayer && entity is EntityPlayer) {
                if (entity.isSpectator || AntiBot.isBot(entity))
                    return false

                if (EntityUtils.isFriend(entity) && !LiquidBounce.moduleManager.noFriends.state)
                    return false

                val teams = LiquidBounce.moduleManager.teams

                return !teams.state || !teams.isInYourTeam(entity)
            }

            return EntityUtils.targetMobs && EntityUtils.isMob(entity) || EntityUtils.targetAnimals &&
                    EntityUtils.isAnimal(entity)
        }

        return false
    }

    private fun isAlive(entity: EntityLivingBase) = entity.isEntityAlive && entity.health > 0
}