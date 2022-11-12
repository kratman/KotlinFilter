
package src


abstract class Unscented(stateLength: Int, measurementLength: Int, weight: Double) {
    var stateSize: Int = stateLength
    var measurementSize: Int = measurementLength
    var diagnonalWeight: Double = weight
    var s = MutableList(stateSize, 1)
    var P = MutableList(stateSize, stateSize)
    var Q = MutableList(stateSize, stateSize)
    var R = MutableList(measurementSize, measurementSize)
    var sigmaPoints = MutableList(stateSize, measurementSize)

    abstract fun predictModel(state, parameters)

    abstract fun measurementModel(state)

    fun predictSigmaPoints(parameters) {

    }

    fun updateSigmaPoints(measurements) {

    }

    fun setState(state) {

    }

    fun getState() {
        return s
    }

    fun setVariance(variance) {

    }

    fun getVariance() {
        return P
    }


}