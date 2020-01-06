package jica.afureko;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

class MenuDialog extends Dialog {
    private Library Lib;
    private Context context;
    public String sceneId;
    public String activity;
    private Activity ownerActivity;

    public MenuDialog(Context context, String sceneId, String activity) {
        super((Context) context);
        this.context = context;
        Lib = new Library(context);
        this.sceneId = sceneId;
        this.activity = activity;

        this.setOwnerActivity((Activity)context);
        this.ownerActivity = getOwnerActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //画面サイズの取得
        Display display =  ownerActivity.getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);

        //ダイアログの設定
        requestWindowFeature(Window.FEATURE_NO_TITLE);  // タイトルなし
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));    //背景透過
        setContentView(R.layout.dialog_menu);
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        lp.width = (int)(point.x * 0.9);
        this.getWindow().setAttributes(lp);

        String[] modeList;
        // 利用するレイアウト
        switch (activity){
            case "AnimeActivity":
                modeList = new String[]{"all", "cut_script"};
                setIcons(modeList);
                break;
            case "StudioActivity":
                modeList = new String[]{"cut_a","cut_b", "cut_voice"};
                setIcons(modeList);
                break;
            case "VocabularyActivity":
            case "MemorizeActivity":
                setIconsVocabulary();
                break;
        }

        // キャンセルボタン動作
        ImageView closeBtn = findViewById(R.id.closeBtn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });
        closeBtn.setOnTouchListener(new Library.ImageViewLowlighter());   //押下感演出
        closeBtn.setLayoutParams( new LinearLayout.LayoutParams( (Integer)(point.y / 8),  ViewGroup.LayoutParams.WRAP_CONTENT ));   //サイズ調整
    }


    private void setIcons(String[] modeList){
        LinearLayout linearLayout = findViewById( R.id.icons );
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins( Lib.dpToPx(10),0,Lib.dpToPx(10),0 );
        layoutParams.weight = 1;                                 //幅を等間隔にする
        for(String mode: modeList) {
            ImageView icon = new ImageView( getContext() );
            //画像ファイル指定
            icon.setImageResource( getContext().getResources().getIdentifier( "select_mode_" + mode, "drawable", getContext().getPackageName() ) );
            icon.setScaleType( ImageView.ScaleType.FIT_CENTER );     //拡大して中央寄せ
            icon.setAdjustViewBounds( true );                         //隙間の排除
            //タップ時の動作
            icon.setTag( mode );
            icon.setOnClickListener( new View.OnClickListener() {
                public void onClick(View view) {
                try {
                    finishActivity();
                    Intent intent = new Intent( getContext(), Class.forName( getContext().getPackageName() + "." + activity) );
                    intent.putExtra( "sceneId", sceneId );
                    intent.putExtra( "mode", (String) view.getTag() );
                    getContext().startActivity( intent );
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                cancel();
            }
            } );
            icon.setOnTouchListener(new Library.ImageViewLowlighter());   //押下感演出
            //レイアウト適用
            icon.setLayoutParams( layoutParams );
            linearLayout.addView( icon );
        }
    }

    private void setIconsVocabulary() {
        //ファイルリストの取得
        List<String> modeList = Lib.getAssetsDirectoryList( sceneId + "/vocabulary", ".*\\.png" );
        //レイアウトの設定
        LinearLayout linearLayout = findViewById( R.id.icons );
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams( 0, LinearLayout.LayoutParams.WRAP_CONTENT );
        layoutParams.setMargins( Lib.dpToPx( 10 ), 0, Lib.dpToPx( 10 ), 0 );
        layoutParams.weight = 1;                                 //幅を等間隔にする
        //画像挿入
        for (final String modeFileName : modeList) {
            //画像ファイル指定
            ImageView icon = new ImageView( getContext() );
            icon.setImageBitmap( Lib.getAssetsBitmap( sceneId + "/vocabulary/" + modeFileName ) );
            icon.setScaleType( ImageView.ScaleType.FIT_CENTER );     //拡大して中央寄せ
            icon.setAdjustViewBounds( true );                         //隙間の排除
            //タップ時の動作
            icon.setTag( modeFileName.replace( "_thumbnail.png", "" ) );
            icon.setOnClickListener( new View.OnClickListener() {
                public void onClick(View view) {
                    finishActivity();
                    Intent intent = new Intent( getContext(), VocabularyActivity.class );
                    intent.putExtra( "sceneId", sceneId );
                    intent.putExtra( "mode", (String) view.getTag() );
                    intent.putExtra( "int", sceneId );
                    getContext().startActivity( intent );
                    cancel();
                }
            } );
            icon.setOnTouchListener(new Library.ImageViewLowlighter());   //押下感演出
            //レイアウト適用
            icon.setLayoutParams( layoutParams );
            linearLayout.addView( icon );
        }
    }
    //呼び出しアクティビティを終了させる
    private void finishActivity() {
        this.setOwnerActivity((Activity)context);
        getOwnerActivity().finish();
        this.dismiss();
    }
}
