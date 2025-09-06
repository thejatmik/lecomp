package com.thejatmik.lecomp

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import android.hardware.SensorManager
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorEvent

class MainActivity : ComponentActivity(), SensorEventListener {
    private var compassImage: ImageView? = null
    private var sensorManager: SensorManager? = null
    private var magnetometer: Sensor? = null
    private var accelerometer: Sensor? = null
    private var lastMagnetometer: FloatArray = FloatArray(3)
    private var lastAccelerometer: FloatArray = FloatArray(3)
    private var lastMagnetometerSet: Boolean = false
    private var lastAccelerometerSet: Boolean = false
    private var rotationMatrix: FloatArray = FloatArray(9)
    private var orientation: FloatArray = FloatArray(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        /*
         */
        compassImage = findViewById(R.id.compass)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onResume() {
        super.onResume()
        /*
         */
        sensorManager?.registerListener(
            this,
            magnetometer,
            SensorManager.SENSOR_DELAY_GAME
        )
        sensorManager?.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    override fun onPause() {
        super.onPause()
        /*
         */
        sensorManager?.unregisterListener(this, magnetometer)
        sensorManager?.unregisterListener(this, accelerometer)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.values == null) {
            return
        }
        if (event.sensor == magnetometer) {
            System.arraycopy(
                event.values,
                0,
                lastMagnetometer,
                0,
                event.values.size
            )
            lastMagnetometerSet = true
        } else if (event.sensor == accelerometer) {
            System.arraycopy(
                event.values,
                0,
                lastAccelerometer,
                0,
                event.values.size
            )
            lastAccelerometerSet = true
        }

        if (lastAccelerometerSet && lastMagnetometerSet) {
            SensorManager.getRotationMatrix(
                rotationMatrix,
                null,
                lastAccelerometer,
                lastMagnetometer
            )
            SensorManager.getOrientation(rotationMatrix, orientation)

            val azimuthInRadians: Double = orientation[0].toDouble()
            val azimuthInDegrees: Float = (Math.toDegrees(azimuthInRadians).toFloat() + 360) % 360

            val mCompassImage = findViewById<ImageView>(R.id.compass)
            mCompassImage.rotation = -azimuthInDegrees
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }
}