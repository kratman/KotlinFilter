
# Example models

Several examples for using the unscented Kalman filter are provided.

## Filters

### Position tracking

- State: Position
- Measurement: Position

This Kalman filter does not predict that the state will change between
measurements. Only the measurement will cause the state to be updated.
This Kalman filter is the minimum that could be implemented.

### Position and velocity tracking

- State: Position and velocity
- Measurement: Position

This Kalman assumes that the position measurements are changing at some
undetermined rate, i.e. the state is moving with a velocity. Only the position
is measured, so the Kalman filter tries to estimate both the position and velocity
in a way that most closely matches the measurements.

This example is to display how unknown quantities can be estimated and used
in models.

### Tuning the filters

The Kalman filters provided in the examples are not well tuned to the input.
The initial covariance, measurement noise and process noise can all be adjusted
to improve the tracking of the input. Large measurement noise implies that
the measurements cannot be trusted, while a large process noise implies that the
predict step cannot be trusted.

## Inputs

There are two simple main() functions provided to show how to interact
with the Kalman filters. Each main() imports a kalman filter as "theFilter",
which allows the user to swap between the two example implementations in the
preceding section.

For instance,
```kotlin
import examples.PositionFilter as theFilter
```
or
```kotlin
import examples.VelocityFilter as theFilter
```
can be used to try to smooth the input.

### Sinusoid with missed points

An example to show how the Kalman filter compensates for missing data.

- y = 20 * sin(x / 50)
- Multiple predictions between measurements

This input function can be found in **missing_data.kt** in the examples.

### Sinusoid with noise

An example to show how the Kalman filter smooths noisy data

- y = 20 * sin(x / 50) + g(x)
- A prediction and measurement at each step
- Random noise, g(x), applied at each step

This input function can be found in **noisy_data.kt** in the examples.
