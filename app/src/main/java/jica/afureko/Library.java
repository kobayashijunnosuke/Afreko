package jica.afureko;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Library {
    private Context context;

    Library(Context context){
        this.context = context;
    }

    //dpからpixelへの変換
    public int dpToPx(float dp){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return  (int) (dp * metrics.density);
    }

    //ファイルリストを取得
    public List getAssetsDirectoryList (String directory, String matches){
        AssetManager assetManager = context.getAssets();
        List<String> fileList = new ArrayList<String>();
        try {
            String assetList[] = assetManager.list(directory);
            for(String fileName: assetList) {
                if(fileName.matches(matches)){
                    fileList.add(fileName);
                }
            }
        } catch (IOException e) {
        }
        return fileList;
    }

    //画像リソースを取得
    public Bitmap getAssetsBitmap(String fileName){
        AssetManager assetManager = context.getAssets();
        Bitmap bitmap = null;
        try{
            InputStream inputStream = assetManager.open(fileName);
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }
    
    //画像押下演出
    public static class ImageViewLowlighter implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    ((ImageView) v).setColorFilter( Color.argb(100, 0, 0, 0));
                    break;
                default:
                    ((ImageView) v).setColorFilter(null);
                    break;
            }
            return false;
        }
    }

    //メディアプレイヤーのファイル設定
    public MediaPlayer setMediaplayer(int id){
        MediaPlayer mp = new MediaPlayer();
        mp = MediaPlayer.create(context, id);
        mp.setLooping(false);
        mp.seekTo(0);
        mp.setVolume(1f, 1f);
        return mp;
    }
}
