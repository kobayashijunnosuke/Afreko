package jica.afureko;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;


public class AnimeActivity extends Activity {
    private String sceneId;
    private String mode;
    private SurfaceMediaPlayer surfacePlayer;
    private Library Lib;

    private ImageButton startBtn;
    private ImageButton restartBtn;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_anime );
        Lib = new Library(this);
        //引数受取
        sceneId = getIntent().getStringExtra( "sceneId" );
        mode = getIntent().getStringExtra( "mode" );

        startBtn = findViewById( R.id.startBtn );
        restartBtn =  findViewById( R.id.restartBtn );

        //サムネイル画像
        ImageView sceneThumbnail = findViewById(R.id.sceneThumbnail);
        sceneThumbnail.setImageBitmap( Lib.getAssetsBitmap(sceneId + "/thumbnail.png") );

        //モード表示
        ImageButton modeThumbnail = findViewById(R.id.modethumbnail);
        int modethumbnailResourceId;
        if(mode.equals( "cut_script" )){
            modethumbnailResourceId = getResources().getIdentifier("btn_script_disactive", "drawable", getPackageName());
        }else {
            modethumbnailResourceId = getResources().getIdentifier("btn_script", "drawable", getPackageName());
        }
        modeThumbnail.setImageResource( modethumbnailResourceId );
        modeThumbnail.setOnTouchListener(new Library.ImageViewLowlighter());   //押下感演出

        //サイドボタン押下感演出
        findViewById( R.id.returnBtn ).setOnTouchListener(new Library.ImageViewLowlighter());
        LinearLayout sideMenu = findViewById( R.id.sideMenu );
        for (int i = 0; i < sideMenu.getChildCount(); i++) {
            ImageButton sideMenuBtn = (ImageButton)sideMenu.getChildAt(i);
            sideMenuBtn.setOnTouchListener(new Library.ImageViewLowlighter());
        }

        //動画設定
        getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );        // スクリーンセーバをオフにする
        getWindow().setFormat( PixelFormat.TRANSPARENT );
        String movieFileName = "movie/" + sceneId + "_"  + mode + ".mp4";
        surfacePlayer = new SurfaceMediaPlayer(this, (SurfaceView)findViewById(R.id.surface), movieFileName,0);

        //音量シークバー設定
        setVolumeSeekBar();
    }

    //再生と一時停止
    public void startMovie(View view){
        if(surfacePlayer.isPlaying()){
            surfacePlayer.pause();
            startBtn.setImageResource( R.drawable.player_start );
        }else{
            surfacePlayer.start();
            startBtn.setImageResource( R.drawable.player_pause );
            restartBtn.setImageResource( R.drawable.player_restart );
            //再生完了後の処理を登録
            surfacePlayer.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    surfacePlayer.seekTo( 0 );
                    startBtn.setImageResource( R.drawable.player_start );
                    restartBtn.setImageResource( R.drawable.player_restart_disactive );
                }
            });
        }
    }

    //頭出し
    public void restartMovie(View view){
        if(surfacePlayer.isPlaying()){
            surfacePlayer.seekTo(0);
            restartBtn.setImageResource( R.drawable.player_restart );
        }else{
            surfacePlayer.seekTo(0);
            restartBtn.setImageResource( R.drawable.player_restart_disactive );
        }
    }

    //モード切替ボタン押下
    public void changeMode(View view) {
        String anotherMode = mode.equals("cut_script") ?  "all" :  "cut_script";

        //アクティビティ再起動
        Intent intent = getIntent();
        intent.putExtra( "sceneId", sceneId );
        intent.putExtra( "mode", anotherMode );

        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();

        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    //音量シークバー設定
    public SeekBar volumeBar;
    private AudioManager audioManager;
    private void setVolumeSeekBar(){
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        volumeBar = findViewById(R.id.volumeBar); //着信音量シークバー
        audioManager = (AudioManager)getSystemService(getApplication().AUDIO_SERVICE);
        volumeBar.setMax( audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) );
        volumeBar.setProgress( audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) );
        volumeBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0); //着信音量設定
                    }
                    public void onStartTrackingTouch(SeekBar seekBar) { }
                    public void onStopTrackingTouch(SeekBar seekBar) { }
                }
        );
        //androidからの変更を監視
        BroadcastReceiver receiver = new VolumeChangeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.media.VOLUME_CHANGED_ACTION");
        registerReceiver(receiver, intentFilter);
    }
    class VolumeChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            ((AnimeActivity) context).volumeBar.setProgress( am.getStreamVolume( AudioManager.STREAM_MUSIC ) );
        }
    }

    //モードセレクトに戻る
    private void returnActivity(){
        Intent intent = new Intent(getApplication(), ModeSelectActivity.class); //画面遷移
        intent.putExtra( "sceneId", sceneId );
        startActivity(intent);
        finish();
    }
    //画像ボタン押下
    public void onClickReturnBtn(View view){
        returnActivity();
    }
    //ハードキー押下
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode != KeyEvent.KEYCODE_BACK) return false;
        returnActivity();
        return true;
    }


}

