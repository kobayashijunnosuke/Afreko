package jica.afureko;

import android.app.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class VocabularyActivity extends Activity {

    private String sceneId;
    private String mode;
    private Library Lib;
    private MenuDialog menuDialog;

    private List<String> wordList = new ArrayList<String>();            //単語リスト
    private List<String> questionWordList = new ArrayList<String>();        //問題単語リスト
    private List<String>  RandamWordList =  new ArrayList<String>();    //表示単語リスト
    private String targetWord;  //現在出題中の単語

    private Boolean touchFlag = false;  //タップ後の制御中はtrueにする
    private ImageView sound_btn;
    private ImageView correctMark;
    private ImageButton restartBtn;
    private LinearLayout counterLayout;

    //効果音
    private MediaPlayer mpEffectCorrect = new MediaPlayer();
    private MediaPlayer mpEffectWrong = new MediaPlayer();
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_vocabulary );
        Lib = new Library(this);
        //引数受取
        sceneId = getIntent().getStringExtra("sceneId");
        mode = getIntent().getStringExtra( "mode" );
        sound_btn = findViewById( R.id.sound_btn );
        correctMark = findViewById( R.id.correctMark );
        restartBtn = findViewById( R.id.restartBtn);
        counterLayout = findViewById( R.id.counter );

        //サムネイル画像
        ImageView sceneThumbnail = findViewById(R.id.sceneThumbnail);
        sceneThumbnail.setImageBitmap( Lib.getAssetsBitmap(sceneId + "/thumbnail.png") );
        //モード表示
        ImageView modeThumbnail = findViewById(R.id.modethumbnail);
        modeThumbnail.setImageBitmap( Lib.getAssetsBitmap( sceneId + "/vocabulary/" + mode + "_thumbnail.png" ) );
        //押下感演出
        findViewById( R.id.returnBtn ).setOnTouchListener(new Library.ImageViewLowlighter());
        LinearLayout sideMenu = findViewById( R.id.sideMenu );
        for (int i = 0; i < sideMenu.getChildCount(); i++) {
            ImageButton sideMenuBtn = (ImageButton)sideMenu.getChildAt(i);
            sideMenuBtn.setOnTouchListener(new Library.ImageViewLowlighter());
        }
        findViewById( R.id.startBtn ).setOnTouchListener(new Library.ImageViewLowlighter());
        //ボリュームバー
        setVolumeSeekBar();

        //効果音準備
        initEffectSound();

        //問題開始
        showCountDownDialog();

    }
    //////////音声再生////////////
    //効果音再生準備
    private void initEffectSound(){
        //正解
        mpEffectCorrect = MediaPlayer.create(this, R.raw.correct);
        mpEffectCorrect.setLooping(false);
        mpEffectCorrect.seekTo(0);
        mpEffectCorrect.setVolume(1f, 1f);
        mpEffectCorrect.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                setScreenIn();
                correctMark.setVisibility( View.INVISIBLE );
                setQuestion();
            }
        });
        //不正解
        mpEffectWrong = MediaPlayer.create(this, R.raw.incorrect);
        mpEffectWrong.setLooping(false);
        mpEffectWrong.seekTo(0);
        mpEffectWrong.setVolume(1f, 1f);
        mpEffectWrong.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                setScreenIn();
                correctMark.setVisibility( View.INVISIBLE );
                soundPlay(targetWord, null);
            }
        });
    }
    //音声再生（タップ誤動作）
    private void soundPlay(String filename, final Boolean correct) {
        if(mediaPlayer != null && mediaPlayer.isPlaying())return;
        if(touchFlag)return;

        //再生中はグレーアウト
        setScreenOut(filename, correct);

        if(Boolean.TRUE.equals(correct)){
            correctMark.setImageResource( R.drawable.mark_correct );
            correctMark.setVisibility( View.VISIBLE );
        }else if(Boolean.FALSE.equals(correct)){
            correctMark.setImageResource( R.drawable.mark_incorrect );
            correctMark.setVisibility( View.VISIBLE );
        }

        //音声登録
        mediaPlayer = new MediaPlayer();
        try {
            AssetFileDescriptor afd = getAssets().openFd(sceneId + "/vocabulary/" + mode + "/" + filename + ".mp3");
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //音声終了後
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.release();
                mediaPlayer = null;
                if(correct == null){
                    setScreenIn(); //画像戻す
                }else if(correct == true){
                    mpEffectCorrect.start();
                }else{
                    mpEffectWrong.start();
                }
            }
        });
        //再生
        mediaPlayer.start();
    }
    //再生ボタン押下時
    public void onClickSoundBtn(View view) {
        soundPlay(targetWord, null);
    }
    ///////出題関連/////////
    //初期化
     private void initQuestions(){
         findViewById( R.id.sideMenu).setVisibility( View.VISIBLE );    //サイドメニュー表示
         counterLayout.setVisibility( View.VISIBLE );
         findViewById( R.id.mainScreen).setVisibility( View.VISIBLE );  //メイン画面表示
         //単語リストをセット
         wordList.clear();
         questionWordList.clear();
         //新規リスト読み込み
         List<String> tempList = Lib.getAssetsDirectoryList(sceneId + "/vocabulary/" + mode, ".*\\.png");
         String value;
         for(String temp: tempList) {
             value = temp.replace( ".png","" );
             wordList.add(value);
             questionWordList.add(value);
         }
         //順番をランダムに混ぜる
         Collections.shuffle(questionWordList);
     }
    //出題する
    private void setQuestion() {
        //残り問題数をセット
        setRetQuestionMeter();

        //全問終了（残り問題が無い）の場合
        if(questionWordList.isEmpty()){
            setFinishScreen(); //終了画面へ遷移
            return;
        }
        //ランダムに1件を選出
        targetWord = questionWordList.get(0);
        questionWordList.remove( 0 );   //出題リストから削除

        //表示リストを作成
        RandamWordList.clear();
        RandamWordList.add(targetWord);

        //正解以外から５件ランダムに選出
        Collections.shuffle(wordList);
        for(int i = 0 ; i < wordList.size(); i++) {
            if(RandamWordList.size() >= 6) break;                   //６つ選出したら終わり
            if(targetWord.equals( wordList.get(i) ) ) continue;    //正解と同じ場合は飛ばす
            RandamWordList.add(wordList.get(i));
        }
        Collections.shuffle(RandamWordList);    //シャッフルする

        //画像セット
        for(int i = 0 ; i < RandamWordList.size(); i++) {
            String word = RandamWordList.get(i);
            ImageView image = findViewById( getResources().getIdentifier("image" + i, "id", getPackageName()) );
            image.setVisibility( View.VISIBLE );
            image.setImageBitmap( Lib.getAssetsBitmap(sceneId + "/vocabulary/" + mode + "/" + word + ".png") );
            image.setPadding( Lib.dpToPx( 5 ),Lib.dpToPx( 5 ),Lib.dpToPx( 5 ),Lib.dpToPx( 5 ) );
            image.setTag( word );
            //押下時の設定
            image.setOnTouchListener(new ImageViewLowlighter());
            image.setOnClickListener( new View.OnClickListener() {
                public void onClick(final View view) {
                    soundPlay( (String) view.getTag(), targetWord.equals(view.getTag()));
                }
            } );
//            //スクリプトセット
            TextView script = findViewById( getResources().getIdentifier("text" + i, "id", getPackageName()) );
            script.setText( RandamWordList.get(i) );
        }
        //出題音声再生
        soundPlay(targetWord, null);
    }
    //画像押下演出
    private class ImageViewLowlighter implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            //音声再生中は押下感演出しない
            if(touchFlag) return false;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    ((ImageView) v).setColorFilter(Color.argb(100, 0, 0, 0));
                    break;
                default:
                    ((ImageView) v).setColorFilter(null);
                    break;
            }
            return false;
        }
    }
    //巻き戻しボタン押下時
    public void onClickRestartBtn(View view) {
        if(touchFlag)return;
        new AlertDialog.Builder(this).setMessage("Retour au début?")
        .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showCountDownDialog();
            }
        })
        .setNegativeButton("Non", null)
        .show();
    }

    ///////画面表示設定/////////
    //スタート画面
    private void setStart(){
        findViewById( R.id.mainScreen).setVisibility( View.VISIBLE );      //メイン画面非表示
        findViewById( R.id.sideMenu).setVisibility( View.VISIBLE );        //サイドメニュー非表示
        findViewById( R.id.resultLayput).setVisibility( View.GONE );    //結果画面表示
        counterLayout.setVisibility( View.VISIBLE );
        initQuestions();
        setQuestion();
    }
    //終了画面
    private void setFinishScreen(){
        findViewById( R.id.mainScreen).setVisibility( View.GONE );    //メイン画面非表示
        findViewById( R.id.sideMenu).setVisibility( View.GONE );       //サイドメニュー非表示
        findViewById( R.id.resultLayput).setVisibility( View.VISIBLE );     //結果画面表示
        counterLayout.setVisibility( View.GONE );

    }
    //問題をグレーアウト
    private void setScreenOut(String selectWord,Boolean correct){
        touchFlag = true;
        sound_btn.setImageResource( R.drawable.btn_sound_disactive);
        restartBtn.setImageResource( R.drawable.return_top_disactive);
        for(int i = 0 ; i < RandamWordList.size(); i++) {
            if(correct != null && selectWord.equals( RandamWordList.get(i) )){
                continue;
            }
            ImageView image = findViewById( getResources().getIdentifier("image" + i, "id", getPackageName()) );
            image.setColorFilter(Color.argb(150, 0, 0, 0));
        }
    }
    //問題をグレーアウトから回復
    private void setScreenIn(){
        touchFlag = false;
        sound_btn.setImageResource( R.drawable.btn_sound);
        restartBtn.setImageResource( R.drawable.return_top);
        for(int i = 0 ; i < RandamWordList.size(); i++) {
            ImageView image = findViewById( getResources().getIdentifier("image" + i, "id", getPackageName()) );
            image.setColorFilter(null);
        }

    }
    //残り問題数セット
    private void setRetQuestionMeter(){
        int totalQuestionCount = wordList.size();                //総問題数
        int restQuestionCount = questionWordList.size();    //残り問題数
        //レイアウト準備
        counterLayout.removeAllViews();
        //画像ファイル準備
        Bitmap rest_question_on = BitmapFactory.decodeResource(getResources(), R.drawable.rest_question_on);
        Bitmap rest_question_off = BitmapFactory.decodeResource(getResources(), R.drawable.rest_question_off);
        //カウンターバーセット
        for(int i=0;i<totalQuestionCount;i++){
            ImageView image = new ImageView(this);
            image.setLayoutParams( new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT,0,1 ) );
            image.setScaleType( ImageView.ScaleType.FIT_CENTER );     //拡大して中央寄せ
            image.setAdjustViewBounds( true );                         //隙間の排除
            image.setPadding( 1,1,1,1 );
            if((totalQuestionCount - restQuestionCount) > i){
                image.setImageBitmap(rest_question_off);
            }else{
                image.setImageBitmap(rest_question_on);
            }
            counterLayout.addView( image );
        }
    }
    ////////////////////////スクリプト表示ボタン//////////////////////////
    private int scriptVisibility = View.INVISIBLE;
    public void onClickScriptBtn(View view){
        Integer setScriptVisibility;
        //表示する
        if(scriptVisibility == View.INVISIBLE){
            setScriptVisibility = scriptVisibility = View.VISIBLE;
            ((ImageView) view).setImageResource(R.drawable.btn_script);
        }
        //非表示する
        else {
            setScriptVisibility = scriptVisibility = View.INVISIBLE;
            ((ImageView) view).setImageResource(R.drawable.btn_script_disactive);
        }
        for(int i = 0 ; i < RandamWordList.size(); i++) {
            TextView script = findViewById( getResources().getIdentifier("text" + i, "id", getPackageName()) );
            script.setVisibility( setScriptVisibility );
        }
    }

    ////////////////////////音量シークバー設定
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
            ((VocabularyActivity) context).volumeBar.setProgress( am.getStreamVolume( AudioManager.STREAM_MUSIC ) );
        }
    }

    ///////////////////////カウントダウン////////////////////////////
    private CountDownDialogVocabulary countDownDialog;
    private CountDownTimer countDownTimer = null;
    private void showCountDownDialog(){
        findViewById( R.id.mainScreen).setVisibility( View.GONE );  //メイン画面表示
        findViewById( R.id.resultLayput).setVisibility( View.GONE );     //結果画面表示
        findViewById( R.id.sideMenu).setVisibility( View.GONE );        //サイドメニュー非表示
        findViewById( R.id.counter).setVisibility( View.GONE );         //カウンター表示
        //ダイアログ表示
        if( countDownDialog != null && countDownDialog.isShowing() ) return;  //重複表示を禁止
        countDownDialog = new CountDownDialogVocabulary(this);
        countDownDialog.setCancelable(false);
        countDownDialog.show();
        //カウントダウン
        if(countDownTimer != null) countDownTimer.cancel();
        countDownTimer = new CountDownTimer( 2000, 100 ) {
            String[] countdownText = {"","①","②"};
            @Override
            public void onTick(long millisUntilFinished) {
                int time = (int) (millisUntilFinished + 1000) / 1000;
                countDownDialog.changeText( countdownText[time] );
            }
            @Override
            public void onFinish() {
                if(countDownDialog.isShowing()) {
                    setStart();   //出題開始
                    countDownDialog.dismiss();
                }
            }
        }.start();
    }
    ///////////////////////画面遷移////////////////////////////
    //モードセレクトに戻る
    private void returnActivity(){
        if(mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if(mpEffectCorrect != null) {
            mpEffectCorrect.stop();
            mpEffectCorrect.release();
            mpEffectCorrect = null;
        }

        Intent intent = new Intent(getApplication(), VocabularyModeSelectActivity.class); //画面遷移
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
        if (keyCode != KeyEvent.KEYCODE_BACK)  return false;
        returnActivity();
        return true;
    }
}
//////////////
//カウントダウンダイアログ
////////////
class CountDownDialogVocabulary extends Dialog {
    public CountDownDialogVocabulary(Context context) {
        super(context);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //ダイアログの設定
        requestWindowFeature( Window.FEATURE_NO_TITLE);  // タイトルなし
        setContentView(R.layout.dialog_countdown_vocabulary);           // 利用するレイアウト
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));    //背景透過
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN); //フルスクリーン
    }
    public void changeText(String countDowmText){
        ((TextView)findViewById(R.id.countdowntext)).setText(countDowmText);
    }
}