package com.thejatmik.lecomp

import android.os.Bundle
import androidx.activity.ComponentActivity
import android.hardware.SensorManager
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorEvent
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext

data class RotationState(val pitch: Float, val roll: Float)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge()
        setContent { this.LoadUI() }
    }

    @Composable
    @Preview
    fun LoadUI() {
        // https://gist.github.com/Pooh3Mobi/f63ac9c808712504b3cb8c75881da958
        val rotationState = remember { mutableStateOf(RotationState(0f, 0f)) }
        val azimuth = remember { mutableFloatStateOf(0f) }

        val sensorManager = LocalContext.current.getSystemService(SENSOR_SERVICE) as SensorManager
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val rotationMatrix = FloatArray(9)
        val orientationAngles = FloatArray(3)
        val accelerometerReading = FloatArray(3)
        val magnetometerReading = FloatArray(3)

        val accelerometerListener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    System.arraycopy(it.values, 0, accelerometerReading, 0, accelerometerReading.size)
                    SensorManager.getRotationMatrix(
                        rotationMatrix,
                        null,
                        accelerometerReading,
                        magnetometerReading
                    )
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)
                    rotationState.value = RotationState(
                        pitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat(),
                        roll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()
                    )
                    val azimuthInRadians: Double = orientationAngles[0].toDouble()
                    azimuth.floatValue = (Math.toDegrees(azimuthInRadians).toFloat() + 360) % 360
                }
            }
        }
        val magnetometerListener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    System.arraycopy(it.values, 0, magnetometerReading, 0, magnetometerReading.size)
                    SensorManager.getRotationMatrix(
                        rotationMatrix,
                        null,
                        accelerometerReading,
                        magnetometerReading,
                    )
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)
                    rotationState.value = RotationState(
                        pitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat(),
                        roll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat(),
                    )
                    val azimuthInRadians: Double = orientationAngles[0].toDouble()
                    azimuth.floatValue = (Math.toDegrees(azimuthInRadians).toFloat() + 360) % 360
                }
            }
        }

        // like useEffect onRender?
        LaunchedEffect(Unit) {
            sensorManager.registerListener(
                accelerometerListener,
                accelerometer,
                SensorManager.SENSOR_DELAY_GAME,
                //SensorManager.SENSOR_DELAY_UI
            )
            sensorManager.registerListener(
                magnetometerListener,
                magnetometer,
                SensorManager.SENSOR_DELAY_GAME,
                //SensorManager.SENSOR_DELAY_UI
            )
        }

        DisposableEffect(Unit) {
            onDispose {
                sensorManager.unregisterListener(accelerometerListener)
                sensorManager.unregisterListener(magnetometerListener)
            }
        }

        Card (modifier = Modifier.fillMaxHeight().padding(24.dp)) {
            Image(
                painter = painterResource(R.drawable.compass),
                contentDescription = "",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth().rotate(-azimuth.floatValue)
            )
            Text(modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, text = "azimuth: ${azimuth.floatValue}")
        }
    }
}