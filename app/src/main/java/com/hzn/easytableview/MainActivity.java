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
//        table.mergeCells(getMergeInfoList());
        table.setOnCellClickListener(new EasyTableView.OnCellClickListener() {
            @Override
            public void onCellClick(EasyTableView.CellInfo gridInfo) {
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

    private ArrayList<EasyTableView.CellInfo> getTestData() {
        ArrayList<EasyTableView.CellInfo> testData = new ArrayList<>();

        testData.add(new EasyTableView.CellInfo(
                EasyTableView.CellInfo.TYPE_NORMAL,
                null,
                0,
                0,
                150,
                100,
                0xffff0000,
                0xffa6a6a6,
                null,
                spToPx(14),
                null,
                new String[] {"呵哈", "哦额"}
        ));

        testData.add(new EasyTableView.CellInfo(
                EasyTableView.CellInfo.TYPE_BUTTON,
                null,
                1,
                0,
                0,
                100,
                0xff00ff00,
                0xffa6a6a6,
                null,
                spToPx(14),
                null,
                new String[] {"厉害了", "a!!!"}
        ));

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
