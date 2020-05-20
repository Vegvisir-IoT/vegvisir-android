package com.vegvisir.app.annotativemap;

/**
 * Created by jiangyi on 09/07/2017.
 */

import com.vegvisir.app.annotativemap.ui.login.LoginActivity;
import com.vegvisir.pub_sub.TransactionID;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import java.util.*;

public class picture extends AppCompatActivity implements View.OnClickListener{
    private Button logout = null;
    public int cur_pic_number = 0;
    final String ANNOTATION = "Annotation";
    final String TIME = "CURRENT";
    final String DEL = "Delete";
    String anno = "";
    String cur_time = "";
    AnnotativeMapApplication thisApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pic);
        PictureTagLayout image = findViewById(R.id.image);
        image.setBackgroundResource(R.drawable.chicago_map);
        image.load(); //read /sdcard/info.txt and init the subviews(annotations)

        thisApp = (AnnotativeMapApplication) this.getApplication();
        thisApp.setCurrentPicture(this);
        thisApp.setRunningMainActivity(true);

        logout = findViewById(R.id.logout);
        logout.setOnClickListener(this);
        thisApp = (AnnotativeMapApplication)this.getApplication();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.logout:
                Intent intent = new Intent(picture.this, LoginActivity.class);
                startActivity(intent);
                thisApp.setRunningMainActivity(false);
                Set<Coordinates> coords = thisApp.getAnnotations().keySet();
                for (Coordinates c: coords) {
                    thisApp.getAnnotations().get(c).setAlreadyAdded(false);
                }
                break;
            default:
                break;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent annotation) {
        if (resultCode == 0) { //Done
            try {
                anno = annotation.getStringExtra(ANNOTATION);
            } catch (NullPointerException e) {
                Log.i("anno","not in intent");
            }
            cur_time = annotation.getStringExtra(TIME);
            PictureTagLayout image = findViewById(R.id.image);
                Log.i("add x", Integer.toString(image.startX));
                Log.i("add y", Integer.toString(image.startY));
                Coordinates coords;
                PictureTagView view = image.justHasView(image.startX,image.startY);

                if (view != null) {
                    coords = new Coordinates(view.getXVal(),view.getYVal());
                }
                else{
                    coords = new Coordinates(image.startX,image.startY);
                }

                Set<String> topics = new HashSet<>();
                Iterator<TransactionID> it = LoginActivity.mapDependencySets.get(coords).iterator();
                topics.add(thisApp.getTopic());
                Set<TransactionID> dependencies = thisApp.getMapTopDeps();

                String payloadString = "9" + coords.getX() + "," + coords.getY() + "," + anno;
                byte[] payload = payloadString.getBytes();
                Log.i("gets","here");

                thisApp.getVirtual().addTransaction(thisApp.getContext(),topics,payload,dependencies);
                Log.i("gets","after");

            super.onActivityResult(requestCode,resultCode,annotation);
        }
        else if (resultCode == 1) { //Del
            try {
                PictureTagLayout image = findViewById(R.id.image);
                Log.i("del x", Integer.toString(image.startX));
                Log.i("del y", Integer.toString(image.startY));
                PictureTagView view = image.justHasView(image.startX, image.startY);
                Coordinates coords;
                if (view != null) {
                    coords = new Coordinates(view.getXVal(), view.getYVal());
                } else {
                    Log.i("View", "somehow not found");
                    coords = new Coordinates(image.startX, image.startY);
                }
                Set<String> topics = new HashSet<>();

                topics.add(thisApp.getTopic());
                Set<TransactionID> dependencies = thisApp.getMapTopDeps();

                if (thisApp.getAnnotations().containsKey(coords)) {
                    anno = thisApp.getAnnotations().get(coords).getAnnotation();
                } else {
                    Log.i("How did we", "get here");
                }

                String payloadString = "8" + view.getXVal() + "," + view.getYVal() + "," + anno;
                byte[] payload = payloadString.getBytes();

                thisApp.getVirtual().addTransaction(thisApp.getContext(), topics, payload, dependencies);
                Log.i("In the try","");
                super.onActivityResult(requestCode, resultCode, annotation);
            } catch (Exception e) {
                Log.i("In the catch","");
            }
        }
        else if (resultCode == 2) { //Send Done
            super.onActivityResult(requestCode,resultCode,annotation);
        }
    }

    public PictureTagView findView() {
        return (findViewById(R.id.image));
    }

    @Override
    public void onBackPressed() {
        Log.d("Picture ", "onBackPressed: Pressed");
    }
}
