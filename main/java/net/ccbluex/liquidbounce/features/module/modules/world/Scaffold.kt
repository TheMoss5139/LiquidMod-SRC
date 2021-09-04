/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https:github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.render.BlockOverlay
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.block.BlockUtils.canBeClicked
import net.ccbluex.liquidbounce.utils.block.BlockUtils.isReplaceable
import net.ccbluex.liquidbounce.utils.block.PlaceInfo
import net.ccbluex.liquidbounce.utils.block.PlaceInfo.Companion.get
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TimeUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.settings.GameSettings
import net.minecraft.init.Blocks
import net.minecraft.item.ItemBlock
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.util.*
import org.lwjgl.input.Keyboard
import java.awt.Color
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin


@ModuleInfo(
    name = "Scaffold",
    description = "Automatically places blocks beneath your feet.",
    category = ModuleCategory.WORLD,
    keyBind = Keyboard.KEY_I
)
class Scaffold : Module() {
    /**
     * OPTIONS
     */
    // Mode
    val modeValue: ListValue = ListValue("Mode", arrayOf("Normal", "Rewinside", "Expand"), "Normal")

    // Delay
    private val maxDelayValue: IntegerValue = object : IntegerValue("MaxDelay", 0, 0, 1000) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = minDelayValue.get()
            if (i > newValue) set(i)
        }
    }

    private val minDelayValue: IntegerValue = object : IntegerValue("MinDelay", 0, 0, 1000) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = maxDelayValue.get()
            if (i < newValue) set(i)
        }
    }
    private val placeableDelay = BoolValue("PlaceableDelay", false)

    // AutoBlock
    private val autoBlockValue = BoolValue("AutoBlock", true)
    private val stayAutoBlock = BoolValue("StayAutoBlock", false)

    // Basic stuff
    val sprintValue = BoolValue("Sprint", true)
    private val swingValue = BoolValue("Swing", true)
    private val searchValue = BoolValue("Search", true)
    private val searchLengthValue = IntegerValue("SearchLength", 1, 1, 5)
    private val downValue = BoolValue("Down", true)
    private val placeModeValue = ListValue("PlaceTiming", arrayOf("Pre", "Post"), "Post")

    // Eagle
    private val eagleValue = BoolValue("Eagle", false)
    private val eagleSilentValue = BoolValue("EagleSilent", false)
    private val blocksToEagleValue = IntegerValue("BlocksToEagle", 0, 0, 10)

    // Expand
    private val expandLengthValue = IntegerValue("ExpandLength", 5, 1, 6)

    // Rotations
    private val rotationsValue = BoolValue("Rotations", true)
    private val keepLengthValue = IntegerValue("KeepRotationLength", 0, 0, 20)
    private val keepRotationValue = BoolValue("KeepRotation", false)

    // Zitter
    private val zitterValue = BoolValue("Zitter", false)
    private val zitterModeValue = ListValue("ZitterMode", arrayOf("Teleport", "Smooth"), "Teleport")
    private val zitterSpeed = FloatValue("ZitterSpeed", 0.13f, 0.1f, 0.3f)
    private val zitterStrength = FloatValue("ZitterStrength", 0.072f, 0.05f, 0.2f)

    // Game
    private val timerValue = FloatValue("Timer", 1f, 0.1f, 10f)
    private val speedModifierValue = FloatValue("SpeedModifier", 1f, 0f, 2f)

    // Safety
    private val sameYValue = BoolValue("SameY", false)
    private val safeWalkValue = BoolValue("SafeWalk", true)
    private val airSafeValue = BoolValue("AirSafe", false)

    // Visuals
    private val counterDisplayValue = BoolValue("Counter", true)
    private val markValue = BoolValue("Mark", false)
    private val smartValue = BoolValue("Smart", false)
    private val strafeValue = BoolValue("Strafe", false)
    private val randomCenterValue = BoolValue("RandomVec", false)
    private val predictValue = BoolValue("Predict", false)

    /**
     * MODULE
     */
    // Target block
    private var targetPlace: PlaceInfo? = null


    // Launch position
    private var launchY = 0

    // Rotation lock
    private var lockRotation: Rotation? = null

    // Auto block slot
    private var slot = 0

    // Zitter Smooth
    private var zitterDirection = false

    // Delay
    private val delayTimer = MSTimer()
    private val zitterTimer = MSTimer()
    private var delay: Long = 0

    // Eagle
    private var placedBlocksWithoutEagle = 0
    private var eagleSneaking = false

    // Down
    private var shouldGoDown = false

    //SameY
    private var jumped = false

    /**
     * Enable module
     */
    override fun onEnable() {
        if (mc.thePlayer == null) return
        launchY = mc.thePlayer.posY.toInt()
        jumped = false
    }

    /**
     * Update event
     *
     * @param event
     */
    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        mc.timer.timerSpeed = timerValue.get()
        shouldGoDown = downValue.get() && GameSettings.isKeyDown(mc.gameSettings.keyBindSneak) && getBlocksAmount() > 1
        if (shouldGoDown) mc.gameSettings.keyBindSneak.pressed = false
        if (mc.thePlayer.fallDistance > 1 || !MovementUtils.isMoving(true)) {
            launchY = mc.thePlayer.posY.toInt()
            if (jumped) {
                jumped = false
            }
        }
        if (mc.thePlayer.onGround) {
            if (jumped) {
                jumped = false
            }
            launchY = mc.thePlayer.posY.toInt()
            val mode = modeValue!!.get()

            // Rewinside scaffold mode
            if (mode.equals("Rewinside", ignoreCase = true)) {
                MovementUtils.strafe(0.2f)
                mc.thePlayer.motionY = 0.0
            }

            // Smooth Zitter
            if (zitterValue.get() && zitterModeValue.get().equals("smooth", ignoreCase = true)) {
                if (!GameSettings.isKeyDown(mc.gameSettings.keyBindRight)) mc.gameSettings.keyBindRight.pressed = false
                if (!GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)) mc.gameSettings.keyBindLeft.pressed = false
                if (zitterTimer.hasTimePassed(100)) {
                    zitterDirection = !zitterDirection
                    zitterTimer.reset()
                }
                if (zitterDirection) {
                    mc.gameSettings.keyBindRight.pressed = true
                    mc.gameSettings.keyBindLeft.pressed = false
                } else {
                    mc.gameSettings.keyBindRight.pressed = false
                    mc.gameSettings.keyBindLeft.pressed = true
                }
            }

            // Eagle
            if (eagleValue.get() && !shouldGoDown) {
                if (placedBlocksWithoutEagle >= blocksToEagleValue.get()) {
                    val shouldEagle = mc.theWorld.getBlockState(
                        BlockPos(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY - 1.0, mc.thePlayer.posZ
                        )
                    ).block === Blocks.air
                    if (eagleSilentValue.get()) {
                        if (eagleSneaking != shouldEagle) {
                            mc.netHandler.addToSendQueue(
                                C0BPacketEntityAction(
                                    mc.thePlayer,
                                    if (shouldEagle) C0BPacketEntityAction.Action.START_SNEAKING else C0BPacketEntityAction.Action.STOP_SNEAKING
                                )
                            )
                        }
                        eagleSneaking = shouldEagle
                    } else mc.gameSettings.keyBindSneak.pressed = shouldEagle
                    placedBlocksWithoutEagle = 0
                } else placedBlocksWithoutEagle++
            }

            // Zitter
            if (zitterValue.get() && zitterModeValue.get().equals("teleport", ignoreCase = true)) {
                MovementUtils.strafe(zitterSpeed.get())
                val yaw = Math.toRadians(mc.thePlayer.rotationYaw + if (zitterDirection) 90.0 else -90.0)
                mc.thePlayer.motionX -= sin(yaw) * zitterStrength.get()
                mc.thePlayer.motionZ += cos(yaw) * zitterStrength.get()
                zitterDirection = !zitterDirection
            }
        }
    }

    @EventTarget
    fun onJump(event:JumpEvent) {
        jumped = true
        if (!keepRotationValue.get() && sameYValue.get()) {
            RotationUtils.setTargetRotation(Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch), 0)
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (mc.thePlayer == null) return
        val packet = event.packet

        // AutoBlock
        if (packet is C09PacketHeldItemChange) {
            slot = packet.slotId
        }
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        val eventState = event.eventState

        // Lock Rotation
        if (rotationsValue.get() && keepRotationValue.get() && lockRotation != null) RotationUtils.setTargetRotation(
            lockRotation
        )

        // Place block
        if (placeModeValue.get().equals(eventState.stateName, ignoreCase = true)) place()

        // Update and search for new block
        if (eventState === EventState.PRE) update()

        // Reset placeable delay
        if (targetPlace == null && placeableDelay.get()) delayTimer.reset()
    }

    private fun update() {
        if (if (autoBlockValue.get()) InventoryUtils.findAutoBlockBlock() == -1 else mc.thePlayer.heldItem == null ||
                    mc.thePlayer.heldItem.item !is ItemBlock
        ) return
        findBlock(modeValue!!.get().equals("expand", ignoreCase = true))
    }

    /**
     * Search for new target block
     */
    private fun findBlock(expand: Boolean) {
        val blockPosition = if (shouldGoDown) if (mc.thePlayer.posY == mc.thePlayer.posY.toInt() + 0.5) BlockPos(
            mc.thePlayer.posX,
            mc.thePlayer.posY - 0.6,
            mc.thePlayer.posZ
        ) else BlockPos(
            mc.thePlayer.posX, mc.thePlayer.posY - 0.6, mc.thePlayer.posZ
        ).down() else if (mc.thePlayer.posY == mc.thePlayer.posY.toInt() + 0.5) BlockPos(
            mc.thePlayer
        ) else BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ).down()
        if (predictValue.get()) blockPosition.add(mc.thePlayer.motionX * 2, mc.thePlayer.motionY * 2, mc.thePlayer.motionZ * 2)
        if (!expand && !isReplaceable(blockPosition))  {
            return
        }
        if (sameYValue.get() && !keepRotationValue.get() && mc.thePlayer.motionY > 0) {
            if (rotationsValue.get()) {
                RotationUtils.setTargetRotation(Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch), 0)
            }
            return
        }
        val defaultSearch = search(blockPosition, !shouldGoDown)
        if (defaultSearch != null) {
            if (rotationsValue.get()) {
                RotationUtils.setTargetRotation(defaultSearch.rotation, keepLengthValue.get())
                lockRotation = defaultSearch.rotation
            }
            targetPlace = defaultSearch.placeInfo
        }
        if (expand) {
            for (i in 0 until expandLengthValue.get()) {
                val expandResult = search(
                    blockPosition.add(
                        if (mc.thePlayer.horizontalFacing == EnumFacing.WEST) -i else if (mc.thePlayer.horizontalFacing == EnumFacing.EAST) i else 0,
                        0,
                        if (mc.thePlayer.horizontalFacing == EnumFacing.NORTH) -i else if (mc.thePlayer.horizontalFacing == EnumFacing.SOUTH) i else 0
                    ), false
                )
                if (expandResult != null) {
                    if (rotationsValue.get()) {
                        RotationUtils.setTargetRotation(expandResult.rotation, keepLengthValue.get())
                        lockRotation = expandResult.rotation
                    }
                    targetPlace = expandResult.placeInfo
                    return
                }
            }
        } else if (searchValue.get()) {
            var bestRotation : PlaceRotation? = null
            var bestDistance = 999.0
            for (y in -searchLengthValue.get()..0) {
                for (x in -searchLengthValue.get()..searchLengthValue.get()) {
                    for (z in -searchLengthValue.get()..searchLengthValue.get()) {
                        val result = search(blockPosition.add(x, y, z), !shouldGoDown)
                        if (result != null) {
                            val position = result.placeInfo.vec3
                            val distance = mc.thePlayer.getDistance(position.xCoord, position.yCoord, position.zCoord)
                            if (distance < bestDistance) {
                                bestRotation = result
                                bestDistance = distance
                            }
                        }
                    }
                }
            }
            if (bestRotation != null) {
                if (rotationsValue.get()) {
                    RotationUtils.setTargetRotation(bestRotation.rotation, keepLengthValue.get())
                    lockRotation = bestRotation.rotation
                }
                targetPlace = bestRotation.placeInfo
            }
            return
        }
    }

    /**
     * Place target block
     */
    private fun place() {
        if (targetPlace == null) {
            if (placeableDelay.get()) delayTimer.reset()
            return
        }
        if (!delayTimer.hasTimePassed(delay)) return
        if (sameYValue.get() && launchY - 1 != targetPlace!!.vec3.yCoord.toInt()) return
        var blockSlot = -1
        var itemStack = mc.thePlayer.heldItem
        if (mc.thePlayer.heldItem == null || mc.thePlayer.heldItem.item !is ItemBlock) {
            if (!autoBlockValue.get()) return
            blockSlot = InventoryUtils.findAutoBlockBlock()
            if (blockSlot == -1) return
            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(blockSlot - 36))
            itemStack = mc.thePlayer.inventoryContainer.getSlot(blockSlot).stack
        }
        if (mc.playerController.onPlayerRightClick(
                mc.thePlayer, mc.theWorld, itemStack, targetPlace!!.blockPos,
                targetPlace!!.enumFacing, targetPlace!!.vec3
            )
        ) {
            delayTimer.reset()
            delay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())
            if (mc.thePlayer.onGround) {
                val modifier = speedModifierValue.get()
                mc.thePlayer.motionX *= modifier.toDouble()
                mc.thePlayer.motionZ *= modifier.toDouble()
            }
            if (swingValue.get()) mc.thePlayer.swingItem() else mc.netHandler.addToSendQueue(C0APacketAnimation())
        }
        if (!stayAutoBlock.get() && blockSlot >= 0) mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))

        // Reset
        targetPlace = null
    }

    /**
     * Disable scaffold module
     */
    override fun onDisable() {
        if (mc.thePlayer == null) return
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) {
            mc.gameSettings.keyBindSneak.pressed = false
            if (eagleSneaking) mc.netHandler.addToSendQueue(
                C0BPacketEntityAction(
                    mc.thePlayer,
                    C0BPacketEntityAction.Action.STOP_SNEAKING
                )
            )
        }
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindRight)) mc.gameSettings.keyBindRight.pressed = false
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)) mc.gameSettings.keyBindLeft.pressed = false
        lockRotation = null
        mc.timer.timerSpeed = 1f
        shouldGoDown = false
        if (slot != mc.thePlayer.inventory.currentItem) mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
    }
    @EventTarget
    fun onStrafe(strafeEvent: StrafeEvent) {
        if (sameYValue.get() && !keepRotationValue.get() && mc.thePlayer.motionY < 0 && jumped) {
            strafeEvent.cancelEvent()
            return
        }
        if (!strafeValue.get()) return
        RotationUtils.targetRotation ?: return
        strafeEvent.cancelEvent()
        RotationUtils.targetRotation.applyStrafeToPlayer(strafeEvent)
    }


    /**
     * Entity movement event
     *
     * @param event
     */
    @EventTarget
    fun onMove(event: MoveEvent) {
        if (!safeWalkValue.get() || shouldGoDown) return
        if (airSafeValue.get() || mc.thePlayer.onGround) event.isSafeWalk = true
    }

    /**
     * Scaffold visuals
     *
     * @param event
     */
    @EventTarget
    fun onRender2D(event: Render2DEvent?) {
        if (counterDisplayValue.get()) {
            GlStateManager.pushMatrix()
            val blockOverlay = LiquidBounce.moduleManager.getModule(BlockOverlay::class.java) as BlockOverlay?
            if (blockOverlay!!.state && blockOverlay.infoValue.get() && blockOverlay.currentBlock != null) GlStateManager.translate(
                0f,
                15f,
                0f
            )
            val info = "Blocks: ${getBlocksAmount()}"
            val scaledResolution = ScaledResolution(mc)
            RenderUtils.drawBorderedRect(
                (scaledResolution.scaledWidth / 2 - 2).toFloat(),
                (scaledResolution.scaledHeight / 2 + 5).toFloat(),
                (scaledResolution.scaledWidth / 2 + Fonts.font40.getStringWidth(info) + 2).toFloat(),
                (scaledResolution.scaledHeight / 2 + 16).toFloat(),
                3f,
                Color.BLACK.rgb,
                Color.BLACK.rgb
            )
            GlStateManager.resetColor()
            Fonts.font40.drawString(
                info,
                scaledResolution.scaledWidth / 2,
                scaledResolution.scaledHeight / 2 + 7,
                Color.WHITE.rgb
            )
            GlStateManager.popMatrix()
        }
    }

    /**
     * Scaffold visuals
     *
     * @param event
     */
    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        if (!markValue.get()) return
        for (i in 0 until if (modeValue!!.get()
                .equals("Expand", ignoreCase = true)
        ) expandLengthValue.get() + 1 else 2) {
            val blockPos = BlockPos(
                mc.thePlayer.posX + if (mc.thePlayer.horizontalFacing == EnumFacing.WEST) -i else if (mc.thePlayer.horizontalFacing == EnumFacing.EAST) i else 0,
                mc.thePlayer.posY - (if (mc.thePlayer.posY == mc.thePlayer.posY.toInt() + 0.5) 0.0 else 1.0) - if (shouldGoDown) 1.0 else 0.0,
                mc.thePlayer.posZ + if (mc.thePlayer.horizontalFacing == EnumFacing.NORTH) -i else if (mc.thePlayer.horizontalFacing == EnumFacing.SOUTH) i else 0
            )
            val placeInfo = get(blockPos)
            if (isReplaceable(blockPos) && placeInfo != null) {
                RenderUtils.drawBlockBox(blockPos, Color(68, 117, 255, 100), false)
                break
            }
        }
    }

    /**
     * Search for placeable block
     *
     * @param blockPosition pos
     * @param checks        visible
     * @return
     */
    private fun search(blockPosition: BlockPos, checks: Boolean): PlaceRotation? {
        if (smartValue.get()) {
            val result = smart(blockPosition, checks)
            if (result != null) return result
        }
        if (!isReplaceable(blockPosition)) return null
        val eyesPos = Vec3(
            mc.thePlayer.posX,
            mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.getEyeHeight(),
            mc.thePlayer.posZ
        )


        var placeRotation: PlaceRotation? = null
        for (side in EnumFacing.values()) {
            val neighbor = blockPosition.offset(side)
            if (!canBeClicked(neighbor)) continue
            val dirVec = Vec3(side.directionVec)
            var xSearch = 0.1
            while (xSearch <= 0.9) {
                var ySearch = 0.1
                while (ySearch <= 0.9) {
                    var zSearch = 0.1
                    while (zSearch <= 0.9) {
                        var posVec = Vec3(blockPosition).addVector(xSearch, ySearch, zSearch)
                        if (randomCenterValue.get()) {
                            posVec = Vec3(blockPosition).addVector(RandomUtils.nextDouble(0.1,0.9), RandomUtils.nextDouble(0.1,0.9), RandomUtils.nextDouble(0.1,0.9))
                        }
                        val distanceSqPosVec = eyesPos.squareDistanceTo(posVec)
                        val hitVec = posVec.add(Vec3(dirVec.xCoord * 0.5, dirVec.yCoord * 0.5, dirVec.zCoord * 0.5))
                        if (checks && (eyesPos.squareDistanceTo(hitVec) > 18.0 || distanceSqPosVec > eyesPos.squareDistanceTo(
                                posVec.add(dirVec)
                            ) || mc.theWorld.rayTraceBlocks(eyesPos, hitVec, false, true, false) != null)
                        ) {
                            zSearch += 0.1
                            continue
                        }

                        // face block
                        val diffX = hitVec.xCoord - eyesPos.xCoord
                        val diffY = hitVec.yCoord - eyesPos.yCoord
                        val diffZ = hitVec.zCoord - eyesPos.zCoord
                        val diffXZ = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ).toDouble()
                        val rotation = Rotation(
                            MathHelper.wrapAngleTo180_float(Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f),
                            MathHelper.wrapAngleTo180_float((-Math.toDegrees(atan2(diffY, diffXZ))).toFloat())
                        )
                        val rotationVector = RotationUtils.getVectorForRotation(rotation)
                        val vector = eyesPos.addVector(
                            rotationVector.xCoord * 5,
                            rotationVector.yCoord * 5,
                            rotationVector.zCoord * 5
                        )
                        val obj = mc.theWorld.rayTraceBlocks(eyesPos, vector, false, false, true)
                        if (!(obj.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && obj.blockPos == neighbor)) {
                            zSearch += 0.1
                            continue
                        }
                        if (placeRotation == null || RotationUtils.getRotationDifference(rotation) < RotationUtils.getRotationDifference(
                                placeRotation.rotation
                            )
                        ) placeRotation = PlaceRotation(PlaceInfo(neighbor, side.opposite, hitVec), rotation)
                        zSearch += 0.1
                    }
                    ySearch += 0.1
                }
                xSearch += 0.1
            }
        }
        if (placeRotation == null) return null
        return placeRotation
    }

    private fun smart(blockPosition: BlockPos, checks: Boolean): PlaceRotation? {
        if (!isReplaceable(blockPosition)) return null
        val eyesPos = Vec3(
            mc.thePlayer.posX,
            mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.getEyeHeight(),
            mc.thePlayer.posZ
        )
        var placeRotation: PlaceRotation? = null
        for (side in EnumFacing.values()) {
            val neighbor = blockPosition.offset(side)
            if (!canBeClicked(neighbor)) continue
            val dirVec = Vec3(side.directionVec)
            var posVec = Vec3(blockPosition)
            if (randomCenterValue.get()) {
                posVec = Vec3(blockPosition).addVector(RandomUtils.nextDouble(0.1,0.9), RandomUtils.nextDouble(0.1,0.9), RandomUtils.nextDouble(0.1,0.9))
            }

            val distanceSqPosVec = eyesPos.squareDistanceTo(posVec)
            val hitVec = posVec.add(Vec3(dirVec.xCoord * 0.5, dirVec.yCoord * 0.5, dirVec.zCoord * 0.5))
            if (checks && (eyesPos.squareDistanceTo(hitVec) > 18.0 || distanceSqPosVec > eyesPos.squareDistanceTo(
                    posVec.add(dirVec)
                ) || mc.theWorld.rayTraceBlocks(eyesPos, hitVec, false, true, false) != null)
            ) {
                continue
            }
            val diffX = posVec.xCoord - eyesPos.xCoord
            val diffY = posVec.yCoord - eyesPos.yCoord
            val diffZ = posVec.zCoord - eyesPos.zCoord
            val diffXZ = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ).toDouble()
            val yaw = MathHelper.wrapAngleTo180_float(Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f)
            val smartYaw = (MovementUtils.CustomRotation(yaw) * 180f / Math.PI).toFloat()
            val rotation = Rotation(
                smartYaw,
                MathHelper.wrapAngleTo180_float((-Math.toDegrees(atan2(diffY, diffXZ))).toFloat()))
            val rotationVector = RotationUtils.getVectorForRotation(rotation)
            val vector = eyesPos.addVector(
                rotationVector.xCoord * 5,
                rotationVector.yCoord * 5,
                rotationVector.zCoord * 5
            )
            val obj = mc.theWorld.rayTraceBlocks(eyesPos, vector, false, false, true)
            if (!(obj.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && obj.blockPos == neighbor)) {
                continue
            }
            if (placeRotation == null || RotationUtils.getRotationDifference(rotation, Rotation(smartYaw, mc.thePlayer.rotationPitch)) < RotationUtils.getRotationDifference(
                    placeRotation.rotation
                    , Rotation(smartYaw, mc.thePlayer.rotationPitch))
            ) placeRotation = PlaceRotation(PlaceInfo(neighbor, side.opposite, hitVec), rotation)
        }
        if (placeRotation == null) return null
        return placeRotation
    }

    /**
     * @return hotbar blocks amount
     */
    private fun getBlocksAmount(): Int {
        var amount = 0
        for (i in 36..44) {
            val itemStack = mc.thePlayer.inventoryContainer.getSlot(i).stack
            if (itemStack != null && itemStack.item is ItemBlock) amount += itemStack.stackSize
        }
        return amount
    }

    override val tag: String?
        get() = modeValue.get()
}