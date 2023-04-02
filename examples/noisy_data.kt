
import examples.VelocityFilter as theFilter
import org.jetbrains.kotlinx.multik.api.*
import org.jetbrains.kotlinx.multik.ndarray.data.*
import src.array1D
import kotlin.random.Random


fun main() {
    fun randScale(noise: Double, scale: Double): Double {
        return (Random.nextDouble() - 0.5) * noise * scale
    }

    val xScale = 50.0
    val yScale = 20.0
    val noiseScale = 0.2
    val maxSteps = 200
    val dx = 1.0
    var x = 0.0
    var y = 0.0

    println("True x, True y, Noisy x, Noisy y, UKF x, UKF y")
    val filter = theFilter(x, y)
    for (i in 0 until maxSteps) {
        val params: array1D = mk.ndarray(mk[1.0])
        filter.predictSigmaPoints(params)
        x = (i.toDouble() + 1.0) * dx
        y = yScale * kotlin.math.sin(x / xScale)
        val noisyX = x
        val noisyY = y + randScale(noiseScale, yScale)
        filter.updateSigmaPoints(mk.ndarray(mk[noisyX, noisyY]))
        val currentState = filter.getState()
        println("$x, $y, $noisyX, $noisyY, ${currentState[0]}, ${currentState[1]}")
    }
}
