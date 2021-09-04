/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.misc.AntiBot
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.renon.Colors
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TimeUtils
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemSword
import net.minecraft.network.play.client.*
import net.minecraft.potion.Potion
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import net.minecraft.world.WorldSettings
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.*
import kotlin.math.max
import kotlin.math.min

@ModuleInfo(
    name = "KillAura", description = "Automatically attacks targets around you.",
    category = ModuleCategory.COMBAT, keyBind = Keyboard.KEY_R
)
class KillAura : Module() {

    /**
     * OPTIONS
     */

    // CPS - Attack speed
    private val maxCPS: IntegerValue = object : IntegerValue("MaxCPS", 8, 1, 20) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = minCPS.get()
            if (i > newValue) set(i)

            attackDelay = TimeUtils.randomClickDelay(minCPS.get(), this.get())
        }
    }

    private val minCPS: IntegerValue = object : IntegerValue("MinCPS", 5, 1, 20) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = maxCPS.get()
            if (i < newValue) set(i)

            attackDelay = TimeUtils.randomClickDelay(this.get(), maxCPS.get())
        }
    }

    private val hurtTimeValue = IntegerValue("HurtTime", 10, 0, 10)

    // Range
    private val rangeValue = FloatValue("Range", 3.7f, 1f, 8f)
    val prelookRangeValue = FloatValue("PrelookRange", 4f, 1f, 8f)
    private val throughWallsRangeValue = FloatValue("ThroughWallsRange", 3f, 0f, 8f)
    private val rangeSprintReducementValue = FloatValue("RangeSprintReducement", 0f, 0f, 0.4f)

    // Modes
    private val priorityValue = ListValue(
        "Priority",
        arrayOf("Health", "Distance", "Direction", "LivingTime", "HighArmor", "LowArmor"),
        "Distance"
    )
    private val targetModeValue = ListValue("TargetMode", arrayOf("Single", "Switch", "Multi"), "Switch")

    // Bypass
    private val swingValue = BoolValue("Swing", true)
    private val keepSprintValue = BoolValue("KeepSprint", true)

    // AutoBlock
    private val autoBlockValue = BoolValue("AutoBlock", false)
    private val interactAutoBlockValue = BoolValue("InteractAutoBlock", true)
    private val delayedBlockValue = BoolValue("DelayedBlock", true)
    private val blockRate = IntegerValue("BlockRate", 100, 1, 100)

    // Raycast
    private val raycastValue = BoolValue("RayCast", true)
    private val raycastIgnoredValue = BoolValue("RayCastIgnored", false)
    private val livingRaycastValue = BoolValue("LivingRayCast", true)

    // Bypass
    private val aacValue = BoolValue("AAC", false)

    // Turn Speed
    private val maxTurnSpeed: FloatValue = object : FloatValue("MaxTurnSpeed", 180f, 0f, 180f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = minTurnSpeed.get()
            if (v > newValue) set(v)
        }
    }

    private val minTurnSpeed: FloatValue = object : FloatValue("MinTurnSpeed", 180f, 0f, 180f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = maxTurnSpeed.get()
            if (v < newValue) set(v)
        }
    }

    private val silentRotationValue = BoolValue("SilentRotation", true)
    val rotationStrafeValue = ListValue("Strafe", arrayOf("Off", "Strict", "Silent"), "Off")
    private val randomCenterValue = BoolValue("RandomCenter", true)
    private val outborderValue = BoolValue("Outborder", false)
    private val fovValue = FloatValue("FOV", 180f, 0f, 180f)

    // Predict
    private val predictValue = BoolValue("Predict", true)

    private val maxPredictSize: FloatValue = object : FloatValue("MaxPredictSize", 1f, 0.1f, 5f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = minPredictSize.get()
            if (v > newValue) set(v)
        }
    }

    private val minPredictSize: FloatValue = object : FloatValue("MinPredictSize", 1f, 0.1f, 5f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = maxPredictSize.get()
            if (v < newValue) set(v)
        }
    }

    // Bypass
    private val failRateValue = FloatValue("FailRate", 0f, 0f, 100f)
    private val fakeSwingValue = BoolValue("FakeSwing", true)
    private val noInventoryAttackValue = BoolValue("NoInvAttack", false)
    private val noInventoryDelayValue = IntegerValue("NoInvDelay", 200, 0, 500)
    private val limitedMultiTargetsValue = IntegerValue("LimitedMultiTargets", 0, 0, 50)
    private val vip_target_name = TextValue("VIP_Name", "No VIP Selected")
    private val tpValue = BoolValue("TPAttack", false)
    private val tpmodeValue = ListValue("TPMode", arrayOf("Normal", "BackSlap"), "Normal")
    private val backslaprangeValue = FloatValue("BackSlapRange", 2.0F, 0.1F, 10.0F)
    private val NewTurnValue = BoolValue("NewTurn", false)

    // Visuals
    private val markValue = BoolValue("Mark", true)
    private val fakeSharpValue = BoolValue("FakeSharp", true)
    val NewStrafeValue = BoolValue("NewStrafe", true)
    val FakeBlockValue = BoolValue("FakeBlock", true)
    val AutoDisablevalue = BoolValue("AutoDisable", true)
    private val minrotateMultiply = FloatValue("MinRotateMultiply", 0.5f, 0.1f, 2f)
    private val maxrotateMultiply = FloatValue("MaxRotateMultiply", 1.25f, 0.1f, 2f)

    /**
     * MODULE
     */

    // Target
    var target: EntityLivingBase? = null
    private var currentTarget: EntityLivingBase? = null
    private var hitable = false
    private val prevTargetEntities = mutableListOf<Int>()

    // Attack delay
    private val attackTimer = MSTimer()
    private var attackDelay = 0L
    private var clicks = 0

    // Container Delay
    private var containerOpen = -1L

    // Fake block status
    var blockingStatus = false

    //RandomVec
    var randomVec: Vec3? = null

    //RandomCPS
    val randomCPS = RandomGenerator<Int>()

    /**
     * Enable kill aura module
     */
    override fun onEnable() {
        mc.thePlayer ?: return
        mc.theWorld ?: return

        updateTarget()
        randomVec = null
        randomCPS.setUP(minCPS.get(), maxCPS.get())
    }

    /**
     * Disable kill aura module
     */
    override fun onDisable() {
        target = null
        currentTarget = null
        hitable = false
        prevTargetEntities.clear()
        attackTimer.reset()
        clicks = 0

        stopBlocking()
        RotationUtils.reset()
    }

    /**
     * Motion event
     */
    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (event.eventState == EventState.POST) {
            target ?: return
            currentTarget ?: return

            // Update hitable
            updateHitable()

            // AutoBlock
            if (autoBlockValue.get() && delayedBlockValue.get() && canBlock && check_range(currentTarget!!))
                startBlocking(currentTarget!!, interactAutoBlockValue.get())
        } else {
            if (tpValue.get() && currentTarget != null) {
                when (tpmodeValue.get().toLowerCase()) {
                    "normal" -> {
                        mc.thePlayer.setPositionAndUpdate(
                            currentTarget!!.posX,
                            currentTarget!!.posY,
                            currentTarget!!.posZ
                        )
                    }
                    "backslap" -> {
                        val length = -(backslaprangeValue.get().toDouble())
                        val yaw = Math.toRadians(currentTarget!!.rotationYaw.toDouble())
                        mc.thePlayer.setPositionAndUpdate(
                            currentTarget!!.posX + -Math.sin(yaw) * length,
                            currentTarget!!.posY,
                            currentTarget!!.posZ + Math.cos(yaw) * length
                        )
                    }
                }
            }
        }

        if (rotationStrafeValue.get().equals("Off", true))
            update()
    }

    /**
     * Strafe event
     */
    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        if (cancelRun) {
            return
        }
        if (rotationStrafeValue.get().equals("Off", true))
            return

        update()

        if (RotationUtils.targetRotation != null) {
            when (rotationStrafeValue.get().toLowerCase()) {
                "strict" -> {
                    val (yaw) = RotationUtils.targetRotation ?: return
                    var strafe = event.strafe
                    var forward = event.forward
                    var friction = event.friction

                    var f = strafe * strafe + forward * forward

                    if (f >= 1.0E-4F) {
                        f = MathHelper.sqrt_float(f)

                        if (f < 1.0F)
                            f = 1.0F

                        f = friction / f
                        strafe *= f
                        forward *= f

                        val yawSin = MathHelper.sin((yaw * Math.PI / 180F).toFloat())
                        val yawCos = MathHelper.cos((yaw * Math.PI / 180F).toFloat())

                        mc.thePlayer.motionX += strafe * yawCos - forward * yawSin
                        mc.thePlayer.motionZ += forward * yawCos + strafe * yawSin
                        event.cancelEvent()
                    }
                }
                "silent" -> {
                    RotationUtils.targetRotation.applyStrafeToPlayer(event)
                    event.cancelEvent()
                }
            }
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        if (AutoDisablevalue.get())
            toggle()
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        if (cancelRun) {
            return
        }
        RotationUtils.targetRotation ?: return
        event.yaw = RotationUtils.targetRotation.yaw
    }

    fun update() {
        if (cancelRun || (noInventoryAttackValue.get() && (mc.currentScreen is GuiContainer ||
                    System.currentTimeMillis() - containerOpen < noInventoryDelayValue.get()))
        )
            return

        // Update target
        updateTarget()

        if (target == null) {
            randomVec = null
            stopBlocking()
            return
        }

        if (!check_range(target!!)) {
            stopBlocking()
            return
        }

        // Target
        currentTarget = target

        if (!targetModeValue.get().equals("Switch", ignoreCase = true) && isEnemy(currentTarget))
            target = currentTarget
    }

    fun check_range(entity: Entity): Boolean {
        return mc.thePlayer.getDistanceToEntityBox(entity) <= rangeValue.get().toDouble()
    }

    /**
     * Update event
     */
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (cancelRun) {
            target = null
            currentTarget = null
            hitable = false
            stopBlocking()
            return
        }

        if (noInventoryAttackValue.get() && (mc.currentScreen is GuiContainer ||
                    System.currentTimeMillis() - containerOpen < noInventoryDelayValue.get())
        ) {
            target = null
            currentTarget = null
            hitable = false
            if (mc.currentScreen is GuiContainer) containerOpen = System.currentTimeMillis()
            return
        }
    }

    @EventTarget
    fun onTick(event: TickEvent) {
        target ?: return
        currentTarget ?: return
        if (clicks <= 0) return
        updateHitable()

        if (!check_range(currentTarget!!)) {
            stopBlocking()
            return
        }
        val isattack = clicks > 0
        if (!isattack) {
            if (mc.thePlayer.isBlocking || blockingStatus) {
                stopBlocking()
            }
            return
        }
        if (isattack) {
            // Stop blocking
            if (mc.thePlayer.isBlocking || blockingStatus) {
                stopBlocking()
            }
        }

        while (clicks > 0) {
            runAttack()
            clicks--
        }

        if (!isattack) return

        // Start blocking after attack
        if (mc.thePlayer.isBlocking || (autoBlockValue.get() && canBlock)) {
            if (!(blockRate.get() > 0 && Random().nextInt(100) <= blockRate.get()))
                return
            if (delayedBlockValue.get())
                return
            startBlocking(currentTarget!!, hitable)
        }
        onVelocity()
        val boundingBox = currentTarget!!.entityBoundingBox
        //SetNew RandomVec
        val x = (boundingBox.maxX - boundingBox.minX) / 2
        val y = (boundingBox.maxY - boundingBox.minY) / 2
        val z = (boundingBox.maxZ - boundingBox.minZ) / 2
        val randomx = RandomUtils.nextDouble(-x + 0.35, x - 0.35)
        val randomy = RandomUtils.nextDouble(-y + 0.4, y - 0.4)
        val randomz = RandomUtils.nextDouble(-z + 0.35, z - 0.35)
        randomVec = Vec3(randomx, randomy, randomz)
    }

    /**
     * Render event
     */
    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (cancelRun) {
            target = null
            currentTarget = null
            hitable = false
            stopBlocking()
            return
        }

        if (noInventoryAttackValue.get() && (mc.currentScreen is GuiContainer ||
                    System.currentTimeMillis() - containerOpen < noInventoryDelayValue.get())
        ) {
            target = null
            currentTarget = null
            hitable = false
            if (mc.currentScreen is GuiContainer) containerOpen = System.currentTimeMillis()
            return
        }

        if (target != null) {

        } else {
            stopBlocking()
            return
        }
        if (markValue.get() && !targetModeValue.get().equals("Multi", ignoreCase = true)) {
            RenderUtils.drawPlatform(target, if (hitable) Color(37, 126, 255, 70) else Color(255, 0, 0, 70))
            GL11.glPushMatrix()
            var x = target!!.posX - mc.thePlayer.posX
            var y = target!!.posY - mc.thePlayer.posY
            var z = target!!.posZ - mc.thePlayer.posZ

            RenderUtils.glColor(0, 0, 0, 0)
            GL11.glTranslated(x, y, z)
            GL11.glRotated(90.0, -1.0, 0.0, 0.0)
            RenderUtils.drawCircle(
                0F,
                0F,
                (target!!.entityBoundingBox.maxX - target!!.entityBoundingBox.minX).toFloat(),
                0,
                360
            )
            GL11.glTranslated(-x, -y, -z)
            RenderUtils.glColor(Colors.getRandomColor2())
            GL11.glPopMatrix()
        }
        if (currentTarget != null && currentTarget!!.hurtTime > hurtTimeValue.get()) {
            if (mc.thePlayer.isBlocking || blockingStatus) {
                stopBlocking()
            }
            return
        }
        if (currentTarget != null && attackTimer.hasTimePassed(attackDelay) &&
            currentTarget!!.hurtTime <= hurtTimeValue.get()
        ) {
            if (!check_range(currentTarget!!)) {
                stopBlocking()
                return
            }
            clicks++
            attackTimer.reset()
            randomCPS.setUP(minCPS.get(), maxCPS.get())
            val newCps = randomCPS.returnNext()
            attackDelay = 1000/newCps.toLong()
        }
    }

    /**
     * Handle entity move
     */
    @EventTarget
    fun onEntityMove(event: EntityMovementEvent) {
        val movedEntity = event.movedEntity

        if (target == null || movedEntity != currentTarget) {
            return
        }

        updateHitable()
    }

    /**
     * Attack enemy
     */
    private fun runAttack() {
        target ?: return
        currentTarget ?: return

        // Settings
        val failRate = failRateValue.get()
        val swing = swingValue.get()
        val multi = targetModeValue.get().equals("Multi", ignoreCase = true)
        val openInventory = aacValue.get() && mc.currentScreen is GuiInventory
        val failHit = failRate > 0 && Random().nextInt(100) <= failRate

        // Close inventory when open
        if (openInventory)
            mc.netHandler.addToSendQueue(C0DPacketCloseWindow())

        // Check is not hitable or check failrate
        if (!hitable || failHit) {
            if (swing && (fakeSwingValue.get() || failHit))
                mc.thePlayer.swingItem()
        } else {
            // Attack
            if (!multi) {
                attackEntity(currentTarget!!)
            } else {
                var targets = 0

                for (entity in mc.theWorld.loadedEntityList) {
                    val distance = mc.thePlayer.getDistanceToEntityBox(entity)

                    if (entity is EntityLivingBase && isEnemy(entity) && distance <= getRange(entity)) {
                        val boundingBox = entity.entityBoundingBox
                        attackEntity(entity)

                        targets += 1

                        if (limitedMultiTargetsValue.get() != 0 && limitedMultiTargetsValue.get() <= targets)
                            break
                    }
                }
            }

            prevTargetEntities.add(if (aacValue.get()) target!!.entityId else currentTarget!!.entityId)

            if (target == currentTarget)
                target = null
        }

        // Open inventory
        if (openInventory)
            mc.netHandler.addToSendQueue(C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT))
    }

    /**
     * Update current target
     */
    private fun updateTarget() {
        // Reset fixed target to null
        target = null

        // Settings
        val fov = fovValue.get()
        val switchMode = targetModeValue.get().equals("Switch", ignoreCase = true)

        // Find possible targets
        val targets = mutableListOf<EntityLivingBase>()

        for (entity in mc.theWorld.loadedEntityList) {
            if (entity !is EntityLivingBase || !isEnemy(entity) || (switchMode && prevTargetEntities.contains(entity.entityId)))
                continue

            val distance = mc.thePlayer.getDistanceToEntityBox(entity)
            val entityFov = RotationUtils.getRotationDifference(entity)


            if (distance <= maxRange && (fov == 180F || entityFov <= fov)) {
                if (entity.name.equals(vip_target_name.get(), true)) {
                    targets.clear()
                    targets.add(entity)
                    break
                }
                targets.add(entity)
            }
        }

        // Sort targets by priority
        when (priorityValue.get().toLowerCase()) {
            "distance" -> targets.sortBy { mc.thePlayer.getDistanceToEntityBox(it) } // Sort by distance
            "health" -> targets.sortBy { it.health } // Sort by health
            "direction" -> targets.sortBy { RotationUtils.getRotationDifference(it) } // Sort by FOV
            "livingtime" -> targets.sortBy { -it.ticksExisted } // Sort by existence
            "higharmor" -> targets.sortBy { -it.totalArmorValue }
            "lowarmor" -> targets.sortBy { it.totalArmorValue }
        }

        // Find best target
        for (entity in targets) {
            // Update rotations to current target
            if (!updateCheck(entity)) // when failed then try another target
                continue

            // Set target to current entity
            target = entity
            return
        }
        // Cleanup last targets when no target found and try again
        if (prevTargetEntities.isNotEmpty()) {
            prevTargetEntities.clear()
            updateTarget()
        }
    }

    /**
     * Check if [entity] is selected as enemy with current target options and other modules
     */
    private fun isEnemy(entity: Entity?): Boolean {
        if (entity is EntityLivingBase && (EntityUtils.targetDead || isAlive(entity)) && entity != mc.thePlayer) {
            if (!EntityUtils.targetInvisible && entity.isInvisible())
                return false

            if (EntityUtils.targetPlayer && entity is EntityPlayer) {
                val auraTeams = LiquidBounce.moduleManager.auraTeams
                if (auraTeams.state) {
                    if (auraTeams.players.contains(entity)) {
                        return false
                    }
                }
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

    /**
     * Attack [entity]
     */
    private fun attackEntity(entity: EntityLivingBase) {

        // Call attack event
        LiquidBounce.eventManager.callEvent(AttackEvent(entity))

        // Attack target
        if (swingValue.get())
            mc.thePlayer.swingItem()
        mc.netHandler.addToSendQueue(C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK))

        if (keepSprintValue.get()) {
            // Critical Effect
            if (mc.thePlayer.fallDistance > 0F && !mc.thePlayer.onGround && !mc.thePlayer.isOnLadder &&
                !mc.thePlayer.isInWater && !mc.thePlayer.isPotionActive(Potion.blindness) && !mc.thePlayer.isRiding
            )
                mc.thePlayer.onCriticalHit(entity)

            // Enchant Effect
            if (EnchantmentHelper.getModifierForCreature(mc.thePlayer.heldItem, entity.creatureAttribute) > 0F)
                mc.thePlayer.onEnchantmentCritical(entity)
        } else {
            if (mc.playerController.currentGameType != WorldSettings.GameType.SPECTATOR)
                mc.thePlayer.attackTargetEntityWithCurrentItem(entity)
        }

        // Extra critical effects
        val criticals = LiquidBounce.moduleManager.criticals

        for (i in 0..2) {
            // Critical Effect
            if (mc.thePlayer.fallDistance > 0F && !mc.thePlayer.onGround && !mc.thePlayer.isOnLadder && !mc.thePlayer.isInWater && !mc.thePlayer.isPotionActive(
                    Potion.blindness
                ) && mc.thePlayer.ridingEntity == null || criticals.state && criticals.msTimer.hasTimePassed(
                    criticals.delayValue.get().toLong()
                ) && !mc.thePlayer.isInWater && !mc.thePlayer.isInLava && !mc.thePlayer.isInWeb
            )
                mc.thePlayer.onCriticalHit(target)

            // Enchant Effect
            if (EnchantmentHelper.getModifierForCreature(
                    mc.thePlayer.heldItem,
                    target!!.creatureAttribute
                ) > 0.0f || fakeSharpValue.get()
            )
                mc.thePlayer.onEnchantmentCritical(target)
        }
    }

    /**
     * Update killaura rotations to enemy
     */
    private fun updateCheck(entity: Entity): Boolean {
        if (maxTurnSpeed.get() <= 0F)
            return true

        var boundingBox = entity.entityBoundingBox

        if (predictValue.get()) {
            boundingBox = boundingBox.offset(
                (entity.posX - entity.prevPosX) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                (entity.posY - entity.prevPosY) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                (entity.posZ - entity.prevPosZ) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get())
            )
            boundingBox = boundingBox.offset(
                -mc.thePlayer.motionX, mc.thePlayer.motionY, -mc.thePlayer.motionZ
            )
        }

        var vecrotation: VecRotation? = if (NewTurnValue.get()) {
            if (randomVec == null) randomVec = Vec3(0.0,0.0,0.0)
            RotationUtils.searchCenter(
                boundingBox,
                randomCenterValue.get(),
                predictValue.get(),
                mc.thePlayer.getDistanceToEntityBox(entity) < throughWallsRangeValue.get(),
                maxRange, randomVec
            )
        }else {
            RotationUtils.searchCenter(
                boundingBox,
                outborderValue.get() && !attackTimer.hasTimePassed(attackDelay / 2),
                randomCenterValue.get(),
                predictValue.get(),
                mc.thePlayer.getDistanceToEntityBox(entity) < throughWallsRangeValue.get(),
                maxRange
            )
        }
        vecrotation ?: return false

        return true
    }

    @EventTarget
    fun onRotation(eventRotationEvent: RotationEvent) {
        if (maxTurnSpeed.get() <= 0F)
            return
        val entity = target ?: return
        var boundingBox = entity.entityBoundingBox

        if (predictValue.get()) {
            boundingBox = boundingBox.offset(
                (entity.posX - entity.prevPosX) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                (entity.posY - entity.prevPosY) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                (entity.posZ - entity.prevPosZ) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get())
            )
            boundingBox = boundingBox.offset(
                -mc.thePlayer.motionX, mc.thePlayer.motionY, -mc.thePlayer.motionZ
            )
        }
        var vecrotation: VecRotation? = if (NewTurnValue.get()) {
            if (randomVec == null) randomVec = Vec3(0.0,0.0,0.0)
            RotationUtils.searchCenter(
                boundingBox,
                randomCenterValue.get(),
                predictValue.get(),
                mc.thePlayer.getDistanceToEntityBox(entity) < throughWallsRangeValue.get(),
                maxRange, randomVec
            )
        }else {
            RotationUtils.searchCenter(
                boundingBox,
                outborderValue.get() && !attackTimer.hasTimePassed(attackDelay / 2),
                randomCenterValue.get(),
                predictValue.get(),
                mc.thePlayer.getDistanceToEntityBox(entity) < throughWallsRangeValue.get(),
                maxRange
            )
        }
        vecrotation ?: return
        var rotation = vecrotation.rotation

        var limitedRotation = RotationUtils.limitAngleChange(
            RotationUtils.serverRotation, rotation,
            (Math.random() * (maxTurnSpeed.get() - minTurnSpeed.get()) + minTurnSpeed.get()).toFloat()
        )

        if (NewTurnValue.get()) {
            val rotateMultiply = RandomUtils.nextFloat(minrotateMultiply.get(), maxrotateMultiply.get())
//            limitedRotation = RotationUtils.limitAngleChange3(RotationUtils.serverRotation, rotation, rotateMultiply)
            limitedRotation = RotationUtils.limitAngleChange2(RotationUtils.serverRotation, rotation, (Math.random() * (maxTurnSpeed.get() - minTurnSpeed.get()) + minTurnSpeed.get()).toFloat(),rotateMultiply)
            RotationUtils.setTargetRotation(limitedRotation, if (aacValue.get()) 35 else 0)
        }
        if (silentRotationValue.get()) {
            RotationUtils.setTargetRotation(limitedRotation, if (aacValue.get()) 35 else 0)
        } else {
            limitedRotation.toPlayer(mc.thePlayer)
        }
    }

    /**
     * Check if enemy is hitable with current rotations
     */
    private fun updateHitable() {
        // Disable hitable check if turn speed is zero
        if (maxTurnSpeed.get() <= 0F) {
            hitable = true
            return
        }

        val reach = min(rangeValue.get().toDouble() + 0.2, mc.thePlayer.getDistanceToEntity(target!!).toDouble())

        if (raycastValue.get()) {
            val raycastedEntity = RaycastUtils.raycastEntity(reach) {
                (!livingRaycastValue.get() || it is EntityLivingBase && it !is EntityArmorStand) &&
                        (isEnemy(it) || raycastIgnoredValue.get() || aacValue.get() && mc.theWorld.getEntitiesWithinAABBExcludingEntity(
                            it,
                            it.entityBoundingBox
                        ).isNotEmpty())
            }

            if (raycastValue.get() && raycastedEntity is EntityLivingBase
                && !EntityUtils.isFriend(raycastedEntity)
            )
                currentTarget = raycastedEntity

            hitable = if (maxTurnSpeed.get() > 0F) currentTarget == raycastedEntity else true
        } else
            hitable = RotationUtils.isFaced(currentTarget, reach)
    }

    /**
     * Start blocking
     */
    private fun startBlocking(interactEntity: Entity, interact: Boolean) {
        val noSlow = LiquidBounce.moduleManager.noSlow
        if (noSlow.state && noSlow.mode.get().equals("AAC4.4.0", ignoreCase = true)) {
            if (!MovementUtils.isMoving()) {
                if (interact) {
                    mc.netHandler.addToSendQueue(C02PacketUseEntity(interactEntity, interactEntity.positionVector))
                    mc.netHandler.addToSendQueue(C02PacketUseEntity(interactEntity, C02PacketUseEntity.Action.INTERACT))
                }
                mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()))
                noSlow.noSlowhasBlocked = true
            }
        } else {
            if (interact) {
                mc.netHandler.addToSendQueue(C02PacketUseEntity(interactEntity, interactEntity.positionVector))
                mc.netHandler.addToSendQueue(C02PacketUseEntity(interactEntity, C02PacketUseEntity.Action.INTERACT))
            }
            mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()))
        }
        blockingStatus = true
    }


    /**
     * Stop blocking
     */
    private fun stopBlocking() {
        if (blockingStatus) {
            val noSlow = LiquidBounce.moduleManager.noSlow
            if (noSlow.state && noSlow.mode.get().equals("AAC4.4.0", ignoreCase = true)) {
                if (noSlow.noSlowhasBlocked) {
                    mc.netHandler.addToSendQueue(
                        C07PacketPlayerDigging(
                            C07PacketPlayerDigging.Action.RELEASE_USE_ITEM,
                            BlockPos.ORIGIN,
                            EnumFacing.UP
                        )
                    )
                    noSlow.noSlowhasBlocked = false
                }
            } else {
                mc.netHandler.addToSendQueue(
                    C07PacketPlayerDigging(
                        C07PacketPlayerDigging.Action.RELEASE_USE_ITEM,
                        BlockPos.ORIGIN,
                        EnumFacing.DOWN
                    )
                )
            }
            blockingStatus = false
        }
    }

    private fun onVelocity() {
        val velocity = LiquidBounce.moduleManager.velocity
        velocity.onVelocityReduce()
    }

    /**
     * Check if run should be cancelled
     */
    private val cancelRun: Boolean
        get() = mc.thePlayer.isSpectator || !isAlive(mc.thePlayer)
                || LiquidBounce.moduleManager.blink.state || LiquidBounce.moduleManager.freeCam.state || isScaffold

    /**
     * Check if [entity] is alive
     */
    private fun isAlive(entity: EntityLivingBase) = entity.isEntityAlive && entity.health > 0


    /**
     * Check if player is able to block
     */
    private val canBlock: Boolean
        get() = mc.thePlayer.heldItem != null && mc.thePlayer.heldItem.item is ItemSword

    /**
     * Range
     */
    private val maxRange: Float
        get() = max(rangeValue.get(), prelookRangeValue.get())

    private fun getRange(entity: Entity) =
        (if (mc.thePlayer.getDistanceToEntityBox(entity) >= prelookRangeValue.get()) rangeValue.get() else prelookRangeValue.get()) - if (mc.thePlayer.isSprinting) rangeSprintReducementValue.get() else 0F

    private val isScaffold:Boolean
        get() = AutoDisablevalue.get() && LiquidBounce.moduleManager.scaffold.state

    /**
     * HUD Tag
     */
    override val tag: String?
        get() = targetModeValue.get()
}