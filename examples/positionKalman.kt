package examples

import src.*
import org.jetbrains.kotlinx.multik.api.*

class PositionFilter : UnscentedBase {
    constructor(position: array1D) : super(2, 2, 0.25) {

    }

    override fun predictModel(state: array1D, parameters: array1D) : array1D {
        return mk.zeros(stateSize)
    }

    override fun measurementModel(state: array1D) : array1D {
        return mk.zeros(measurementSize)
    }
}
