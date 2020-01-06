package jica.afureko;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.KeyEvent;
import android.view.View;

public class Credit extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_credit );
    }
    //画面遷移
    public void onReturnButton(View view) {
        Intent intent = new Intent(getApplication(), TitleActivity.class); //画面遷移
        startActivity(intent);
        finish();
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
