package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.network.play.client.C01PacketChatMessage

@ModuleInfo(name = "AutoGay", description = "Say something that is kind na gay. [@r = randomplayer, @index = index of player, @s = you]", category = ModuleCategory.MISC)
class AutoSay : Module() {
    val delay = IntegerValue("Delay", 12000, 0, 20000)
    val indexnumber = IntegerValue("IndexNumber", 2, 1, 10)
    val resend = BoolValue("Retry", false)
    val timer = MSTimer()
    var listplayercopyed = mutableListOf<String>()
    var messagetosend:String? = null

    override fun onEnable() {
        timer.reset()
        messagetosend = null
        listplayercopyed.clear()
        if (mc.theWorld != null && mc.thePlayer != null && mc.netHandler != null) {
            var listPlayers = mutableListOf<String>()
            for (playerInfo in mc.netHandler.playerInfoMap) {
                val playerName = playerInfo.gameProfile.name
                if (playerName == mc.thePlayer.name) continue
                listPlayers.add(playerName)
            }
            listplayercopyed = listPlayers
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (event.packet is C01PacketChatMessage) {
            val message = event.packet.getMessage()
            if (message.contains("@r") ||
                message.contains("@index") ||
                message.contains("@s")) {
                var listPlayers = mutableListOf<String>()
                for (playerInfo in mc.netHandler.playerInfoMap) {
                    val playerName = playerInfo.gameProfile.name
                    if (playerName == mc.thePlayer.name) continue
                    listPlayers.add(playerName)
                }
                listplayercopyed = listPlayers
                messagetosend = message
                event.cancelEvent()
            }
        }
    }

    @EventTarget
    fun onUpdate(updateEvent: UpdateEvent) {
        if (messagetosend == null) return
        if (!timer.hasTimePassed(delay.get().toLong())) return

        if (listplayercopyed.isEmpty()) {
            if (resend.get()) {
                var listPlayers = mutableListOf<String>()
                for (playerInfo in mc.netHandler.playerInfoMap) {
                    val playerName = playerInfo.gameProfile.name
                    if (playerName == mc.thePlayer.name) continue
                    listPlayers.add(playerName)
                }
                listplayercopyed = listPlayers
            }else {
                messagetosend == null
            }
        }
        if (messagetosend != null) {
            val message = replace(messagetosend!!)
            mc.thePlayer.sendChatMessage(message)
            timer.reset()
        }
    }

    fun replace(message: String): String? {
        var replacemessage = message
        while (replacemessage.contains("@r")) {
            val i = RandomUtils.nextInt(0,listplayercopyed.size)
            val randomPlayer = listplayercopyed[i]
            listplayercopyed.removeAt(i)
            replacemessage = replacemessage.replace("@r", randomPlayer)
        }

        while (replacemessage.contains("@s")) {
            replacemessage = replacemessage.replace("@s", mc.thePlayer.name)
        }
        while (replacemessage.contains("@index")) {
            var string = arrayOf<String>()
            for (i in 0..indexnumber.get()) {
                try {
                    var name = listplayercopyed[0]
                    listplayercopyed.removeAt(0)
                    string[i] = name
                }catch (exception : Exception) {
                    ClientUtils.displayChatMessage(exception.message)
                    return replacemessage
                }
            }
            var msgstring = ""
            for (i in 0..string.size) {
                msgstring += string[i]
                if (i != string.size - 1) {
                    msgstring += ","
                }
            }
            replacemessage = replacemessage.replace("@index", msgstring)
        }

        return replacemessage
    }
}