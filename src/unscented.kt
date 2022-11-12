
package src

import org.jetbrains.kotlinx.multik.api.*
import org.jetbrains.kotlinx.multik.ndarray.data.*

abstract class UnscentedBase(stateLength: Int, measurementLength: Int, weight: Double) {
    private var stateSize: Int = stateLength
    private var measurementSize: Int = measurementLength
    var diagonalWeight: Double = weight
    var s = mk.zeros<Double>(stateSize)
    var P = mk.identity<Double>(stateSize)
    var Q = mk.identity<Double>(stateSize)
    var R = mk.identity<Double>(measurementSize)
    var sigmaPoints = mk.identity<Double>(stateSize)

    abstract fun predictModel(state: D1Array<Double>, parameters: D1Array<Double>)

    abstract fun measurementModel(state: D1Array<Double>)

    fun predictSigmaPoints(parameters: D1Array<Double>) {

    }

    fun updateSigmaPoints(measurements: D2Array<Double>) {

    }

    fun setState(state: D1Array<Double>) {
        s = state
    }

    fun getState(): D1Array<Double> {
        return s
    }

    fun setVariance(variance: D2Array<Double>) {
        P = variance
    }

    fun getVariance(): D2Array<Double> {
        return P
    }

    fun setProcessNoise(noise: D2Array<Double>) {
        Q = noise
    }

    fun getProcessNoise(): D2Array<Double> {
        return Q
    }

    fun setMeasurementNoise(noise: D2Array<Double>) {
        R = noise
    }

    fun getMeasurementNoise(): D2Array<Double> {
        return R
    }
}
