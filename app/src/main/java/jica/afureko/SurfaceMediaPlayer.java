package jica.afureko;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import java.io.IOException;

public class SurfaceMediaPlayer {
    public MediaPlayer mediaPlayer;
    private String movieFilePath;
    private Context context;
    private SurfaceView surfaceView;
    private int seekTIme;

    SurfaceMediaPlayer(Context context, SurfaceView surfaceView, String fileName, int seekTIme) {
        this.context = context;
        this.surfaceView = surfaceView;
        this.seekTIme = seekTIme;
        movieFilePath = fileName;
        surfaceView.getHolder().addCallback(mCallback);
    }

    public final SurfaceHolder.Callback mCallback = new SurfaceHolder.Callback() {
        /**
         * SurfaceViewが生成された時に呼び出される
         */
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            setMediaPlayer();
        }
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }
         @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }
    };

    public void setMediaPlayer(){
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        try {
            mediaPlayer = new MediaPlayer();
            AssetFileDescriptor afd = context.getAssets().openFd(movieFilePath);     //Assetから動画を読み込む
            mediaPlayer.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(), afd.getLength());
            mediaPlayer.setDisplay(surfaceView.getHolder());
            mediaPlayer.prepare();
            mediaPlayer.seekTo( seekTIme );
            //surfaceViewの大きさに合わせて画面サイズを計算
            setSurfaceSize();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
    }

    public void start(){
        mediaPlayer.start();
    }

    public void pause(){
        mediaPlayer.pause();
    }

    public boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }

    public void seekTo(int mses)
    {
        mediaPlayer.seekTo(mses);
    }

    public int getCurrentPosition(){
        return mediaPlayer.getCurrentPosition();
    }

    public void setSurfaceSize(){
        float surfaceWidth = surfaceView.getWidth();        //画面の幅
        float surfaceHeight = surfaceView.getHeight();      //画面の高さ
        float videoWidth = mediaPlayer.getVideoWidth();     //ビデオの幅
        float videoHeight = mediaPlayer.getVideoHeight();   //ビデオの高さ
        //高さに合わせて幅を計算
        int width = (int)(videoWidth * (surfaceHeight/videoHeight));
        int height =  (int)surfaceHeight;
        //幅が狭い場合は、幅に合わせて高さを計算
        if(width > surfaceWidth){
            height =  (int)(videoHeight * (surfaceWidth/videoWidth));
            width =  (int)surfaceWidth;
        }
        //セットする
        ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
        lp.width = width;
        lp.height = height;
        surfaceView.setLayoutParams(lp);
    }
}
