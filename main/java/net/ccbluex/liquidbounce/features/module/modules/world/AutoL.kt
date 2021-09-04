package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.features.module.modules.world.AutoL
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.exploit.ForceUnicodeChat
import net.minecraft.network.play.server.S02PacketChat
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.util.ArrayList

@ModuleInfo(name = "AutoL", description = "L Ling People on SuckSin", category = ModuleCategory.WORLD)
class AutoL : Module() {
    var liststr = ArrayList<String>()
    var file = File(LiquidBounce.fileManager.dir, "AutoL.txt")
    var lmode = ListValue("L-Mode", arrayOf("L", "Text", "AntiName"), "L")
    //%player% , %your%
    override fun onEnable() {
        if (mc.thePlayer == null) return
        if (lmode.get().equals("Text", ignoreCase = true)) {
            val str = read(file)
            if (str == null) {
                ClientUtils.displayChatMessage("No AutoL.txt")
                return
            }
            liststr = str
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S02PacketChat) {
            val message = packet.chatComponent.unformattedText
            if (message.toLowerCase().contains("has been killed by " + mc.thePlayer.name.toLowerCase() + "!") ||
                message.toLowerCase()
                    .contains("weapon could not stand against " + mc.thePlayer.name.toLowerCase() + "!") ||
                message.toLowerCase().contains("was brutally murdered by " + mc.thePlayer.name.toLowerCase() + "!") ||
                message.toLowerCase().contains(mc.thePlayer.name.toLowerCase() + " could not resist killing") ||
                message.toLowerCase().contains(mc.thePlayer.name.toLowerCase() + " gave a helping hand in ")
            ) {
                var message1 = ""
                val split = message.split(" ".toRegex()).toTypedArray()
                if (message.toLowerCase().contains(mc.thePlayer.name.toLowerCase() + " gave a helping hand in ")) {
                    message1 = split[split.size - 2]
                    sendL(message1)
                } else {
                    if (!split[2].equals(mc.thePlayer.name, ignoreCase = true)) {
                        message1 = split[2]
                        sendL(message1)
                    } else if (!split[split.size - 1].equals(
                            mc.thePlayer.name + ".",
                            ignoreCase = true
                        ) || !split[split.size].equals(
                            mc.thePlayer.name, ignoreCase = true
                        )
                    ) {
                        message1 = split[split.size - 1]
                        sendL(message1)
                    }
                }
            }
            //1v1seksin
            if (message.toLowerCase().contains(mc.thePlayer.name.toLowerCase() + " slained ")) {
                val split = message.split(" ".toRegex()).toTypedArray()
                var message1 = split[split.size - 1]
                message1 = message1.replace("!", "")
                sendL(message1)
            }
        }
    }

    fun sendL(name: String) {
        val named = ForceUnicodeChat.convert(name)
        when (lmode.get().toLowerCase()) {
            "l" -> {
                mc.thePlayer.sendChatMessage("L $name")
            }
            "text" -> {
                if (liststr.isNotEmpty()) {
                    val string = liststr[RandomUtils.nextInt(0, liststr.size)]
                    var message = string.replace("%player%".toRegex(), name)
                    message = message.replace("%your%", mc.thePlayer.name)
                    mc.thePlayer.sendChatMessage(message)
                }
            }
            "antiname" -> {
                if (liststr.isNotEmpty()) {
                    val string = liststr[RandomUtils.nextInt(0, liststr.size)]
                    var message = string.replace("%player%".toRegex(), named)
                    message = message.replace("%your%", ForceUnicodeChat.convert(mc.thePlayer.name))
                    mc.thePlayer.sendChatMessage(message)
                }
            }
        }
    }

    companion object {
        fun read(inputFile: File?): ArrayList<String>? {
            var readContent = ArrayList<String>()
            try {
                val `in` = BufferedReader(InputStreamReader(FileInputStream(inputFile), "UTF8"))
                var str: String
                while (`in`.readLine().also { str = it } != null) {
                    readContent.add(str)
                }
                `in`.close()
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
            return readContent
        }
    }

    override val tag: String?
        get() = lmode.get()
}