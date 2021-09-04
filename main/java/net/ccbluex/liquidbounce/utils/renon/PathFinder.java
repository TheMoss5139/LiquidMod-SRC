package net.ccbluex.liquidbounce.utils.renon;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class PathFinder {

    protected Minecraft mc = Minecraft.getMinecraft();
    public ArrayList<Node> open = new ArrayList<>();
    public ArrayList<Node> close = new ArrayList<>();
    public ArrayList<Vec3> addVecWays = new ArrayList<>();
    public Comparator<Node> thecomparator = Comparator.comparingDouble(Node::getFcost);
    public ArrayList<Vec3> founded = null;
    public Vec3 startpoint = null;
    public Vec3 endpoint = null;
    public Vec3 startpoint_unfloor = null;
    public Vec3 endpoint_unfloor = null;
    public List<Block> arrays = Arrays.asList(Blocks.air,
            Blocks.carpet, Blocks.tallgrass, Blocks.portal,
            Blocks.redstone_wire.getBlockState().getBlock(),
            Blocks.water, Blocks.ladder, Blocks.fire, Blocks.standing_sign,
            Blocks.wall_sign, Blocks.red_flower, Blocks.yellow_flower, Blocks.water,
            Blocks.flowing_water, Blocks.flower_pot, Blocks.lava, Blocks.flowing_lava,
            Blocks.vine, Blocks.rail, Blocks.activator_rail, Blocks.golden_rail,
            Blocks.detector_rail, Blocks.cocoa, Blocks.wooden_pressure_plate,
            Blocks.stone_pressure_plate, Blocks.heavy_weighted_pressure_plate, Blocks.light_weighted_pressure_plate,
            Blocks.powered_comparator, Blocks.powered_repeater, Blocks.unpowered_comparator, Blocks.unpowered_repeater);

    public PathFinder() {}

    public Vec3 floor(Vec3 vec) {
        return new Vec3(((int)vec.xCoord) + 0.5, (int)vec.yCoord, ((int)vec.zCoord) - 0.5);
    }

    private boolean checkvaildposition(Vec3 vec3) {
        if (vec3 == null) {
            return false;
        }
        BlockPos pos = new BlockPos(vec3.xCoord, vec3.yCoord + 0.5, vec3.zCoord);
        Block block = getBlock(pos);
        if (getWhitelist_block().contains(block)) {
            BlockPos pos2 = pos.add(0, 1, 0);
            //UPPER Block
            if (getWhitelist_block().contains(getBlock(pos2))) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<Vec3> compute(int findloopcount, Vec3 startpos, Vec3 endpos) {
        if (!checkvaildposition(endpos) || startpos == null || endpos == null) {
            return null;
        }
        startpoint_unfloor = startpos;
        endpoint_unfloor = endpos;
        startpoint = floor(startpos);
        endpoint = floor(endpos);
        founded = null;
        close.clear();
        open.clear();
        ArrayList<Vec3> test = new ArrayList<>();
        test.add(startpoint);
        Node startnode = new Node(startpoint, test);
        if (check_is_sameposition_as_endpoint(startnode.current)) {
            founded = startnode.passed;
        }
        open.add(startnode);
        while (!open.isEmpty() && findloopcount > 0 && founded == null) {
            finding();
            findloopcount--;
        }
        return founded;
    }

    public void finding() {
        getNearestPoint();
        Node nearestnode = open.get(0);
        open.remove(0);
        close.add(nearestnode);
        for (Vec3 way : addVecWays) {
            Vec3 newway = nearestnode.current;
            newway = newway.add(way);
            if (!checkcanpass(nearestnode.current, newway)) {
                continue;
            }
            if (checkexistonclose(newway)) {
                continue;
            }

            if (check_is_sameposition_as_endpoint(newway)) {
                founded = nearestnode.passed;
                return;
            }

            //Add Self Position
            ArrayList<Vec3> list = nearestnode.passed;
            list.add(nearestnode.current);
            Node newnode = new Node(newway, list);
            // Setting
            newnode.hcost = newway.distanceTo(startpoint);
            newnode.unfloorhcost = newway.distanceTo(startpoint_unfloor);
            newnode.gcost = newway.distanceTo(endpoint);
            newnode.unfloorgcost = newway.distanceTo(endpoint_unfloor);
            open.add(newnode);
        }
    }

    public boolean checkexistonclose(Vec3 tochecknode) {
        for (Node closednode : close) {
            if (closednode.current.xCoord == tochecknode.xCoord &&
                    closednode.current.yCoord == tochecknode.yCoord &&
                    closednode.current.zCoord == tochecknode.zCoord) {
                return true;
            }
        }

        for (Node closednode : open) {
            if (closednode.current.xCoord == tochecknode.xCoord &&
                    closednode.current.yCoord == tochecknode.yCoord &&
                    closednode.current.zCoord == tochecknode.zCoord) {
                return true;
            }
        }
        return false;
    }

    public boolean checkcanpass(Vec3 lastVec,Vec3 vec3) {
        Vec3[] vec2 = {lastVec, vec3};

        BlockPos rayTraceBlock = rayTrace(lastVec.addVector(0,0.5,0),vec3.addVector(0,0.5,0));

        BlockPos rayTraceBlock2 = rayTrace(vec2[0].addVector(0,1.5,0),vec2[1].addVector(0,1.5,0));

        if (rayTraceBlock == null) {
            if (rayTraceBlock2 == null) {
                return true;
            }
            return false;
        }
//        BlockPos pos = new BlockPos(vec3.xCoord, vec3.yCoord + 0.5, vec3.zCoord);
//        if (getWhitelist_block().contains(getBlock(pos))) {
//            BlockPos pos2 = pos.add(0, 1, 0);
//            //UPPER Block
//            if (getWhitelist_block().contains(getBlock(pos2))) {
//                return true;
//            }
//        }

        return false;
    }

    @Nullable
    private BlockPos rayTrace(Vec3 start, Vec3 end) {
        MovingObjectPosition result = rayTraceBlocks(start, end, false, true, false);

        if (result != null && result.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            return result.getBlockPos();
        }
        return null;
    }

    public MovingObjectPosition rayTraceBlocks(Vec3 vec31, Vec3 vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
        if (!Double.isNaN(vec31.xCoord) && !Double.isNaN(vec31.yCoord) && !Double.isNaN(vec31.zCoord)) {
            if (!Double.isNaN(vec32.xCoord) && !Double.isNaN(vec32.yCoord) && !Double.isNaN(vec32.zCoord)) {
                int xPos_end = MathHelper.floor_double(vec32.xCoord);
                int yPos_end = MathHelper.floor_double(vec32.yCoord);
                int zPos_end = MathHelper.floor_double(vec32.zCoord);
                int xPos_start = MathHelper.floor_double(vec31.xCoord);
                int yPos_start = MathHelper.floor_double(vec31.yCoord);
                int zPos_start = MathHelper.floor_double(vec31.zCoord);
                BlockPos blockpos = new BlockPos(xPos_start, yPos_start, zPos_start);
                IBlockState iblockstate = this.getBlockState(blockpos);
                Block block = iblockstate.getBlock();
                MovingObjectPosition movingobjectposition2;
                if ((!ignoreBlockWithoutBoundingBox || block.getCollisionBoundingBox(mc.theWorld, blockpos, iblockstate) != null) && block.canCollideCheck(iblockstate, stopOnLiquid)) {
                    movingobjectposition2 = block.collisionRayTrace(mc.theWorld, blockpos, vec31, vec32);
                    if (movingobjectposition2 != null && !getWhitelist_block().contains(block)) {
                        return movingobjectposition2;
                    }
                }

                movingobjectposition2 = null;
                int var16 = 200;

                while(var16-- >= 0) {
                    if (Double.isNaN(vec31.xCoord) || Double.isNaN(vec31.yCoord) || Double.isNaN(vec31.zCoord)) {
                        return null;
                    }

                    if (xPos_start == xPos_end && yPos_start == yPos_end && zPos_start == zPos_end) {
                        return returnLastUncollidableBlock ? movingobjectposition2 : null;
                    }

                    boolean flag2 = true;
                    boolean flag = true;
                    boolean flag1 = true;
                    double d0 = 999.0D;
                    double d1 = 999.0D;
                    double d2 = 999.0D;
                    if (xPos_end > xPos_start) {
                        d0 = (double)xPos_start + 1.0D;
                    } else if (xPos_end < xPos_start) {
                        d0 = (double)xPos_start + 0.0D;
                    } else {
                        flag2 = false;
                    }

                    if (yPos_end > yPos_start) {
                        d1 = (double)yPos_start + 1.0D;
                    } else if (yPos_end < yPos_start) {
                        d1 = (double)yPos_start + 0.0D;
                    } else {
                        flag = false;
                    }

                    if (zPos_end > zPos_start) {
                        d2 = (double)zPos_start + 1.0D;
                    } else if (zPos_end < zPos_start) {
                        d2 = (double)zPos_start + 0.0D;
                    } else {
                        flag1 = false;
                    }

                    double d3 = 999.0D;
                    double d4 = 999.0D;
                    double d5 = 999.0D;
                    double d6 = vec32.xCoord - vec31.xCoord;
                    double d7 = vec32.yCoord - vec31.yCoord;
                    double d8 = vec32.zCoord - vec31.zCoord;
                    if (flag2) {
                        d3 = (d0 - vec31.xCoord) / d6;
                    }

                    if (flag) {
                        d4 = (d1 - vec31.yCoord) / d7;
                    }

                    if (flag1) {
                        d5 = (d2 - vec31.zCoord) / d8;
                    }

                    if (d3 == -0.0D) {
                        d3 = -1.0E-4D;
                    }

                    if (d4 == -0.0D) {
                        d4 = -1.0E-4D;
                    }

                    if (d5 == -0.0D) {
                        d5 = -1.0E-4D;
                    }

                    EnumFacing enumfacing;
                    if (d3 < d4 && d3 < d5) {
                        enumfacing = xPos_end > xPos_start ? EnumFacing.WEST : EnumFacing.EAST;
                        vec31 = new Vec3(d0, vec31.yCoord + d7 * d3, vec31.zCoord + d8 * d3);
                    } else if (d4 < d5) {
                        enumfacing = yPos_end > yPos_start ? EnumFacing.DOWN : EnumFacing.UP;
                        vec31 = new Vec3(vec31.xCoord + d6 * d4, d1, vec31.zCoord + d8 * d4);
                    } else {
                        enumfacing = zPos_end > zPos_start ? EnumFacing.NORTH : EnumFacing.SOUTH;
                        vec31 = new Vec3(vec31.xCoord + d6 * d5, vec31.yCoord + d7 * d5, d2);
                    }

                    xPos_start = MathHelper.floor_double(vec31.xCoord) - (enumfacing == EnumFacing.EAST ? 1 : 0);
                    yPos_start = MathHelper.floor_double(vec31.yCoord) - (enumfacing == EnumFacing.UP ? 1 : 0);
                    zPos_start = MathHelper.floor_double(vec31.zCoord) - (enumfacing == EnumFacing.SOUTH ? 1 : 0);
                    blockpos = new BlockPos(xPos_start, yPos_start, zPos_start);
                    IBlockState iblockstate1 = this.getBlockState(blockpos);
                    Block block1 = iblockstate1.getBlock();
                    if (!ignoreBlockWithoutBoundingBox || block1.getCollisionBoundingBox(mc.theWorld, blockpos, iblockstate1) != null) {
                        if (block1.canCollideCheck(iblockstate1, stopOnLiquid)) {
                            MovingObjectPosition movingobjectposition1 = block1.collisionRayTrace(mc.theWorld, blockpos, vec31, vec32);
                            if (movingobjectposition1 != null && !getWhitelist_block().contains(block1)) {
                                return movingobjectposition1;
                            }
                        } else {
                            movingobjectposition2 = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, vec31, enumfacing, blockpos);
                        }
                    }
                }

                return returnLastUncollidableBlock ? movingobjectposition2 : null;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public List<Block> getWhitelist_block() {
        return arrays;
    }

    public Block getBlock(BlockPos pos) {
        return mc.theWorld.getBlockState(pos).getBlock();
    }

    public IBlockState getBlockState(BlockPos blockPos) {
        return mc.theWorld.getBlockState(blockPos);
    }

    public void getNearestPoint() {
        open.sort(thecomparator);
    }

    public boolean check_is_sameposition_as_endpoint(Vec3 tocheck) {
        tocheck = floor(tocheck);
        return endpoint.xCoord == tocheck.xCoord &&
                endpoint.yCoord == tocheck.yCoord &&
                endpoint.zCoord == tocheck.zCoord;
    }

    public void setRange(int range) {
        addVecWays.clear();
        for (int y = -range; y <= range;y++) {
            for (int x = -range; x <= range;x++) {
                for (int z = -range; z <= range;z++) {
                    addVecWays.add(new Vec3(x,y,z));
                }
            }
        }
    }
}
