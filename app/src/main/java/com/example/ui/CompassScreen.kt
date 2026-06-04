package com.example.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PrayerTimesCalculator
import com.example.data.UserSettings
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CompassScreen(
    settings: UserSettings,
    modifier: Modifier = Modifier
) {
    val isBangla = settings.language == "bangla"
    val context = LocalContext.current

    // Sensory states
    var deviceHeading by remember { mutableStateOf(0f) }
    var sensorSupported by remember { mutableStateOf(false) }

    // Manual Simulation Slider (if sensor is inactive or missing)
    var manualDialValue by remember { mutableStateOf(120f) }
    var isSimulatedMode by remember { mutableStateOf(false) }

    // Calculate dynamic angles
    // Mecca is roughly at Latitude: 21.4225 N, Longitude: 39.8262 E
    // From Bangladesh, Qibla angle is roughly 277 degrees (West-Northwest)
    val qiblaAngleFromNorth = 277f

    // Connect to system magnetometer and accelerometer sensors
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val rotSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        
        var sensorEventListener: SensorEventListener? = null

        if (rotSensor != null) {
            sensorSupported = true
            sensorEventListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if (event != null && event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                        val rotationMatrix = FloatArray(9)
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        val orientationValues = FloatArray(3)
                        SensorManager.getOrientation(rotationMatrix, orientationValues)
                        
                        // Azimuth from orientation: Convert radians to degrees
                        val azimuthRad = orientationValues[0]
                        var heading = Math.toDegrees(azimuthRad.toDouble()).toFloat()
                        if (heading < 0) heading += 360f
                        
                        deviceHeading = heading
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }
            sensorManager.registerListener(sensorEventListener, rotSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            // Fallback sensors if TYPE_ROTATION_VECTOR is missing
            val accel = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val magnet = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            if (accel != null && magnet != null) {
                sensorSupported = true
                
                val accelerometerReading = FloatArray(3)
                val magnetometerReading = FloatArray(3)
                
                sensorEventListener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent?) {
                        if (event == null) return
                        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
                        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
                        }
                        
                        val rotationMatrix = FloatArray(9)
                        val inclinationMatrix = FloatArray(9)
                        val success = SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, accelerometerReading, magnetometerReading)
                        
                        if (success) {
                            val orientationValues = FloatArray(3)
                            SensorManager.getOrientation(rotationMatrix, orientationValues)
                            var heading = Math.toDegrees(orientationValues[0].toDouble()).toFloat()
                            if (heading < 0) heading += 360f
                            deviceHeading = heading
                        }
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                }
                sensorManager.registerListener(sensorEventListener, accel, SensorManager.SENSOR_DELAY_UI)
                sensorManager.registerListener(sensorEventListener, magnet, SensorManager.SENSOR_DELAY_UI)
            } else {
                sensorSupported = false
                isSimulatedMode = true
            }
        }

        onDispose {
            if (sensorEventListener != null) {
                sensorManager?.unregisterListener(sensorEventListener)
            }
        }
    }

    // Determine target dial heading
    val activeHeading = if (isSimulatedMode) manualDialValue else deviceHeading
    val relativeQiblaHeading = (qiblaAngleFromNorth - activeHeading + 360f) % 360f

    // Smooth heading rotations
    val smoothRelativeHeading by animateFloatAsState(
        targetValue = relativeQiblaHeading,
        animationSpec = tween(durationMillis = 150),
        label = "Qibla Rotation"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // HEADER TITLE
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isBangla) "কিবলা কম্পাস" else "Qibla Compass",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("compass_title")
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isBangla) "মক্কা শরীফ (কাবা)-র দিক নির্দেশনা" else "Locate Kaaba (Mecca) Direction",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )
        }

        // HEADING DEGREES STATUS BADGE
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val roundHeading = activeHeading.toInt()
                val qiblaDiff = ((roundHeading - 277 + 360) % 360)
                val alignedStatus = if (qiblaDiff in 355..360 || qiblaDiff in 0..5) {
                    if (isBangla) "🎯 কিবলা বরাবর সঠিক! (২৭৭°)" else "🎯 Aligned with Kaaba! (277°)"
                } else {
                    if (isBangla) "ডিভাইস ঘোরান (২৭৭° তে কাবা)" else "Rotate device (Kaaba at 277°)"
                }

                Text(
                    text = if (isBangla) {
                        "ডিভাইস কোণ: ${PrayerTimesCalculator.convertToBengaliNumerals(roundHeading.toString())}°"
                    } else {
                        "Heading: $roundHeading°"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = alignedStatus,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (alignedStatus.startsWith("🎯")) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        // COMPASS GRAPHICAL WHEEL DRAWINGS
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .border(6.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .testTag("compass_dial"),
            contentAlignment = Alignment.Center
        ) {
            // Draw compass markings on Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.width / 2 - 20.dp.toPx()

                // Draw Card Points N, S, E, W
                // We rotate the draw state depending on active heading to represent dynamic orientation
                // North represents 0, East=90, South=180, West=270
                rotate(degrees = -activeHeading, pivot = center) {
                    drawCompassTicks(center, radius)
                }

                // Draw Qibla pointer Arrow (points precisely towards Mecca)
                // Qibla is 277.0 degrees
                rotate(degrees = -activeHeading + qiblaAngleFromNorth, pivot = center) {
                    drawQiblaPointer(center, radius)
                }
            }

            // Central golden circle embellishment
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE5A11E))
                    .border(2.dp, Color.White, CircleShape)
            )
        }

        // SENSOR STATUS BANNER / SIMULATOR OPTIONS
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!sensorSupported) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Sensor warning",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBangla) {
                            "ডিভাইসে চৌম্বকীয় সেন্সর নেই! ম্যানুয়াল সিমুলেশন মোড সক্রিয়।"
                        } else {
                            "Compass sensor missing on this emulator! Manual adjustment mode activated."
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Mode Selector: Manual vs Sensor (only if sensor is physically available)
            if (sensorSupported) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBangla) "ম্যানুয়াল সিমুলেটার সক্রিয় করুন" else "Enable Manual Simulator",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Switch(
                        checked = isSimulatedMode,
                        onCheckedChange = { isSimulatedMode = it },
                        modifier = Modifier.testTag("simulator_switch")
                    )
                }
            }

            // Slider to simulated heading if enabled
            if (isSimulatedMode) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (isBangla) "কম্পাস ঘোরান (সিমুলেটর)" else "Rotate Compass Dial (Simulator)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Slider(
                        value = manualDialValue,
                        onValueChange = { manualDialValue = it },
                        valueRange = 0f..360f,
                        modifier = Modifier.testTag("heading_slider")
                    )
                }
            }
        }
    }
}

