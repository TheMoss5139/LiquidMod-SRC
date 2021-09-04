package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.misc.AntiBot
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.extensions.getVec
import net.ccbluex.liquidbounce.utils.renon.*
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.block.BlockAir
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.Packet
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C03PacketPlayer.*
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.*
import java.util.function.Consumer

@ModuleInfo(
    name = "InfiniteAura",
    description = "InfiniteAura for Vanilla",
    category = ModuleCategory.COMBAT,
    keyBind = Keyboard.KEY_NONE
)
class InfiniteAura : Module() {
    var mode = arrayOf("AStar")
    var modes = ListValue("Path-Mode", mode, "AStar")
    var testpath = BoolValue("Test-Path", true)
    var sortmode = arrayOf("Angle", "Range", "FOV", "Armor", "Health")
    var sortmodes = ListValue("Sort-Mode", sortmode, "Angle")
    var rangeValue = FloatValue("Range", 20.0f, 10.0f, 256.0f)
    var mincps = IntegerValue("Min-CPS", 2, 1, 20)
    var maxcps = IntegerValue("Max-CPS", 4, 1, 20)
    var loopCount = IntegerValue("LoopCount", 200, 1, 1000)
    var searchRange = IntegerValue("SearchRange", 3, 1, 5)
    var targetsize = IntegerValue("TargetSize", 3, 1, 12)
    var target: ArrayList<EntityLivingBase>? = ArrayList()
    var pathfinder = PathFinder2()
    var path: ArrayList<Vec3>? = ArrayList()
    var timer = 0
    override fun onEnable() {
        target = null
        path!!.clear()
        timer = 0
    }

