package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import java.util.ArrayList

class RandomGenerator<T : Number?> {
    private var min: T? = null
    private var max: T? = null
    private val seed = ArrayList<T>()
    fun setUP(min: T, max: T) {
        if (this.min == null || this.max == null) {
            this.min = min
            this.max = max
            generated()
            return
        }
        if (this.min == min && this.max == max) return
        this.min = min
        this.max = max
        generated()
    }

    fun returnNext() : T {
        if (seed.isEmpty()) {
            generated()
        }
        val next = seed[0]
        seed.removeAt(0)
        return next
    }

    private fun generated() {
        seed.clear()
        val firstSeed = arrayListOf<T>()
        var lastRandom :Any? = null
        var i = 0
        val check = min != max
        while (i <= 10) {
            var random = if (min is Int) RandomUtils.nextInt(min as Int, max as Int) else if (min is Float) RandomUtils.nextFloat(min as Float, max as Float) else if (min is Double) RandomUtils.nextDouble(min as Double, max as Double) else return
            if (check && lastRandom != null && lastRandom == random) continue
            lastRandom = random
            firstSeed.add(random as T)
            i++
        }
        for (x in firstSeed) {
            repeat(10) {
                seed.add(x)
            }
        }
    }
}