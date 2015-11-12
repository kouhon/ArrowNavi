/**
 * スプラッシュ画面アクティビティクラス
 * 
 * @author K.Honda
 * Copyright (C) 2012 Kouichi Honda All Rights Reserved.
 */
package com.komegu.arrownavi.Activity;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.KeyEvent;
import java.util.Timer;
import java.util.TimerTask;
import com.komegu.arrownavi.Utility.ArrowNaviCommon;
import com.komegu.arrownavi.R;

/**
 * @author kouichi
 *
 */
public class ArrowNaviSplashActivity extends Activity
{
	/**
	 * スプラッシュ画面終了ディレイ秒数
	 */
	private final int SPLASH_FINISH_DELAY = 2000;
	/**
	 * スプラッシュ終了用のタイマー
	 */
	private final Timer mTimer = new Timer();


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.arrow_navi_main);
		
        SensorManager sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        LocationManager lm = (LocationManager)getSystemService(LOCATION_SERVICE);
        if((sm == null) || (lm == null))
        {
    		//　アプリを使っている端末がGPSと方位取得ができない場合はアプリを終了する
        	Builder dialog = ArrowNaviCommon.makeDialog(
					this,
					this.getString(R.string.dialog_sorry_title),
					this.getString(R.string.sensor_location_negative_message),
					R.drawable.dialog_bow,
					false);
        	dialog.setPositiveButton(
					this.getString(R.string.yes),
					new OnClickListener()
        	{
				public void onClick(DialogInterface dialog, int which)
				{
					finish();
				}
			});
        	dialog.show();
        }
        else
		{
			TimerTask tt = new TimerTask()
			{
				@Override
				public void run()
				{
					Intent intent = new Intent(
							ArrowNaviSplashActivity.this,
							ArrowNaviSettingDstActivity.class);
					startActivity(intent);
					finish();
				}
			};
			this.mTimer.schedule(tt, this.SPLASH_FINISH_DELAY);
        }
	}

	@Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
		if (event.getAction() == KeyEvent.ACTION_DOWN &&
				event.getKeyCode() == KeyEvent.KEYCODE_BACK)
		{
			this.mTimer.cancel();
		}

		return super.dispatchKeyEvent(event);
    }
}
