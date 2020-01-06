package jica.afureko;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.AppLaunchChecker;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {
    private final int REQUEST_CODE = 100;                 //コールバック判別用コード
    private boolean checkPermissionStorage = false;     //承認チェック（書き込み権限）
    private boolean checkPermissionRecord = false;      //承認チェック （録音権限）

    //権限リスト
    public String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        //androidのversionがMARSHMALLOW以下は権限承認は不要。全て許可済み。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //承認ダイアログ表示
            ActivityCompat.requestPermissions( this, permissions, REQUEST_CODE );
        } else {
            openTitleActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (ContextCompat.checkSelfPermission( MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED) {
            // 未承認時の処理
            checkPermissionStorage = false;
        } else {
            //承認時の処理
            checkPermissionStorage = true;
        }

        if (ContextCompat.checkSelfPermission( MainActivity.this, Manifest.permission.RECORD_AUDIO ) != PackageManager.PERMISSION_GRANTED) {
            // 未承認時の処理
            checkPermissionRecord = false;
        } else {
            //承認時の処理
            checkPermissionRecord = true;
        }

        //承認状態の確認
        if (checkPermissionStorage && checkPermissionRecord) {
            openTitleActivity();    //タイトル画面遷移
        } else {
            //警告表示
            LinearLayout linearLayout = findViewById( R.id.attention );
            linearLayout.setVisibility( LinearLayout.VISIBLE );
        }
    }

    //タイトル画面遷移
    private void openTitleActivity(){
        Intent intent = new Intent( getApplication(), TitleActivity.class );
        startActivity( intent );
        finish();
    }

    //アプリの設定を開く
    public void openSetting(View view){
        String uriString = "package:" + getPackageName();
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse(uriString));
        startActivity(intent);
        finish();
    }

    //リスタート
    public void onClickRestart(View view){
        restart();
    }
    private void restart(){
        Intent intent = new Intent( getApplication(), MainActivity.class );
        finish();
        startActivity( intent );
    }

    //戻るボタン押下時は、終了確認ダイアログ表示
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode != KeyEvent.KEYCODE_BACK) {
            return false;
        }
        moveTaskToBack (true);
        return true;
    }
}