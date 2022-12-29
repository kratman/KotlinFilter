package examples

import src.*
import org.jetbrains.kotlinx.multik.api.*
import org.jetbrains.kotlinx.multik.ndarray.data.*

class VelocityFilter : UnscentedBase {
    constructor(x: Double, y: Double) : super(4, 2, 0.25) {
        setState(mk.ndarray(mk[x, y, 0.0, 0.0]))
        setVariance(mk.ndarray(mk[mk[5.0, 0.0, 0.0, 0.0],
                                  mk[0.0, 5.0, 0.0, 0.0],
                                  mk[0.0, 0.0, 0.2, 0.0],
                                  mk[0.0, 0.0, 0.0, 0.2]]))
        setProcessNoise(mk.ndarray(mk[mk[0.5, 0.0, 0.0, 0.0],
                                      mk[0.0, 0.5, 0.0, 0.0],
                                      mk[0.0, 0.0, 0.1, 0.0],
                                      mk[0.0, 0.0, 0.0, 0.1]]))
        setMeasurementNoise(mk.ndarray(mk[mk[0.1, 0.0], mk[0.0, 0.1]]))
    }

    override fun predictModel(state: array1D, parameters: array1D) : array1D {
        val newState = state.copy()
        val timeStep = 1.0
        newState[0] += state[2] * timeStep
        newState[1] += state[3] * timeStep
        return newState
    }

    override fun measurementModel(state: array1D) : array1D {
        return mk.ndarray(mk[state[0], state[1]])
    }
}
