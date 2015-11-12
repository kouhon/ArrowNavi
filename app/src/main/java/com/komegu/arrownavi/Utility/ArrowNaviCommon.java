/**
 * 共通処理クラス
 * 
 * @author K.Honda
 * Copyright (C) 2012 Kouichi Honda All Rights Reserved.
 */
package com.komegu.arrownavi.Utility;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.support.annotation.NonNull;

public class ArrowNaviCommon
{
	/**
	 * ダイアログ作成
	 *
	 * @param context		コンテキスト
	 * @param strTitle		タイトル
	 * @param strMessage	メッセージ
	 * @param iIconId		アイコンID
	 * @param isCancelable	戻るボタンでのキャンセル許可フラグ
	 * @return ダイアログオブジェクト
	 */
	public static Builder makeDialog(
			@NonNull Context context,
			@NonNull String strTitle,
			@NonNull String strMessage,
			int iIconId,
			boolean isCancelable)
	{
		Builder dialog = new Builder(context, AlertDialog.THEME_HOLO_LIGHT);
		dialog.setTitle(strTitle);
		dialog.setMessage(strMessage);
		dialog.setIcon(iIconId);
		dialog.setCancelable(isCancelable);
		return dialog;
	}
}

