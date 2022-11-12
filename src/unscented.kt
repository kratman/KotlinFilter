
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

    fun getNumberOfStates(): Int {
        return 2 * stateSize + 1
    }

    fun sigmaStepSize(): Double {
        return kotlin.math.sqrt(stateSize / (1.0 - weightDiagonal()))
    }

    fun weightOffDiagonal(): Double {
        return (1.0 - weightDiagonal()) / (2.0 * stateSize)
    }

    private fun weightDiagonal(): Double {
        return diagonalWeight
    }

    fun predictSigmaPoints(parameters: D1Array<Double>) {

    }

    fun updateSigmaPoints(measurements: D2Array<Double>) {

    }

    fun unscentedSample() {

    }

    private fun choleskyDecomposition(matrixToDecompose: D2Array<Double>): D2Array<Double> {
        return mk.zeros(1, 1)
    }
}
