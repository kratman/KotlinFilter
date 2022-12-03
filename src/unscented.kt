
package src

import org.jetbrains.kotlinx.multik.api.*
import org.jetbrains.kotlinx.multik.api.linalg.*
import org.jetbrains.kotlinx.multik.ndarray.data.*
import org.jetbrains.kotlinx.multik.ndarray.operations.plus
import org.jetbrains.kotlinx.multik.ndarray.operations.minus
import org.jetbrains.kotlinx.multik.ndarray.operations.times

typealias array1D = D1Array<Double>
typealias array2D = D2Array<Double>

abstract class UnscentedBase(stateLength: Int, measurementLength: Int, weight: Double) {
    private var stateSize: Int = stateLength
    private var measurementSize: Int = measurementLength
    private var diagonalWeight: Double = weight
    private var s = mk.zeros<Double>(stateSize)
    private var P = mk.identity<Double>(stateSize)
    private var Q = mk.identity<Double>(stateSize)
    private var R = mk.identity<Double>(measurementSize)
    private var sigmaPoints = mk.identity<Double>(stateSize)

    abstract fun predictModel(state: array1D, parameters: array1D) : array1D

    abstract fun measurementModel(state: array1D) : array1D

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
        val gain = crossCoVar dot mk.linalg.inv(residual)
        s += gain dot (measurement - averageMeasurement)
        P -= gain dot (residual dot gain.transpose())
    }

    private fun unscentedSample() {
        val decomposedVariances = choleskyDecomposition(P)
        for (j in 0 until stateSize) {
            sigmaPoints[j, 0] = s[j]
        }
        for (i in 0 until stateSize) {
            val sigmaIndex = i + 1
            for (j in 0 until stateSize) {
                sigmaPoints[j, sigmaIndex] = s[j] + (sigmaStepSize() * decomposedVariances[j, i])
            }
        }
        for (i in 0 until stateSize) {
            val sigmaIndex = i + stateSize + 1
            for (j in 0 until stateSize) {
                sigmaPoints[j, sigmaIndex] = s[j] - (sigmaStepSize() * decomposedVariances[j, i])
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

    private fun generatePredictedStates(parameters: array1D): array2D {
        val predictedStates: array2D = mk.zeros(stateSize, stateSize)
        for (i in 0 until getNumberOfStates()) {
            val nextState = predictModel(sigmaPoints[0 until stateSize, i] as array1D, parameters)
            for (j in 0 until stateSize) {
                predictedStates[j, i] = nextState[j]
            }
        }
        return predictedStates
    }

    private fun determineAverageState(predictedStates: array2D): array1D {
        val newState: array1D = mk.zeros(stateSize)
        var weight = weightDiagonal()
        for (i in 0 until getNumberOfStates()) {
            for (j in 0 until stateSize) {
                newState[j] += weight * predictedStates[j, i]
            }
            weight = weightOffDiagonal()
        }
        return newState
    }

    private fun calculatePredictedVariance(predictedStates: array2D,
                                           newState: array1D): array2D {
        var newVariances = Q.copy()
        val update: array1D = mk.zeros(stateSize)
        var weight = weightDiagonal()
        for (i in 0 until getNumberOfStates()) {
            for (j in 0 until stateSize) {
                update[j] = predictedStates[j, i] - newState[j]
            }
            newVariances += weight * update dot update.transpose()
            weight = weightOffDiagonal()
        }
        return newVariances
    }

    private fun generateMeasuredStates(): array2D {
        val measuredStates: array2D = mk.zeros(measurementSize, getNumberOfStates())
        for (i in 0 until getNumberOfStates()) {
            val measurement = measurementModel(sigmaPoints[0 until stateSize, i] as array1D)
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
                stateDiff[j] = sigmaPoints[j, i] - s[j]
            }
            crossCoVar += weight * stateDiff dot measDiff.transpose()
            weight = weightOffDiagonal()
        }
        return crossCoVar
    }
}
