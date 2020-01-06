package jica.afureko;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.Random;


public class TitleActivity extends Activity {

    public AlphaAnimation blink;

    //音

    private MediaPlayer mpBienvenue = new MediaPlayer();
    private MediaPlayer mpTitle = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title);

        //ボタン点滅アニメ開始
        startBlink(findViewById(R.id.welcome_btn));

        //エントリーボタン音声再生準備
        setMediaPlayer();

        //タイトルロゴ関連
        setTitleLogo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //背景
        ImageView background = findViewById( R.id.background );
        String filename = "background_title_" + (int)((Math.random() * 4) + 1);
        background.setImageResource( getResources().getIdentifier(filename, "drawable", getPackageName()) );
    }

    //ボタン点滅アニメ
    private ImageView startBlink(View view){
        blink = new AlphaAnimation(1.1f, 0.3f);      //フェード幅　※全灯時間を長くするため1.3fを使用
        blink.setDuration(1000);                     //1秒でフェード
        blink.setRepeatCount( Animation.INFINITE ); //無限繰り返し
        blink.setRepeatMode( Animation.REVERSE );   //フェード反転
        view.startAnimation(blink);                  //アニメスタート
        return null;
    }

    //音声準備
    private void setMediaPlayer( ) {
        //スタートボタン
        mpBienvenue = MediaPlayer.create(this, R.raw.bienvenue);
        mpBienvenue.setLooping(false);
        mpBienvenue.seekTo(0);
        mpBienvenue.setVolume(1f, 1f);
        mpBienvenue.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
            }
        });
    }

    //タイトルロゴ関連
    private ImageView titleLogo;
    private void setTitleLogo(){
        titleLogo = findViewById( R.id.titleLogo);

        //音声再生
        mpTitle = MediaPlayer.create(this, R.raw.title);
        mpTitle.setLooping(false);
        mpTitle.seekTo(0);
        mpTitle.setVolume(1f, 1f);
        mpTitle.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                titleLogo.setColorFilter(null);
            }
        });

        //タッチリスナー
        titleLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                titleLogo.setColorFilter( Color.YELLOW);
                mpTitle.start();
            }
        });
    }

    //メニューに画面遷移
    public void startMenuActivity(View view) {
        if(mpBienvenue.isPlaying()) mpBienvenue.stop();
        mpBienvenue.start();  //音再生

        view.clearAnimation();                     //アニメ停止しないと非表示出来ない
        view.setVisibility( View.INVISIBLE );    //ボタン非表示で押下感を演出
        Intent intent = new Intent(getApplication(), SceneSelectActivity.class); //画面遷移
        startActivity(intent);
        finish();
    }

    //クレジット画面に遷移
    public void  bonusCredit(View view){
        Intent intent = new Intent(getApplication(), Credit.class); //画面遷移
        startActivity(intent);
        finish();
    }

    //戻るボタン押下時は、終了確認ダイアログ表示
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode != KeyEvent.KEYCODE_BACK) {
            return false;
        }
        new AlertDialog.Builder(this).setMessage("Quitter l'application?")
                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        moveTaskToBack (true);
                    }
                })
                .setNegativeButton("Non", null)
                .show();
        return true;
    }
}
