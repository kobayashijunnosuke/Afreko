package jica.afureko;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;


public class SceneSelectActivity extends Activity {
    private Library Lib;
    private MediaPlayer mediaPlayer = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_menu );
        Lib = new Library( this );
        setMenuBar();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        int height = findViewById(R.id.ListRight).getHeight();

    }

    //バナー設置
    private void setMenuBar(){
        //assetsフォルダ直下にあるフォルダ名scene[0-9]を抽出
        List<String> sceneList = Lib.getAssetsDirectoryList("", "^scene[0-9]+$");
        LinearLayout listLeft =  findViewById( R.id.ListLeft);  //子レイアウト左列
        LinearLayout listRight =  findViewById( R.id.ListRight); //子レイアウト右列
        //右列を半分ずらす
        ImageView viewSpace = new ImageView(this);
        viewSpace.setLayoutParams( new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT) );
        viewSpace.setScaleType( ImageView.ScaleType.FIT_CENTER );
        viewSpace.setAdjustViewBounds( true );
        viewSpace.setImageResource( R.drawable.view_space );
        listRight.addView( viewSpace );

        //フォルダの数だけバナー設置
        Boolean odd = true;
        for(final String sceneId: sceneList){
            //バナー設定
            ImageView banner = new ImageView(this);
            banner.setImageBitmap( Lib.getAssetsBitmap(sceneId + "/banner.png") );           //画像ファイル指定
            //サイズ調整
            banner.setLayoutParams( new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT) );
            banner.setPadding( Lib.dpToPx( 15 ),Lib.dpToPx( 0 ),Lib.dpToPx( 15 ),Lib.dpToPx( 30 ) );
            banner.setScaleType( ImageView.ScaleType.FIT_CENTER );
            banner.setAdjustViewBounds( true );

            //タップ時の動作
            banner.setOnTouchListener(new Library.ImageViewLowlighter());   //押下感演出
            banner.setOnClickListener(new View.OnClickListener() {public void onClick(View view) {
                moveToMenu(sceneId);
                try {
                    int id = getResources().getIdentifier("unite_" + sceneId, "raw", getPackageName());
                    mediaPlayer = Lib.setMediaplayer(id);
                    mediaPlayer.start();
                }catch(IllegalArgumentException e) { }

            }});

            //子レイアウト
            if(odd){
                listLeft.addView( banner );
            } else {
                listRight.addView( banner );
            }
            odd = !odd;
        }

    }

    //画面遷移
    private void moveToMenu(String sceneId) {
        Intent intent = new Intent(getApplication(), ModeSelectActivity.class); //画面遷移
        intent.putExtra("sceneId", sceneId);
        startActivity(intent);
    }

    //戻るボタン押下時は、タイトル画面に遷移
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode != KeyEvent.KEYCODE_BACK) {
            return false;
        }
        Intent intent = new Intent(getApplication(), TitleActivity.class); //画面遷移
        startActivity(intent);
        finish();
        return true;
    }
}
