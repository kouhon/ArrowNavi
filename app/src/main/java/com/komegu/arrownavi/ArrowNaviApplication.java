/**
 * アプリケーションクラス
 *
 * @author K.Honda
 * Copyright (C) 2015 Kouichi Honda All Rights Reserved.
 */
package com.komegu.arrownavi;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by kouichi on 15/11/11.
 */
public class ArrowNaviApplication extends Application
{
    /**
     * デフォルトバッファサイズ
     */
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 1024;
    /**
     * フォント
     */
    @NonNull public Typeface mFont;

    @Override
    public void onCreate()
    {
        super.onCreate();

        ArrayList<String> alTtfFilePath = new ArrayList<String>();
        alTtfFilePath.add("fonts/nagurip.ttf.1");
        alTtfFilePath.add("fonts/nagurip.ttf.2");
        alTtfFilePath.add("fonts/nagurip.ttf.3");
        alTtfFilePath.add("fonts/nagurip.ttf.4");
        alTtfFilePath.add("fonts/nagurip.ttf.5");
        String strFontFilePath = this.getFilesDir().toString() + "/nagurip.ttf";
        if(this.combineFile(this, alTtfFilePath, strFontFilePath))
        {
            this.mFont = Typeface.createFromFile(strFontFilePath);
        }
        else
        {
            this.mFont = Typeface.DEFAULT;
        }
    }

    /**
     * ファイル結合
     *
     * @param context			コンテキスト
     * @param alSrcFilePath		分割ファイルパス
     * @param strDstFilePath	結合ファイルパス
     * @return 結合結果(true:成功 false:失敗)
     */
    private boolean combineFile(
            @NonNull Context context,
            @NonNull ArrayList<String> alSrcFilePath,
            @NonNull String strDstFilePath)
    {
        boolean result;
        File dstFile = new File(strDstFilePath);
        dstFile.delete();
        InputStream input;
        try
        {
            OutputStream output = new FileOutputStream(dstFile, false);
            for (int i = 0; i < alSrcFilePath.size(); i++)
            {
                input = context.getAssets().open(
                        alSrcFilePath.get(i),
                        AssetManager.ACCESS_STREAMING);
                byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
                while (true)
                {
                    int iSize = input.read(buf);
                    if (iSize <= 0)
                    {
                        break;
                    }
                    output.write(buf, 0, iSize);
                }
                input.close();
            }
            result = true;
        }
        catch(IOException e)
        {
            Log.e(this.getPackageName(), e.getMessage());
            result = false;
        }
        return result;
    }

}
