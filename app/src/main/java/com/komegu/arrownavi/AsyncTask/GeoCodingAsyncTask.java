/**
 * 非同期ジオコード取得クラス
 *
 * @author K.Honda
 * Copyright (C) 2015 Kouichi Honda All Rights Reserved.
 */
package com.komegu.arrownavi.AsyncTask;

import android.os.AsyncTask;
import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderResult;
import com.google.code.geocoder.model.LatLng;
import com.google.code.geocoder.model.GeocoderStatus;
import android.util.Log;
import java.io.IOException;
import com.komegu.arrownavi.Event.GeoCodingAsyncTaskEvent;
import de.greenrobot.event.EventBus;

/**
 * Created by kouichi on 15/11/09.
 */
public class GeoCodingAsyncTask extends AsyncTask<String, Integer, GeocodeResponse>
{
    @Override
    protected GeocodeResponse doInBackground(String ... address)
    {
        if((address == null) || (address.length == 0))
        {
            return null;
        }

        Geocoder geocoder = new Geocoder();
        GeocoderRequest geocoderRequest = new GeocoderRequestBuilder().
                setAddress(address[0]).
                setLanguage("ja").
                getGeocoderRequest();
        try
        {
            return geocoder.geocode(geocoderRequest);
        }
        catch(IOException e)
        {
            Log.e("error", e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(GeocodeResponse geocodeResponse)
    {
        // ジオコーディングAPIのレスポンス結果を通知する
        boolean result = this.resultGeocodeResponse(geocodeResponse);
        String address = "";
        String simpleAddress = "";
        LatLng latlng = new LatLng();
        if(result == true)
        {
            GeocoderResult gr = geocodeResponse.getResults().get(0);
            address = gr.getFormattedAddress();
            simpleAddress = gr.getAddressComponents().get(0).getLongName();
            latlng = gr.getGeometry().getLocation();
        }

        EventBus event = EventBus.getDefault();
        GeoCodingAsyncTaskEvent eventObject = new GeoCodingAsyncTaskEvent(
                result,
                address,
                simpleAddress,
                latlng);
        event.post(eventObject);
    }

    /**
     * ジオコードの取得結果を取得する
     * @param geocodeResponse ジオコードオブジェクト
     * @return true:取得成功 false:取得失敗
     */
    private boolean resultGeocodeResponse(GeocodeResponse geocodeResponse)
    {
        if(geocodeResponse == null)
        {
            return false;
        }
        GeocoderStatus gs = geocodeResponse.getStatus();
        if(gs != GeocoderStatus.OK)
        {
            return false;
        }
        return true;
    }
}
