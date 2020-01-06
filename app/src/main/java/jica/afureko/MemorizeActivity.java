package jica.afureko;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemorizeActivity extends Activity {
    private Library Lib;
    private String sceneId;
    private String mode;
    Point displayPoint = new Point();;
    private List<String> wordList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_memorize );
        //画面サイズの取得
        Display display =  this.getWindowManager().getDefaultDisplay();
        display.getSize(displayPoint);

        //変数設定
        Lib = new Library(this);
        sceneId = getIntent().getStringExtra("sceneId");
        mode = getIntent().getStringExtra( "mode" );

        //サムネイル画像
        ImageView sceneThumbnail = findViewById(R.id.sceneThumbnail);
        sceneThumbnail.setImageBitmap( Lib.getAssetsBitmap(sceneId + "/thumbnail.png") );
        //モード表示
        ImageView modeThumbnail = findViewById(R.id.modethumbnail);
        modeThumbnail.setImageBitmap( Lib.getAssetsBitmap( sceneId + "/vocabulary/" + mode + "_thumbnail.png" ) );
        //押下感演出
        findViewById( R.id.returnBtn ).setOnTouchListener(new Library.ImageViewLowlighter());

        //単語リスト読み込み
        List<String> tempList = Lib.getAssetsDirectoryList(sceneId + "/vocabulary/" + mode, ".*\\.png");
        for(String temp: tempList) {
            wordList.add(temp.replace( ".png","" ));
        }
        Collections.shuffle(wordList);
        //レイアウトセット
        setVolumeSeekBar();
        setImages();
    }

    private void setImages(){
        //親レイアウト
        LinearLayout parentLayout = findViewById( R.id.parentLayout );

        //子レイアウトの設定
        LinearLayout.LayoutParams childParams = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT );
        childParams.setMargins(40,40,40,20);

        for(int i = 0 ; i < wordList.size(); i++) {
            //画像の設定
            ImageView image = new ImageView(this);
            image.setLayoutParams( new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT,0,6 ) );
            image.setImageBitmap( Lib.getAssetsBitmap(sceneId + "/vocabulary/" + mode + "/" +  wordList.get(i) + ".png") );
            image.setScaleType( ImageView.ScaleType.FIT_CENTER );     //拡大して中央寄せ
            image.setAdjustViewBounds( true );                         //隙間の排除
            image.setBackgroundResource(R.drawable.style_border);       //枠線
            image.setPadding( Lib.dpToPx( 5 ),Lib.dpToPx( 5 ),Lib.dpToPx( 5 ),Lib.dpToPx( 5 ) );
            image.setId(Integer.parseInt("image",36) + i);    //idをセット
            image.setTag( i );                         //タグをセット
            image.setOnClickListener( new View.OnClickListener() {     //クリック時の動作
                public void onClick(final View view) {
                    int imageId = (int)view.getTag();
                    soundPlay(imageId, view);
                }
            } );
            image.setOnTouchListener(new Library.ImageViewLowlighter()); //押下感

            //テキストの設定
            TextView text = new TextView( this );
            text.setLayoutParams( new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, 0,1 ) );
            text.setId(Integer.parseInt("text",36) + i);
            text.setText( wordList.get(i) );
            text.setGravity( Gravity.CENTER );
            text.setPadding( 10,0,10,0 );
            text.setBackgroundResource(R.drawable.style_border);       //枠線
            text.setTextSize( TypedValue.COMPLEX_UNIT_PX, displayPoint.y / 16);
            text.setVisibility( View.INVISIBLE );

            //子レイアウトの設定
            LinearLayout childLayout = new LinearLayout(this);
            childLayout.setLayoutParams(childParams);
            childLayout.setOrientation( LinearLayout.VERTICAL );

            //追加
            childLayout.addView( image );
            childLayout.addView( text );
            parentLayout.addView( childLayout );
        }

    }


    ////////////////////////音量シークバー設定//////////////////////////
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
            ((MemorizeActivity) context).volumeBar.setProgress( am.getStreamVolume( AudioManager.STREAM_MUSIC ) );
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
        for(int i = 0 ; i < wordList.size(); i++) {
            TextView script = findViewById(Integer.parseInt("text",36) + i );
            script.setVisibility( setScriptVisibility );
        }
    }
    ////////////////////////音楽再生//////////////////////////
    private MediaPlayer mediaPlayer = null;
    private void soundPlay(int imageId, View view ){
        String filename = wordList.get( imageId );
        if(mediaPlayer != null && mediaPlayer.isPlaying())return;   //連打防止

        //スクロール
        HorizontalScrollView scrollView = findViewById( R.id.scrollView );
        LinearLayout parentLayout = findViewById( R.id.parentLayout );
        int scrolleX = (imageId * (int)(parentLayout.getWidth() / wordList.size())) - (int)(scrollView.getWidth()) / 2 + (int)(view.getWidth() / 2);
        scrollView.smoothScrollTo( scrolleX,0);

        mediaPlayer = new MediaPlayer();
        try {
            AssetFileDescriptor afd = getAssets().openFd(sceneId + "/vocabulary/" + mode + "/" + filename + ".mp3");
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mediaPlayer.prepare();

        } catch (IOException e) {
            e.printStackTrace();
        }
        //タッチした画像以外は暗転
        for(int i = 0 ; i < wordList.size(); i++) {
            if( wordList.get(i) == filename )continue;
            ImageView image = findViewById(Integer.parseInt("image",36) + i );
            image.setColorFilter( Color.argb(100, 0, 0, 0));
        }
        //再生終了後は明転
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.release();
                mediaPlayer = null;
                for(int i = 0 ; i < wordList.size(); i++) {
                    ImageView image = findViewById(Integer.parseInt("image",36) + i );
                    image.setColorFilter(null);
                }
            }
        });
        mediaPlayer.start();
    }

    ///////////////////////ハードキー押下時////////////////////////////
    //モードセレクトに戻る
    private void returnActivity(){
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
        if (keyCode != KeyEvent.KEYCODE_BACK) return false;
        returnActivity();
        return true;
    }

}
