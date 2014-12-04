package net.sourceforge.opencamera;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Video.VideoColumns;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.ZoomControls;

class MyDebug {
	static final boolean LOG = false;
}

public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";

	private SensorManager mSensorManager = null;  //���崫����
	private Sensor mSensorAccelerometer = null;
	private Sensor mSensorMagnetic = null;
	/*a.��ȡϵͳ����SENSOR_SERVICE)����һ��SensorManager ����
    sensormanager = (SensorManager)getSystemSeriver(SENSOR_SERVICE);
b.ͨ��SensorManager�����ȡ��Ӧ��Sensor���͵Ķ���
    sensorObject = sensormanager.getDefaultSensor(sensor Type);
c.����һ��SensorEventListener ������������Sensor �¼���������onSensorChanged����
     SensorEventListener sensorListener = new SensorEventListener(){
      };
d.ע����Ӧ��SensorService
      sensormanager.registerListener(sensorListener, sensorObject, Sensor TYPE);
e.������Ӧ��SensorService
     sensormanager.unregisterListener(sensorListener, sensorObject);

f: SensorListener �ӿ��Ǵ�����Ӧ�ó�������ġ��������������跽����
   onSensorChanged(int sensor,float values[]) �����ڴ�����ֵ����ʱ���á�
   �÷���ֻ���ܴ�Ӧ�ó�����ӵĴ���������(�������ݼ�����)���÷����Ĳ���������һ��������ָʾ���ĵĴ�����;һ������ֵ���飬��ʾ���������ݱ�����Щ������ֻ�ṩһ������ֵ����һЩ���ṩ��������ֵ������ͼ��ٱ��������ṩ��������ֵ��
����������׼ȷ�Ը���ʱ�������� onAccuracyChanged(int sensor,int accuracy) ������������������������һ����ʾ����������һ����ʾ�ô������µ�׼ȷֵ��*/
	
	
	//locationManagerΪ��λ����һ����Ƭ������߶��������ַ��Ϣ
	private LocationManager mLocationManager = null;
	private LocationListener locationListener = null;
	
	
	private Preview preview = null; //����preview����
	private int current_orientation = 0; //׷�ٷ�����Ϣ
	private OrientationEventListener orientationEventListener = null; //�����¼�������
	private boolean supports_auto_stabilise = false; //֧���Զ��ȶ�����   ����Ϊ����ֻ�Ӳ����
	private boolean supports_force_video_4k = false;  //֧��4Kvideo
	private ArrayList<String> save_location_history = new ArrayList<String>();// �洢��ʷλ����Ϣ
	private boolean camera_in_background = false; // ������Ƿ�����һ��Ƭ�λ�Ի��򸲸ǣ����ڿ����û��������߼���
    private GestureDetector gestureDetector;  //Adroid�ṩ�����Ƽ����࣬���������Ƽ���
    private boolean screen_is_locked = false;  //��Ļ�Ƿ���������ʱ������ɶ��
    
    private Map<Integer, Bitmap> preloaded_bitmap_resources = new Hashtable<Integer, Bitmap>();
    //Map ���������ڴ洢Ԫ�ضԣ������������͡�ֵ����������ÿ����ӳ�䵽һ��ֵ����ʵ��Ԥ����λͼ��Դ�����ڼ�����Ƭ
    
    private PopupView popup_view = null; //����������ͼʵ��

    //����toastΪû�н���ģ���һ��ʱ����Զ���ʧ�ĵ�����
    private ToastBoxer screen_locked_toast = new ToastBoxer(); //��ͼ�Ƿ�����toast������
    ToastBoxer changed_auto_stabilise_toast = new ToastBoxer(); //�Ƿ�Ϊ�Զ��ȶ�toast������
    
    /*����ͨ�����ÿؼ���setDrawingCacheEnabled��true��������������ͼ���湦�ܣ�
     * �ڻ���View��ʱ���ͼ�񻺴�������Ȼ��ͨ��getDrawingCache����������ȡ��������Bitmap��
     * ��Ҫע����ǣ�������ʹ�����Bitmapʱ����Ҫ����destroyDrawingCache()�������ͷ�Bitmap��Դ��
     * �����ڻ���View����Ļʱ����ͼ��ή�Ϳؼ����Ƶ�Ч�ʣ�
     * ���ֻ������Ҫʹ��View��ͼ�񻺴��ʱ��ŵ���setDrawingCacheEnabled(true)��������ͼ�񻺴湦�ܣ�
     * ������ʹ��ͼ�񻺴�ʱ��Ҫ����setDrawingCacheEnabled(false) �ر�ͼ�񻺴湦�ܡ�
     * ���ַ�����֧����ק���͵�Ӧ���о�����������Androidϵͳ��LauncherӦ����Ҳʹ�������ַ�����
     * ���û���קӦ�õĿ��ͼ��ʱ����ȡ���ؼ���Ӧ��Bitmap��Ȼ��������Bitmap������ָ�ƶ���*/
	// for testing:
	public boolean is_test = false;
	public Bitmap gallery_bitmap = null;

	
	/*onCreate�����Ĳ�����һ��Bundle���͵Ĳ�����Bundle���͵�������Map���͵��������ƣ�������key-value����ʽ�洢���ݵġ�
  	 *�������Ͽ�saveInsanceState���Ǳ���ʵ��״̬�ġ�ʵ���ϣ�saveInsanceStateҲ���Ǳ���Activity��״̬�ġ�*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if( MyDebug.LOG ) {
			Log.d(TAG, "onCreate");
		}
    	long time_s = System.currentTimeMillis();   //��ȡϵͳʱ��
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main); //����activity_main.XML�ļ�������ͼ����
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false); //������ѡ�����ã��Ժ���Ҫ�о�

		if( getIntent() != null && getIntent().getExtras() != null ) {
			is_test = getIntent().getExtras().getBoolean("test_project");
			if( MyDebug.LOG )
				Log.d(TAG, "is_test: " + is_test);
		}
		
		//SharedPreferences���ڴ���һ����ֵ��
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		//���ڹ����豸�ϵ�activity�������жϵ�ǰapp״̬
		ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		if( MyDebug.LOG ) {
			Log.d(TAG, "standard max memory = " + activityManager.getMemoryClass() + "MB");
			Log.d(TAG, "large max memory = " + activityManager.getLargeMemoryClass() + "MB");
		}
		//if( activityManager.getMemoryClass() >= 128 ) { // test
		if( activityManager.getLargeMemoryClass() >= 128 ) {
			supports_auto_stabilise = true;
		}
		if( MyDebug.LOG )
			Log.d(TAG, "supports_auto_stabilise? " + supports_auto_stabilise);

		// hack to rule out phones unlikely to have 4K video, so no point even offering the option!
		// both S5 and Note 3 have 128MB standard and 512MB large heap (tested via Samsung RTL), as does Galaxy K Zoom
		// also added the check for having 128MB standard heap, to support modded LG G2, which has 128MB standard, 256MB large - see https://sourceforge.net/p/opencamera/tickets/9/
		if( activityManager.getMemoryClass() >= 128 || activityManager.getLargeMemoryClass() >= 512 ) {
			supports_force_video_4k = true;
		}
		if( MyDebug.LOG )
			Log.d(TAG, "supports_force_video_4k? " + supports_force_video_4k);

        setWindowFlagsForCamera(); //Ϊ������ô��ڱ�־

        // read save locations
        save_location_history.clear();
        int save_location_history_size = sharedPreferences.getInt("save_location_history_size", 0);//��ȡ��ֵ
		if( MyDebug.LOG )
			Log.d(TAG, "save_location_history_size: " + save_location_history_size);
        for(int i=0;i<save_location_history_size;i++) {
        	String string = sharedPreferences.getString("save_location_history_" + i, null);
        	if( string != null ) {
    			if( MyDebug.LOG )
    				Log.d(TAG, "save_location_history " + i + ": " + string);
        		save_location_history.add(string);
        	}
        }
        // also update, just in case a new folder has been set
		updateFolderHistory(); //�����ļ�����ʷ
		//updateFolderHistory("/sdcard/Pictures/OpenCameraTest");

		//��ȡϵͳ����������
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		if( mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null ) {
			if( MyDebug.LOG )
				Log.d(TAG, "found accelerometer");
			mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		}
		else {
			if( MyDebug.LOG )
				Log.d(TAG, "no support for accelerometer");
		}
		if( mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null ) {
			if( MyDebug.LOG )
				Log.d(TAG, "found magnetic sensor");
			mSensorMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		}
		else {
			if( MyDebug.LOG )
				Log.d(TAG, "no support for magnetic sensor");
		}

		//��ȡϵͳ��λ����
		mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

		updateGalleryIcon();	//������Ƭͼ��
		clearSeekBar();		//��ʼ��������

		preview = new Preview(this, savedInstanceState); //����ǰactivity״̬��Ϊ��������preview�Դ���ʵ����
		((ViewGroup) findViewById(R.id.preview)).addView(preview);	//���������ɵ����嶮
		
		//�����¼������������ڼ�����ǰ�ֻ�����״̬
		orientationEventListener = new OrientationEventListener(this) {
			@Override
			public void onOrientationChanged(int orientation) {
				MainActivity.this.onOrientationChanged(orientation);
			}
        };

        
        //��ʼ�ٿ���Ƭͼ�겼��
        View galleryButton = (View)findViewById(R.id.gallery);
        galleryButton.setOnLongClickListener(new View.OnLongClickListener() {	//�����¼�
			@Override
			public boolean onLongClick(View v) {
				//preview.showToast(null, "Long click");
				longClickedGallery();
				return true;
			}
        });
        
        
        //���Ƽ��������ڼ�����ǰ����
        gestureDetector = new GestureDetector(this, new MyGestureDetector());
        
        
        //�ж��Ƿ�Ϊ��һ�ν���,���´���Ϊ���Ƶ�һ�ν������ʾ��
        final String done_first_time_key = "done_first_time";
		boolean has_done_first_time = sharedPreferences.contains(done_first_time_key);
        if( !has_done_first_time && !is_test ) {
	        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle(R.string.app_name);
            alertDialog.setMessage(R.string.intro_text);
            alertDialog.setPositiveButton(R.string.intro_ok, null);
            alertDialog.show();

			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putBoolean(done_first_time_key, true);
			editor.apply();
        }

        preloadIcons(R.array.flash_icons); //���������״̬��ť����Ϣ
        preloadIcons(R.array.focus_mode_icons);	//����Խ�ѡ�ť����Ϣ

		if( MyDebug.LOG )
			Log.d(TAG, "time for Activity startup: " + (System.currentTimeMillis() - time_s));
	}
	
	//�˷������������ȡ����ѡ����Ϣ����Ӧ�ļ�ֵ
	private void preloadIcons(int icons_id) {
    	long time_s = System.currentTimeMillis();
    	String [] icons = getResources().getStringArray(icons_id);
    	for(int i=0;i<icons.length;i++) {
    		int resource = getResources().getIdentifier(icons[i], null, this.getApplicationContext().getPackageName());
    		if( MyDebug.LOG )
    			Log.d(TAG, "load resource: " + resource);
    		Bitmap bm = BitmapFactory.decodeResource(getResources(), resource);
    		this.preloaded_bitmap_resources.put(resource, bm);  
    	}
		if( MyDebug.LOG ) {
			Log.d(TAG, "time for preloadIcons: " + (System.currentTimeMillis() - time_s));
			Log.d(TAG, "size of preloaded_bitmap_resources: " + preloaded_bitmap_resources.size());
		}
	}
	
	@Override
	//�����ǰ���м�ֵ����Ϣ
	protected void onDestroy() {
		if( MyDebug.LOG ) {
			Log.d(TAG, "onDestroy");
			Log.d(TAG, "size of preloaded_bitmap_resources: " + preloaded_bitmap_resources.size());
		}
		// Need to recycle to avoid out of memory when running tests - probably good practice to do anyway
		for(Map.Entry<Integer, Bitmap> entry : preloaded_bitmap_resources.entrySet()) {
			if( MyDebug.LOG )
				Log.d(TAG, "recycle: " + entry.getKey());
			entry.getValue().recycle();
		}
		preloaded_bitmap_resources.clear();
		super.onDestroy();
	}
	
	
	@Override//Menu��������ʽ��Option menu��Context menu��ǰ���ǰ����豸��MenuӲ��ť�����������ǳ���widget������
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	//��ť�������������о�
	public boolean onKeyDown(int keyCode, KeyEvent event) { 
		if( MyDebug.LOG )
			Log.d(TAG, "onKeyDown: " + keyCode);
		switch( keyCode ) {
        case KeyEvent.KEYCODE_VOLUME_UP:
        case KeyEvent.KEYCODE_VOLUME_DOWN:
	        {
	    		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
	    		String volume_keys = sharedPreferences.getString("preference_volume_keys", "volume_take_photo");
	    		if( volume_keys.equals("volume_take_photo") ) {
	            	takePicture();
	                return true;
	    		}
	    		else if( volume_keys.equals("volume_focus") ) {
					preview.requestAutoFocus();
					return true;
	    		}
	    		else if( volume_keys.equals("volume_zoom") ) {
	    			if( keyCode == KeyEvent.KEYCODE_VOLUME_UP )
	    				this.preview.zoomIn();
	    			else
	    				this.preview.zoomOut();
	                return true;
	    		}
	    		else if( volume_keys.equals("volume_exposure") ) {
	    			if( keyCode == KeyEvent.KEYCODE_VOLUME_UP )
	    				this.preview.changeExposure(1, true);
	    			else
	    				this.preview.changeExposure(-1, true);
	                return true;
	    		}
	    		else if( volume_keys.equals("volume_auto_stabilise") ) {
	    			if( this.supports_auto_stabilise ) {
						boolean auto_stabilise = sharedPreferences.getBoolean("preference_auto_stabilise", false);
						auto_stabilise = !auto_stabilise;
						SharedPreferences.Editor editor = sharedPreferences.edit();
						editor.putBoolean("preference_auto_stabilise", auto_stabilise);
						editor.apply();
						String message = getResources().getString(R.string.preference_auto_stabilise) + ": " + getResources().getString(auto_stabilise ? R.string.on : R.string.off);
						preview.showToast(changed_auto_stabilise_toast, message);
	    			}
	    			else {
	    				preview.showToast(changed_auto_stabilise_toast, R.string.auto_stabilise_not_supported);
	    			}
	    			return true;
	    		}
	    		else if( volume_keys.equals("volume_really_nothing") ) {
	    			// do nothing, but still return true so we don't change volume either
	    			return true;
	    		}
	    		// else do nothing here, but still allow changing of volume (i.e., the default behaviour)
	    		break;
	        }
		case KeyEvent.KEYCODE_MENU:
			{
	        	// needed to support hardware menu button
	        	// tested successfully on Samsung S3 (via RTL)
	        	// see http://stackoverflow.com/questions/8264611/how-to-detect-when-user-presses-menu-key-on-their-android-device
				openSettings();
	            return true;
			}
		case KeyEvent.KEYCODE_CAMERA:
			{
				if( event.getRepeatCount() == 0 ) {
					View view = findViewById(R.id.take_photo);
					clickedTakePhoto(view);
		            return true;
				}
			}
		case KeyEvent.KEYCODE_FOCUS:
			{
				preview.requestAutoFocus();
	            return true;
			}
		case KeyEvent.KEYCODE_ZOOM_IN:
			{
				preview.zoomIn();
	            return true;
			}
		case KeyEvent.KEYCODE_ZOOM_OUT:
			{
				preview.zoomOut();
	            return true;
			}
		}
        return super.onKeyDown(keyCode, event); 
    }

	private SensorEventListener accelerometerListener = new SensorEventListener() {
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			preview.onAccelerometerSensorChanged(event);
		}
	};
	
	//���������¼�
	private SensorEventListener magneticListener = new SensorEventListener() {
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			preview.onMagneticSensorChanged(event); //���о�
		}
	};
	
	//���õ�ǰλ�ü����¼�
	private void setupLocationListener() {
		if( MyDebug.LOG )
			Log.d(TAG, "setupLocationListener");
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		// Define a listener that responds to location updates
		boolean store_location = sharedPreferences.getBoolean("preference_location", false);
		if( store_location && locationListener == null ) {
			locationListener = new LocationListener() {
			    public void onLocationChanged(Location location) {
					if( MyDebug.LOG )
						Log.d(TAG, "onLocationChanged");
			    	preview.locationChanged(location);
			    }

			    public void onStatusChanged(String provider, int status, Bundle extras) {
			    }

			    public void onProviderEnabled(String provider) {
			    }

			    public void onProviderDisabled(String provider) {
			    }
			};
			
			// see https://sourceforge.net/p/opencamera/tickets/1/
			if( mLocationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER) ) {
				mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
			}
			if( mLocationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER) ) {
				mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
			}
		}
		else if( !store_location && locationListener != null ) {
	        if( this.locationListener != null ) {
	            mLocationManager.removeUpdates(locationListener);
	            locationListener = null;
	        }
		}
	}

	@Override//��ʼ������ʾ���������о���
    protected void onResume() {
		if( MyDebug.LOG )
			Log.d(TAG, "onResume");
        super.onResume();

        mSensorManager.registerListener(accelerometerListener, mSensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(magneticListener, mSensorMagnetic, SensorManager.SENSOR_DELAY_NORMAL);
        orientationEventListener.enable();

        setupLocationListener();

        if( !this.camera_in_background ) {
			// immersive mode is cleared when app goes into background
			setImmersiveMode(true);
		}

		layoutUI();	//���ȼ���ui����

		updateGalleryIcon(); // update in case images deleted whilst idle

		preview.onResume();

    }

    @Override//pause״̬Ϊ��ǰ��һ���ؼ����ǣ����о�
    protected void onPause() {
		if( MyDebug.LOG )
			Log.d(TAG, "onPause");
        super.onPause();
		closePopup(); //���ȹرյ���u
        mSensorManager.unregisterListener(accelerometerListener);
        mSensorManager.unregisterListener(magneticListener);
        orientationEventListener.disable();   //�������ʧЧ
        if( this.locationListener != null ) {
            mLocationManager.removeUpdates(locationListener);
            locationListener = null;
        }
		// reset location, as may be out of date when resumed - the location listener is reinitialised when resuming
        preview.resetLocation();
		preview.onPause();
    }

    
    //��Ϊ������Ʋ��ֵķ���
    public void layoutUI() {
		if( MyDebug.LOG )
			Log.d(TAG, "layoutUI");
		this.preview.updateUIPlacement();
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this); //��ȡ�û�ϲ����Ϣ������ѡ��
		String ui_placement = sharedPreferences.getString("preference_ui_placement", "ui_right");
		boolean ui_placement_right = ui_placement.equals("ui_right");	//UI�ķ����Ƿ����ұ�,��֮Ϊ��߲���/
		if( MyDebug.LOG )
			Log.d(TAG, "ui_placement: " + ui_placement);
		// new code for orientation fixed to landscape	
		// the display orientation should be locked to landscape, but how many degrees is that?
	    int rotation = this.getWindowManager().getDefaultDisplay().getRotation(); //��Ļ�ķ�����Ϣ������ȡ��Ļ��ǰ��תֵ��
	    int degrees = 0;	//�������
	    switch (rotation) {
	    	case Surface.ROTATION_0: degrees = 0; break;
	        case Surface.ROTATION_90: degrees = 90; break;
	        case Surface.ROTATION_180: degrees = 180; break;
	        case Surface.ROTATION_270: degrees = 270; break;
	    }
	    // getRotation is anti-clockwise, but current_orientation is clockwise, so we add rather than subtract
	    // relative_orientation is clockwise from landscape-left
    	//int relative_orientation = (current_orientation + 360 - degrees) % 360;
    	int relative_orientation = (current_orientation + degrees) % 360;
    	//���Ͼ�Ϊ��ȡ��Ļ��ǰλ��״̬�����Կ���������������ͼ
    	
    	
		if( MyDebug.LOG ) {
			Log.d(TAG, "    current_orientation = " + current_orientation);
			Log.d(TAG, "    degrees = " + degrees);
			Log.d(TAG, "    relative_orientation = " + relative_orientation);
		}
		int ui_rotation = (360 - relative_orientation) % 360;
		preview.setUIRotation(ui_rotation);	//���ڿ�����Ļ�����Կ�����Ƭ����
		int align_left = RelativeLayout.ALIGN_LEFT;   //��Ԫ�غ�ĳԪ�����Ե����
		int align_right = RelativeLayout.ALIGN_RIGHT;  //��Ԫ�غ�ĳԪ���ұ�Ե����
		//int align_top = RelativeLayout.ALIGN_TOP;
		//int align_bottom = RelativeLayout.ALIGN_BOTTOM;
		int left_of = RelativeLayout.LEFT_OF;  //λ��ĳԪ�����
		int right_of = RelativeLayout.RIGHT_OF;	//λ��ĳԪ���ұ�
		int above = RelativeLayout.ABOVE;	//���ÿؼ��ĵײ����ڸ���ID�Ŀؼ�֮��
		int below = RelativeLayout.BELOW;	//���ÿؼ��ĵײ����ڸ���ID�Ŀؼ�֮��
		int align_parent_left = RelativeLayout.ALIGN_PARENT_LEFT;	//���Ϊtrue,���ÿؼ��������丸�ؼ����󲿶���;
		int align_parent_right = RelativeLayout.ALIGN_PARENT_RIGHT;	//���Ϊtrue,���ÿؼ��������丸�ؼ����Ҳ�����;
		int align_parent_top = RelativeLayout.ALIGN_PARENT_TOP;	//���Ϊtrue,���ÿؼ��Ķ������丸�ؼ��Ķ�������
		int align_parent_bottom = RelativeLayout.ALIGN_PARENT_BOTTOM;	//���Ϊtrue,���ÿؼ��ĵײ����丸�ؼ��ĵײ�����;
		if( !ui_placement_right ) {//���UI�������ò����ұߣ������·�ת�����������ַ���
			//align_top = RelativeLayout.ALIGN_BOTTOM;
			//align_bottom = RelativeLayout.ALIGN_TOP;
			above = RelativeLayout.BELOW;
			below = RelativeLayout.ABOVE;
			align_parent_top = RelativeLayout.ALIGN_PARENT_BOTTOM;
			align_parent_bottom = RelativeLayout.ALIGN_PARENT_TOP;
		}
		
		//���벼����
		{
			//���ð�ť
			View view = findViewById(R.id.settings);
			//�½���ʵ�����ڶ�̬�ı�view���֣�������������ʵ�����ٿ�view�Ĳ��֣����޸ĺú�Ĳ�������setLayoutPara�޸�
			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams(); 
			
			/*addRule(int verb, int anchor) ���÷��������ýڵ�����Ա�������������ֵܽڵ��������Ϊ����ֵ
			 * �� ����Ϊ����ֵʱ��anchor Ϊ RelativeLayout.TRUE ��ʾ true��anchor Ϊ0��ʾ false����
			 * ���磺addRule(RelativeLayout.ALIGN_LEFT, R.id.date) �ͱ�ʾ RelativeLayout 
			 * �е���Ӧ�ڵ������һ�� id ֵΪ date ���ֵܽڵ�����
			 */
			layoutParams.addRule(align_parent_left, 0);//�����ԣ��丸��ͼΪ���õģ���Ϊ����������ʱ����ת90��
			layoutParams.addRule(align_parent_right, RelativeLayout.TRUE);
			layoutParams.addRule(align_parent_top, 0);
			layoutParams.addRule(align_parent_bottom, 0);
			layoutParams.addRule(above, R.id.switch_camera);
			layoutParams.addRule(right_of, 0);
