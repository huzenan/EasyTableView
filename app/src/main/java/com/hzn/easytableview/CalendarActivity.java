package com.hzn.easytableview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.hzn.library.decoration.CircleDecoration;
import com.hzn.library.decoration.EasyDecoration;
import com.hzn.library.decoration.RangeDecoration;
import com.hzn.library.table.CellInfo;
import com.hzn.library.table.EasyTableView;
import com.hzn.library.table.MergeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Calendar View with EasyTableView setting EasyDecoration,
 * to show that decorations can be switch easily by
 * calling {@link EasyTableView#setBottomDecorations(EasyDecoration)} or
 * {@link EasyTableView#setTopDecorations(EasyDecoration)}
 * <br/>
 * Created by huzn on 2017/9/27.
 */

public class CalendarActivity extends AppCompatActivity {

    public static final int ROWS = 8;
    public static final int LINES = 7;

    private EasyTableView calendar;
    private ArrayList<CellInfo> cellInfos;
    private MergeInfo mergeInfo;

    private int start = -1;
    private int end = -1;
    private float radius;
    private RangeDecoration rangeDecoration;

    private int titleColor;
    private int weekColor;
    private int dayColor;
    private int rangeColor;
    private int rangeTextColor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        initColor();
        initData();
        initCalendar();
        initRangeDecoration();
    }

    private void initColor() {
        titleColor = getResources().getColor(R.color.dark_gray);
        weekColor = getResources().getColor(R.color.green_blue_dark);
        dayColor = getResources().getColor(R.color.dark_gray);
        rangeColor = getResources().getColor(R.color.green_blue_light);
        rangeTextColor = getResources().getColor(R.color.white);
    }

    private void initData() {
        cellInfos = new ArrayList<>();
        CellInfo info = null;

        // title
        mergeInfo = new MergeInfo();
        mergeInfo.startRow = 0;
        mergeInfo.endRow = 0;
        mergeInfo.startLine = 0;
        mergeInfo.endLine = LINES - 1;
        mergeInfo.texts = new String[1];
        mergeInfo.texts[0] = "SEPTEMBER";
        mergeInfo.textSize = dipToPx(25.0f);
        mergeInfo.textColor = titleColor;

        // empty
        for (int l = 0; l < LINES; l++) {
            info = new CellInfo();
            info.row = 0;
            info.line = l;
            cellInfos.add(info);
        }

        // week
        for (int l = 0; l < LINES; l++) {
            info = new CellInfo();
            info.row = 1;
            info.line = l;
            info.texts = new String[1];
            info.texts[0] = getWeek(l);
            info.textSize = dipToPx(16.0f);
            info.textColor = weekColor;
            cellInfos.add(info);
        }

        // empty
        for (int l = 0; l < 5; l++) {
            info = new CellInfo();
            info.row = 2;
            info.line = l;
            cellInfos.add(info);
        }

        // day
        for (int i = 2 * LINES + 5, j = 1; i < ROWS * LINES - 6; i++, j++) {
            info = new CellInfo();
            info.row = i / LINES;
            info.line = i % LINES;
            info.type = CellInfo.TYPE_BUTTON;
            info.texts = new String[1];
            info.texts[0] = "" + j;
            info.textSize = dipToPx(14);
            info.textColor = dayColor;
            cellInfos.add(info);
        }
    }

    private void initCalendar() {
        calendar = (EasyTableView) findViewById(R.id.calendar);
        calendar.setOnCellClickListener(new EasyTableView.OnCellClickListener() {
            @Override
            public void onCellClick(CellInfo cellInfo) {
                if (cellInfo.type != CellInfo.TYPE_BUTTON)
                    return;
                if (start == -1) {
                    start = cellInfo.row * LINES + cellInfo.line;
                    setCircleDecoration(cellInfo);
                } else if (end == -1) {
                    end = cellInfo.row * LINES + cellInfo.line + 1;
                    setRangeDecoration();
                } else {
                    clearDecoration();
//                    start = cellInfo.row * LINES + cellInfo.line;
//                    setCircleDecoration(cellInfo);
                }
                Log.d("atag", start + ", " + end);
            }

            @Override
            public void onMergedCellClick(MergeInfo mergeInfo) {

            }
        });
        calendar.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                int calendarWidth = calendar.getMeasuredWidth();
                ViewGroup.LayoutParams layoutParams = calendar.getLayoutParams();
                layoutParams.height = calendarWidth;
                int cellWidth = (calendarWidth - dipToPx(40.0f)) / LINES;
                radius = (cellWidth - dipToPx(10.0f)) / 2.0f;

                calendar.setLayoutParams(layoutParams);
                calendar.setData(cellInfos);
                calendar.mergeCells(mergeInfo);
                calendar.getViewTreeObserver().removeOnPreDrawListener(this);
                return false;
            }
        });
    }

    private void initRangeDecoration() {
        rangeDecoration = new RangeDecoration(0, rangeColor, cellInfos);
    }

    private void setCircleDecoration(CellInfo cellInfo) {
        List<CircleDecoration.CircleDecorationInfo> decorationInfoList = new ArrayList<>();
        CircleDecoration.CircleDecorationInfo info = new CircleDecoration.CircleDecorationInfo();
        info.cellInfo = cellInfo;
        info.radius = radius;
        decorationInfoList.add(info);
        CircleDecoration circleDecoration = new CircleDecoration(rangeColor, 0, 0, decorationInfoList);

        calendar.setBottomDecorations(circleDecoration);
        calendar.reset();
    }

    private void setRangeDecoration() {
        rangeDecoration.setRadius(radius);
        rangeDecoration.setRange(start, end);
        rangeDecoration.setRangeTextColor(calendar, rangeTextColor);
        calendar.setBottomDecorations(rangeDecoration);
        calendar.reset();
    }

    private void clearDecoration() {
        start = -1;
        end = -1;
        rangeDecoration.setRangeTextColor(calendar, dayColor);
        calendar.setBottomDecorations(null);
        calendar.reset();
    }

    private String getWeek(int week) {
        String strWeek = "";
        switch (week) {
            case 0:
                strWeek = "SUN";
                break;
            case 1:
                strWeek = "MON";
                break;
            case 2:
                strWeek = "TUE";
                break;
            case 3:
                strWeek = "WED";
                break;
            case 4:
                strWeek = "THU";
                break;
            case 5:
                strWeek = "FRI";
                break;
            case 6:
                strWeek = "SAT";
                break;
        }
        return strWeek;
    }

    private int spToPx(float spValue) {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        return (int) (spValue * dm.scaledDensity + 0.5f);
    }

    private int dipToPx(float dipValue) {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        return (int) (dipValue * dm.density + 0.5f);
    }

    private int getScreenWidth() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
            clearDecoration();
        return super.onTouchEvent(event);
    }
}
