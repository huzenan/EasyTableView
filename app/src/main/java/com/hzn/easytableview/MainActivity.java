package com.hzn.easytableview;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout layoutMain;
    private EasyPathView epv;
    private EasyTableView table;
    private LinearLayout layoutSettings;
    private SeekBar sbWidth;
    private SeekBar sbHeight;
    private TextView tvWidthPercent;
    private TextView tvHeightPercent;
    private EditText et_text;
    private EditText etRows;
    private EditText etLines;
    private EditText etCorner;
    private EditText etSize;
    private RadioGroup rg;
    private EditText et_new_rows_lines;
    private EditText et_new_size;
    private EditText et_start;
    private EditText et_end;
    private Button btnClear;
    private Button btnDone;

    private int rows;
    private int lines;
    private DisplayMetrics dm;
    private ArrayList<EasyTableView.CellInfo> cellInfoList;

    private EasyTableView.CellInfo curCellInfo;
    //    private int[][] pWidth;
//    private int[][] pHeight;
    private float w;
    private float h;

    public static final int SELECT_TEXTS = 0;
    public static final int SELECT_BG = 1;
    public static final int SELECT_STROKE = 2;
    public static final int SELECT_OUT_STROKE = 3;
    private int curSelect = SELECT_TEXTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData(5, 4);
        initViews();
    }

    private void initData(int rows, int lines) {
        this.rows = rows;
        this.lines = lines;

        curCellInfo = null;
//        pWidth = new int[rows][lines];
//        pHeight = new int[rows][lines];
//        for (int r = 0; r < rows; r++) {
//            for (int l = 0; l < lines; l++) {
//                pWidth[r][l] = 100;
//                pHeight[r][l] = 100;
//            }
//        }

        dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        w = 1.0f * (dm.widthPixels - dipToPx(40)) / lines;
        h = 1.0f * (dm.heightPixels - dipToPx(60)) / rows;
        cellInfoList = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            EasyTableView.CellInfo cellInfo = new EasyTableView.CellInfo();
            cellInfo.row = r;
            cellInfo.line = 0;
            cellInfo.width = w;
            cellInfo.height = h;
            cellInfoList.add(cellInfo);
        }
        for (int l = 1; l < lines; l++) {
            EasyTableView.CellInfo cellInfo = new EasyTableView.CellInfo();
            cellInfo.row = 0;
            cellInfo.line = l;
            cellInfo.width = w;
            cellInfo.height = h;
            cellInfoList.add(cellInfo);
        }
    }

    private void initViews() {
        layoutSettings = (LinearLayout) findViewById(R.id.layout_settings);

        tvWidthPercent = (TextView) findViewById(R.id.tv_width_percent);
        tvHeightPercent = (TextView) findViewById(R.id.tv_height_percent);

        et_text = (EditText) findViewById(R.id.et_text);
        etRows = (EditText) findViewById(R.id.et_rows);
        etLines = (EditText) findViewById(R.id.et_lines);
        etCorner = (EditText) findViewById(R.id.et_corner);
        etSize = (EditText) findViewById(R.id.et_size);

        et_new_rows_lines = (EditText) findViewById(R.id.et_new_rows_lines);
        et_new_size = (EditText) findViewById(R.id.et_new_size);

        et_start = (EditText) findViewById(R.id.et_start);
        et_end = (EditText) findViewById(R.id.et_end);

        // main
        layoutMain = (RelativeLayout) findViewById(R.id.layout_main);
        layoutMain.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (null != curCellInfo) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            if (event.getRawY() < layoutSettings.getY())
                                unselectCell();
                            break;
                    }
                }
                return false;
            }
        });

        // table
        table = (EasyTableView) findViewById(R.id.table);
        table.setData(cellInfoList);
        table.setOnCellClickListener(new EasyTableView.OnCellClickListener() {
            @Override
            public void onCellClick(EasyTableView.CellInfo cellInfo) {
                if (null == curCellInfo || curCellInfo != cellInfo) {
                    selectCell(cellInfo);
                } else {
                    unselectCell();
                }
            }

            @Override
            public void onMergedCellClick(EasyTableView.MergeInfo mergeInfo) {
                // TODO
            }
        });


        // seek bar
        sbWidth = (SeekBar) findViewById(R.id.sb_width);
        sbWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (null != curCellInfo) {
                    // 更新宽度时，该单元格所在列都得更新
//                    for (int r = 0; r < rows; r++)
//                        pWidth[r][curCellInfo.line] = progress;

                    tvWidthPercent.setText(progress + "%");
                    curCellInfo.width = w * progress / 100.0f;
                    table.updateData(curCellInfo);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                layoutSettings.setAlpha(0.2f);
                removeSelectRect();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                layoutSettings.setAlpha(0.9f);
                addSelectRect();
            }
        });
        sbHeight = (SeekBar) findViewById(R.id.sb_height);
        sbHeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (null != curCellInfo) {
                    // 更新高度时，该单元格所在行都得更新
//                    for (int l = 0; l < lines; l++)
//                        pHeight[curCellInfo.row][l] = progress;

                    tvHeightPercent.setText(progress + "%");
                    curCellInfo.height = h * progress / 100.0f;
                    table.updateData(curCellInfo);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                layoutSettings.setAlpha(0.2f);
                removeSelectRect();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                layoutSettings.setAlpha(0.9f);
                addSelectRect();
            }
        });


        // rg
        rg = (RadioGroup) findViewById(R.id.rg);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_texts:
                        curSelect = SELECT_TEXTS;
                        break;
                    case R.id.rb_bg:
                        curSelect = SELECT_BG;
                        break;
                    case R.id.rb_stroke:
                        curSelect = SELECT_STROKE;
                        break;
                    case R.id.rb_out_stroke:
                        curSelect = SELECT_OUT_STROKE;
                        break;
                }
            }
        });


        // add
        (findViewById(R.id.tv_rt)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(et_new_rows_lines.getText().toString())) {
                    int newRowsLines = Integer.valueOf(et_new_rows_lines.getText().toString());

                    int newSize = -1;
                    if (!TextUtils.isEmpty(et_new_size.getText().toString()))
                        newSize = Integer.valueOf(et_new_size.getText().toString());

                    table.addNewRows(curCellInfo.row, newRowsLines, newSize, EasyTableView.ADD_ROWS_TOP);
                    unselectCell();
                }
            }
        });
        (findViewById(R.id.tv_rb)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(et_new_rows_lines.getText().toString())) {
                    int newRowsLines = Integer.valueOf(et_new_rows_lines.getText().toString());

                    int newSize = -1;
                    if (!TextUtils.isEmpty(et_new_size.getText().toString()))
                        newSize = Integer.valueOf(et_new_size.getText().toString());

                    table.addNewRows(curCellInfo.row, newRowsLines, newSize, EasyTableView.ADD_ROWS_BOTTOM);
                    unselectCell();
                }
            }
        });
        (findViewById(R.id.tv_ll)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(et_new_rows_lines.getText().toString())) {
                    int newRowsLines = Integer.valueOf(et_new_rows_lines.getText().toString());

                    int newSize = -1;
                    if (!TextUtils.isEmpty(et_new_size.getText().toString()))
                        newSize = Integer.valueOf(et_new_size.getText().toString());

                    table.addNewLines(curCellInfo.line, newRowsLines, newSize, EasyTableView.ADD_LINES_LEFT);
                    unselectCell();
                }
            }
        });
        (findViewById(R.id.tv_lr)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(et_new_rows_lines.getText().toString())) {
                    int newRowsLines = Integer.valueOf(et_new_rows_lines.getText().toString());

                    int newSize = -1;
                    if (!TextUtils.isEmpty(et_new_size.getText().toString()))
                        newSize = Integer.valueOf(et_new_size.getText().toString());

                    table.addNewLines(curCellInfo.line, newRowsLines, newSize, EasyTableView.ADD_LINES_RIGHT);
                    unselectCell();
                }
            }
        });


        // remove
        (findViewById(R.id.tv_remove_rows)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(et_start.getText().toString()) &&
                        !TextUtils.isEmpty(et_end.getText().toString())) {
                    int start = Integer.valueOf(et_start.getText().toString());
                    int end = Integer.valueOf(et_end.getText().toString());
                    boolean success = table.removeRows(start, end);
                    if (success)
                        unselectCell();
                }
            }
        });
        (findViewById(R.id.tv_remove_lines)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(et_start.getText().toString()) &&
                        !TextUtils.isEmpty(et_end.getText().toString())) {
                    int start = Integer.valueOf(et_start.getText().toString());
                    int end = Integer.valueOf(et_end.getText().toString());
                    boolean success = table.removeLines(start, end);
                    if (success)
                        unselectCell();
                }
            }
        });


        // btn
        btnClear = (Button) findViewById(R.id.btn_clear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != curCellInfo) {
                    curCellInfo.texts = null;
                    curCellInfo.bgColor = 0;
                    table.updateData(curCellInfo);
                }
            }
        });
        btnDone = (Button) findViewById(R.id.btn_done);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != curCellInfo) {
                    boolean tableChanged = false;
                    int rows = MainActivity.this.rows;
                    int lines = MainActivity.this.lines;
                    int corner = table.getOutStrokeCorner();
                    if (!TextUtils.isEmpty(etRows.getText())) {
                        rows = Integer.valueOf(etRows.getText().toString());
                        tableChanged = true;
                    }
                    if (!TextUtils.isEmpty(etLines.getText())) {
                        lines = Integer.valueOf(etLines.getText().toString());
                        tableChanged = true;
                    }
                    if (!TextUtils.isEmpty(etCorner.getText())) {
                        corner = Integer.valueOf(etCorner.getText().toString());
                        tableChanged = true;
                    }

                    if (tableChanged) {
                        // table
                        initData(rows, lines);
                        table.setOutStrokeCorner(corner);
                        table.setData(rows, lines, cellInfoList);
                        table.reset();

                        et_text.setText("");
                        etRows.setText("");
                        etLines.setText("");
                        etCorner.setText("");

                        unselectCell();
                    } else {
                        // texts
                        if (!TextUtils.isEmpty(et_text.getText())) {
                            String texts = et_text.getText().toString();
                            curCellInfo.texts = texts.split(";");
                        }

                        if (!TextUtils.isEmpty(etSize.getText())) {
                            int size = Integer.valueOf(etSize.getText().toString());
                            switch (curSelect) {
                                case SELECT_TEXTS:
                                    curCellInfo.textSize = spToPx(size);
                                    table.updateData(curCellInfo);
                                    break;
                                case SELECT_STROKE:
                                    table.setStrokeSize(dipToPx(size));
                                    table.reset();
                                    break;
                                case SELECT_OUT_STROKE:
                                    table.setOutStrokeSize(dipToPx(size));
                                    table.reset();
                                    break;
                            }
                        }

                        table.updateData(curCellInfo);
                    }
                }
            }
        });
    }

    private void selectCell(EasyTableView.CellInfo cellInfo) {
        curCellInfo = cellInfo;
        layoutSettings.setVisibility(View.VISIBLE);
        int pWidth = (int) ((1.0f * cellInfo.width / w) * 100);
        int pHeight = (int) ((1.0f * cellInfo.height / h) * 100);
        sbWidth.setProgress(pWidth);
        sbHeight.setProgress(pHeight);
        tvWidthPercent.setText(pWidth + "%");
        tvHeightPercent.setText(pHeight + "%");
        removeSelectRect();
        addSelectRect();
    }

    private void unselectCell() {
        curCellInfo = null;
        layoutSettings.setVisibility(View.GONE);
        removeSelectRect();
    }

    // 添加选中框
    private void addSelectRect() {
        StringBuilder ps = new StringBuilder();
        ps.append("m").append(curCellInfo.getStartX()).append(",").append(curCellInfo.getStartY()).append(" ");
        ps.append("l").append(curCellInfo.getStartX() + curCellInfo.width).append(",").append(curCellInfo.getStartY()).append(" ");
        ps.append("l").append(curCellInfo.getStartX() + curCellInfo.width).append(",").append(curCellInfo.getStartY() + curCellInfo.height).append(" ");
        ps.append("l").append(curCellInfo.getStartX()).append(",").append(curCellInfo.getStartY() + curCellInfo.height).append(" ");
        ps.append("l").append(curCellInfo.getStartX()).append(",").append(curCellInfo.getStartY());
        addEpv(ps.toString());
    }

    private void addEpv(String pathString) {
        EasyPathView epv = new EasyPathView(this);
        this.epv = epv;
        layoutMain.addView(epv);

        epv.setLayoutParams(table.getLayoutParams());
        epv.setFixedWidth(table.getWidth());
        epv.setFixedHeight(table.getHeight());
        epv.setDynamic(true);
        epv.setAnimMode(EasyPathView.ANIM_MODE_SEPARATE);
        epv.setAnimDurations("800");
        epv.setStrokeColor(Color.WHITE);
        epv.setStrokeWidth(dipToPx(1));
        epv.setStrokeFixedWidth(10);
        epv.setStrokeIsRound(true);
        epv.setPathString(pathString);
        epv.reset();
        epv.startDraw(true);
    }

    // 移除选中框
    private void removeSelectRect() {
        layoutMain.removeView(this.epv);
    }

    // 改变颜色
    public void onChangedTextColor(View view) {
        Drawable background = view.getBackground();
        ColorDrawable colorDrawable = (ColorDrawable) background;

        switch (curSelect) {
            case SELECT_TEXTS:
                curCellInfo.textColor = colorDrawable.getColor();
                table.updateData(curCellInfo);
                break;
            case SELECT_BG:
                curCellInfo.bgColor = colorDrawable.getColor();
                table.updateData(curCellInfo);
                break;
            case SELECT_STROKE:
                table.setStrokeColor(colorDrawable.getColor());
                table.reset();
                break;
            case SELECT_OUT_STROKE:
                table.setOutStrokeColor(colorDrawable.getColor());
                table.reset();
                break;
        }
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
        return (int) (spValue * dm.scaledDensity + 0.5f);
    }

    private int dipToPx(float dipValue) {
        return (int) (dipValue * dm.density + 0.5f);
    }
}