//			layoutParams.height = 100;    //�������޸ĵ�ǰview��СС
//			layoutParams.width = 100;
			view.setLayoutParams(layoutParams);
			//���õ�ǰview��ת�Ƕ���
			view.setRotation(ui_rotation);   
			
			
			
			//ͼ�ⰴť
			view = findViewById(R.id.gallery);
			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
			layoutParams.addRule(align_parent_top, 0);
			layoutParams.addRule(align_parent_bottom, 0);
			layoutParams.addRule(below, R.id.take_photo);
			layoutParams.addRule(left_of, R.id.settings);
			layoutParams.addRule(RelativeLayout.ALIGN_TOP, R.id.exposure_lock); //�ϱ�Ե����
			view.setLayoutParams(layoutParams);
			view.setRotation(ui_rotation);
	
			
			//����ѡ������ť
			view = findViewById(R.id.popup);
			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
			layoutParams.addRule(align_parent_right, RelativeLayout.TRUE);
			layoutParams.addRule(align_parent_left, 0);
//			layoutParams.addRule(left_of, R.id.gallery);
//			layoutParams.addRule(right_of, 0);
			view.setLayoutParams(layoutParams);
			view.setRotation(ui_rotation);
	
			//����Ц��ʶ��ť
			view = findViewById(R.id.smile);
			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
			layoutParams.addRule(align_parent_top, 0);
			layoutParams.addRule(align_parent_right, RelativeLayout.TRUE);
			layoutParams.addRule(below, R.id.exposure);
			layoutParams.addRule(right_of, 0);
			view.setLayoutParams(layoutParams);
			view.setRotation(ui_rotation);
			
			//�����عⰴť��
			view = findViewById(R.id.exposure_lock);
			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
			layoutParams.addRule(align_parent_top, 0);
			layoutParams.addRule(align_parent_right, RelativeLayout.TRUE);
			layoutParams.addRule(below, R.id.exposure);
			layoutParams.addRule(right_of, 0);
			view.setLayoutParams(layoutParams);
			view.setVisibility(View.GONE);
			view.setRotation(ui_rotation);
	
			//�ع����ð�ť
			view = findViewById(R.id.exposure);
			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
			layoutParams.addRule(align_parent_right, RelativeLayout.TRUE);
			layoutParams.addRule(align_parent_left, 0);
			layoutParams.addRule(below, R.id.popup);
			layoutParams.addRule(right_of, 0);
			view.setLayoutParams(layoutParams);
			view.setRotation(ui_rotation);
	
			//���������л���ť
			view = findViewById(R.id.switch_video);
			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
			layoutParams.addRule(align_parent_top, 0);
			layoutParams.addRule(align_parent_bottom, 0);
			layoutParams.addRule(left_of, R.id.exposure);
			layoutParams.addRule(above, R.id.take_photo);
			layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.settings);  //�����±�Ե����
			view.setLayoutParams(layoutParams);
			view.setRotation(ui_rotation);
	
			//ǰ������ͷ�л���ť
			view = findViewById(R.id.switch_camera);
			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
			layoutParams.addRule(align_parent_left, 0);
			layoutParams.addRule(align_parent_right, RelativeLayout.TRUE);
			layoutParams.addRule(align_parent_top, 0);
			layoutParams.addRule(align_parent_bottom, 0);
			layoutParams.addRule(above, R.id.popup);
			layoutParams.addRule(right_of, 0);
			view.setLayoutParams(layoutParams);
			view.setRotation(ui_rotation);
	
			//ɾ����ť����Ļ�ϱ�������
			view = findViewById(R.id.trash);
			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
			layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
			layoutParams.addRule(align_parent_bottom, 0);
			layoutParams.addRule(left_of, R.id.switch_camera);
			layoutParams.addRule(right_of, 0);
			view.setLayoutParams(layoutParams);
			view.setRotation(ui_rotation);
	
			//����ť����������
			view = findViewById(R.id.share);
			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
			layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
			layoutParams.addRule(align_parent_bottom, 0);
			layoutParams.addRule(left_of, R.id.trash);
			layoutParams.addRule(right_of, 0);
			view.setLayoutParams(layoutParams);
			view.setRotation(ui_rotation);
	
			
			//���հ�ť
			view = findViewById(R.id.take_photo);
			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
			layoutParams.addRule(align_parent_left, 0);
			layoutParams.addRule(align_parent_right, 0);
			layoutParams.addRule(left_of, R.id.popup);
			view.setLayoutParams(layoutParams);
			view.setRotation(ui_rotation);
	
			
			//�����ఴť,������Ϊ����
			view = findViewById(R.id.zoom);
			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
			layoutParams.addRule(align_parent_left, 0);
			layoutParams.addRule(align_parent_right, RelativeLayout.TRUE);
			layoutParams.addRule(align_parent_top, 0);
			layoutParams.addRule(align_parent_bottom, RelativeLayout.TRUE);
			view.setLayoutParams(layoutParams);
			view.setVisibility(View.GONE);
			view.setRotation(180.0f); // should always match the zoom_seekbar, so that zoom in and out are in the same directions
	
			//����������������ذɣ��Ž����ϲ��ÿ�
			view = findViewById(R.id.zoom_seekbar);
			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
			layoutParams.addRule(align_left, 0);
			layoutParams.addRule(align_right, R.id.zoom);
			layoutParams.addRule(above, R.id.zoom);
			layoutParams.addRule(below, 0);
			view.setLayoutParams(layoutParams);
			view.setVisibility(View.GONE);
		}
		
		{
			// set seekbar info
			View view = findViewById(R.id.seekbar);
			view.setRotation(ui_rotation);
			int width_dp = 0;
			if( ui_rotation == 0 || ui_rotation == 180 ) {
				width_dp = 300;
			}
			else {
				width_dp = 200;
			}
			int height_dp = 50;
			final float scale = getResources().getDisplayMetrics().density;
			int width_pixels = (int) (width_dp * scale + 0.5f); // convert dps to pixels
			int height_pixels = (int) (height_dp * scale + 0.5f); // convert dps to pixels
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)view.getLayoutParams();
			lp.width = width_pixels;
			lp.height = height_pixels;
			view.setLayoutParams(lp);

			view = findViewById(R.id.seekbar_zoom);
			view.setRotation(ui_rotation);
			view.setAlpha(0.5f);
			// n.b., using left_of etc doesn't work properly when using rotation (as the amount of space reserved is based on the UI elements before being rotated)
			if( ui_rotation == 0 ) {
				view.setTranslationX(0);
				view.setTranslationY(height_pixels);
			}
			else if( ui_rotation == 90 ) {
				view.setTranslationX(-height_pixels);
				view.setTranslationY(0);
			}
			else if( ui_rotation == 180 ) {
				view.setTranslationX(0);
				view.setTranslationY(-height_pixels);
			}
			else if( ui_rotation == 270 ) {
				view.setTranslationX(height_pixels);
				view.setTranslationY(0);
			}
		}

		{
			//���ǵ������ô��ڵĲ���
			
			View view = findViewById(R.id.popup_container);
			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
			//layoutParams.addRule(left_of, R.id.popup);
			layoutParams.addRule(left_of, R.id.take_photo);
//			layoutParams.addRule(below, R.id.popup);
			layoutParams.addRule(align_parent_bottom, 0);
//			layoutParams.addRule(above, 0);
//			layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
			layoutParams.addRule(RelativeLayout.ALIGN_TOP, R.id.switch_video); //�ϱ�Ե����
//			layoutParams.addRule(above, R.id.smile);
//			layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);  //��ֱ����
			view.setLayoutParams(layoutParams);

			view.setRotation(ui_rotation);
			// reset:
			view.setTranslationX(0.0f);
			view.setTranslationY(0.0f);
			if( MyDebug.LOG ) {
				Log.d(TAG, "popup view width: " + view.getWidth());
				Log.d(TAG, "popup view height: " + view.getHeight());
			}
			if( ui_rotation == 0 || ui_rotation == 180 ) {
				view.setPivotX(view.getWidth()/2.0f);
				view.setPivotY(view.getHeight()/2.0f);
			}
			else {
				view.setPivotX(view.getWidth());
				view.setPivotY(ui_placement_right ? 0.0f : view.getHeight());
				if( ui_placement_right ) {
					if( ui_rotation == 90 )
						view.setTranslationY( view.getWidth() );
					else if( ui_rotation == 270 )
						view.setTranslationX( - view.getHeight() );
				}
				else {
					if( ui_rotation == 90 )
						view.setTranslationX( - view.getHeight() );
					else if( ui_rotation == 270 )
						view.setTranslationY( - view.getWidth() );
				}
			}
		}

		{
			// set icon for taking photos vs videos
			ImageButton view = (ImageButton)findViewById(R.id.take_photo);
			if( preview != null ) {
				view.setImageResource(preview.isVideo() ? R.drawable.take_video_selector : R.drawable.take_photo_selector);
			}
		}
    }

    
    //����ı���Ӧ�¼�
    private void onOrientationChanged(int orientation) {
		/*if( MyDebug.LOG ) {
			Log.d(TAG, "onOrientationChanged()");
			Log.d(TAG, "orientation: " + orientation);
			Log.d(TAG, "current_orientation: " + current_orientation);
		}*/
		if( orientation == OrientationEventListener.ORIENTATION_UNKNOWN )
			return;
		int diff = Math.abs(orientation - current_orientation);
		if( diff > 180 )
			diff = 360 - diff;
		// only change orientation when sufficiently changed
		if( diff > 60 ) {
		    orientation = (orientation + 45) / 90 * 90;
		    orientation = orientation % 360;
		    if( orientation != current_orientation ) {
			    this.current_orientation = orientation;
				if( MyDebug.LOG ) {
					Log.d(TAG, "current_orientation is now: " + current_orientation);
				}
				layoutUI();
		    }
		}
	}
    
    @Override
    //���ָı���Ӧ�¼�
    public void onConfigurationChanged(Configuration newConfig) {
		if( MyDebug.LOG )
			Log.d(TAG, "onConfigurationChanged()");
		// configuration change can include screen orientation (landscape/portrait) when not locked (when settings is open)
		// needed if app is paused/resumed when settings is open and device is in portrait mode
        preview.setCameraDisplayOrientation(this);
        super.onConfigurationChanged(newConfig);
    }

    //���������Ӧ�¼����������Զ�����ʱ�ĵ���
    public void clickedTakePhoto(View view) {
		if( MyDebug.LOG )
			Log.d(TAG, "clickedTakePhoto");
    	this.takePicture();
    }

    //����л�����ͷ��Ӧ�¼�
    public void clickedSwitchCamera(View view) {
		if( MyDebug.LOG )
			Log.d(TAG, "clickedSwitchCamera");
		this.closePopup();
		this.preview.switchCamera();
    }

    //�����������ģʽ�л���Ӧ�¼�
    public void clickedSwitchVideo(View view) {
		if( MyDebug.LOG )
			Log.d(TAG, "clickedSwitchVideo");
		this.closePopup();
		this.preview.switchVideo(true, true);
    }

    //�������Ӧ�¼�
    public void clickedFlash(View view) {
		if( MyDebug.LOG )
			Log.d(TAG, "clickedFlash");
    	this.preview.cycleFlash();
    }
    
    //�Խ���Ӧ�¼�������Ҫ���Ժ�Ц������ʶ����Ҫ�޸Ĵ˷�����
    public void clickedFocusMode(View view) {
		if( MyDebug.LOG )
			Log.d(TAG, "clickedFocusMode");
    	this.preview.cycleFocusMode();
    }

    //���½�����
    void clearSeekBar() {
		View view = findViewById(R.id.seekbar);
		view.setVisibility(View.GONE);
		view = findViewById(R.id.seekbar_zoom);
		view.setVisibility(View.GONE);
    }
    
    //�����ع������
    void setSeekBarExposure() {
		SeekBar seek_bar = ((SeekBar)findViewById(R.id.seekbar));
		final int min_exposure = preview.getMinimumExposure();
		seek_bar.setMax( preview.getMaximumExposure() - min_exposure );
		seek_bar.setProgress( preview.getCurrentExposure() - min_exposure );
    }
    
    //����عⰴť��Ӧ�¼�
    public void clickedExposure(View view) {
		if( MyDebug.LOG )
			Log.d(TAG, "clickedExposure");
		this.closePopup();
		SeekBar seek_bar = ((SeekBar)findViewById(R.id.seekbar));
		int visibility = seek_bar.getVisibility();
		if( visibility == View.GONE && preview.getCamera() != null && preview.supportsExposures() ) {
			final int min_exposure = preview.getMinimumExposure();
			seek_bar.setVisibility(View.VISIBLE);
			setSeekBarExposure();
			seek_bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					if( MyDebug.LOG )
						Log.d(TAG, "exposure seekbar onProgressChanged");
					preview.setExposure(min_exposure + progress, false);
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
			});

			ZoomControls seek_bar_zoom = (ZoomControls)findViewById(R.id.seekbar_zoom);
