package net.ccbluex.liquidbounce.utils.renon;

import net.minecraft.util.Vec3;

import java.util.ArrayList;

public class Node {
    public double hcost = 0;
    public double unfloorhcost = 0;
    public double gcost = 0;
    public double unfloorgcost = 0;
    public Vec3 current;
    public ArrayList<Vec3> passed;
    public Node(Vec3 currentpos, ArrayList<Vec3> havepassed) {
        this.current = currentpos;
        this.passed = havepassed;
    }

    public double getFcost() {
        return hcost + gcost + gcost;
    }
}
