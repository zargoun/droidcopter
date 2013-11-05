package org.byteguy.droidcopter;

import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

import org.byteguy.droidcopter.kinematics.RotorController;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

public class DroidCopterMainActivity extends IOIOActivity {

	private static final String TAG = "DroidCopterMainActivity";

	private ToggleButton toggleButton;

	private Button runScriptButton;
	private RotorController rotorController;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_droid_copter_main);

		runScriptButton = (Button) findViewById(R.id.button1);
		toggleButton = (ToggleButton) findViewById(R.id.toggleButton1);

		rotorController = new RotorController(this);

		runScriptButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				float[] calculateRotorData = rotorController
						.calculateRotorData();
				Log.i(TAG, "Rotor 1: " + calculateRotorData[0]);
			}
		});

		toggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (arg1) {
					rotorController.startSensorMonitoring();
				} else {
					rotorController.stopSensorMonitoring();
				}
			}

		});
	}

	/**
	 * This is the thread on which all the IOIO activity happens. It will be run
	 * every time the application is resumed and aborted when it is paused. The
	 * method setup() will be called right after a connection with the IOIO has
	 * been established (which might happen several times!). Then, loop() will
	 * be called repetitively until the IOIO gets disconnected.
	 */
	class Looper extends BaseIOIOLooper {
		// /** The on-board LED. */
		// private DigitalOutput led_;

		/**
		 * Called every time a connection with IOIO has been established.
		 * Typically used to open pins.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#setup()
		 */
		@Override
		protected void setup() throws ConnectionLostException {
			// led_ = ioio_.openDigitalOutput(0, true);
		}

		/**
		 * Called repetitively while the IOIO is connected.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#loop()
		 */
		@Override
		public void loop() throws ConnectionLostException {
			// led_.write(!toggleButton.isChecked());
			// try {
			// Thread.sleep(100);
			// } catch (InterruptedException e) {
			// }
		}
	}

	/**
	 * A method to create our IOIO thread.
	 * 
	 * @see ioio.lib.util.AbstractIOIOActivity#createIOIOThread()
	 */
	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.droid_copter_main, menu);
		return true;
	}
}
