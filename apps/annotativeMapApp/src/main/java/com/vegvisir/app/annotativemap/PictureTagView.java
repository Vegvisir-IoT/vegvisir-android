package com.vegvisir.app.annotativemap;

/**
 * Created by jiangyi on 11/07/2017.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PictureTagView extends RelativeLayout{
    private Context context;
    public TextView tvPictureTagLabel;
    private View loTag;
    private String time;
    public enum Status{Normal,Edit,Del}
    public enum Direction{Left,Right}
    private Direction direction = Direction.Left;
    private InputMethodManager imm;
    private static final int ViewWidth = 80;
    private static final int ViewHeight = 50;
    private int x = -1;
    private int y = -1;
    public String fullText;

    public PictureTagView(Context context,Direction direction) {
        super(context);
        this.context = context;
        this.direction = direction;
        initViews();
        init();
    }

    public PictureTagView(Context context,Direction direction, int x, int y) {
        super(context);
        this.context = context;
        this.direction = direction;
        this.x = x;
        this.y = y;
        initViews();
        init();
    }

    /** 初始化视图 **/
    protected void initViews(){
        LayoutInflater.from(context).inflate(R.layout.picturetagview, this,true);
        tvPictureTagLabel = (TextView) findViewById(R.id.tvPictureTagLabel);
        loTag = findViewById(R.id.loTag);
    }
    /** 初始化 **/
    protected void init(){
        imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
//        directionChange();
    }


    public void setAnnotation(String anno) {
        if (anno.length() > 10) {
            tvPictureTagLabel.setText(anno.substring(0,10)+"...");
        }
        else {
            tvPictureTagLabel.setText(anno);
        }
        fullText = anno;
    }


    public void setStatus(Status status){
        switch(status){
            case Normal:
                tvPictureTagLabel.setVisibility(View.VISIBLE);
//                tvPictureTagLabel.setText(etPictureTagLabel.getText());
                //隐藏键盘
//                imm.hideSoftInputFromWindow(etPictureTagLabel.getWindowToken() , 0);
//                tvPictureTagLabel.setVisibility(View.GONE);
//
//                Intent intent = new Intent();
//                intent.setClass(context,add_annotation.class);
//                intent.putExtra("CUR_ANNO",tvPictureTagLabel.getText());
//                // 把已经添加的tag发送给add_annotation
//                time = getTime();
//                intent.putExtra("TIME",time);
//                ((Activity)context).startActivityForResult(intent,0);

                break;
            case Edit:
                //tvPictureTagLabel.setVisibility(View.GONE);

                Intent intent = new Intent();
                intent.setClass(context,add_annotation.class);
                intent.putExtra("CUR_ANNO",fullText);
                // 把已经添加的tag发送给add_annotation
                time = getTime();
                intent.putExtra("TIME",time);
                ((Activity)context).startActivityForResult(intent,0);

                //弹出键盘
//                imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
                break;
            case Del:
                tvPictureTagLabel.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        View parent = (View) getParent();
        int halfParentW = (int) (parent.getWidth()*0.5);
        int center = (int) (l + (this.getWidth()*0.5));
//        if(center<=halfParentW){
//            direction = Direction.Left;
//        }
//        else{
//            direction = Direction.Right;
//        }
//        directionChange();
    }
    private void directionChange(){
        switch(direction){
            case Left:
//                loTag.setBackgroundResource(R.drawable.fire_left_small);
                loTag.setBackgroundResource(R.drawable.bg_picturetagview_tagview_left);
                break;
            case Right:
//                loTag.setBackgroundResource(R.drawable.fire_right_small);
                loTag.setBackgroundResource(R.drawable.bg_picturetagview_tagview_right);
                break;
        }
    }
    public static int getViewWidth(){
        return ViewWidth;
    }
    public static int getViewHeight(){
        return ViewHeight;
    }

    public String getTime() {
        long time = System.currentTimeMillis();
        SimpleDateFormat time_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d1 = new Date(time);
        String t1 = time_format.format(d1);
        return t1;
    }

    public int getXVal() {
        return this.x;
    }

    public int getYVal() {
        return this.y;
    }

    public void setXVal(int x) {
        this.x = x;
    }

    public void setYVal(int y) {
        this.y = y;
    }
}