/**
 * Draws the dial ticks, labels, and directions.
 */
private fun DrawScope.drawCompassTicks(center: Offset, radius: Float) {
    // Labels N, E, S, W
    val textPaintNorth = android.graphics.Paint().apply {
        color = android.graphics.Color.RED
        textSize = 45f
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        textAlign = android.graphics.Paint.Align.CENTER
    }
    val textPaintOther = android.graphics.Paint().apply {
        color = android.graphics.Color.DKGRAY
        textSize = 38f
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        textAlign = android.graphics.Paint.Align.CENTER
    }

    // Draw Letters
    drawContext.canvas.nativeCanvas.drawText("N", center.x, center.y - radius + 15f, textPaintNorth)
    drawContext.canvas.nativeCanvas.drawText("S", center.x, center.y + radius + 15f, textPaintOther)
    drawContext.canvas.nativeCanvas.drawText("E", center.x + radius, center.y + 15f, textPaintOther)
    drawContext.canvas.nativeCanvas.drawText("W", center.x - radius, center.y + 15f, textPaintOther)

    // Draw Ticks
    for (angle in 0 until 360 step 15) {
        if (angle % 90 == 0) continue // Labels are already drawn there
        val angleRad = Math.toRadians(angle.toDouble())
        val tickLength = if (angle % 30 == 0) 25f else 12f
        val startX = (center.x + (radius - tickLength) * sin(angleRad)).toFloat()
        val startY = (center.y - (radius - tickLength) * cos(angleRad)).toFloat()
        val endX = (center.x + radius * sin(angleRad)).toFloat()
        val endY = (center.y - radius * cos(angleRad)).toFloat()

        drawLine(
            color = Color.LightGray,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = if (angle % 30 == 0) 3f else 1.5f
        )
    }
}

/**
 * Draws the stunning Qibla indicator pointing towards Kaaba.
 */
private fun DrawScope.drawQiblaPointer(center: Offset, radius: Float) {
    // Outer golden warning circle for pointing zone
    drawCircle(
        color = Color(0x33E5A11E),
        radius = 24.dp.toPx(),
        center = Offset(center.x, center.y - radius + 32.dp.toPx())
    )

    // Draw Mecca pointing arrow/dome icon
    val arrowPath = Path().apply {
        moveTo(center.x, center.y - radius + 5.dp.toPx()) // Tip pointing forward
        lineTo(center.x - 14.dp.toPx(), center.y - radius + 30.dp.toPx()) // left wing
        lineTo(center.x - 6.dp.toPx(), center.y - radius + 25.dp.toPx())  // inner tail left
        lineTo(center.x + 6.dp.toPx(), center.y - radius + 25.dp.toPx())  // inner tail right
        lineTo(center.x + 14.dp.toPx(), center.y - radius + 30.dp.toPx()) // right wing
        close()
    }

    drawPath(
        path = arrowPath,
        color = Color(0xFF0C5A3E) // Islamic Deep Emerald Green
    )

    // Tiny Kabba silhouette tag on arrow tail
    drawCircle(
        color = Color(0xFFE5A11E),
        radius = 5.dp.toPx(),
        center = Offset(center.x, center.y - radius + 40.dp.toPx())
    )
}
