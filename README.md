
# Kalman filtering with Kotlin

## Usage

The **UnscentedBase** class provides all the machinery for the Unscented Kalman filter.
The user needs to define two functions,
```kotlin
    abstract fun predictModel(state: array1D, parameters: array1D) : array1D

    abstract fun measurementModel(state: array1D) : array1D
```
inside a child class.

### Initialization

An instance of the child class should be created for each object being tracked by
the Kalman filter. Each child instance of **UnscentedBase** is initialized with a
starting state, initial guess of the co-variance matrix, the measurement variance,
and the prediction variance. The starting state is estimated from the first measurement
which is used to initialize the filter.

### Prediction
```kotlin
    abstract fun predictModel(state: array1D, parameters: array1D) : array1D
```
The parameters argument is an array of the inputs to the dynamic model used for the
prediction. The prediction model should handle the time update of the Kalman filter.

For instance if the filter is tracking a car, then the state might be the {x,y} position
of the vehicle. The prediction model might take velocity and time as the parameters.
```kotlin
    override fun predictModel(state: array1D, parameters: array1D) : array1D {
        state[xIndex] += parameters[xVelocityIndex] * parameters[timeIndex]
        state[yIndex] += parameters[yVelocityIndex] * parameters[timeIndex]
        return state
  }
```
The prediction step can be essentially be any model for how the state changes over
time. The prediction model is typically based on the physics of the system, however,
in some cases heuristics or approximations may be required.

### Measurement
```kotlin
    abstract fun measurementModel(state: array1D) : array1D
```
The measurement model converts the state into the same coordinates as the measurement.
For instance, if the state is {x,y} position and the measurement is in polar coordinates:
```kotlin
    override fun measurementModel(state: array1D) : array1D {
        val measurement: array1D = mk.zeros(measurementSize)
        val radius = state[xIndex] * state[xIndex] + state[yIndex] * state[yIndex]
        measurement[radiusIndex] = sqrt(radius)
        measurement[thetaIndex] = atan2(state[yIndex], state[xIndex])
        return measurement
    }
```
The output of the measurement model is compared to the actual measurements fed into the
Kalman filter. For this reason, the **UnscentedKalman** class assumes the measurement
model output matches the shape, size, and order of the measurements.

## Examples

The *examples* directory contains a few implementations of **UnscentedBase** to
demonstrate how to use the filter.

## Dependencies

The following matrix libraries are required for running the filter:
```
jetbrains.kotlinx.multik.api
jetbrains.kotlinx.multik.default
```

## Theoretical references

```
Eric A. Wan and Rudolph van der Merwe,  "The Unscented Kalman Filter for
Nonlinear Estimation", Proceedings of the IEEE 2000 Adaptive Systems for
Signal Processing, Communications, and Control Symposium, (2000)
```
