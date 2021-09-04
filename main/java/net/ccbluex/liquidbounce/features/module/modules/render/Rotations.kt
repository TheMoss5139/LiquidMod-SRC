/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.network.play.client.C03PacketPlayer

@ModuleInfo(name = "Rotations", description = "Allows you to see server-sided head and body rotations.", category = ModuleCategory.RENDER)
class Rotations : Module() {

    val bodyValue = BoolValue("Body", true)

    var playerYaw: Float? = null

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val thePlayer = mc.thePlayer

        if (!shouldRotate() || thePlayer == null) {
            playerYaw = null
            return
        }

        val packet = event.packet

        if (packet is C03PacketPlayer.C06PacketPlayerPosLook || packet is C03PacketPlayer.C05PacketPlayerLook) {
            val packetPlayer = packet as C03PacketPlayer

            playerYaw = packetPlayer.yaw

            thePlayer.rotationYawHead = packetPlayer.yaw
        } else {
            thePlayer.rotationYawHead = this.playerYaw!!
        }
    }

    private fun getManager() = LiquidBounce.moduleManager

    fun shouldRotate(): Boolean {
        val killAura = LiquidBounce.moduleManager.killAura
        return getManager().scaffold.state || getManager().tower.state ||
                (killAura.state && killAura.target != null) ||
                getManager().derp.state || getManager().bowAimbot.state ||
                getManager().fucker.state || getManager().civBreak.state || getManager().nuker.state ||
                getManager().chestaura.state
                || getManager().perspectiveMod.state
    }
}