//			seek_bar_zoom.setVisibility(View.VISIBLE);    //���±߼Ӽ��ؼ�����
			seek_bar_zoom.setOnZoomInClickListener(new View.OnClickListener(){
	            public void onClick(View v){
	            	preview.changeExposure(1, true);
	            }
	        });
			seek_bar_zoom.setOnZoomOutClickListener(new View.OnClickListener(){
		    	public void onClick(View v){
	            	preview.changeExposure(-1, true);
		        }
		    });
		}
		else if( visibility == View.VISIBLE ) {
			clearSeekBar();
		}
    }
    
    //����ع�������ť��Ӧ�¼�
    public void clickedExposureLock(View view) {
		if( MyDebug.LOG )
			Log.d(TAG, "clickedExposureLock");
    	this.preview.toggleExposureLock();
    }
    
    public void clickedSmileSetting(View view){
    	this.preview.toggleSmileSetting();
    }
    
    //������ð�ť��Ӧ�¼�
    public void clickedSettings(View view) {
		if( MyDebug.LOG )
			Log.d(TAG, "clickedSettings");
		openSettings();
    }

    //�ж��Ƿ����е�����
    public boolean popupIsOpen() {
		if( popup_view != null ) {
			return true;
		}
		return false;
    }

    // for testing
    public View getPopupButton(String key) {
    	return popup_view.getPopupButton(key);
    }

    void closePopup() {
		if( MyDebug.LOG )
			Log.d(TAG, "close popup");
		if( popupIsOpen() ) {
			ViewGroup popup_container = (ViewGroup)findViewById(R.id.popup_container);
			popup_container.removeAllViews();
			popup_view.close();
			popup_view = null;
		}
    }
    
    Bitmap getPreloadedBitmap(int resource) {
		Bitmap bm = this.preloaded_bitmap_resources.get(resource);
		return bm;
    }

    public void clickedPopupSettings(View view) {
		if( MyDebug.LOG )
			Log.d(TAG, "clickedPopupSettings");
		final ViewGroup popup_container = (ViewGroup)findViewById(R.id.popup_container);
		if( popupIsOpen() ) {
			closePopup();
			return;
		}
		if( preview.getCamera() == null ) {
			if( MyDebug.LOG )
				Log.d(TAG, "camera not opened!");
			return;
		}

		if( MyDebug.LOG )
			Log.d(TAG, "open popup");

		clearSeekBar();
		preview.cancelTimer(); // best to cancel any timer, in case we take a photo while settings window is open, or when changing settings

    	final long time_s = System.currentTimeMillis();

    	{
			// prevent popup being transparent
			popup_container.setBackgroundColor(Color.BLACK);
			popup_container.setAlpha(0.95f);
		}

    	popup_view = new PopupView(this);
		popup_container.addView(popup_view);
		
        // need to call layoutUI to make sure the new popup is oriented correctly
		// but need to do after the layout has been done, so we have a valid width/height to use
		popup_container.getViewTreeObserver().addOnGlobalLayoutListener( 
			new OnGlobalLayoutListener() {
				@SuppressWarnings("deprecation")
			    @SuppressLint("NewApi")
				@Override
			    public void onGlobalLayout() {
					if( MyDebug.LOG )
						Log.d(TAG, "onGlobalLayout()");
					if( MyDebug.LOG )
						Log.d(TAG, "time after global layout: " + (System.currentTimeMillis() - time_s));
		    		layoutUI();
					if( MyDebug.LOG )
						Log.d(TAG, "time after layoutUI: " + (System.currentTimeMillis() - time_s));
		    		// stop listening - only want to call this once!
		            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
		            	popup_container.getViewTreeObserver().removeOnGlobalLayoutListener(this);
		            } else {
		            	popup_container.getViewTreeObserver().removeGlobalOnLayoutListener(this);
		            }

		            
		            //�ߴ����������� �涨view�����ڸ�����Χ���Զ������Լ��ĳߴ��С
		            ScaleAnimation animation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
		    		animation.setDuration(100);
		    		popup_container.setAnimation(animation);
		        }
			}
		);

		if( MyDebug.LOG )
			Log.d(TAG, "time to create popup: " + (System.currentTimeMillis() - time_s));
    }
    
    private void openSettings() {
		if( MyDebug.LOG )
			Log.d(TAG, "openSettings");
		closePopup();
		preview.cancelTimer(); // best to cancel any timer, in case we take a photo while settings window is open, or when changing settings
		preview.stopVideo(false); // important to stop video, as we'll be changing camera parameters when the settings window closes
		
		Bundle bundle = new Bundle();
		bundle.putInt("cameraId", this.preview.getCameraId());
		bundle.putBoolean("supports_auto_stabilise", this.supports_auto_stabilise);
		bundle.putBoolean("supports_force_video_4k", this.supports_force_video_4k);
		bundle.putBoolean("supports_face_detection", this.preview.supportsFaceDetection());
		bundle.putBoolean("supports_video_stabilization", this.preview.supportsVideoStabilization());

		putBundleExtra(bundle, "color_effects", this.preview.getSupportedColorEffects());
		putBundleExtra(bundle, "scene_modes", this.preview.getSupportedSceneModes());
		putBundleExtra(bundle, "white_balances", this.preview.getSupportedWhiteBalances());
		putBundleExtra(bundle, "isos", this.preview.getSupportedISOs());
		bundle.putString("iso_key", this.preview.getISOKey());
		if( this.preview.getCamera() != null ) {
			bundle.putString("parameters_string", this.preview.getCamera().getParameters().flatten());
		}

		List<Camera.Size> preview_sizes = this.preview.getSupportedPreviewSizes();
		if( preview_sizes != null ) {
			int [] widths = new int[preview_sizes.size()];
			int [] heights = new int[preview_sizes.size()];
			int i=0;
			for(Camera.Size size: preview_sizes) {
				widths[i] = size.width;
				heights[i] = size.height;
				i++;
			}
			bundle.putIntArray("preview_widths", widths);
			bundle.putIntArray("preview_heights", heights);
		}
		
		List<Camera.Size> sizes = this.preview.getSupportedPictureSizes();
		if( sizes != null ) {
			int [] widths = new int[sizes.size()];
			int [] heights = new int[sizes.size()];
			int i=0;
			for(Camera.Size size: sizes) {
				widths[i] = size.width;
				heights[i] = size.height;
				i++;
			}
			bundle.putIntArray("resolution_widths", widths);
			bundle.putIntArray("resolution_heights", heights);
		}
		
		List<String> video_quality = this.preview.getSupportedVideoQuality();
		if( video_quality != null ) {
			String [] video_quality_arr = new String[video_quality.size()];
			String [] video_quality_string_arr = new String[video_quality.size()];
			int i=0;
			for(String value: video_quality) {
				video_quality_arr[i] = value;
				video_quality_string_arr[i] = this.preview.getCamcorderProfileDescription(value);
				i++;
			}
			bundle.putStringArray("video_quality", video_quality_arr);
			bundle.putStringArray("video_quality_string", video_quality_string_arr);
		}

		List<Camera.Size> video_sizes = this.preview.getSupportedVideoSizes();
		if( video_sizes != null ) {
			int [] widths = new int[video_sizes.size()];
			int [] heights = new int[video_sizes.size()];
			int i=0;
			for(Camera.Size size: video_sizes) {
				widths[i] = size.width;
				heights[i] = size.height;
				i++;
			}
			bundle.putIntArray("video_widths", widths);
			bundle.putIntArray("video_heights", heights);
		}
		
		putBundleExtra(bundle, "flash_values", this.preview.getSupportedFlashValues());
		putBundleExtra(bundle, "focus_values", this.preview.getSupportedFocusValues());

		setWindowFlagsForSettings();
		MyPreferenceFragment fragment = new MyPreferenceFragment();
		fragment.setArguments(bundle);
        getFragmentManager().beginTransaction().add(R.id.prefs_container, fragment, "PREFERENCE_FRAGMENT").addToBackStack(null).commit();
    }

    public void updateForSettings() {
    	updateForSettings(null);
    }

    public void updateForSettings(String toast_message) {
		if( MyDebug.LOG ) {
			Log.d(TAG, "updateForSettings()");
			if( toast_message != null ) {
				Log.d(TAG, "toast_message: " + toast_message);
			}
		}
    	String saved_focus_value = null;
    	if( preview.getCamera() != null && preview.isVideo() && !preview.focusIsVideo() ) {
    		saved_focus_value = preview.getCurrentFocusValue(); // n.b., may still be null
			// make sure we're into continuous video mode
			// workaround for bug on Samsung Galaxy S5 with UHD, where if the user switches to another (non-continuous-video) focus mode, then goes to Settings, then returns and records video, the preview freezes and the video is corrupted
			// so to be safe, we always reset to continuous video mode, and then reset it afterwards
			preview.updateFocusForVideo(false);
    	}
		if( MyDebug.LOG )
			Log.d(TAG, "saved_focus_value: " + saved_focus_value);
    	
		updateFolderHistory();

		// update camera for changes made in prefs - do this without closing and reopening the camera app if possible for speed!
		// but need workaround for Nexus 7 bug, where scene mode doesn't take effect unless the camera is restarted - I can reproduce this with other 3rd party camera apps, so may be a Nexus 7 issue...
		boolean need_reopen = false;
		if( preview.getCamera() != null ) {
			Camera.Parameters parameters = preview.getCamera().getParameters();
			if( MyDebug.LOG )
				Log.d(TAG, "scene mode was: " + parameters.getSceneMode());
			String key = Preview.getSceneModePreferenceKey();
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
			String value = sharedPreferences.getString(key, Camera.Parameters.SCENE_MODE_AUTO);
			if( !value.equals(parameters.getSceneMode()) ) {
				if( MyDebug.LOG )
					Log.d(TAG, "scene mode changed to: " + value);
				need_reopen = true;
			}
		}

		layoutUI(); // needed in case we've changed left/right handed UI
        setupLocationListener(); // in case we've enabled GPS
		if( need_reopen || preview.getCamera() == null ) { // if camera couldn't be opened before, might as well try again
			preview.onPause();
			preview.onResume(toast_message);
		}
		else {
			preview.setCameraDisplayOrientation(this); // need to call in case the preview rotation option was changed
			preview.pausePreview();
			preview.setupCamera(toast_message);
		}

    	if( saved_focus_value != null ) {
			if( MyDebug.LOG )
				Log.d(TAG, "switch focus back to: " + saved_focus_value);
    		preview.updateFocus(saved_focus_value, true, false);
    	}
    }
    
    boolean cameraInBackground() {
    	return this.camera_in_background;
    }
    
    MyPreferenceFragment getPreferenceFragment() {
        MyPreferenceFragment fragment = (MyPreferenceFragment)getFragmentManager().findFragmentByTag("PREFERENCE_FRAGMENT");
        return fragment;
    }
    
    @Override
    public void onBackPressed() {
        final MyPreferenceFragment fragment = getPreferenceFragment();
        if( screen_is_locked ) {
			preview.showToast(screen_locked_toast, R.string.screen_is_locked);
        	return;
        }
        if( fragment != null ) {
			if( MyDebug.LOG )
				Log.d(TAG, "close settings");
			setWindowFlagsForCamera();
			updateForSettings();
        }
        else {
			if( popupIsOpen() ) {
    			closePopup();
    			return;
    		}
        }
        super.onBackPressed();        
    }
    
    //@TargetApi(Build.VERSION_CODES.KITKAT)
	private void setImmersiveMode(boolean on) {
		// Andorid 4.4 immersive mode disabled for now, as not clear of a good way to enter and leave immersive mode, and "sticky" might annoy some users
        /*if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ) {
        	if( on )
        		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
        	else
        		getWindow().getDecorView().setSystemUiVisibility(0);
        }*/
    	if( on )
    		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
    	else
    		getWindow().getDecorView().setSystemUiVisibility(0);
    }
    
    private void setWindowFlagsForCamera() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		// force to landscape mode
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		// keep screen active - see http://stackoverflow.com/questions/2131948/force-screen-on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		if( sharedPreferences.getBoolean("preference_show_when_locked", true) ) {
	        // keep Open Camera on top of screen-lock (will still need to unlock when going to gallery or settings)
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		}
		else {
	        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		}

        // set screen to max brightness - see http://stackoverflow.com/questions/11978042/android-screen-brightness-max-value
		// done here rather than onCreate, so that changing it in preferences takes effect without restarting app
		{
	        WindowManager.LayoutParams layout = getWindow().getAttributes();
			if( sharedPreferences.getBoolean("preference_max_brightness", true) ) {
		        layout.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
	        }
			else {
		        layout.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
			}
	        getWindow().setAttributes(layout); 
		}
		
		setImmersiveMode(true);

		camera_in_background = false;
    }
    
    private void setWindowFlagsForSettings() {
		// allow screen rotation
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		// revert to standard screen blank behaviour
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // settings should still be protected by screen lock
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

		{
	        WindowManager.LayoutParams layout = getWindow().getAttributes();
	        layout.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
	        getWindow().setAttributes(layout); 
		}

		setImmersiveMode(false);

		camera_in_background = true;
    }
    
    class Media {
    	public long id;
    	public boolean video;
    	public Uri uri;
    	public long date;
    	public int orientation;

    	Media(long id, boolean video, Uri uri, long date, int orientation) {
    		this.id = id;
    		this.video = video;
    		this.uri = uri;
    		this.date = date;
    		this.orientation = orientation;
    	}
    }
    
    private Media getLatestMedia(boolean video) {
    	Media media = null;
		Uri baseUri = video ? Video.Media.EXTERNAL_CONTENT_URI : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		Uri query = baseUri.buildUpon().appendQueryParameter("limit", "1").build();
		String [] projection = video ? new String[] {VideoColumns._ID, VideoColumns.DATE_TAKEN} : new String[] {ImageColumns._ID, ImageColumns.DATE_TAKEN, ImageColumns.ORIENTATION};
		String selection = video ? "" : ImageColumns.MIME_TYPE + "='image/jpeg'";
		String order = video ? VideoColumns.DATE_TAKEN + " DESC," + VideoColumns._ID + " DESC" : ImageColumns.DATE_TAKEN + " DESC," + ImageColumns._ID + " DESC";
		Cursor cursor = null;
		try {
			cursor = getContentResolver().query(query, projection, selection, null, order);
			if( cursor != null && cursor.moveToFirst() ) {
				long id = cursor.getLong(0);
				long date = cursor.getLong(1);
				int orientation = video ? 0 : cursor.getInt(2);
				Uri uri = ContentUris.withAppendedId(baseUri, id);
				if( MyDebug.LOG )
					Log.d(TAG, "found most recent uri for " + (video ? "video" : "images") + ": " + uri);
				media = new Media(id, video, uri, date, orientation);
			}
		}
		finally {
			if( cursor != null ) {
				cursor.close();
			}
		}
		return media;
    }
    
    private Media getLatestMedia() {
		Media image_media = getLatestMedia(false);
		Media video_media = getLatestMedia(true);
		Media media = null;
		if( image_media != null && video_media == null ) {
			if( MyDebug.LOG )
				Log.d(TAG, "only found images");
			media = image_media;
		}
		else if( image_media == null && video_media != null ) {
			if( MyDebug.LOG )
				Log.d(TAG, "only found videos");
			media = video_media;
		}
		else if( image_media != null && video_media != null ) {
			if( MyDebug.LOG ) {
				Log.d(TAG, "found images and videos");
				Log.d(TAG, "latest image date: " + image_media.date);
				Log.d(TAG, "latest video date: " + video_media.date);
			}
			if( image_media.date >= video_media.date ) {
				if( MyDebug.LOG )
					Log.d(TAG, "latest image is newer");
				media = image_media;
			}
			else {
				if( MyDebug.LOG )
					Log.d(TAG, "latest video is newer");
				media = video_media;
			}
		}
		return media;
    }

    public void updateGalleryIconToBlank() {
		if( MyDebug.LOG )
			Log.d(TAG, "updateGalleryIconToBlank");
    	ImageButton galleryButton = (ImageButton) this.findViewById(R.id.gallery);
	    int bottom = galleryButton.getPaddingBottom();
	    int top = galleryButton.getPaddingTop();
	    int right = galleryButton.getPaddingRight();
	    int left = galleryButton.getPaddingLeft();
	    /*if( MyDebug.LOG )
			Log.d(TAG, "padding: " + bottom);*/
	    galleryButton.setImageBitmap(null);
		galleryButton.setImageResource(R.drawable.gallery);
		// workaround for setImageResource also resetting padding, Android bug
		galleryButton.setPadding(left, top, right, bottom);
		gallery_bitmap = null;
    }
    
    public void updateGalleryIconToBitmap(Bitmap bitmap) {
		if( MyDebug.LOG )
			Log.d(TAG, "updateGalleryIconToBitmap");
    	ImageButton galleryButton = (ImageButton) this.findViewById(R.id.gallery);
		galleryButton.setImageBitmap(bitmap);
		gallery_bitmap = bitmap;
    }
    
    public void updateGalleryIcon() {
		if( MyDebug.LOG )
			Log.d(TAG, "updateGalleryIcon");
    	long time_s = System.currentTimeMillis();
    	Media media = getLatestMedia();
		Bitmap thumbnail = null;
    	if( media != null ) {
    		if( media.video ) {
    			  thumbnail = MediaStore.Video.Thumbnails.getThumbnail(getContentResolver(), media.id, MediaStore.Video.Thumbnails.MINI_KIND, null);
    		}
    		else {
    			  thumbnail = MediaStore.Images.Thumbnails.getThumbnail(getContentResolver(), media.id, MediaStore.Images.Thumbnails.MINI_KIND, null);
    		}
    		if( thumbnail != null ) {
	    		if( media.orientation != 0 ) {
	    			if( MyDebug.LOG )
	    				Log.d(TAG, "thumbnail size is " + thumbnail.getWidth() + " x " + thumbnail.getHeight());
	    			Matrix matrix = new Matrix();
	    			matrix.setRotate(media.orientation, thumbnail.getWidth() * 0.5f, thumbnail.getHeight() * 0.5f);
	    			try {
	    				Bitmap rotated_thumbnail = Bitmap.createBitmap(thumbnail, 0, 0, thumbnail.getWidth(), thumbnail.getHeight(), matrix, true);
	        		    // careful, as rotated_thumbnail is sometimes not a copy!
	        		    if( rotated_thumbnail != thumbnail ) {
	        		    	thumbnail.recycle();
	        		    	thumbnail = rotated_thumbnail;
	        		    }
	    			}
	    			catch(Throwable t) {
		    			if( MyDebug.LOG )
		    				Log.d(TAG, "failed to rotate thumbnail");
	    			}
	    		}
    		}
    	}
    	if( thumbnail != null ) {
			if( MyDebug.LOG )
				Log.d(TAG, "set gallery button to thumbnail");
			updateGalleryIconToBitmap(thumbnail);
    	}
    	else {
			if( MyDebug.LOG )
				Log.d(TAG, "set gallery button to blank");
			updateGalleryIconToBlank();
    	}
		if( MyDebug.LOG )
			Log.d(TAG, "time to update gallery icon: " + (System.currentTimeMillis() - time_s));
    }
    
    public void clickedGallery(View view) {
		if( MyDebug.LOG )
			Log.d(TAG, "clickedGallery");
		//Intent intent = new Intent(Intent.ACTION_VIEW, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		Uri uri = null;
		Media media = getLatestMedia();
		if( media != null ) {
			uri = media.uri;
		}

		if( uri != null ) {
			// check uri exists
			if( MyDebug.LOG )
				Log.d(TAG, "found most recent uri: " + uri);
			try {
				ContentResolver cr = getContentResolver();
				ParcelFileDescriptor pfd = cr.openFileDescriptor(uri, "r");
				if( pfd == null ) {
					if( MyDebug.LOG )
						Log.d(TAG, "uri no longer exists (1): " + uri);
					uri = null;
				}
				pfd.close();
			}
			catch(IOException e) {
				if( MyDebug.LOG )
					Log.d(TAG, "uri no longer exists (2): " + uri);
				uri = null;
			}
		}
		if( uri == null ) {
			uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		}
		if( !is_test ) {
			// don't do if testing, as unclear how to exit activity to finish test (for testGallery())
			if( MyDebug.LOG )
				Log.d(TAG, "launch uri:" + uri);
			final String REVIEW_ACTION = "com.android.camera.action.REVIEW";
			try {
				// REVIEW_ACTION means we can view video files without autoplaying
				Intent intent = new Intent(REVIEW_ACTION, uri);
				this.startActivity(intent);
			}
			catch(ActivityNotFoundException e) {
				if( MyDebug.LOG )
					Log.d(TAG, "REVIEW_ACTION intent didn't work, try ACTION_VIEW");
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				// from http://stackoverflow.com/questions/11073832/no-activity-found-to-handle-intent - needed to fix crash if no gallery app installed
				//Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("blah")); // test
				if( intent.resolveActivity(getPackageManager()) != null ) {
					this.startActivity(intent);
				}
				else{
					preview.showToast(null, R.string.no_gallery_app);
				}
			}
		}
    }
    
    public void updateFolderHistory() {
		String folder_name = getSaveLocation();
		updateFolderHistory(folder_name);
    }
    
    private void updateFolderHistory(String folder_name) {
		while( save_location_history.remove(folder_name) ) {
		}
		save_location_history.add(folder_name);
		while( save_location_history.size() > 6 ) {
			save_location_history.remove(0);
		}
		writeSaveLocations();
    }
    
    public void clearFolderHistory() {
		save_location_history.clear();
		updateFolderHistory(); // to re-add the current choice, and save
    }
    
    private void writeSaveLocations() {
		if( MyDebug.LOG )
			Log.d(TAG, "writeSaveLocations");
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt("save_location_history_size", save_location_history.size());
		if( MyDebug.LOG )
			Log.d(TAG, "save_location_history_size = " + save_location_history.size());
        for(int i=0;i<save_location_history.size();i++) {
        	String string = save_location_history.get(i);
    		editor.putString("save_location_history_" + i, string);
        }
		editor.apply();
    }
    
    private void longClickedGallery() {
		if( MyDebug.LOG )
			Log.d(TAG, "longClickedGallery");
		if( save_location_history.size() <= 1 ) {
			return;
		}
		final int theme = android.R.style.Theme_Black_NoTitleBar_Fullscreen;
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this, theme);
        alertDialog.setTitle(R.string.choose_save_location);
        CharSequence [] items = new CharSequence[save_location_history.size()+1];
        int index=0;
        // save_location_history is stored in order most-recent-last
        for(int i=0;i<save_location_history.size();i++) {
        	items[index++] = save_location_history.get(save_location_history.size() - 1 - i);
        }
        final int clear_index = index;
        items[index++] = getResources().getString(R.string.clear_folder_history);
        /*final int new_index = index;
        items[index++] = getResources().getString(R.string.new_save_location);*/
		alertDialog.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if( which == clear_index ) {
					if( MyDebug.LOG )
						Log.d(TAG, "selected clear save history");
				    new AlertDialog.Builder(MainActivity.this)
			        	.setIcon(android.R.drawable.ic_dialog_alert)
			        	.setTitle(R.string.clear_folder_history)
			        	.setMessage(R.string.clear_folder_history_question)
			        	.setPositiveButton(R.string.answer_yes, new DialogInterface.OnClickListener() {
			        		@Override
					        public void onClick(DialogInterface dialog, int which) {
								if( MyDebug.LOG )
									Log.d(TAG, "confirmed clear save history");
								clearFolderHistory();
					        }
			        	})
			        	.setNegativeButton(R.string.answer_no, null)
			        	.show();
					setWindowFlagsForCamera();
				}
				/*else if( which == new_index ) {
					if( MyDebug.LOG )
						Log.d(TAG, "selected choose new folder");
		    		FolderChooserDialog fragment = new FolderChooserDialog();
		    		fragment.setStyle(DialogFragment.STYLE_NORMAL, theme);
		    		fragment.show(getFragmentManager(), "FOLDER_FRAGMENT");
					FragmentTransaction ft = getFragmentManager().beginTransaction();
					//DialogFragment newFragment = MyDialogFragment.newInstance();
					FolderChooserDialog fragment = new FolderChooserDialog();
					ft.add(R.id.prefs_container, fragment);
					ft.commit();
		    		fragment.getDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							if( MyDebug.LOG )
								Log.d(TAG, "FolderChooserDialog dismissed");
							setWindowFlagsForCamera();
						}
;		    		});
				}*/
				else {
					if( MyDebug.LOG )
						Log.d(TAG, "selected: " + which);
					if( which >= 0 && which < save_location_history.size() ) {
						String save_folder = save_location_history.get(save_location_history.size() - 1 - which);
						if( MyDebug.LOG )
							Log.d(TAG, "changed save_folder from history to: " + save_folder);
						preview.showToast(null, getResources().getString(R.string.changed_save_location) + "\n" + save_folder);
						SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
						SharedPreferences.Editor editor = sharedPreferences.edit();
						editor.putString("preference_save_location", save_folder);
						editor.apply();
						updateFolderHistory(); // to move new selection to most recent
					}
					setWindowFlagsForCamera();
				}
			}
        });
		alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface arg0) {
		        setWindowFlagsForCamera();
			}
		});
        alertDialog.show();
		//getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		setWindowFlagsForSettings();
    }

    static private void putBundleExtra(Bundle bundle, String key, List<String> values) {
		if( values != null ) {
			String [] values_arr = new String[values.size()];
			int i=0;
			for(String value: values) {
				values_arr[i] = value;
				i++;
			}
			bundle.putStringArray(key, values_arr);
		}
    }

    public void clickedShare(View view) {
		if( MyDebug.LOG )
			Log.d(TAG, "clickedShare");
    	this.preview.clickedShare();
    }

    public void clickedTrash(View view) {
		if( MyDebug.LOG )
			Log.d(TAG, "clickedTrash");
    	this.preview.clickedTrash();
    	// Calling updateGalleryIcon() immediately has problem that it still returns the latest image that we've just deleted!
    	// But works okay if we call after a delay. 100ms works fine on Nexus 7 and Galaxy Nexus, but set to 500 just to be safe.
    	final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
		    	updateGalleryIcon();
			}
		}, 500);
    }

    private void takePicture() {
		if( MyDebug.LOG )
			Log.d(TAG, "takePicture");
		closePopup();
    	this.preview.takePicturePressed();
    }
    
    void lockScreen() {
		((ViewGroup) findViewById(R.id.locker)).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
                //return true;
            }
		});
		screen_is_locked = true;
    }
    
    void unlockScreen() {
		((ViewGroup) findViewById(R.id.locker)).setOnTouchListener(null);
		screen_is_locked = false;
    }
    
    boolean isScreenLocked() {
    	return screen_is_locked;
    }

    class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
        		if( MyDebug.LOG )
        			Log.d(TAG, "from " + e1.getX() + " , " + e1.getY() + " to " + e2.getX() + " , " + e2.getY());
        		final ViewConfiguration vc = ViewConfiguration.get(MainActivity.this);
        		//final int swipeMinDistance = 4*vc.getScaledPagingTouchSlop();
    			final float scale = getResources().getDisplayMetrics().density;
    			final int swipeMinDistance = (int) (160 * scale + 0.5f); // convert dps to pixels
        		final int swipeThresholdVelocity = vc.getScaledMinimumFlingVelocity();
        		if( MyDebug.LOG ) {
        			Log.d(TAG, "from " + e1.getX() + " , " + e1.getY() + " to " + e2.getX() + " , " + e2.getY());
        			Log.d(TAG, "swipeMinDistance: " + swipeMinDistance);
        		}
                float xdist = e1.getX() - e2.getX();
                float ydist = e1.getY() - e2.getY();
                float dist2 = xdist*xdist + ydist*ydist;
                float vel2 = velocityX*velocityX + velocityY*velocityY;
                if( dist2 > swipeMinDistance*swipeMinDistance && vel2 > swipeThresholdVelocity*swipeThresholdVelocity ) {
                	preview.showToast(screen_locked_toast, R.string.unlocked);
                	unlockScreen();
                }
            }
            catch(Exception e) {
            }
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
			preview.showToast(screen_locked_toast, R.string.screen_is_locked);
			return true;
        }
    }	

	@Override
	protected void onSaveInstanceState(Bundle state) {
		if( MyDebug.LOG )
			Log.d(TAG, "onSaveInstanceState");
	    super.onSaveInstanceState(state);
	    if( this.preview != null ) {
	    	preview.onSaveInstanceState(state);
	    }
	}

    public void broadcastFile(File file, boolean is_new_picture, boolean is_new_video) {
    	// note that the new method means that the new folder shows up as a file when connected to a PC via MTP (at least tested on Windows 8)
    	if( file.isDirectory() ) {
    		//this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.fromFile(file)));
        	// ACTION_MEDIA_MOUNTED no longer allowed on Android 4.4! Gives: SecurityException: Permission Denial: not allowed to send broadcast android.intent.action.MEDIA_MOUNTED
    		// note that we don't actually need to broadcast anything, the folder and contents appear straight away (both in Gallery on device, and on a PC when connecting via MTP)
    		// also note that we definitely don't want to broadcast ACTION_MEDIA_SCANNER_SCAN_FILE or use scanFile() for folders, as this means the folder shows up as a file on a PC via MTP (and isn't fixed by rebooting!)
    	}
    	else {
        	// both of these work fine, but using MediaScannerConnection.scanFile() seems to be preferred over sending an intent
    		//this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
        	MediaScannerConnection.scanFile(this, new String[] { file.getAbsolutePath() }, null,
        			new MediaScannerConnection.OnScanCompletedListener() {
    		 		public void onScanCompleted(String path, Uri uri) {
    		 			if( MyDebug.LOG ) {
    		 				Log.d("ExternalStorage", "Scanned " + path + ":");
    		 				Log.d("ExternalStorage", "-> uri=" + uri);
    		 			}
    		 		}
    			}
    		);
        	if( is_new_picture ) {
    	        /*ContentValues values = new ContentValues(); 
    	        values.put(ImageColumns.TITLE, file.getName().substring(0, file.getName().lastIndexOf(".")));
    	        values.put(ImageColumns.DISPLAY_NAME, file.getName());
    	        values.put(ImageColumns.DATE_TAKEN, System.currentTimeMillis()); 
    	        values.put(ImageColumns.MIME_TYPE, "image/jpeg");
    	        // TODO: orientation
    	        values.put(ImageColumns.DATA, file.getAbsolutePath());
    	        Location location = preview.getLocation();
    	        if( location != null ) {
        	        values.put(ImageColumns.LATITUDE, location.getLatitude()); 
        	        values.put(ImageColumns.LONGITUDE, location.getLongitude()); 
    	        }
    	        try {
    	    		this.getContentResolver().insert(Images.Media.EXTERNAL_CONTENT_URI, values); 
    	        }
    	        catch (Throwable th) { 
        	        // This can happen when the external volume is already mounted, but 
        	        // MediaScanner has not notify MediaProvider to add that volume. 
        	        // The picture is still safe and MediaScanner will find it and 
        	        // insert it into MediaProvider. The only problem is that the user 
        	        // cannot click the thumbnail to review the picture. 
        	        Log.e(TAG, "Failed to write MediaStore" + th); 
        	    }*/
        		this.sendBroadcast(new Intent(Camera.ACTION_NEW_PICTURE, Uri.fromFile(file)));
        		// for compatibility with some apps - apparently this is what used to be broadcast on Android?
        		this.sendBroadcast(new Intent("com.android.camera.NEW_PICTURE", Uri.fromFile(file)));
        	}
        	else if( is_new_video ) {
        		this.sendBroadcast(new Intent(Camera.ACTION_NEW_VIDEO, Uri.fromFile(file)));
        	}
    	}
	}
    
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private String getSaveLocation() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		String folder_name = sharedPreferences.getString("preference_save_location", "OpenCamera");
		return folder_name;
    }
    
    static File getBaseFolder() {
    	return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
    }

    static File getImageFolder(String folder_name) {
		File file = null;
		if( folder_name.length() > 0 && folder_name.lastIndexOf('/') == folder_name.length()-1 ) {
			// ignore final '/' character
			folder_name = folder_name.substring(0, folder_name.length()-1);
		}
		//if( folder_name.contains("/") ) {
		if( folder_name.startsWith("/") ) {
			file = new File(folder_name);
		}
		else {
	        file = new File(getBaseFolder(), folder_name);
		}
		/*if( MyDebug.LOG ) {
			Log.d(TAG, "folder_name: " + folder_name);
			Log.d(TAG, "full path: " + file);
		}*/
        return file;
    }
    
    public File getImageFolder() {
		String folder_name = getSaveLocation();
		return getImageFolder(folder_name);
    }

    /** Create a File for saving an image or video */
    @SuppressLint("SimpleDateFormat")
	public File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

    	File mediaStorageDir = getImageFolder();
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if( !mediaStorageDir.exists() ) {
            if( !mediaStorageDir.mkdirs() ) {
        		if( MyDebug.LOG )
        			Log.e(TAG, "failed to create directory");
                return null;
            }
            broadcastFile(mediaStorageDir, false, false);
        }

        //��
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String index = "";
        File mediaFile = null;
        for(int count=1;count<=100;count++) {
            if( type == MEDIA_TYPE_IMAGE ) {
                mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + index + ".jpg");
            }
            else if( type == MEDIA_TYPE_VIDEO ) {
                mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "VID_"+ timeStamp + index + ".mp4");
            }
            else {
                return null;
            }
            if( !mediaFile.exists() ) {
            	break;
            }
            index = "_" + count; // try to find a unique filename
        }
        

		if( MyDebug.LOG ) {
			Log.d(TAG, "getOutputMediaFile returns: " + mediaFile);
		}
        return mediaFile;
    }
    
    public boolean supportsAutoStabilise() {
    	return this.supports_auto_stabilise;
    }

    public boolean supportsForceVideo4K() {
    	return this.supports_force_video_4k;
    }

    @SuppressWarnings("deprecation")
	public long freeMemory() { // return free memory in MB
    	try {
    		File image_folder = this.getImageFolder();
	        StatFs statFs = new StatFs(image_folder.getAbsolutePath());
	        // cast to long to avoid overflow!
	        long blocks = statFs.getAvailableBlocks();
	        long size = statFs.getBlockSize();
	        long free  = (blocks*size) / 1048576;
			/*if( MyDebug.LOG ) {
				Log.d(TAG, "freeMemory blocks: " + blocks + " size: " + size + " free: " + free);
			}*/
	        return free;
    	}
    	catch(IllegalArgumentException e) {
    		// can fail on emulator, at least!
    		return -1;
    	}
    }
    
    public static String getDonateLink() {
    	return "https://play.google.com/store/apps/details?id=harman.mark.donation";
    }

    /*public static String getDonateMarketLink() {
    	return "market://details?id=harman.mark.donation";
    }*/

    public Preview getPreview() {
    	return this.preview;
    }

    // for testing:
	public ArrayList<String> getSaveLocationHistory() {
		return this.save_location_history;
	}
	
	public LocationListener getLocationListener() {
		return this.locationListener;
	}
}
