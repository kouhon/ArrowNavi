/**
 * 非同期ジオコード取得イベントオブジェクトクラス
 *
 * @author K.Honda
 * Copyright (C) 2015 Kouichi Honda All Rights Reserved.
 */
package com.komegu.arrownavi.Event;

import android.support.annotation.NonNull;
import com.google.code.geocoder.model.LatLng;

/**
 * Created by kouichi on 15/11/10.
 */
public class GeoCodingAsyncTaskEvent
{
    /**
     * イベント結果(true:取得成功 false:取得失敗)
     */
    public boolean mResult;
    /**
     * 住所
     */
    public String mAddress;
    /**
     * 簡易住所
     */
    public String mSimpleAddress;
    /**
     * 緯度経度
     */
    public LatLng mLatLng;

    /**
     * コンストラクタ
     * @param result イベント結果(true:取得成功 false:取得失敗)
     * @param address 住所
     * @param simpleAddress 簡易住所
     * @param latlng 緯度経度
     */
    public GeoCodingAsyncTaskEvent(
            boolean result,
            @NonNull String address,
            @NonNull String simpleAddress,
            @NonNull LatLng latlng)
    {
        this.mResult = result;
        this.mAddress = address;
        this.mSimpleAddress = simpleAddress;
        this.mLatLng = latlng;
    }
}