    override fun onDisable() {
        target = null
        path!!.clear()
        timer = 0
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        timer++
        val cps = Utils.random(mincps.get(), maxcps.get())
        target = nearTargets
        if (target!!.size == 0) {
            path!!.clear()
            return
        }
        if (timer >= 20 / cps) {
            path!!.clear()
            timer = 0
            if (modes.get().equals("AStar", ignoreCase = true)) {
                val packaging = arrayListOf<Packet<*>>()
                for (i in target!!.indices) {
                    if (i > targetsize.get()) {
                        break
                    }
                    val playerpos = BlockPos(mc.thePlayer.positionVector)
                    val targetpos = BlockPos(target!![i].positionVector)
                    pathfinder.settingPath(
                        loopcount = loopCount.get(),
                        start = playerpos.getVec(),
                        end = targetpos.getVec(),
                        searchlength = searchRange.get()
                    )
                    val listpath = pathfinder.running()
                    if (listpath != null) {
                        for (nodedata in listpath) {
                            val pos = nodedata.currentpos
                            path!!.add(pos)
                            val isonground = mc.theWorld.getBlockState(BlockPos(pos)).block !is BlockAir
                            packaging.add(C04PacketPlayerPosition(pos.xCoord, pos.yCoord, pos.zCoord, isonground))
                        }
                        packaging.add(C02PacketUseEntity(target!![i], C02PacketUseEntity.Action.ATTACK))
                        for (nodedata in listpath.reversed()) {
                            val pos = nodedata.currentpos
                            path!!.add(pos)
                            val isonground = mc.theWorld.getBlockState(BlockPos(pos)).block !is BlockAir
                            packaging.add(C04PacketPlayerPosition(pos.xCoord, pos.yCoord, pos.zCoord, isonground))
                        }
                    }
                }
                if (packaging.isNotEmpty())
                    packaging.forEach(Consumer { mc.thePlayer.sendQueue.addToSendQueue(it) })
            }
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        if (path != null && path!!.isNotEmpty()) {
            for (i in path!!.indices) {
                val vec = path!![i]
                var last: Vec3? = null
                if (i > 0) {
                    last = path!![i - 1]
                }
                if (last == null) {
                    continue
                }
                drawPath(last, vec)
            }
        }
    }

    fun drawPath(last: Vec3?, vec: Vec3) {
        var last = last
        val renderColor = Color.BLUE.rgb
        if (last == null) {
            last = vec
        }
        val beginX = (last.xCoord.toFloat() - mc.renderManager.renderPosX).toFloat()
        val beginY = (last.yCoord.toFloat() - mc.renderManager.renderPosY).toFloat()
        val beginZ = (last.zCoord.toFloat() - mc.renderManager.renderPosZ).toFloat()
        val endX = (vec.xCoord.toFloat() - mc.renderManager.renderPosX).toFloat()
        val endY = (vec.yCoord.toFloat() - mc.renderManager.renderPosY).toFloat()
        val endZ = (vec.zCoord.toFloat() - mc.renderManager.renderPosZ).toFloat()
        RenderingUtil.draw3DLine(beginX, beginY, beginZ, endX, endY, endZ, renderColor)
    }

    fun drawPathNode(vec1: Node) {
        val vec = vec1.current
        val x = vec.xCoord - mc.renderManager.renderPosX
        val y = vec.yCoord - mc.renderManager.renderPosY
        val z = vec.zCoord - mc.renderManager.renderPosZ
        val width = 0.3
        val height = mc.thePlayer.getEyeHeight().toDouble()
        RenderingUtil.pre3D()
        GL11.glLoadIdentity()
        mc.entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 2)
        val colors = intArrayOf(Colors.getColor(Color.black), Colors.getColor(Color.red))
        for (i in 0..1) {
            RenderingUtil.glColor(colors[i])
            GL11.glLineWidth((3 - i * 2).toFloat())
            GL11.glBegin(GL11.GL_LINE_STRIP)
            GL11.glVertex3d(x - width, y, z - width)
            GL11.glVertex3d(x - width, y, z - width)
            GL11.glVertex3d(x - width, y + height, z - width)
            GL11.glVertex3d(x + width, y + height, z - width)
            GL11.glVertex3d(x + width, y, z - width)
            GL11.glVertex3d(x - width, y, z - width)
            GL11.glVertex3d(x - width, y, z + width)
            GL11.glEnd()
            GL11.glBegin(GL11.GL_LINE_STRIP)
            GL11.glVertex3d(x + width, y, z + width)
            GL11.glVertex3d(x + width, y + height, z + width)
            GL11.glVertex3d(x - width, y + height, z + width)
            GL11.glVertex3d(x - width, y, z + width)
            GL11.glVertex3d(x + width, y, z + width)
            GL11.glVertex3d(x + width, y, z - width)
            GL11.glEnd()
            GL11.glBegin(GL11.GL_LINE_STRIP)
            GL11.glVertex3d(x + width, y + height, z + width)
            GL11.glVertex3d(x + width, y + height, z - width)
            GL11.glEnd()
            GL11.glBegin(GL11.GL_LINE_STRIP)
            GL11.glVertex3d(x - width, y + height, z + width)
            GL11.glVertex3d(x - width, y + height, z - width)
            GL11.glEnd()
        }
        RenderingUtil.post3D()
    }

