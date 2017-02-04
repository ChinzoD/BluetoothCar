package khumun.arduino.bluetoothcar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MonitorActivity extends MyActivity implements SensorEventListener {

	private static final int REQUEST_DISCOVERY = 0x1;;
	private final String TAG = "MonitorActivity";
	private Handler _handler = new Handler();
	private final int maxlength = 2048;

	private BluetoothDevice device = null;
	private BluetoothSocket socket = null;
	private EditText sEditText;
	//private TextView sTextView;
	private OutputStream outputStream;
	private InputStream inputStream;

	public static StringBuffer hexString = new StringBuffer();

	
	private SensorManager sensorManager;
    private TextView txtViewY;
    private ImageView steel;
    private Bitmap bitmap;
    private Button btnForward, btnBackward;
	  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		setContentView(R.layout.sensor);

		//sTextView = (TextView) findViewById(R.id.sTextView);
		sEditText = (EditText) findViewById(R.id.sEditText);

		steel = (ImageView) findViewById(R.id.imageView1);
	    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.steering_wheel);
	    
	    //txtViewY = (TextView) findViewById(R.id.textViewY);
	    
	    sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	    //lastUpdate = System.currentTimeMillis();
	    
	    btnForward = (Button) findViewById(R.id.btnForward);
	    btnForward.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				String value = "";
				int eventaction = event.getAction();
				switch (eventaction ) { 
		        	case MotionEvent.ACTION_DOWN: value = "f"; break;
		        	case MotionEvent.ACTION_UP: value = "s"; break;
				 }
				 
				command(value);
				 
				return false;
			}
		});
	    
	    btnBackward = (Button) findViewById(R.id.btnBackward);
	    btnBackward.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				String value = "";
				int eventaction = event.getAction();
				switch (eventaction ) { 
		        	case MotionEvent.ACTION_DOWN: value = "b"; break;
		        	case MotionEvent.ACTION_UP: value = "s"; break;
				 }
				 
				command(value);
				 
				return false;
			}
		});

		BluetoothDevice finalDevice = this.getIntent().getParcelableExtra(
				BluetoothDevice.EXTRA_DEVICE);
		//�鿴�Ƿ����Ѿ����ӹ���豸
		SocketApplication app = (SocketApplication) getApplicationContext();
		device = app.getDevice();
		if (finalDevice == null) {
			if (device == null) {
				Intent intent = new Intent(this, SearchDeviceActivity.class);
				startActivity(intent);
				finish();
				return;
			}
		} else if (finalDevice != null) {
			app.setDevice(finalDevice);
			device = app.getDevice();
		}
		new Thread() {
			public void run() {
				connect(device);
			};
		}.start();
	}
	
	public void command(String cmd){
		try {
			if (outputStream != null) {
				outputStream.write(cmd.getBytes());
			} else {
				Toast.makeText(getBaseContext(),
						"wait",
						Toast.LENGTH_SHORT).show();
			}
		} catch (IOException e) {
			Log.e(TAG, ">>", e);
			e.printStackTrace();
		}
		
	}
	/**
	 * Send
	 * @param view
	 */
	public void onButtonClicksend(View view) {
		String editText = sEditText.getText().toString();
		String tempHex = "";
		byte bytes[] = editText.getBytes();

		/*
		tempHex = SamplesUtils.stringToHex(editText);

		String hex = hexString.toString();
		if (hex == "") {
			hexString.append("-->");
		} else {
			if (hex.lastIndexOf("-->") < hex.lastIndexOf("<--")) {
				hexString.append("\n-->");
			}
		}
		hexString.append(tempHex);
		//����ָ����С �����ǰ�������
		hex = hexString.toString();
		if (hex.length() > maxlength) {
			try {
				hex = hex.substring(hex.length() - maxlength, hex.length());
				hex = hex.substring(hex.indexOf(" "));
				hex = "-->" + hex;
				hexString = new StringBuffer();
				hexString.append(hex);
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG, "e", e);
			}
		}

		sTextView.setText(bufferStrToHex(hexString.toString(), false)
					.trim());
*/
		try {
			if (outputStream != null) {
				outputStream.write(bytes);
			} else {
				Toast.makeText(getBaseContext(),
						"wait",
						Toast.LENGTH_SHORT).show();
			}
		} catch (IOException e) {
			Log.e(TAG, ">>", e);
			e.printStackTrace();
		}
	}

	/* after select, connect to device */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode != REQUEST_DISCOVERY) {
			finish();
			return;
		}
		if (resultCode != RESULT_OK) {
			finish();
			return;
		}
		final BluetoothDevice device = data
				.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		new Thread() {
			public void run() {
				connect(device);
			};
		}.start();
	}

	protected void onDestroy() {
		super.onDestroy();
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			Log.e(TAG, ">>", e);
		}
	}

	protected void connect(BluetoothDevice device) {
		try {
			// Create a Socket connection: need the server's UUID number of
			// registered
			socket = device.createRfcommSocketToServiceRecord(UUID
					.fromString("00001101-0000-1000-8000-00805F9B34FB"));

			socket.connect();
			Log.d(TAG, ">>Client connectted");
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
			int read = -1;
			final byte[] bytes = new byte[2048];
			for (; (read = inputStream.read(bytes)) > -1;) {
				final int count = read;
				_handler.post(new Runnable() {
					public void run() {
						String str = "";
						str = SamplesUtils.byteToHex(bytes, count);
						Log.d(TAG, "test1:" + str);
						String hex = hexString.toString();
						if (hex == "") {
							hexString.append("<--");
						} else {
							if (hex.lastIndexOf("<--") < hex.lastIndexOf("-->")) {
								hexString.append("\n<--");
							}
						}
						hexString.append(str);
						hex = hexString.toString();
						Log.d(TAG, "test2:" + hex);
						if (hex.length() > maxlength) {
							try {
								hex = hex.substring(hex.length() - maxlength,
										hex.length());
								hex = hex.substring(hex.indexOf(" "));
								hex = "<--" + hex;
								hexString = new StringBuffer();
								hexString.append(hex);
							} catch (Exception e) {
								e.printStackTrace();
								Log.e(TAG, "e", e);
							}
						}

							//sTextView.setText(bufferStrToHex(
							//		hexString.toString(), false).trim());

					}
				});
			}

		} catch (IOException e) {
			Log.e(TAG, ">>", e);
			Toast.makeText(getBaseContext(),
					"exception",
					Toast.LENGTH_SHORT).show();
			return;
		} finally {
			if (socket != null) {
				try {
					Log.d(TAG, ">>Client Socket Close");
					socket.close();
					finish();
					return;
				} catch (IOException e) {
					Log.e(TAG, ">>", e);
				}
			}
		}
	}

	public String bufferStrToHex(String buffer, boolean flag) {
		String all = buffer;
		StringBuffer sb = new StringBuffer();
		String[] ones = all.split("<--");
		for (int i = 0; i < ones.length; i++) {
			if (ones[i] != "") {
				String[] twos = ones[i].split("-->");
				for (int j = 0; j < twos.length; j++) {
					if (twos[j] != "") {
						if (flag) {
							sb.append(SamplesUtils.stringToHex(twos[j]));
						} else {
							sb.append(SamplesUtils.hexToString(twos[j]));
						}
						if (j != twos.length - 1) {
							if (sb.toString() != "") {
								sb.append("\n");
							}
							sb.append("-->");
						}
					}
				}
				if (i != ones.length - 1) {
					if (sb.toString() != "") {
						sb.append("\n");
					}
					sb.append("<--");
				}
			}
		}
		return sb.toString();
	}

	@Override
	  public void onSensorChanged(SensorEvent event) {
	    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
	      getAccelerometer(event);
	    }

	  }

	  private void getAccelerometer(SensorEvent event) {
	    float[] values = event.values;
	    // Movement
	    // float x = values[0];
	    //float y = values[1];
	    //float z = values[2];
	    
	    //txtViewX.setText(x+"");
	    //txtViewY.setText(y+"");
	    //txtViewZ.setText(z+"");
	    
	    updateRotation(values[1]);
	    
	    /*float accelationSquareRoot = (x * x + y * y + z * z)
	        / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
	    long actualTime = System.currentTimeMillis();
	    if (accelationSquareRoot >= 2) //
	    {
	      if (actualTime - lastUpdate < 200) {
	        return;
	      }
	      lastUpdate = actualTime;
	      Toast.makeText(this, "Device was shuffed", Toast.LENGTH_SHORT)
	          .show();
	      if (color) {
	        view.setBackgroundColor(Color.GREEN);

	      } else {
	        view.setBackgroundColor(Color.RED);
	      }
	      color = !color;
	    }*/
	  }

	  private void updateRotation(float y)
	  {
		  if(y > 1)
			 command("r");
		  else if(y < -1)
			 command("l");
			  
	      Matrix matrix = new Matrix();
	      matrix.postRotate((float)(y*11));
	      
	      Bitmap redrawnBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
	      steel.setImageBitmap(redrawnBitmap);
	  }
	  
	  
	  @Override
	  public void onAccuracyChanged(Sensor sensor, int accuracy) {

	  }

	  @Override
	  protected void onResume() {
	    super.onResume();
	    // register this class as a listener for the orientation and
	    // accelerometer sensors
	    sensorManager.registerListener(this,
	        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
	        SensorManager.SENSOR_DELAY_NORMAL);
	  }

	  @Override
	  protected void onPause() {
	    // unregister listener
	    super.onPause();
	    sensorManager.unregisterListener(this);
	  }

}