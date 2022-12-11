package examples

import src.*
import org.jetbrains.kotlinx.multik.api.*

class PositionFilter(weight: Double) : UnscentedBase(2, 2, weight) {
    override fun predictModel(state: array1D, parameters: array1D) : array1D {
        return mk.zeros(stateSize)
    }

    override fun measurementModel(state: array1D) : array1D {
        return mk.zeros(measurementSize)
    }
}