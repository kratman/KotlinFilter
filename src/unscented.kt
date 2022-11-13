
package src

import org.jetbrains.kotlinx.multik.api.*
import org.jetbrains.kotlinx.multik.ndarray.data.*

typealias array1D = D1Array<Double>
typealias array2D = D2Array<Double>

abstract class UnscentedBase(stateLength: Int, measurementLength: Int, weight: Double) {
    private var stateSize: Int = stateLength
    private var measurementSize: Int = measurementLength
    var diagonalWeight: Double = weight
    var s = mk.zeros<Double>(stateSize)
    var P = mk.identity<Double>(stateSize)
    var Q = mk.identity<Double>(stateSize)
    var R = mk.identity<Double>(measurementSize)
    var sigmaPoints = mk.identity<Double>(stateSize)

    abstract fun predictModel(state: array1D, parameters: array1D)

    abstract fun measurementModel(state: array1D)

    fun setState(state: array1D) {
        s = state
    }

    fun getState(): array1D {
        return s
    }

    fun setVariance(variance: array2D) {
        P = variance
    }

    fun getVariance(): array2D {
        return P
    }

    fun setProcessNoise(noise: array2D) {
        Q = noise
    }

    fun getProcessNoise(): array2D {
        return Q
    }

    fun setMeasurementNoise(noise: array2D) {
        R = noise
    }

    fun getMeasurementNoise(): array2D {
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

    fun predictSigmaPoints(parameters: array1D) {

    }

    fun updateSigmaPoints(measurements: array2D) {

    }

    fun unscentedSample() {

    }

    private fun choleskyDecomposition(matrixToDecompose: array2D): array2D {
        return mk.zeros(1, 1)
    }

    fun generatePredictedStates(parameters: array1D): array2D {
        return mk.zeros(1, 1)
    }

    fun determineAverageState(parameters: array1D): array1D {
        return mk.zeros(1)
    }

    fun calculatePredictedVariance(predictedStates: array2D,
                                   newState: array1D): array2D {
        return mk.zeros(1, 1)
    }

    fun generateMeasuredStates(): array2D {
        return mk.zeros(1, 1)
    }

    fun calculateAverageMeasurement(measuredStates: array2D): array1D {
        return mk.zeros(1)
    }

    fun estimateResidual(measuredStates: array2D,
                         averageMeasurement: array1D): array2D {
        return mk.zeros(1, 1)
    }

    fun calculateCrossCovariance(measuredStates: array2D,
                                 averageMeasurement: array1D): array2D {
        return mk.zeros(1, 1)
    }
}
