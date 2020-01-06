package jica.afureko;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;


public class ModeSelectActivity extends Activity {
    private String sceneId;
    private Library Lib;
    private MenuDialog menuDialog;

    private MediaPlayer mpAnime;
    private MediaPlayer mpStudio;
    private MediaPlayer mpVocabulary;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_modeselect );
        Lib = new Library(this);
        sceneId = getIntent().getStringExtra( "sceneId" );  //引数受取

        //シーン画像表示
        ImageView imageView = findViewById(R.id.thumbnail);
        imageView.setImageBitmap( Lib.getAssetsBitmap(sceneId + "/thumbnail.png") );

        //音声準備
        mpAnime = Lib.setMediaplayer( R.raw.mode_anime );
        mpStudio = Lib.setMediaplayer( R.raw.mode_studio );
        mpVocabulary = Lib.setMediaplayer( R.raw.mode_vocabulary );

        //押下感演出
        findViewById( R.id.returnBtn ).setOnTouchListener(new Library.ImageViewLowlighter());
        LinearLayout mainContents = findViewById( R.id.mainContents );
        for (int i = 0; i < mainContents.getChildCount(); i++) {
            mainContents.getChildAt(i).setOnTouchListener(new Library.ImageViewLowlighter());
        }
    }

    //アニメアクティビティに遷移
    public  void startAnimeActivity(View view){
        mpAnime.start();

        Intent intent = new Intent(getApplication(), AnimeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra( "sceneId", sceneId );
        intent.putExtra( "mode", "cut_script");
        intent.putExtra( "seek", 0 );
        getApplication().startActivity( intent );
    }

    //ボキャブラリー（モードセレクト）に遷移
    public  void startVocaburaryModeSelectActivity(View view){
        mpVocabulary.start();

        Intent intent = new Intent(getApplication(), VocabularyModeSelectActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra( "sceneId", sceneId );
        intent.putExtra( "mode", "cut_script");
        intent.putExtra( "seek", 0 );
        getApplication().startActivity( intent );
        finish();
    }

    //モード選択ダイアログ表示
    public void showDialog(View view){
        mpStudio.start();

        if( menuDialog != null && menuDialog.isShowing() ) return;  //重複表示を禁止
        menuDialog = new MenuDialog(this, sceneId, (String) view.getTag());
        menuDialog.show();
    }

    //シーンセレクトに戻る
    public void returnMenu(View view){
        Intent intent = new Intent(getApplication(), SceneSelectActivity.class); //シーン選択画面に遷移
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        //戻るキー押下時
        if (keyCode != KeyEvent.KEYCODE_BACK) {
            return false;
        }
        Intent intent = new Intent(getApplication(), SceneSelectActivity.class); //シーン選択画面に遷移
        startActivity(intent);
        finish();
        return true;
    }
}
