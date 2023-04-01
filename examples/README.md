
# Example models

Several examples for using the unscented Kalman filter are provided.

## Filters

### Position tracking

- State: Position
- Measurement: Position

### Position and velocity tracking

- State: Position and velocity
- Measurement: Position

## Inputs

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