    fun drawPathNodeToWork(vec1: Node) {
        val vec = vec1.current
        val x = vec.xCoord - mc.renderManager.renderPosX
        val y = vec.yCoord - mc.renderManager.renderPosY
        val z = vec.zCoord - mc.renderManager.renderPosZ
        val width = 0.3
        val height = mc.thePlayer.getEyeHeight().toDouble()
        RenderingUtil.pre3D()
        GL11.glLoadIdentity()
        mc.entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 2)
        val colors = intArrayOf(Colors.getColor(Color.black), Colors.getColor(Color.green))
        for (i in 0..1) {
            RenderingUtil.glColor(colors[i])
            GL11.glLineWidth((3 - i * 2).toFloat())
            GL11.glBegin(GL11.GL_LINE_STRIP)
            GL11.glVertex3d(x - width, y, z - width)
            GL11.glVertex3d(x - width, y, z - width)
            GL11.glVertex3d(x - width, y + height, z - width)
            GL11.glVertex3d(x + width, y + height, z - width)
            GL11.glVertex3d(x + width, y, z - width)
            GL11.glVertex3d(x - width, y, z - width)
            GL11.glVertex3d(x - width, y, z + width)
            GL11.glEnd()
            GL11.glBegin(GL11.GL_LINE_STRIP)
            GL11.glVertex3d(x + width, y, z + width)
            GL11.glVertex3d(x + width, y + height, z + width)
            GL11.glVertex3d(x - width, y + height, z + width)
            GL11.glVertex3d(x - width, y, z + width)
            GL11.glVertex3d(x + width, y, z + width)
            GL11.glVertex3d(x + width, y, z - width)
            GL11.glEnd()
            GL11.glBegin(GL11.GL_LINE_STRIP)
            GL11.glVertex3d(x + width, y + height, z + width)
            GL11.glVertex3d(x + width, y + height, z - width)
            GL11.glEnd()
            GL11.glBegin(GL11.GL_LINE_STRIP)
            GL11.glVertex3d(x - width, y + height, z + width)
            GL11.glVertex3d(x - width, y + height, z - width)
            GL11.glEnd()
        }
        val xyz = floatArrayOf(
            (vec.xCoord - mc.thePlayer.posX).toFloat(), (vec.yCoord - mc.thePlayer.posY).toFloat(),
            (vec.zCoord - mc.thePlayer.posZ).toFloat()
        )
        val f = mc.renderManager.playerViewY
        val f1 = mc.renderManager.playerViewX
        val flag1 = mc.renderManager.options.thirdPersonView == 2
        val fr = mc.fontRendererObj
        drawNameplate(fr, "G:" + vec1.gcost, xyz[0], xyz[1] + 1, xyz[2], 0, f, f1, flag1, false)
        drawNameplate(fr, "H:" + vec1.hcost, xyz[0], xyz[1] + 2, xyz[2], 0, f, f1, flag1, false)
        drawNameplate(fr, "F:" + vec1.fcost, xyz[0], xyz[1] + 3, xyz[2], 0, f, f1, flag1, false)
        RenderingUtil.post3D()
    }

    fun drawNameplate(
        fontRendererIn: FontRenderer, str: String?, x: Float, y: Float, z: Float, verticalShift: Int,
        viewerYaw: Float, viewerPitch: Float, isThirdPersonFrontal: Boolean, isSneaking: Boolean
    ) {
        val mc = Minecraft.getMinecraft()
        val renderManager = mc.renderManager
        val f = 1.6f
        val f1 = 0.016666668f * f
        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, z)
        GL11.glNormal3f(0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(-renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(renderManager.playerViewX, 1.0f, 0.0f, 0.0f)
        GlStateManager.scale(-f1, -f1, f1)
        GlStateManager.disableLighting()
        GlStateManager.depthMask(false)
        GlStateManager.disableDepth()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        val b0: Byte = 0
        val renderColor = Color.WHITE.rgb
        val i = (fontRendererIn.getStringWidth(str) / 2)
        GlStateManager.disableTexture2D()
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        worldrenderer.pos((-i - 1).toDouble(), (-1 + b0).toDouble(), 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex()
        worldrenderer.pos((-i - 1).toDouble(), (8 + b0).toDouble(), 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex()
        worldrenderer.pos((i + 1).toDouble(), (8 + b0).toDouble(), 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex()
        worldrenderer.pos((i + 1).toDouble(), (-1 + b0).toDouble(), 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex()
        tessellator.draw()
        GlStateManager.enableTexture2D()
        fontRendererIn.drawString(str, -fontRendererIn.getStringWidth(str) / 2, b0.toInt(), renderColor)
        GlStateManager.enableDepth()
        GlStateManager.depthMask(true)
        fontRendererIn.drawString(str, -fontRendererIn.getStringWidth(str) / 2, b0.toInt(), renderColor)
        GlStateManager.enableLighting()
        GlStateManager.disableBlend()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        GlStateManager.popMatrix()
    }

    private fun isEnemy(entity: EntityLivingBase): Boolean {
        if (entity is EntityLivingBase && (EntityUtils.targetDead || entity.isEntityAlive) && entity !== mc.thePlayer) {
            if (!EntityUtils.targetInvisible && entity.isInvisible) return false
            if (EntityUtils.targetPlayer && entity is EntityPlayer) {
                val auraTeams = LiquidBounce.moduleManager.auraTeams
                if (auraTeams.state) {
                    if (auraTeams.players.contains(entity)) {
                        return false
                    }
                }
                if (entity.isSpectator || AntiBot.isBot(entity)) return false
                if (EntityUtils.isFriend(entity) && !LiquidBounce.moduleManager.noFriends.state) return false
                val teams = LiquidBounce.moduleManager.teams
                return !teams.state || !teams.isInYourTeam(entity)
            }
            return EntityUtils.targetMobs && EntityUtils.isMob(entity) || EntityUtils.targetAnimals &&
                    EntityUtils.isAnimal(entity)
        }
        return false
    }

    private val targets: ArrayList<EntityLivingBase>
        private get() {
            val targets = ArrayList<EntityLivingBase>()
            for (o in mc.theWorld.loadedEntityList) {
                if (o is EntityLivingBase) {
                    val entity = o
                    if (isEnemy(entity)) {
                        targets.add(entity)
                    }
                    if (mc.thePlayer.getDistanceToEntity(entity) > rangeValue.get()) {
                        targets.remove(entity)
                    }
                }
            }
            return targets
        }
    private val nearTargets: ArrayList<EntityLivingBase>?
        private get() {
            val targets = targets ?: return null
            if (targets.isEmpty()) {
                return null
            }
            sortList(targets)
            return targets
        }

    private fun sortList(weed: ArrayList<EntityLivingBase>) {
        val current = sortmodes.get()
        when (current) {
            "Range" -> weed.sortWith(Comparator { o1: EntityLivingBase, o2: EntityLivingBase ->
                (o1.getDistanceToEntity(
                    mc.thePlayer
                ) * 1000
                        - o2.getDistanceToEntity(mc.thePlayer) * 1000).toInt()
            })
            "FOV" -> //			weed.sort(Comparator.comparingDouble(o -> (RotationUtils.getDistanceBetweenAngles(mc.thePlayer.rotationYaw,
//					RotationUtils.getRotations(o)[0]))));
                weed.sortWith(Comparator<EntityLivingBase?> { o1, o2 ->
                    val yaw = mc.thePlayer.rotationYaw
                    val rot1 = RotationUtils.getRotations(o1)
                    val rot2 = RotationUtils.getRotations(o2)
                    if (RotationUtils.getDistanceBetweenAngles(yaw, rot1[0]) < RotationUtils.getDistanceBetweenAngles(
                            yaw,
                            rot2[0]
                        )
                    ) {
                        return@Comparator -1
                    } else if (RotationUtils.getDistanceBetweenAngles(
                            yaw,
                            rot1[0]
                        ) > RotationUtils.getDistanceBetweenAngles(yaw, rot2[0])
                    ) {
                        return@Comparator 1
                    }
                    0
                })
            "Angle" -> weed.sortWith(Comparator<EntityLivingBase> { o1: EntityLivingBase?, o2: EntityLivingBase? ->
                val rot1 = RotationUtils.getRotations(o1)
                val rot2 = RotationUtils.getRotations(o2)
                (mc.thePlayer.rotationYaw - rot1[0] - (mc.thePlayer.rotationYaw - rot2[0])).toInt()
            })
            "Health" -> weed.sortWith(Comparator { o1: EntityLivingBase, o2: EntityLivingBase -> (o1.health - o2.health).toInt() })
            "Armor" -> weed.sortWith(Comparator.comparingInt { o1: EntityLivingBase ->
                if (o1 is EntityPlayer) o1.totalArmorValue else o1.health
                    .toInt()
            })
        }
    }
}