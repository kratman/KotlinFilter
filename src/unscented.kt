
package src

import org.jetbrains.kotlinx.multik.api.*
import org.jetbrains.kotlinx.multik.api.linalg.*
import org.jetbrains.kotlinx.multik.ndarray.data.*
import org.jetbrains.kotlinx.multik.ndarray.operations.plus
import org.jetbrains.kotlinx.multik.ndarray.operations.minus
import org.jetbrains.kotlinx.multik.ndarray.operations.times

typealias array1D = D1Array<Double>
typealias array2D = D2Array<Double>

abstract class UnscentedBase {
    var stateSize: Int
    var measurementSize: Int
    private var diagonalWeight: Double
    private var s: array2D
    private var P: array2D
    private var Q: array2D
    private var R: array2D
    private var sigmaPoints: array2D

    constructor(stateLength: Int, measurementLength: Int, weight: Double) {
        stateSize = stateLength
        measurementSize = measurementLength

        diagonalWeight = weight
        s = mk.zeros(stateSize, 1)
        P = mk.identity(stateSize)
        Q = mk.identity(stateSize)
        R = mk.identity(measurementSize)
        sigmaPoints = mk.zeros(stateSize, getNumberOfStates())
    }

    abstract fun predictModel(state: array1D, parameters: array1D?) : array1D

    abstract fun measurementModel(state: array1D) : array1D

    fun setState(state: array1D) {
        for (i in 0 until stateSize) {
            s[i, 0] = state[i]
        }
    }

    fun getState(): array1D {
        return s[0..stateSize, 0] as array1D
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

    private fun getNumberOfStates(): Int {
        return 2 * stateSize + 1
    }

    private fun sigmaStepSize(): Double {
        return kotlin.math.sqrt(stateSize / (1.0 - weightDiagonal()))
    }

    private fun weightOffDiagonal(): Double {
        return (1.0 - weightDiagonal()) / (2.0 * stateSize)
    }

    private fun weightDiagonal(): Double {
        return diagonalWeight
    }

    fun predictSigmaPoints(parameters: array1D? = null) {
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
        val gain = crossCoVar dot mk.linalg.inv(residual)
        val measurementDiff = getMeasurementDifference(measurement, averageMeasurement)
        s += gain dot measurementDiff
        P -= gain dot (residual dot gain.transpose())
    }

    private fun getMeasurementDifference(measurement: array1D,
                                         averageMeasurement: array1D): array2D {
        val measurementDiff: array2D = mk.zeros(measurementSize, 1)
        for (i in 0 until measurementSize) {
            measurementDiff[i, 0] = measurement[i] - averageMeasurement[i]
        }
        return measurementDiff
    }

    private fun unscentedSample() {
        val decomposedVariances = choleskyDecomposition(P)
        for (j in 0 until stateSize) {
            sigmaPoints[j, 0] = s[j, 0]
        }
        for (i in 0 until stateSize) {
            val sigmaIndex = i + 1
            for (j in 0 until stateSize) {
                sigmaPoints[j, sigmaIndex] = s[j, 0] + (sigmaStepSize() * decomposedVariances[j, i])
            }
        }
        for (i in 0 until stateSize) {
            val sigmaIndex = i + stateSize + 1
            for (j in 0 until stateSize) {
                sigmaPoints[j, sigmaIndex] = s[j, 0] - (sigmaStepSize() * decomposedVariances[j, i])
            }
        }
    }

    private fun choleskyDecomposition(matrixToDecompose: array2D): array2D {
        val lowerMatrix = matrixToDecompose.copy()
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

    private fun generatePredictedStates(parameters: array1D?): array2D {
        val predictedStates: array2D = mk.zeros(stateSize, getNumberOfStates())
        for (i in 0 until getNumberOfStates()) {
            val nextState = predictModel(sigmaPoints[0..stateSize, i] as array1D, parameters)
            for (j in 0 until stateSize) {
                predictedStates[j, i] = nextState[j]
            }
        }
        return predictedStates
    }

    private fun determineAverageState(predictedStates: array2D): array2D {
        val newState: array2D = mk.zeros(stateSize, 1)
        var weight = weightDiagonal()
        for (i in 0 until getNumberOfStates()) {
            for (j in 0 until stateSize) {
                newState[j, 0] += weight * predictedStates[j, i]
            }
            weight = weightOffDiagonal()
        }
        return newState
    }

    private fun calculatePredictedVariance(predictedStates: array2D,
                                           newState: array2D): array2D {
        var newVariances = Q.copy()
        val update: array2D = mk.zeros(stateSize, 1)
        var weight = weightDiagonal()
        for (i in 0 until getNumberOfStates()) {
            for (j in 0 until stateSize) {
                update[j, 0] = predictedStates[j, i] - newState[j, 0]
            }
            newVariances += weight * (update dot update.transpose())
            weight = weightOffDiagonal()
        }
        return newVariances
    }

    private fun generateMeasuredStates(): array2D {
        val measuredStates: array2D = mk.zeros(measurementSize, getNumberOfStates())
        for (i in 0 until getNumberOfStates()) {
            val measurement = measurementModel(sigmaPoints[0..stateSize, i] as array1D)
            for (j in 0 until measurementSize) {
                measuredStates[j, i] = measurement[j]
            }
        }
        return measuredStates
    }

    private fun calculateAverageMeasurement(measuredStates: array2D): array1D {
        val averageMeasurement: array1D = mk.zeros(measurementSize)
        var weight = weightDiagonal()
        for (i in 0 until getNumberOfStates()) {
            for (j in 0 until measurementSize) {
                averageMeasurement[j] += weight * measuredStates[j, i]
            }
            weight = weightOffDiagonal()
        }
        return averageMeasurement
    }

    private fun estimateResidual(measuredStates: array2D,
                                 averageMeasurement: array1D): array2D {
        var residual = R.copy()
        val diff: array1D = mk.zeros(measurementSize)
        var weight = weightDiagonal()
        for (i in 0 until getNumberOfStates()) {
            for (j in 0 until measurementSize) {
                diff[j] = measuredStates[j, i] - averageMeasurement[j]
            }
            residual += weight * diff dot diff.transpose()
            weight = weightOffDiagonal()
        }
        return residual
    }

    private fun calculateCrossCovariance(measuredStates: array2D,
                                         averageMeasurement: array1D): array2D {
        var crossCoVar: array2D = mk.zeros(stateSize, measurementSize)
        val measDiff: array1D = mk.zeros(measurementSize)
        val stateDiff: array1D = mk.zeros(stateSize)
        var weight = weightDiagonal()
        for (i in 0 until getNumberOfStates()) {
            for (j in 0 until measurementSize) {
                measDiff[j] = measuredStates[j, i] - averageMeasurement[j]
            }
            for (j in 0 until stateSize) {
                stateDiff[j] = sigmaPoints[j, i] - s[j, 0]
            }
            crossCoVar += weight * stateDiff dot measDiff.transpose()
            weight = weightOffDiagonal()
        }
        return crossCoVar
    }
}
