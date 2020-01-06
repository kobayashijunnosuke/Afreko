package jica.afureko;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

public class VocabularyModeSelectActivity extends Activity {
    private String sceneId;
    private Library Lib;

    private MediaPlayer mpFormation;
    private MediaPlayer mpExament;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_vocabulary_mode_select );
        Lib = new Library(this);
        sceneId = getIntent().getStringExtra( "sceneId" );  //引数受取

        //シーン画像表示
        ImageView imageView = findViewById(R.id.thumbnail);
        imageView.setImageBitmap( Lib.getAssetsBitmap(sceneId + "/thumbnail.png") );

        //モード画像表示
        String[] modeList =  {"a", "b", "c"};
        for(String mode:modeList){
            ImageView modeImageView = findViewById(getResources().getIdentifier( "mode_" + mode,"id", getPackageName()));
            modeImageView.setImageBitmap( Lib.getAssetsBitmap(sceneId + "/vocabulary/" + mode + "_thumbnail.png") );
        }

        //音声準備
        mpFormation = Lib.setMediaplayer( R.raw.formation );
        mpExament = Lib.setMediaplayer( R.raw.exament );

        //押下感演出
        findViewById( R.id.returnBtn ).setOnTouchListener(new Library.ImageViewLowlighter());
        for(String mode:modeList){
            LinearLayout modeGroup =  findViewById(getResources().getIdentifier( "group_" + mode,"id", getPackageName()));
            modeGroup.getChildAt(1).setOnTouchListener(new Library.ImageViewLowlighter());
            modeGroup.getChildAt(2).setOnTouchListener(new Library.ImageViewLowlighter());
        }
    }

    //練習画面に遷移
    public void startFormation(View view){
        mpFormation.start();

        Intent intent = new Intent(getApplication(), MemorizeActivity.class); //シーン選択画面に遷移
        intent.putExtra( "sceneId", sceneId );
        intent.putExtra( "mode", (String) ((LinearLayout) view.getParent()).getTag() );
        startActivity(intent);
        finish();
    }

    //テスト画面に遷移
    public void startExament(View view){
        mpExament.start();

        Intent intent = new Intent(getApplication(), VocabularyActivity.class); //シーン選択画面に遷移
        intent.putExtra( "sceneId", sceneId );
        intent.putExtra( "mode", (String) ((LinearLayout) view.getParent()).getTag() );
        startActivity(intent);
        finish();
    }

    //モードセレクトに戻る
    public void returnMenu(View view){
        Intent intent = new Intent(getApplication(), ModeSelectActivity.class); //シーン選択画面に遷移
        intent.putExtra( "sceneId", sceneId );
        startActivity(intent);
        finish();
    }
    //ハードキー押下
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode != KeyEvent.KEYCODE_BACK) return false;
        Intent intent = new Intent(getApplication(), ModeSelectActivity.class); //シーン選択画面に遷移
        intent.putExtra( "sceneId", sceneId );
        startActivity(intent);
        finish();
        return true;
    }
}
