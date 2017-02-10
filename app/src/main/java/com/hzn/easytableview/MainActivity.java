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
    private EditText etText;
    private EditText etRows;
    private EditText etLines;
    private EditText etCorner;
    private EditText etSize;
    private RadioGroup rg;
    private EditText etNewRowsLines;
    private EditText etNewSize;
    private EditText etStart;
    private EditText etEnd;
    private EditText etStartRow;
    private EditText etStartLine;
    private EditText etEndRow;
    private EditText etEndLine;
    private TextView tvMerge;
    private Button btnClear;
    private Button btnDone;

    private int rows;
    private int lines;
    private DisplayMetrics dm;
    private ArrayList<EasyTableView.CellInfo> cellInfoList;

    private EasyTableView.CellInfo curCellInfo;
    private EasyTableView.MergeInfo curMergeInfo;
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

        etText = (EditText) findViewById(R.id.et_text);
        etRows = (EditText) findViewById(R.id.et_rows);
        etLines = (EditText) findViewById(R.id.et_lines);
        etCorner = (EditText) findViewById(R.id.et_corner);
        etSize = (EditText) findViewById(R.id.et_size);

        etNewRowsLines = (EditText) findViewById(R.id.et_new_rows_lines);
        etNewSize = (EditText) findViewById(R.id.et_new_size);

        etStart = (EditText) findViewById(R.id.et_start);
        etEnd = (EditText) findViewById(R.id.et_end);

        etStartRow = (EditText) findViewById(R.id.et_start_row);
        etStartLine = (EditText) findViewById(R.id.et_start_line);
        etEndRow = (EditText) findViewById(R.id.et_end_row);
        etEndLine = (EditText) findViewById(R.id.et_end_line);

        // main
        layoutMain = (RelativeLayout) findViewById(R.id.layout_main);
        layoutMain.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (event.getRawY() < layoutSettings.getY()) {
                            if (null != curCellInfo)
                                unselectCell();
                            else if (null != curMergeInfo)
                                unselectMergeCell();
                        }
                        break;
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
                    curMergeInfo = null;
                    tvMerge.setText("MERGE");
                    selectCell(cellInfo);
                } else {
                    unselectCell();
                }
            }

            @Override
            public void onMergedCellClick(EasyTableView.MergeInfo mergeInfo) {
                if (null == curMergeInfo || curMergeInfo != mergeInfo) {
                    curCellInfo = null;
                    tvMerge.setText("UNMERGE");
                    selectMergeCell(mergeInfo);
                } else {
                    unselectMergeCell();
                }
            }
        });


        // seek bar
        sbWidth = (SeekBar) findViewById(R.id.sb_width);
        sbWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (null != curCellInfo) {
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
                if (!TextUtils.isEmpty(etNewRowsLines.getText().toString())) {
                    int newRowsLines = Integer.valueOf(etNewRowsLines.getText().toString());

                    int newSize = -1;
                    if (!TextUtils.isEmpty(etNewSize.getText().toString()))
                        newSize = Integer.valueOf(etNewSize.getText().toString());

                    table.addNewRows(curCellInfo.row, newRowsLines, dipToPx(newSize), EasyTableView.ADD_ROWS_TOP);
                    unselectCell();
                }
            }
        });
        (findViewById(R.id.tv_rb)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(etNewRowsLines.getText().toString())) {
                    int newRowsLines = Integer.valueOf(etNewRowsLines.getText().toString());

                    int newSize = -1;
                    if (!TextUtils.isEmpty(etNewSize.getText().toString()))
                        newSize = Integer.valueOf(etNewSize.getText().toString());

                    table.addNewRows(curCellInfo.row, newRowsLines, dipToPx(newSize), EasyTableView.ADD_ROWS_BOTTOM);
                    unselectCell();
                }
            }
        });
        (findViewById(R.id.tv_ll)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(etNewRowsLines.getText().toString())) {
                    int newRowsLines = Integer.valueOf(etNewRowsLines.getText().toString());

                    int newSize = -1;
                    if (!TextUtils.isEmpty(etNewSize.getText().toString()))
                        newSize = Integer.valueOf(etNewSize.getText().toString());

                    table.addNewLines(curCellInfo.line, newRowsLines, dipToPx(newSize), EasyTableView.ADD_LINES_LEFT);
                    unselectCell();
                }
            }
        });
        (findViewById(R.id.tv_lr)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(etNewRowsLines.getText().toString())) {
                    int newRowsLines = Integer.valueOf(etNewRowsLines.getText().toString());

                    int newSize = -1;
                    if (!TextUtils.isEmpty(etNewSize.getText().toString()))
                        newSize = Integer.valueOf(etNewSize.getText().toString());

                    table.addNewLines(curCellInfo.line, newRowsLines, dipToPx(newSize), EasyTableView.ADD_LINES_RIGHT);
                    unselectCell();
                }
            }
        });


        // remove
        (findViewById(R.id.tv_remove_rows)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(etStart.getText().toString()) &&
                        !TextUtils.isEmpty(etEnd.getText().toString())) {
                    int start = Integer.valueOf(etStart.getText().toString());
                    int end = Integer.valueOf(etEnd.getText().toString());
                    boolean success = table.removeRows(start, end);
                    if (success)
                        unselectCell();
                }
            }
        });
        (findViewById(R.id.tv_remove_lines)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(etStart.getText().toString()) &&
                        !TextUtils.isEmpty(etEnd.getText().toString())) {
                    int start = Integer.valueOf(etStart.getText().toString());
                    int end = Integer.valueOf(etEnd.getText().toString());
                    boolean success = table.removeLines(start, end);
                    if (success)
                        unselectCell();
                }
            }
        });


        // merge/unmerge
        tvMerge = (TextView) findViewById(R.id.tv_merge);
        tvMerge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != curMergeInfo) { // unmerge
                    table.unmergeCells(curMergeInfo);
                    unselectMergeCell();
                } else { // merge
                    if (!TextUtils.isEmpty(etStartRow.getText()) &&
                            !TextUtils.isEmpty(etStartLine.getText()) &&
                            !TextUtils.isEmpty(etEndRow.getText()) &&
                            !TextUtils.isEmpty(etEndLine.getText())) {
                        EasyTableView.MergeInfo mergeInfo = new EasyTableView.MergeInfo();
                        mergeInfo.startRow = Integer.valueOf(etStartRow.getText().toString());
                        mergeInfo.startLine = Integer.valueOf(etStartLine.getText().toString());
                        mergeInfo.endRow = Integer.valueOf(etEndRow.getText().toString());
                        mergeInfo.endLine = Integer.valueOf(etEndLine.getText().toString());
                        table.mergeCells(mergeInfo);
                        unselectMergeCell();
                    }
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
                boolean tableChanged = false;
                int rows = MainActivity.this.rows;
                int lines = MainActivity.this.lines;
                if (!TextUtils.isEmpty(etRows.getText())) {
                    rows = Integer.valueOf(etRows.getText().toString());
                    tableChanged = true;
                }
                if (!TextUtils.isEmpty(etLines.getText())) {
                    lines = Integer.valueOf(etLines.getText().toString());
                    tableChanged = true;
                }

                if (tableChanged) {
                    // table
                    initData(rows, lines);
                    table.setData(rows, lines, cellInfoList);
                    table.reset();

                    etText.setText("");
                    etRows.setText("");
                    etLines.setText("");
                    etCorner.setText("");

                    unselectCell();
                } else if (null != curCellInfo) {
                    // corner
                    if (!TextUtils.isEmpty(etCorner.getText())) {
                        int corner = Integer.valueOf(etCorner.getText().toString());
                        table.setOutStrokeCorner(corner);
                        table.reset();

                        removeSelectRect();
                        addSelectRect();
                    }

                    // texts
                    if (!TextUtils.isEmpty(etText.getText())) {
                        String texts = etText.getText().toString();
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

                } else if (null != curMergeInfo) {
                    // corner
                    if (!TextUtils.isEmpty(etCorner.getText())) {
                        int corner = Integer.valueOf(etCorner.getText().toString());
                        table.setOutStrokeCorner(corner);
                        table.reset();

                        removeSelectRect();
                        addSelectRect();
                    }

                    // texts
                    if (!TextUtils.isEmpty(etText.getText())) {
                        String texts = etText.getText().toString();
                        curMergeInfo.texts = texts.split(";");
                    }

                    if (!TextUtils.isEmpty(etSize.getText())) {
                        int size = Integer.valueOf(etSize.getText().toString());
                        switch (curSelect) {
                            case SELECT_TEXTS:
                                curMergeInfo.textSize = spToPx(size);
                                table.updateMergeData(curMergeInfo);
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

                    table.updateMergeData(curMergeInfo);
                }
            }
        });
    }

    private void selectCell(EasyTableView.CellInfo cellInfo) {
        curCellInfo = cellInfo;
        layoutSettings.setVisibility(View.VISIBLE);
        int pWidth = (int) ((1.0f * cellInfo.width / w) * 100);
        int pHeight = (int) ((1.0f * cellInfo.height / h) * 100);
        sbWidth.setEnabled(true);
        sbHeight.setEnabled(true);
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

    private void selectMergeCell(EasyTableView.MergeInfo mergeInfo) {
        curMergeInfo = mergeInfo;
        layoutSettings.setVisibility(View.VISIBLE);
        sbWidth.setEnabled(false);
        sbHeight.setEnabled(false);
        removeSelectRect();
        addSelectRect();
    }

    private void unselectMergeCell() {
        curMergeInfo = null;
        layoutSettings.setVisibility(View.GONE);
        sbWidth.setEnabled(true);
        sbHeight.setEnabled(true);
        removeSelectRect();
    }

    // 添加选中框
    private void addSelectRect() {
        StringBuilder ps = new StringBuilder();
        if (null != curCellInfo) {
            ps.append("m").append(curCellInfo.getStartX()).append(",").append(curCellInfo.getStartY()).append(" ");
            ps.append("l").append(curCellInfo.getStartX() + curCellInfo.width).append(",").append(curCellInfo.getStartY()).append(" ");
            ps.append("l").append(curCellInfo.getStartX() + curCellInfo.width).append(",").append(curCellInfo.getStartY() + curCellInfo.height).append(" ");
            ps.append("l").append(curCellInfo.getStartX()).append(",").append(curCellInfo.getStartY() + curCellInfo.height).append(" ");
            ps.append("l").append(curCellInfo.getStartX()).append(",").append(curCellInfo.getStartY());
            addEpv(ps.toString());
        } else if (null != curMergeInfo) {
            ps.append("m").append(curMergeInfo.getStartX()).append(",").append(curMergeInfo.getStartY()).append(" ");
            ps.append("l").append(curMergeInfo.getStartX() + curMergeInfo.getWidth()).append(",").append(curMergeInfo.getStartY()).append(" ");
            ps.append("l").append(curMergeInfo.getStartX() + curMergeInfo.getWidth()).append(",").append(curMergeInfo.getStartY() + curMergeInfo.getHeight()).append(" ");
            ps.append("l").append(curMergeInfo.getStartX()).append(",").append(curMergeInfo.getStartY() + curMergeInfo.getHeight()).append(" ");
            ps.append("l").append(curMergeInfo.getStartX()).append(",").append(curMergeInfo.getStartY());
            addEpv(ps.toString());
        }
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
                if (null != curCellInfo) {
                    curCellInfo.textColor = colorDrawable.getColor();
                    table.updateData(curCellInfo);
                } else if (null != curMergeInfo) {
                    curMergeInfo.textColor = colorDrawable.getColor();
                    table.updateMergeData(curMergeInfo);
                }
                break;
            case SELECT_BG:
                if (null != curCellInfo) {
                    curCellInfo.bgColor = colorDrawable.getColor();
                    table.updateData(curCellInfo);
                } else if (null != curMergeInfo) {
                    curMergeInfo.bgColor = colorDrawable.getColor();
                    table.updateMergeData(curMergeInfo);
                }
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
