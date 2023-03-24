package src

import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.data.set


class Cholesky {
    fun decompose(matrixToDecompose: array2D): array2D {
        if (matrixToDecompose.shape[0] !=  matrixToDecompose.shape[1]) {
            throw Exception("Non-square matrices cannot be decomposed.")
        }
        val size = matrixToDecompose.shape[0]
        val lowerMatrix = matrixToDecompose.copy()
        for (i in 0 until size) {
            for (j in i until size) {
                var sum = lowerMatrix[i, j]
                for (k in i-1 downTo 0) {
                    sum -= lowerMatrix[i, k] * lowerMatrix[j, k]
                }
                if (i == j) {
                    if (sum <= 0.0) {
                        throw Exception("Matrix is not positive definite and cannot be decomposed.")
                    }
                    lowerMatrix[i, i] = kotlin.math.sqrt(sum)
                } else {
                    lowerMatrix[j, i] = sum / lowerMatrix[i, i]
                }
            }
        }
        for (i in 0 until size) {
            for (j in 0 until i) {
                lowerMatrix[j, i] = 0.0
            }
        }
        return lowerMatrix
    }
}