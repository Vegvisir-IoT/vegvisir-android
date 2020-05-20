package com.vegvisir.app.annotativemap;

/**
 * Created by jiangyi on 11/07/2017.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Picture;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.vegvisir.app.annotativemap.PictureTagView.Direction;
import com.vegvisir.app.annotativemap.PictureTagView.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

@SuppressLint("NewApi")

public class PictureTagLayout extends RelativeLayout implements OnTouchListener {
    private static final int CLICKRANGE = 10;
    public int startX = 0;
    public int startY = 0;
    int startTouchViewLeft = 0;
    int startTouchViewTop = 0;
    private int cur_pic_num = 0;
    private String fileName = "/sdcard/info.txt";
    private JSONObject info;

    public View touchView,clickView;

    public PictureTagLayout(Context context) {
        super(context, null);
    }
    public PictureTagLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        this.setOnTouchListener(this);
    }


    public void setStatus(Status status, String anno) {
        if (status == Status.Normal) {
            if (clickView != null) {
                ((PictureTagView) clickView).setStatus(Status.Normal);
                ((PictureTagView) clickView).setAnnotation(anno);
                clickView = null;
            }
        }
        else if (status == Status.Del) {
            if (clickView != null) {
                View view = clickView;
                clickView = null;
                this.removeView(view);
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.i("Action","down");
                touchView = null;
                if(clickView!=null){
                    ((PictureTagView)clickView).setStatus(Status.Normal);
                    clickView = null;
                }
                startX = (int) event.getX();
                startY = (int) event.getY();
                if(hasView(startX,startY)){
                    startTouchViewLeft = touchView.getLeft();
                    startTouchViewTop = touchView.getTop();
                }
                else{
//                    hasView(startX, startY);
                    touchView = addItem(startX,startY);

                    Log.i("flag","before");
                    justHasView(-1,-1);
                    Log.i("flag","after");
//                    ((PictureTagView) touchView).setXVal(startX);
//                    ((PictureTagView) touchView).setYVal(startY);
                    justHasView(-1,-1);

                    ((PictureTagView) touchView).setStatus(Status.Edit);
                    clickView = touchView;
                    touchView = null;

//                    Log.i("Before adding item",MainActivity.annotations.toString());
//                    MainActivity.annotations.put(new Coordinates(startX,startY),"");
//                    Log.i("After adding item",MainActivity.annotations.toString());

                }
                break;
            case MotionEvent.ACTION_MOVE:
//                Log.i("Action","move");
//                moveView((int) event.getX(),
//                        (int) event.getY());
                break;
            case MotionEvent.ACTION_UP:
                Log.i("Action","up");
                int endX = (int) event.getX();
                int endY = (int) event.getY();
                //如果挪动的范围很小，则判定为单击
                if(touchView!=null && Math.abs(endX - startX)<CLICKRANGE && Math.abs(endY - startY)<CLICKRANGE){
                    //当前点击的view进入编辑状态
                    ((PictureTagView)touchView).setStatus(Status.Edit);
                    clickView = touchView;
                }
                touchView = null;
                break;
        }
        return true;
    }


    public View addItem(int x, int y){
        Log.i("add","item");
        Log.i("additemx",""+x);
        Log.i("additemy",""+y);
        int getWidth, getHeight;
        if (getWidth()==0) getWidth = 764;
        else getWidth=getWidth();
        if (getHeight()==0) getHeight = 1018;
        else getHeight=getHeight();

        View view = null;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//        System.out.println("width "+getWidth);
//        System.out.println("height "+getHeight);
        if(x>getWidth*0.5){
            params.leftMargin = x - PictureTagView.getViewWidth();
            view = new PictureTagView(getContext(),Direction.Right,x,y);
        }
        else{
            params.leftMargin = x;
            view = new PictureTagView(getContext(),Direction.Left,x,y);
        }


        params.topMargin = y;
        //上下位置在视图内
        if(params.topMargin<0)params.topMargin =0;
        else if((params.topMargin+PictureTagView.getViewHeight())>getHeight) {
            params.topMargin = getHeight - PictureTagView.getViewHeight();
        }

        this.addView(view, params);
        view.setLayoutParams(params);
//        Log.i("flag","before");
//        justHasView(-1,-1);
//        Log.i("flag","after");
//        ((PictureTagView) view).setXVal(x);
//        ((PictureTagView) view).setYVal(y);
//        justHasView(-1,-1);
//        System.out.println(params.leftMargin+" "+params.topMargin);
//        System.out.println(" "+x+" "+y);

        return view;
    }

    private void moveView(int x,int y){
        if(touchView == null) return;
        RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.leftMargin = x - startX + startTouchViewLeft;
        params.topMargin = y - startY + startTouchViewTop;
        //限制子控件移动必须在视图范围内
        if(params.leftMargin<0||(params.leftMargin+touchView.getWidth())>getWidth())params.leftMargin = touchView.getLeft();
        if(params.topMargin<0||(params.topMargin+touchView.getHeight())>getHeight())params.topMargin = touchView.getTop();
        touchView.setLayoutParams(params);
    }

    private boolean hasView(int x,int y){
        //循环获取子view，判断xy是否在子view上，即判断是否按住了子view
        for(int index = 0; index < this.getChildCount(); index ++){
            View view = this.getChildAt(index);
            int left = (int) view.getX();
            int top = (int) view.getY();
            int right = view.getRight();
            int bottom = view.getBottom();
            Rect rect = new Rect(left, top, right, bottom);
            boolean contains = rect.contains(x, y);

            //如果是与子view重叠则返回真,表示已经有了view不需要添加新view了
            if(contains){
                touchView = view;
                touchView.bringToFront();
                return true;
            }
        }
        touchView = null;
        return false;
    }

    public PictureTagView justHasView(int x,int y){
//        Log.i("numberofviews",Integer.toString(this.getChildCount()));
        for(int index = 0; index < this.getChildCount(); index ++){
            PictureTagView view = (PictureTagView) this.getChildAt(index);
            int left = (int) view.getX();
            int top = (int) view.getY();
            int right = view.getRight();
            int bottom = view.getBottom();
//            Log.i("view number",Integer.toString(index));
//            Log.i("view left",Integer.toString(left));
//            Log.i("view right",Integer.toString(right));
//            Log.i("view mapTop",Integer.toString(mapTop));
//            Log.i("view bottom",Integer.toString(bottom));
//            Log.i("view x",""+view.getXVal());
//            Log.i("view y",""+view.getYVal());
            Rect rect = new Rect(left, top, right, bottom);
            boolean contains = rect.contains(x, y);

            if(contains){
                return (PictureTagView) view;
            }
        }
        return null;
    }

    public void load() {
        String res = "";    //read /sdcard/info.txt
        try {
            FileInputStream fin = new FileInputStream(fileName);
            int length = fin.available();
            byte[] buffer = new byte[length];
            fin.read(buffer);
            res = new String(buffer,"UTF-8");
            System.out.println(res);
            fin.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {   //string --> JSON Object
            info = new JSONObject(res);
            JSONObject anno_array = info.getJSONObject("ANNOTATION");
            JSONArray cur_pic_anno_array = anno_array.getJSONArray(cur_pic_num+"");
            if (cur_pic_anno_array.length()==0);
            else {
                for (int i = 0;i < cur_pic_anno_array.length(); i++) {
                    JSONObject temp = cur_pic_anno_array.getJSONObject(i);
                    int x = temp.getInt("x");
                    int y = temp.getInt("y");
                    String a = temp.getString("anno");
                    View view = addItem(x,y);
                    ((PictureTagView)view).setAnnotation(a);

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String message() {
        try {
            JSONObject temp = info;
            info = new JSONObject();
            JSONObject temp_anno = new JSONObject();
            for (int i = 1; i < 9; i++) {
                if (i != cur_pic_num) {
                    JSONArray temp_array = temp.getJSONObject("ANNOTATION").getJSONArray(i + "");
                    temp_anno.put(i+"",temp_array);
                }
            }

            JSONArray cur = new JSONArray();
            for (int index = 0; index < this.getChildCount(); index++) {
                View view = this.getChildAt(index);
                int left = (int) view.getX();
                int top = (int) view.getY();
                String anno = (String) ((PictureTagView) view).tvPictureTagLabel.getText();
                long time = ((PictureTagView) view).tvPictureTagLabel.getDrawingTime();
                SimpleDateFormat time_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date d1 = new Date(time);
                String t1 = time_format.format(d1);

                JSONObject a = new JSONObject();
                a.put("x",left);
                a.put("y",top);
                a.put("anno",anno);
                a.put("time",t1);
                cur.put(a);
            }
            temp_anno.put(cur_pic_num+"",cur);
            info.put("ANNOTATION",temp_anno);
            info.put("ADDRESS",temp.getJSONArray("ADDRESS"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return info.toString();
    }

    public void setNum(int num) { // pic number
        cur_pic_num = num;
    }

    public void write(String msg) {
        try{
            FileOutputStream outputStream = new FileOutputStream(fileName);
            outputStream.write(msg.getBytes());
            outputStream.flush();
            outputStream.close();
            Toast.makeText(getContext(), "WRITE", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
