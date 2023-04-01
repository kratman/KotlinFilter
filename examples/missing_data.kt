
import examples.VelocityFilter as theFilter
import org.jetbrains.kotlinx.multik.api.*
import org.jetbrains.kotlinx.multik.ndarray.data.*
import src.array1D

fun main() {
    val cadence = 5
    val xScale = 50.0
    val yScale = 20.0
    val maxSteps = 200
    val dx = 1.0
    var x = 0.0
    var y = 0.0

    println("True X,  True y, UKF x, UKF y")
    val filter = theFilter(x, y)
    for (i in 0 until maxSteps) {
        val params: array1D = mk.ndarray(mk[1.0])
        filter.predictSigmaPoints(params)
        x = (i.toDouble() + 1.0) * dx
        y = yScale * kotlin.math.sin(x / xScale)
        if (i % cadence == 0) {
            filter.updateSigmaPoints(mk.ndarray(mk[x, y]))
        }
        val currentState = filter.getState()
        println("$x, $y, ${currentState[0]}, ${currentState[1]}")
    }
}
