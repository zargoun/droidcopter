/**
 * 
 */
package org.byteguy.droidcopter.kinematics;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.byteguy.droidcopter.R;
import org.byteguy.droidcopter.util.AbstractSensorEventListener;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.Float3;
import android.renderscript.RenderScript;

/**
 * Main logic for calculating rotor speeds based on the current situation
 * (acceleration, rotation and magnetic field data).
 * 
 * @author Florian Assmus
 * 
 */
public class RotorController {

	/**
	 * Sensor listener for gathering environment data.
	 * 
	 * @author Florian Assmus
	 * 
	 */
	private class SensorListener extends AbstractSensorEventListener {

		private Float3 values;

		private ReentrantReadWriteLock lock;

		public SensorListener(Float3 values, ReentrantReadWriteLock lock) {
			this.values = values;
			this.lock = lock;
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			try {
				lock.writeLock().lock();
				values.x = event.values[0];
				values.y = event.values[1];
				values.z = event.values[2];
			} finally {
				lock.writeLock().unlock();
			}
		}
	}

	/**
	 * Android sensor manager
	 */
	private SensorManager mSensorManager;

	/**
	 * Relevant sensors.
	 */
	private Sensor accelerometer;
	private Sensor gyroscope;
	private Sensor magneticField;

	/**
	 * Sensor listeners
	 */
	private SensorListener accelerometerListener;
	private SensorListener gyroscopeListener;
	private SensorListener magneticFieldListener;

	/**
	 * Rotor control render script.
	 */
	private ScriptC_RotorControl rotorControlScript;

	/**
	 * Sensor data input for rotor control render script
	 */
	private ScriptField_SensorData sensorData;

	/**
	 * Rotor control render script output
	 */
	private Allocation outAlloc;
	private float[] rotorSpeeds;

	/**
	 * Locks for synchronizing sensor data reception.
	 */
	private ReentrantReadWriteLock accelerometerLock = new ReentrantReadWriteLock();
	private ReentrantReadWriteLock gyroscopeLock = new ReentrantReadWriteLock();
	private ReentrantReadWriteLock magneticFieldLock = new ReentrantReadWriteLock();

	/**
	 * Constructor taking the Android context.
	 * 
	 * @param context
	 *            Current Android context.
	 */
	public RotorController(Context context) {
		initRotorControl(context);
		initSensorMonitoring(context);
	}

	/**
	 * Initialize the rotor control RenderScript.
	 * 
	 * @param context
	 *            Current Android context.
	 */
	private void initRotorControl(Context context) {
		RenderScript rotorControlRenderScript = RenderScript.create(context);

		sensorData = new ScriptField_SensorData(rotorControlRenderScript, 1);
		sensorData.set_accelerometer(0, new Float3(), false);
		sensorData.set_gyroscope(0, new Float3(), false);
		sensorData.set_magneticField(0, new Float3(), false);

		outAlloc = Allocation.createSized(rotorControlRenderScript,
				Element.F32_4(rotorControlRenderScript), 1);
		rotorSpeeds = new float[4];

		rotorControlScript = new ScriptC_RotorControl(rotorControlRenderScript,
				context.getResources(), R.raw.rotorcontrol);
	}

	/**
	 * Initialize the sensor monitoring.
	 * 
	 * @param context
	 *            Current Android context.
	 */
	private void initSensorMonitoring(Context context) {
		mSensorManager = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);

		accelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		gyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		magneticField = mSensorManager
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		accelerometerListener = new SensorListener(
				sensorData.get_accelerometer(0), accelerometerLock);
		gyroscopeListener = new SensorListener(sensorData.get_gyroscope(0),
				gyroscopeLock);
		magneticFieldListener = new SensorListener(
				sensorData.get_magneticField(0), magneticFieldLock);
	}

	/**
	 * Start the sensor monitoring.
	 */
	public void startSensorMonitoring() {
		mSensorManager.registerListener(accelerometerListener, accelerometer,
				SensorManager.SENSOR_DELAY_FASTEST);
		mSensorManager.registerListener(gyroscopeListener, gyroscope,
				SensorManager.SENSOR_DELAY_FASTEST);
		mSensorManager.registerListener(magneticFieldListener, magneticField,
				SensorManager.SENSOR_DELAY_FASTEST);
	}

	/**
	 * Stop the sensor monitoring.
	 */
	public void stopSensorMonitoring() {
		mSensorManager.unregisterListener(accelerometerListener);
		mSensorManager.unregisterListener(gyroscopeListener);
		mSensorManager.unregisterListener(magneticFieldListener);
	}

	/**
	 * Calculate rotor speeds based on the current sensor data.
	 * 
	 * @return Rotor speeds.
	 */
	public float[] calculateRotorData() {
		try {
			magneticFieldLock.readLock().lock();
			gyroscopeLock.readLock().lock();
			accelerometerLock.readLock().lock();

			sensorData.copyAll();
		} finally {
			accelerometerLock.readLock().unlock();
			gyroscopeLock.readLock().unlock();
			magneticFieldLock.readLock().unlock();
		}

		rotorControlScript.forEach_root(sensorData.getAllocation(), outAlloc);
		outAlloc.copyTo(rotorSpeeds);

		return rotorSpeeds;
	}
}
