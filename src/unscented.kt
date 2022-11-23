
package src

import org.jetbrains.kotlinx.multik.api.*
import org.jetbrains.kotlinx.multik.api.linalg.*
import org.jetbrains.kotlinx.multik.ndarray.data.*
import org.jetbrains.kotlinx.multik.ndarray.operations.plus
import org.jetbrains.kotlinx.multik.ndarray.operations.minus

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
        unscentedSample()
        val predictedStates = generatePredictedStates(parameters)
        val newState = determineAverageState(predictedStates)
        P = calculatePredictedVariance(predictedStates, newState)
        s = newState
    }

    fun updateSigmaPoints(measurement: array1D) {
        unscentedSample()
        val measuredStates = generateMeasuredStates()
        val averageMeasurement = calculateAverageMeasurement(measuredStates)
        val residual = estimateResidual(measuredStates, averageMeasurement)
        val crossCoVar = calculateCrossCovariance(measuredStates, averageMeasurement)
        val gain = mk.linalg.dot(crossCoVar, mk.linalg.inv(residual))
        s += mk.linalg.dot(gain, (measurement - averageMeasurement))
        P -= mk.linalg.dot(gain, mk.linalg.dot(residual, gain.transpose()))
    }

    fun unscentedSample() {

    }

    private fun choleskyDecomposition(matrixToDecompose: array2D): array2D {
        var lowerMatrix: array2D = matrixToDecompose
        for (i in 0 until stateSize) {
            for (j in i until stateSize) {
                var sum = lowerMatrix[i, j]
                for (k in i-1 downTo 0) {
                    sum -= lowerMatrix[i, k] * lowerMatrix[j, k]
                }
                if (i == j) {
                    lowerMatrix[i, i] = kotlin.math.sqrt(sum)
                } else {
                    lowerMatrix[j, i] = sum / lowerMatrix[i, i]
                }
            }
        }
        for (i in 0 until stateSize) {
            for (j in 0 until i) {
                lowerMatrix[j, i] = 0.0
            }
        }
        return lowerMatrix
    }

    fun generatePredictedStates(parameters: array1D): array2D {
        return mk.zeros(1, 1)
    }

    fun determineAverageState(predictedStates: array2D): array1D {
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
