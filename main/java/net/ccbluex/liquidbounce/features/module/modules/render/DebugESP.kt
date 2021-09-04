package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11
import java.awt.Color
@ModuleInfo(
    name = "DebugESP",
    description = "Allows you to see Client Side Rotation",
    category = ModuleCategory.RENDER
)
class DebugESP : Module() {
    val scaffoldValue = BoolValue("Scaffold", false)
    val killAuraValue = BoolValue("KillAura", false)

    private val colorMode = ListValue("Color", arrayOf("Custom", "DistanceColor", "Rainbow"), "Custom")

    private val thicknessValue = FloatValue("Thickness", 2F, 1F, 5F)

    private val colorRedValue = IntegerValue("R", 0, 0, 255)
    private val colorGreenValue = IntegerValue("G", 160, 0, 255)
    private val colorBlueValue = IntegerValue("B", 255, 0, 255)

    val rotationArray = ArrayList<Rotation>()
    private var count = 10

    override fun onEnable() {
        rotationArray.clear()
        count = 10
    }

    override fun onDisable() {
        rotationArray.clear()
    }

    @EventTarget
    fun onRender3D(event:Render3DEvent) {
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glLineWidth(thicknessValue.get())
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(false)

        GL11.glBegin(GL11.GL_LINES)

        for (rotation in rotationArray) {
            val rotationVector = RotationUtils.getVectorForRotation(rotation)
            val eyesPos = Vec3(
                mc.thePlayer.posX,
                mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.getEyeHeight(),
                mc.thePlayer.posZ
            )
            var vec3 = eyesPos.addVector(rotationVector.xCoord * 4, rotationVector.yCoord * 4, rotationVector.zCoord * 4)
            var dist = (mc.thePlayer.getDistance(vec3.xCoord, vec3.yCoord, vec3.zCoord) * 2).toInt()

            if (dist > 255) dist = 255

            val color = when (colorMode.get().toLowerCase()) {
                "custom" -> Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get(), 150)
                "distancecolor" -> Color(255 - dist, dist, 0, 150)
                "rainbow" -> ColorUtils.rainbow()
                else -> Color(255, 255, 255, 150)
            }

            drawTraces(vec3, color)
        }
        if (rotationArray.size > 0 && count == 0) {
            rotationArray.removeAt(0)
            count = 10
        }
        if (count > 0) {
            count--
        }
        GL11.glEnd()

        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(true)
        GL11.glDisable(GL11.GL_BLEND)
        GlStateManager.resetColor()
    }

    private fun drawTraces(vec: Vec3, color: Color) {
        val x = (vec.xCoord) * mc.timer.renderPartialTicks

        val y = (vec.yCoord) * mc.timer.renderPartialTicks

        val z = (vec.zCoord) * mc.timer.renderPartialTicks


        val eyeVector = Vec3(
            mc.thePlayer.posX,
            mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.getEyeHeight(),
            mc.thePlayer.posZ
        )

        RenderUtils.glColor(color)

        GL11.glVertex3d(eyeVector.xCoord - mc.renderManager.renderPosX, eyeVector.yCoord - mc.renderManager.renderPosY, eyeVector.zCoord - mc.renderManager.renderPosZ)
        GL11.glVertex3d(x - mc.renderManager.renderPosX, y - mc.renderManager.renderPosY, z - mc.renderManager.renderPosZ)
        GL11.glVertex3d(x - mc.renderManager.renderPosX, y - mc.renderManager.renderPosY, z - mc.renderManager.renderPosZ)
        GL11.glVertex3d(x - mc.renderManager.renderPosX, y - mc.renderManager.renderPosY, z - mc.renderManager.renderPosZ)
    }
}