package com.vegvisir.app.annotativemap;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by jiangyi on 09/07/2017.
 */

public class add_annotation extends AppCompatActivity implements View.OnClickListener{
    private Button done, del = null;
    private EditText anno1;
    private int seq, cur_seq;
    private TextView anno_prev, edited_time;
    final String ANNOTATION = "Annotation";
    final String DEL = "Delete";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_annotation);

        Intent intent = getIntent();
        String cur_anno = intent.getStringExtra("CUR_ANNO");
        String time = intent.getStringExtra("TIME");

        anno1 = (EditText) findViewById(R.id.anno1);
        done = (Button) findViewById(R.id.done);
        del = (Button) findViewById(R.id.del);
        anno_prev = (TextView) findViewById(R.id.anno_prev);
        edited_time = (TextView) findViewById(R.id.edited_time);

        anno_prev.setText(cur_anno);
        anno1.setText(cur_anno);
        edited_time.setText(time);

        done.setOnClickListener(this);
        del.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        String anno_added;
        switch (view.getId()) {
            case R.id.done:
                intent = new Intent(add_annotation.this, picture.class);
//                final String cur_time = getTime();
                anno_added = gettext().trim();
                if (!TextUtils.isEmpty(anno_added)) {
//                    intent.putExtra(TIME, cur_time);
                    intent.putExtra(ANNOTATION, anno_added);
                    setResult(0, intent);
                    finish();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Empty Annotation", Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;
            case R.id.del:
//                intent = new Intent(add_annotation.this, picture.class);
//                anno_added = gettext();
//                intent.putExtra(ANNOTATION, "");
//                setResult(0, intent);
//                finish();

                Intent intent1 = new Intent(add_annotation.this, picture.class);
                intent1.putExtra(DEL, "delete");
                setResult(1, intent1);
                finish();
                break;
            default:
                break;
        }
    }

    public String gettext() {
        return anno1.getText().toString();
    }
}
