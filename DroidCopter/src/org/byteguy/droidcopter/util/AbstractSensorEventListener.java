/**
 * 
 */
package org.byteguy.droidcopter.util;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;

/**
 * @author fassmus
 * 
 */
public abstract class AbstractSensorEventListener implements
		SensorEventListener {

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// Do nothing.
	}

}
