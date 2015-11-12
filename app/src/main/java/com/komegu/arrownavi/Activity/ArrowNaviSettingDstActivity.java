/**
 * 目的地設定画面アクティビティクラス
 * 
 * @author K.Honda
 * Copyright (C) 2012 Kouichi Honda All Rights Reserved.
 */
package com.komegu.arrownavi.Activity;

import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.komegu.arrownavi.ArrowNaviApplication;
import com.komegu.arrownavi.AsyncTask.GeoCodingAsyncTask;
import com.komegu.arrownavi.Event.GeoCodingAsyncTaskEvent;
import de.greenrobot.event.EventBus;
import com.komegu.arrownavi.Utility.ArrowNaviCommon;
import com.komegu.arrownavi.R;

public class ArrowNaviSettingDstActivity extends Activity
{
	/**
	 * 音声入力を呼び出す際のリクエストコード
	 */
	private static final int MIC_REQUEST_CODE = 0;
	/**
	 * イベントバス
	 */
	private final EventBus mEventBus = EventBus.getDefault();
	/**
	 * 住所検索用のプログレスダイアログ
	 */
	private ProgressDialog mProgressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.arrow_navi_setting_dst);

		Typeface tf = ((ArrowNaviApplication)this.getApplication()).mFont;
		EditText dstEditText = (EditText)findViewById(R.id.DstEditText);
		dstEditText.setTypeface(tf);
		TextView dstTextView = (TextView)findViewById(R.id.DstTextView);
		dstTextView.setTypeface(tf);

		this.mProgressDialog = new ProgressDialog(this, AlertDialog.THEME_HOLO_LIGHT);
		this.mProgressDialog.setIcon(R.drawable.dialog_normal);
		this.mProgressDialog.setTitle(R.string.progress_title);
		this.mProgressDialog.setMessage(this.getString(R.string.progress_message));
		this.mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		this.mProgressDialog.setCancelable(false);
	}

	@Override
	public void onResume()
	{
		super.onResume();
		this.mEventBus.register(this);
	}

	@Override
	public void onPause()
	{
		this.mEventBus.unregister(this);
		super.onPause();
	}
		
	/**
	 * 音声入力ダイアログを呼び出す
	 * 
	 * @param view
	 */
	public void clickMicBtn(View view)
	{
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(
				RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, this.getString(R.string.mic_dialog_title));
        startActivityForResult(intent, MIC_REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == MIC_REQUEST_CODE && resultCode == RESULT_OK) 
		{
			// 音声入力ダイアログで入力された文字を画面上に表示する
			ArrayList<String> results = data.getStringArrayListExtra(
					RecognizerIntent.EXTRA_RESULTS);
			final String strResult = results.get(0);
			EditText dstEditText = (EditText)findViewById(R.id.DstEditText);
			dstEditText.setText(strResult);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
    /**
     * 住所検索を実行する
     *
     * @param view
     */
    public void clickAddressSearch(View view)
    {
		EditText dstEditText = (EditText)findViewById(R.id.DstEditText);
		String address = dstEditText.getText().toString();
		address = address.trim();
		if(address.equals(""))
		{
			Builder dialog = ArrowNaviCommon.makeDialog(
					this,
					this.getString(R.string.destination_not_input_title),
					this.getString(R.string.destination_not_input_message),
					R.drawable.dialog_sad,
					false);
			dialog.setPositiveButton(R.string.yes, null);
			dialog.show();
			return;
		}

		this.mProgressDialog.show();
		GeoCodingAsyncTask task = new GeoCodingAsyncTask();
		task.execute(address);
    }

	/**
	 * ジオコーディング終了イベントを処理する
	 * @param event イベントオブジェクト
	 */
	public void onEvent(GeoCodingAsyncTaskEvent event)
	{
		this.mProgressDialog.dismiss();
		if(event.mResult == false)
		{
			Builder dialog = ArrowNaviCommon.makeDialog(
					this,
					this.getString(R.string.destination_not_found_title),
					this.getString(R.string.destination_not_found_message),
					R.drawable.dialog_sad, false);
			dialog.setPositiveButton(R.string.yes, null);
			dialog.show();
			return;
		}

		// 目的地の確認メッセージを表示して、ユーザが同意したら誘導画面に遷移する
		Builder dialog = ArrowNaviCommon.makeDialog(
				this,
				this.getString(R.string.destination_confirmation_message),
				event.mAddress,
				R.drawable.dialog_smile,
				false);
		final GeoCodingAsyncTaskEvent tmpEvent = event;
		dialog.setPositiveButton(R.string.yes, new OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				Intent intent = new Intent(
						ArrowNaviSettingDstActivity.this,
						ArrowNaviInductionActivity.class);
				intent.putExtra("dstLatLon", tmpEvent.mLatLng);
				intent.putExtra("simpleAddress", tmpEvent.mSimpleAddress);
				startActivity(intent);
			}
		});
		dialog.setNegativeButton(R.string.no, null);
		dialog.show();
	}

	@Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
		if((event.getAction() == KeyEvent.ACTION_DOWN) &&
				(event.getKeyCode() == KeyEvent.KEYCODE_BACK))
		{
			this.finish();
		}
		return super.dispatchKeyEvent(event);
    }
}
