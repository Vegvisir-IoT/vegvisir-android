package com.vegvisir.app.annotativemap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import java.util.*;

import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private Button editButton = null;
    private TimerTask task;
    private String anno;

    private static Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("comes","here");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Context androidContext = getApplicationContext();

        editButton = findViewById(R.id.editimg);

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, picture.class);
                startActivity(intent);
            }
        });
        AnnotativeMapApplication thisApp = (AnnotativeMapApplication) this.getApplication();

        task = new TimerTask() {
            @Override
            public void run() {
                MainActivity.this.runOnUiThread(() -> {
                    if (thisApp.getCurrentPicture() != null && thisApp.isRunningMainActivity()) {
                        if (!thisApp.isPrintedOnce()) {
                            Log.i("annosatstart", thisApp.getAnnotations().toString());
                        }
                        Set<Coordinates> entriesToRemove = new HashSet<>();

                        for (Map.Entry<Coordinates, Annotation> entry : thisApp.getAnnotations().entrySet()) {

                            Coordinates coords = entry.getKey();
                            if (!thisApp.isPrintedOnce()) {
                                Log.i("coords", coords.getX() + "," + coords.getY());
                                Log.i("alreadyAdded", entry.getValue().getAlreadyAdded().toString());
                            }
                            Annotation annoObj = entry.getValue();
                            anno = annoObj.getAnnotation();
                            PictureTagLayout image = thisApp.getCurrentPicture().findViewById(R.id.image);


                            if (annoObj.getShouldRemove()) {
                                entriesToRemove.add(coords);
                                View v = image.justHasView(coords.getX(),coords.getY());
                                if (v != null) {
                                    image.removeView(v);
                                }
                                else {
                                    Log.i("view","for remove not found");
                                }

                            } else {
                                if (!thisApp.isPrintedOnce()) {
                                    Log.i("annoobj", annoObj.toString());
                                }
                                if (!annoObj.getAlreadyAdded()) {
//
                                    PictureTagView view = image.justHasView(coords.getX(), coords.getY());
                                    if (view == null) {
                                        view = (PictureTagView) image.addItem(coords.getX(), coords.getY());
                                    }

                                    view.setAnnotation(anno);

                                    annoObj.setAlreadyAdded(true);
                                } else {
                                    PictureTagView view = image.justHasView(coords.getX(), coords.getY());
                                    if (view != null) {
                                        view.setAnnotation(anno);
                                    }
                                }
                            }

                        }

                        for (Coordinates coords : entriesToRemove) {
                            thisApp.getAnnotations().remove(coords);
                        }
                        if (!thisApp.isPrintedOnce()) {
                            Log.i("annos", thisApp.getAnnotations().toString());
                        }
                        thisApp.setPrintedOnce(true);
                    }
                });
            }
        };

        timer = new Timer();

        timer.schedule(task,0,1000);

        Intent intent = new Intent(MainActivity.this, picture.class);
        startActivity(intent);

    }

    @Override
    public void onBackPressed() {
        Log.d("Main Activity", "onBackPressed: Pressed");
        return;
    }
}
