package khumun.arduino.bluetoothcar.sensor;

import khumun.arduino.bluetoothcar.R;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class SensorMainActivity extends Activity implements SensorEventListener {
  private SensorManager sensorManager;
  private TextView txtViewY;
  private ImageView steel;
  private Bitmap bitmap;
  
/** Called when the activity is first created. */

  @Override
  public void onCreate(Bundle savedInstanceState) {
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.sensor);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    WindowManager.LayoutParams lp = getWindow().getAttributes();
    lp.screenBrightness = 100 / 100.0f;
    getWindow().setAttributes(lp);
    
    steel = (ImageView) findViewById(R.id.imageView1);
    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.steering_wheel);
    
    //txtViewY = (TextView) findViewById(R.id.textViewY);
    
    sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    //lastUpdate = System.currentTimeMillis();
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