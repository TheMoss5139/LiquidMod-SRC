package net.ccbluex.liquidbounce.utils.renon

import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import kotlin.math.atan2
import kotlin.math.sqrt

class PathFinder2 : MinecraftInstance() {

    private var loopcount = 0
    private var start: Vec3? = null
    private var end: Vec3? = null
    private var searchlength = 0
    private val quickpath = arrayListOf<NodeData>()
    private val fullpath = arrayListOf<NodeData>()
    var arrays = listOf(
        Blocks.air,
        Blocks.carpet, Blocks.tallgrass, Blocks.portal,
        Blocks.redstone_wire.blockState.block,
        Blocks.water, Blocks.ladder, Blocks.fire, Blocks.standing_sign,
        Blocks.wall_sign, Blocks.red_flower, Blocks.yellow_flower, Blocks.water,
        Blocks.flowing_water, Blocks.flower_pot, Blocks.lava, Blocks.flowing_lava,
        Blocks.vine, Blocks.rail, Blocks.activator_rail, Blocks.golden_rail,
        Blocks.detector_rail, Blocks.cocoa, Blocks.wooden_pressure_plate,
        Blocks.stone_pressure_plate, Blocks.heavy_weighted_pressure_plate, Blocks.light_weighted_pressure_plate,
        Blocks.powered_comparator, Blocks.powered_repeater, Blocks.unpowered_comparator, Blocks.unpowered_repeater
    )

    fun settingPath(loopcount: Int, start: Vec3, end: Vec3, searchlength: Int) {
        this.start = start
        this.end = end
        this.loopcount = loopcount
        this.searchlength = searchlength
        quickpath.clear()
        fullpath.clear()
        quickpath.add(NodeData(start, start, start.distanceTo(end)))
    }

    fun running(): ArrayList<NodeData>? {
        //Quick find path to target
        for (i in 0 until loopcount) {
            quickpath.sortBy { it.distancetoend }
            var nearestpath = quickpath[0]
            if (nearestpath.currentpos == end) {
                break
            }
            addPath(nearestpath.currentpos)
        }
        //Full find path or fix to the right ways
        while (quickpath.isNotEmpty()) {
            val currentnode = quickpath[0]
            val nextnode = quickpath[1]
            var islast = false
            if (2 <= quickpath.size)
                islast = true
            quickpath.removeAt(0)
            fullpath.add(currentnode)
            addFullPath(currentnode.currentpos, currentnode.lastpos)

            if (islast) {
                fullpath.add(nextnode)
                quickpath.clear()
            }
        }
        //return
        if (fullpath.isNotEmpty())
            return fullpath
        return null
    }

    fun addFullPath(thisvec3: Vec3, lastvec:Vec3) {
        val arraystomove = arrayListOf(NodeData(lastvec, lastvec, lastvec.distanceTo(thisvec3)))
        val bestmove = arrayListOf<NodeData>()
        for (i in 0 until loopcount) {
            //Get sorted Move
            arraystomove.sortBy { it.distancetoend }
            val nearest = arraystomove[0]
            //Check is same pos
            if (nearest.currentpos == thisvec3) {
                break
            }
            if (arraystomove.size > 1) {
                // Select Best Move
                bestmove.add(nearest)
            }
            //Add way
            val sl = -searchlength until searchlength
            for (x in sl) {
                for (y in sl) {
                    for (z in sl) {
                        val vec3 = Vec3(nearest.currentpos.xCoord + x, nearest.currentpos.yCoord + y, nearest.currentpos.zCoord + z)
                        val distance = vec3.distanceTo(thisvec3)
                        if (distance > 0)
                            continue
                        if (!isBypassBlock(nearest.currentpos, vec3) or !isBypassBlock(
                                nearest.currentpos.addVector(0.0, 1.0, 0.0),
                                vec3.addVector(0.0, 1.0, 0.0)
                            )
                        ) {
                            continue
                        }
                        arraystomove.add(NodeData(vec3, nearest.currentpos, distance))
                    }
                }
            }
        }
        //add best move to fullpath
        if (bestmove.isNotEmpty())
            fullpath.addAll(bestmove)
    }

    fun addPath(thisvec3: Vec3) {
        this.end ?: return
        val sl = -5 until 5
        var lastbestnode : NodeData? = null
        for (x in sl) {
            for (y in sl) {
                for (z in sl) {
                    val vec3 = Vec3(thisvec3.xCoord + x, thisvec3.yCoord + y, thisvec3.zCoord + z)
                    val distance = vec3.distanceTo(end)
                    if (distance > thisvec3.distanceTo(end))
                        continue
                    if (!isBypassBlock(thisvec3, vec3) or !isBypassBlock(
                            thisvec3.addVector(0.0, 1.0, 0.0),
                            vec3.addVector(0.0, 1.0, 0.0)
                        )
                    ) {
                        continue
                    }

                    if (lastbestnode == null || distance < lastbestnode.distancetoend)
                        lastbestnode = NodeData(vec3, thisvec3, distance)
                }
            }
        }
        quickpath.add(lastbestnode!!)
    }

    private fun isBypassBlock(start: Vec3, end: Vec3): Boolean {
        val endblock = BlockPos(end)
        for (i in 0 until loopcount) {
            val vector3d = RotationUtils.getVectorForRotation(getYawPitch(start, end))
            start.addVector(vector3d.xCoord, vector3d.yCoord, vector3d.zCoord)
            val pos = BlockPos(start)
            val stateblock = mc.theWorld.getBlockState(pos)
            if (stateblock.block !in arrays)
                return false
            if (pos == endblock)
                break
        }
        return true
    }

    fun getYawPitch(start: Vec3, end: Vec3): Rotation {
        val diffX: Double = end.xCoord - start.xCoord
        val diffY: Double = end.yCoord - start.yCoord
        val diffZ: Double = end.zCoord - start.zCoord

        return Rotation(
            MathHelper.wrapAngleTo180_float(
                Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f
            ), MathHelper.wrapAngleTo180_float(
                (-Math.toDegrees(atan2(diffY, sqrt(diffX * diffX + diffZ * diffZ)))).toFloat()
            )
        )
    }
}