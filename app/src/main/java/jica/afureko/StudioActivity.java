package jica.afureko;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

public class StudioActivity extends Activity {
    private Library Lib;
    private String sceneId;
    private String mode;
    private String filePath;
    private String moviePath;
    //プレイヤー
    private SurfaceMediaPlayer moviePlayer = null;
    private MediaPlayer soundPlayer = null;
    private MediaRecorder soundRecorder = null;
    //各種ボタン
    private ImageButton recordBtn;
    private ImageButton startBtn;
    private ImageButton restartBtn;
    private ImageButton modeThumbnail;
    //ダイアログ
    private MenuDialog menuDialog;
    private CountDownDialog countDownDialog;
    private CountDownTimer countDownTimer = null;
    //その他
    private boolean soundFinish = false;
    private AlphaAnimation blink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_studio);
        Lib = new Library(this);
        //引数受取
        sceneId = getIntent().getStringExtra( "sceneId" );
        mode = getIntent().getStringExtra( "mode" );
        filePath = String.valueOf( getExternalFilesDir( null )) + "/"+sceneId + mode + ".wave"; //保存ファイル名
        moviePath = "movie/" + sceneId + "_" + mode + ".mp4";

        //各種ボタン
        recordBtn = findViewById(R.id.recordBtn);
        startBtn = findViewById( R.id.startBtn );
        restartBtn =  findViewById( R.id.restartBtn );
        modeThumbnail = findViewById(R.id.modethumbnail);

        // スクリーンセーバをオフにする
        getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
        getWindow().setFormat( PixelFormat.TRANSPARENT );

        //サムネイル画像表示
        ImageView imageView = findViewById(R.id.thumbnail);
        imageView.setImageBitmap( Lib.getAssetsBitmap(sceneId + "/thumbnail.png") );

        //モード表示
        modeThumbnail.setImageResource(getResources().getIdentifier("select_mode_" + mode, "drawable", getPackageName()));

        //押下感演出
        findViewById( R.id.returnBtn ).setOnTouchListener(new Library.ImageViewLowlighter());
        findViewById( R.id.startBtn ).setOnTouchListener(new Library.ImageViewLowlighter());
        findViewById( R.id.restartBtn ).setOnTouchListener(new Library.ImageViewLowlighter());

        //音量調整
        setVolumeSeekBar();

        //音声・動画再生準備
        prepareSoundPlayer();
        prepareMoviePlayer();
    }

    /////////////////////
    //ボタン押下関連
    ////////////////////
    //録画ボタン押下
    public void onClickRecordBtn(View view){
        if (soundRecorder == null)
        {//録音開始
            showCountDownDialog(3000, 100);
        }else
        {//録音停止
            recordStop();
        }
    }

    //再生ボタン押下
    public void onClickStartBtn(View view){
        if(soundRecorder != null) return;   //録音中は操作不可
        if (soundPlayer == null) return;    //音声無しは操作不可
        //再生開始
        if (!moviePlayer.isPlaying()) {
            startBtn.setImageResource( R.drawable.player_pause );
            restartBtn.setImageResource( R.drawable.player_restart );
            if(!soundFinish) soundStart();
            movieStart();
        }else{//一時停止
            moviePause();
        }
    }

    //頭出しボタン押下
    public void onClickRestartBtn(View view){
        if(soundRecorder != null) return;   //録音中は操作不可
        if (soundPlayer == null) return;    //音声無しは操作不可
        if (moviePlayer.isPlaying())
        {
            movieRestart();
            soundStart();
        }else{
            movieRestart();
        }
    }

    //連打防止
    private void barrageGuard(final View view){
        view.setEnabled(false);
        new Handler().postDelayed( new Runnable() {
            public void run() {
                view.setEnabled(true);
            }
        }, 1000L);
    }

    /////////////////////
    //メディア準備
    ////////////////////
    //動画再生準備
    private void prepareMoviePlayer(){
        moviePlayer = new SurfaceMediaPlayer(this, (SurfaceView)findViewById(R.id.surface), moviePath, 0);
    }

    //音声再生準備
    private void prepareSoundPlayer(){
        File soundFile = new File(filePath);
        if(!soundFile.exists()){
            startBtn.setImageResource( R.drawable.player_start_disactive );
            return;
        }
        startBtn.setImageResource( R.drawable.player_start );
        try {
            soundPlayer = new MediaPlayer();
            soundPlayer.setDataSource(filePath);
            soundPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //レコーダー準備
    private void prepareSoundRecorder(){
        soundRecorder = new MediaRecorder();
        soundRecorder.setOutputFile(filePath);  //ファイルの保存先を指定
        try {
            soundRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);        //マイクからの音声を録音する
            soundRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);  //ファイルへの出力フォーマット
            soundRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);  //音声のエンコーダーも合わせてdefaultにする
            soundRecorder.prepare();
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    /////////////////////
    //録音関連
    ////////////////////
    //録音開始カウントダウン
    private void showCountDownDialog(long millisInFuture, long countDownInterval){
        //再生中の場合は停止する
        if(moviePlayer.isPlaying()) moviePause();
        //ダイアログ表示
        if( countDownDialog != null && countDownDialog.isShowing() ) return;  //重複表示を禁止
        countDownDialog = new CountDownDialog(this);
        countDownDialog.setCancelable(false);
        countDownDialog.show();
        //カウントダウン
        if(countDownTimer != null) countDownTimer.cancel();
        countDownTimer = new CountDownTimer( 3000, 100 ) {
            String[] countdownText = {"","①","②","③"};
            @Override
            public void onTick(long millisUntilFinished) {
                int time = (int) (millisUntilFinished + 1000) / 1000;
                countDownDialog.changeText( countdownText[time] );
            }
            @Override
            public void onFinish() {
                if(countDownDialog.isShowing()) {
                    barrageGuard(recordBtn);    //連打防止
                    recordStart();   //録音開始
                    countDownDialog.dismiss();
                }
            }
        }.start();
    }

    //録音開始
    private void recordStart(){
        //ファイルがある場合は削除
        File soundFile = new File(filePath);
        if(soundFile.exists()) {
            soundFile.delete();
        }
        movieRestart();//頭出し
        recordBtn.setImageResource( R.drawable.player_record_disactive );
        startBtn.setImageResource( R.drawable.player_start_disactive );
        modeThumbnail.setColorFilter( Color.argb(100, 0, 0, 0));

        prepareSoundRecorder();    //レコーダー準備
        soundRecorder.start();   //録音開始
        moviePlayer.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer surfaceMediaPlayer) {
                if(soundRecorder != null) {
                    recordStop();
                }
            }
        });
        movieStart(); //動画再生開始
        startBlink(findViewById( R.id.recording ));        //録音中表示
    }
    //録音停止
    private void recordStop(){
        if (soundRecorder == null)return;
        soundRecorder.stop();
        soundRecorder.reset();
        soundRecorder.release();
        soundRecorder = null;
        recordBtn.setImageResource( R.drawable.player_record );
        modeThumbnail.setColorFilter( Color.argb(0, 0, 0, 0));
        movieStop();
        prepareSoundPlayer();
        stoptBlink(findViewById( R.id.recording ));        //録音中非表示
    }
    //点滅アニメ（録音中表示）
    private void startBlink(View view){
        findViewById( R.id.recording ).setVisibility( View.VISIBLE );
        blink = new AlphaAnimation(1.2f, 0.2f);      //フェード幅
        blink.setDuration(1500);                     //1秒でフェード
        blink.setRepeatCount( Animation.INFINITE ); //無限繰り返し
        blink.setRepeatMode( Animation.REVERSE );   //フェード反転
        view.startAnimation(blink);                  //アニメスタート
    }
    //点滅アニメ終了（非表示）
    public void stoptBlink(View view) {
        view.clearAnimation();
        view.setVisibility( View.INVISIBLE );
    }
    /////////////////////
    //再生関連
    ////////////////////
    //音声再生
    private void soundStart(){
        soundPlayer.start();
        //再生完了後の処理を登録
        soundPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer surfaceMediaPlayer) {
                soundFinish = true;
            }
        });
    }
    //動画再生
    private void movieStart(){
        moviePlayer.start();
        //再生完了後の処理を登録
        moviePlayer.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer surfaceMediaPlayer) {
                movieStop();
                if(soundRecorder != null) {
                    recordStop();
                }
                prepareSoundPlayer();   //音声プレイヤー読み込み直し
                soundFinish = false;
            }
        });
    }
    //一時停止
    private void moviePause(){
        if(soundPlayer != null) soundPlayer.pause();
        if (moviePlayer.isPlaying()){
            moviePlayer.pause();
            startBtn.setImageResource( R.drawable.player_start );
        }

    }
    //頭出し
    private void movieRestart(){
        moviePlayer.seekTo(0);
        if(soundPlayer != null) soundPlayer.seekTo( 0 );
        soundFinish = false;
        if (moviePlayer.isPlaying()){
            restartBtn.setImageResource( R.drawable.player_restart );
        }else{
            restartBtn.setImageResource( R.drawable.player_restart_disactive );
        }
    }

    //停止
    private void movieStop(){
        moviePause();
        movieRestart();
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
    /////////////////////
    //ハードキー押下時
    ////////////////////
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode != KeyEvent.KEYCODE_BACK)  return false;
        returnActivity();
        return true;
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (soundRecorder != null)recordStop();
        movieStop();
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
            ((StudioActivity) context).volumeBar.setProgress( am.getStreamVolume( AudioManager.STREAM_MUSIC ) );
        }
    }

    //////////////
    //モード切替ダイアログ
    ////////////
    public void showMenuDialog(View view){
        if(soundRecorder != null) return;   //録音中は操作不可
        if( menuDialog != null && menuDialog.isShowing() ) return;  //重複表示を禁止
        menuDialog = new MenuDialog(this, sceneId, getClass().getSimpleName());
        menuDialog.show();
    }
}

//////////////
//カウントダウンダイアログ
////////////
class CountDownDialog extends Dialog {
    public CountDownDialog(Context context) {
        super(context);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //ダイアログの設定
        requestWindowFeature( Window.FEATURE_NO_TITLE);  // タイトルなし
        setContentView(R.layout.dialog_countdown);           // 利用するレイアウト
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));    //背景透過
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN); //フルスクリーン
        // キャンセルボタン動作
        findViewById(R.id.buttonClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dismiss();
            }
        });
    }
    public void changeText(String countDowmText){
        ((TextView)findViewById(R.id.countdowntext)).setText(countDowmText);
    }
}

