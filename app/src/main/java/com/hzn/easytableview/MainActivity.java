package com.hzn.easytableview;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EasyTableView table;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        table = (EasyTableView) findViewById(R.id.table);
        table.setData(getTestData());
        table.mergeCells(getMergeInfoList());
        table.setOnCellClickListener(new EasyTableView.OnCellClickListener() {
            @Override
            public void onCellClick(EasyTableView.GridInfo gridInfo) {
                if (null != gridInfo.texts)
                    Toast.makeText(MainActivity.this, gridInfo.texts[0], Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onMergedCellClick(EasyTableView.MergeInfo mergeInfo) {
                if (null != mergeInfo.texts)
                    Toast.makeText(MainActivity.this, mergeInfo.texts[0], Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private ArrayList<EasyTableView.GridInfo> getTestData() {
        ArrayList<EasyTableView.GridInfo> testData = new ArrayList<>();
        for (int i = 1; i < 8; i++) {
            EasyTableView.GridInfo gridInfo = new EasyTableView.GridInfo();
            gridInfo.type = EasyTableView.GridInfo.TYPE_NORMAL;
            gridInfo.row = 0;
            gridInfo.line = i;
            gridInfo.texts = new String[2];
            gridInfo.texts[0] = "周" + i;
            gridInfo.texts[1] = "1/" + (15 + i);
            gridInfo.textSizes = new int[2];
            gridInfo.textSizes[0] = spToPx(12);
            gridInfo.textSizes[1] = spToPx(10);
            gridInfo.textColor = Color.BLACK;
            testData.add(gridInfo);
        }

        EasyTableView.GridInfo gridInfo1 = new EasyTableView.GridInfo();
        gridInfo1.type = EasyTableView.GridInfo.TYPE_NORMAL;
        gridInfo1.row = 1;
        gridInfo1.line = 0;
        gridInfo1.texts = new String[1];
        gridInfo1.texts[0] = "上午";
        gridInfo1.textSize = spToPx(14);
        gridInfo1.textColor = 0xffa6a6a6;
        testData.add(gridInfo1);

        EasyTableView.GridInfo gridInfo2 = new EasyTableView.GridInfo();
        gridInfo2.type = EasyTableView.GridInfo.TYPE_NORMAL;
        gridInfo2.row = 2;
        gridInfo2.line = 0;
        gridInfo2.texts = new String[1];
        gridInfo2.texts[0] = "下午";
        gridInfo2.textSize = spToPx(14);
        gridInfo2.textColor = 0xffa6a6a6;
        testData.add(gridInfo2);

        EasyTableView.GridInfo gridInfo3 = new EasyTableView.GridInfo();
        gridInfo3.type = EasyTableView.GridInfo.TYPE_BUTTON;
        gridInfo3.row = 2;
        gridInfo3.line = 5;
        gridInfo3.texts = new String[1];
        gridInfo3.texts[0] = "约满";
        gridInfo3.textSizes = new int[1];
        gridInfo3.textSizes[0] = spToPx(14);
        gridInfo3.textColor = 0xffa6a6a6;
        gridInfo3.bgColor = 0xffeae7e7;
        testData.add(gridInfo3);

        EasyTableView.GridInfo gridInfo4 = new EasyTableView.GridInfo();
        gridInfo4.type = EasyTableView.GridInfo.TYPE_BUTTON;
        gridInfo4.row = 1;
        gridInfo4.line = 2;
        gridInfo4.texts = new String[2];
        gridInfo4.texts[0] = "点击";
        gridInfo4.texts[1] = "预约";
        gridInfo4.textSize = spToPx(14);
        gridInfo4.textColor = Color.WHITE;
        gridInfo4.bgColor = 0xff00d3c2;
        testData.add(gridInfo4);

        return testData;
    }

    private ArrayList<EasyTableView.MergeInfo> getMergeInfoList() {
        ArrayList<EasyTableView.MergeInfo> mergeInfoList = new ArrayList<>();
        EasyTableView.MergeInfo mergeInfo = new EasyTableView.MergeInfo();
        mergeInfo.startRow = 3;
        mergeInfo.startLine = 0;
        mergeInfo.endRow = 3;
        mergeInfo.endLine = 7;
        mergeInfo.bgColor = Color.WHITE;
        mergeInfo.texts = new String[1];
        mergeInfo.texts[0] = "左右滑动日历查看其它日期排班";
        mergeInfo.textColor = 0xffa6a6a6;
        mergeInfoList.add(mergeInfo);
        return mergeInfoList;
    }

    private int spToPx(float spValue) {
        final float fontScale = this.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}
