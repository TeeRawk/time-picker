package com.yalantis.timepicker;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.yalantis.library.ClockFace;


/**
 * Created by Alexey on 04.08.2016.
 */
public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final FrameLayout scrollView = (FrameLayout) findViewById(R.id.holder);
        scrollView.requestDisallowInterceptTouchEvent(true);
        final ClockFace clockFace = (ClockFace) findViewById(R.id.clock);
    }
}
