
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

- y = 10 * sin(x / 100)
- Multiple predictions between measurements

### Sinusoid with noise

An example to show how the Kalman filter smooths noisy data

- y = 10 * sin(x / 100) + g(x)
- A prediction and measurement at each step
- Random noise, g(x), applied at each step
