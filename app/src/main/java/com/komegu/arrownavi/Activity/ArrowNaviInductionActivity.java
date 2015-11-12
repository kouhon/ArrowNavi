/**
 * 目的地誘導画面アクティビティクラス
 * Copyright (C) 2012 Kouichi Honda All Rights Reserved.
 */
package com.komegu.arrownavi.Activity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.GpsStatus;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.code.geocoder.model.LatLng;
import com.komegu.arrownavi.ArrowNaviApplication;
import com.komegu.arrownavi.Utility.ArrowNaviCommon;
import com.komegu.arrownavi.R;
import android.provider.Settings;

public class ArrowNaviInductionActivity extends Activity implements
		SensorEventListener,
		LocationListener,
		GpsStatus.Listener
{
	/**
	 * 初期角度
	 */
	private final static double INI_DEGREES = -1;
	/**
	 * 初期距離
	 */
	private final static double INI_DIST = -1;
	/**
	 * 地球直径(m)
	 */
	private final static double EARTH_DIAMETER = 12756274;
	/**
	 * 赤道円周距離
	 */
	private final static double EQUATOR_LENGTH = EARTH_DIAMETER * Math.PI;
	/**
	 * 位置情報更新間隔時間
	 */
	private final static int LOCATION_UPDATE_MILLI_SECONDS = 5000;
	/**
	 * 位置情報更新距離
	 */
	private final static int LOCATION_UPDATE_METRE = 1;
	/**
	 * センサーマネージャ
	 */
	private SensorManager mSensorManager;
	/**
	 * ロケーションマネージャ
	 */
	private LocationManager mLocationManager;
	/**
	 * 方位角度(0:北 90:東 180:南 270:西)
	 */
	private double mOrientationDegrees = INI_DEGREES;
	/**
	 * 前回方位角度(0:北 90:東 180:南 270:西)
	 */
	private double mPrevOrientationDegrees = INI_DEGREES;
	/**
	 * 自ロケーション情報
	 */
	private Location mDeviceLocation;
	/**
	 * 目的地ロケーション情報
	 */
	private LatLng mDstLatLon;
	/**
	 * 目的地の簡易住所
	 */
	private String mDstSimpleAddress;
	/**
	 * 矢印角度
	 */
	private double mArrowDegrees = INI_DEGREES;
	/**
	 * 前回矢印角度
	 */
	private double mPrevArrowDegrees = INI_DEGREES;
	/**
	 * 目的地までの距離(m)
	 */
	private double mDistanceToDestination = INI_DIST;
	/**
	 * 目的地までの距離を表示するTextView
	 */
	private TextView mDistTextView;

	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.arrow_navi_induction);

		Intent intent = getIntent();
		this.mDstLatLon = (LatLng)intent.getSerializableExtra("dstLatLon");
		this.mDstSimpleAddress = intent.getStringExtra("simpleAddress");

		this.mDistTextView = (TextView)findViewById(R.id.DistTextView);
		Typeface tf = ((ArrowNaviApplication)this.getApplication()).mFont;
        this.mDistTextView.setTypeface(tf);
        
        this.mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        this.mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
	}

	@Override
    public void onResume()
    {
		super.onResume();
		String gpsStatus = Settings.Secure.getString(
				getContentResolver(),
				Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		if(gpsStatus.indexOf(this.mLocationManager.GPS_PROVIDER) < 0)
		{
			Builder dialog = ArrowNaviCommon.makeDialog(this,
					this.getString(R.string.gps_setting_negative_notice_title),
					this.getString(R.string.gps_setting_negative_notice_message),
					R.drawable.dialog_bow,
					false);
			dialog.setPositiveButton(R.string.yes, new OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					startActivity(intent);
				}
			});
			dialog.setNegativeButton(R.string.no, new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					finish();
				}
			});
			dialog.show();
			return;
		}
		else
		{
			this.mDistTextView.setText(R.string.gps_info_retrieving_message);
		}

		// ロケーションのイベント登録(5秒ごと、1mごとに更新)
		this.mLocationManager.addGpsStatusListener(this);
		this.mLocationManager.requestLocationUpdates(
				this.mLocationManager.GPS_PROVIDER,
				this.LOCATION_UPDATE_MILLI_SECONDS,
				this.LOCATION_UPDATE_METRE,
				this);
		// 方位センセーのイベントを登録
		List<Sensor> sensors = this.mSensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
		for(Sensor sensor : sensors)
		{
			this.mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
		}
    }

	@Override
    public void onPause()
    {
		this.mLocationManager.removeGpsStatusListener(this);
		this.mLocationManager.removeUpdates(this);
		this.mSensorManager.unregisterListener(this);
		super.onPause();
    }

	@Override
	public void onDestroy()
	{
		this.mDistTextView = null;
		super.onDestroy();
	}

	/**
     * 方位センサーが更新された時にコールされる
	 * @param sensor センサーイベントオブジェクト
     */
    public void onSensorChanged(SensorEvent sensor)
    {
    	this.mPrevOrientationDegrees = this.mOrientationDegrees;
    	this.mOrientationDegrees = sensor.values[0];
		if(this.mDeviceLocation != null)
		{
			this.calcArrowDegrees(this.mDeviceLocation, this.mOrientationDegrees);
		}
		this.rotateArrow(this.mArrowDegrees, this.mPrevArrowDegrees);
		this.rotateConnpass();
    }

    /**
     * ロケーションが更新された時にコールされる
	 * @param location ロケーションオブジェクト
     */
	public void onLocationChanged(Location location)
	{
		this.mDeviceLocation = location;
		if(this.mDeviceLocation == null)
		{
			return;
		}

		this.calcArrowDegrees(this.mDeviceLocation, this.mOrientationDegrees);
		this.calcDist(location);
		this.rotateArrow(this.mArrowDegrees, this.mPrevOrientationDegrees);
		this.dispDistance(this.mDistanceToDestination);
	}

	/**
	 * 目的地を示す矢印の角度計算
	 * @param deviceLocation デバイスの位置情報
	 * @param azimuthAngle 方位角
	 */
	private void calcArrowDegrees(@NonNull Location deviceLocation, double azimuthAngle)
	{
		if(azimuthAngle == INI_DEGREES)
		{
			return;
		}

		this.mPrevArrowDegrees = this.mArrowDegrees;

		LatLng dstLatLon = this.mDstLatLon;

		// デバイスの座標を(0,0)として相対座標に変換
		double relativeLat = dstLatLon.getLat().doubleValue() - deviceLocation.getLatitude();
		double relativeLng = dstLatLon.getLng().doubleValue() - deviceLocation.getLongitude();

		// 北を向いている時の目的地との角度を算出
		double dBufDegrees;
		if((relativeLng >= 0) && (relativeLat >= 0))
		{
			dBufDegrees = (Math.atan(relativeLng / relativeLat) * 180 / Math.PI);
		}
		else if((relativeLng >= 0) && (relativeLat < 0))
		{
			relativeLat = relativeLat * -1;
			dBufDegrees = (Math.atan(relativeLat / relativeLng) * 180 / Math.PI) + 90;
		}
		else if((relativeLng < 0) && (relativeLat < 0))
		{
			relativeLng = relativeLng * -1;
			relativeLat = relativeLat * -1;
			dBufDegrees = 270 - (Math.atan(relativeLat / relativeLng) * 180 / Math.PI);
		}
		else
		{
			relativeLng = relativeLng * -1;
			dBufDegrees = (Math.atan(relativeLat / relativeLng) * 180 / Math.PI) + 270;
		}
		
		// 現在のデバイスが向いている方位から実際の目的地との角度を算出
		this.mArrowDegrees = dBufDegrees - azimuthAngle;
		if(this.mArrowDegrees < 0)
		{
			this.mArrowDegrees = this.mArrowDegrees + 360;
		}
	}
	
	/**
	 * デバイスと目的地までの距離を計算する
	 * @param deviceLocation デバイスの位置情報
	 */
	private void calcDist(@NonNull Location deviceLocation)
	{
		// デバイスの座標を(0,0)として相対座標に変換し、
		// 三角関数と地球の円周から距離を算出する
		double relativeLat =
				this.mDstLatLon.getLat().doubleValue() - deviceLocation.getLatitude();
		double relativeLng =
				this.mDstLatLon.getLng().doubleValue() - deviceLocation.getLongitude();
		double dDistDegrees = Math.sqrt((relativeLng * relativeLng) + (relativeLat * relativeLat));
		this.mDistanceToDestination = Math.abs(dDistDegrees * (EQUATOR_LENGTH / 360) - deviceLocation.getAccuracy());
	}

	/**
	 * 目的地を示す矢印を回転させる
	 * @param arrowDegrees 矢印の回転角度
	 * @param prevArrowDegrees 前回の矢印の回転角度
	 */
	private void rotateArrow(double arrowDegrees, double prevArrowDegrees)
	{
		double tmpArrowDegrees = arrowDegrees;
		double tmpPrevArrowDegrees = prevArrowDegrees;
		if(tmpArrowDegrees == INI_DEGREES)
		{
			tmpArrowDegrees = 0;
		}
		if(tmpPrevArrowDegrees == INI_DEGREES)
		{
			tmpPrevArrowDegrees = 0;
		}
		ImageView ArrowImageView = (ImageView)findViewById(R.id.ArrowImageView);
		RotateAnimation ArrowRotate = new RotateAnimation(
				(float)tmpPrevArrowDegrees,
				(float)tmpArrowDegrees,
				ArrowImageView.getWidth() / 2,
				ArrowImageView.getHeight() / 2);
		ArrowRotate.setDuration(1000);
		ArrowImageView.startAnimation(ArrowRotate);
	}

	/**
	 * コンパスを回転させる
	 */
	private void rotateConnpass()
	{
		double dOrientationDegrees = 360 - this.mOrientationDegrees;
		double dPrevOrientationDegrees = 360 - this.mPrevOrientationDegrees;
		if(this.mPrevOrientationDegrees == INI_DEGREES)
		{
			dPrevOrientationDegrees = 0;
		}
		if(this.mOrientationDegrees == INI_DEGREES)
		{
			dOrientationDegrees = 0;
		}
		ImageView CompassImageView = (ImageView)findViewById(R.id.CompassImageView);
		RotateAnimation CompassRotate = new RotateAnimation(
				(float)dPrevOrientationDegrees,
				(float)dOrientationDegrees,
				CompassImageView.getWidth() / 2,
				CompassImageView.getHeight() / 2);
		CompassRotate.setDuration(1000);
		CompassImageView.startAnimation(CompassRotate);
	}
	
	/**
	 * 目的地までの距離を表示する
	 * @param distanceToDestination 目的地までの距離
	 */
	private void dispDistance(double distanceToDestination)
	{
		if(distanceToDestination == INI_DIST)
		{
			// 距離が初期値のままの場合は距離表示を更新しない
			return;
		}

		TextView distTextView = (TextView)findViewById(R.id.DistTextView);
		// 距離を10M単位にする
		int iMDist = (int)distanceToDestination;
		iMDist = new BigDecimal((double)iMDist / 10).setScale(0, RoundingMode.HALF_UP).intValue();
		iMDist = iMDist * 10;
		
		// KM単位の算出
		int iKmDist = 0;
		if((iMDist / 1000) != 0)
		{
			iKmDist = new BigDecimal((double)iMDist / 1000).setScale(0, RoundingMode.HALF_UP).intValue();
		}
		
		if(iKmDist != 0)
		{
			String km = this.getString(R.string.distance_is_km);
			km = km.replace("0", Integer.toString(iKmDist)).replace("dst", this.mDstSimpleAddress);
			distTextView.setText(km);
		}
		else if(iMDist != 0)
		{
			String m = this.getString(R.string.distance_is_m);
			m = m.replace("0", Integer.toString(iMDist)).replace("dst", this.mDstSimpleAddress);
			distTextView.setText(m);
		}
		else
		{
			distTextView.setText(R.string.destination_is_close_message);
		}
	}

	public void onGpsStatusChanged(int event) {}
	public void onStatusChanged(String provider, int status, Bundle extras) {}
   	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	public void onProviderDisabled(String provider) {}
	public void onProviderEnabled(String provider) {}
}