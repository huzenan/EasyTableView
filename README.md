# EasyTableView
EasyTableView is a light table view for Android. It has 4 modes: normal, fixWidth, fixHeight and fixWidthHeight. "Fix" means the width or height of cells are auto set, and can't be edited. Also you can add decorations on the table such as drawing a calendar. It's simple and fun, enjoy it!

## ScreenShots
>Edit one cell

![table_one_cell](https://github.com/huzenan/EasyTableView/blob/master/screenshots/table_one_cell.gif)

>Edit the hole table

![table_hole_table](https://github.com/huzenan/EasyTableView/blob/master/screenshots/table_hole_table.gif)

>Using decorations

![table_calendar_decorations](https://github.com/huzenan/EasyTableView/blob/master/screenshots/calendar.gif)

## Usage
>layout

```xml
    <com.hzn.easytableview.EasyTableView
        android:id="@+id/table"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        custom:etvBgColor="#f1f1f1"
        custom:etvHeaderHColor="#66cccc"
        custom:etvHeaderHVColor="#228181"
        custom:etvHeaderVColor="#66cccc"
        custom:etvLines="4"
        custom:etvMode="normal"
        custom:etvOutStrokeColor="#228181"
        custom:etvOutStrokeCorner="10dp"
        custom:etvOutStrokeSize="3dp"
        custom:etvRows="5"
        custom:etvStrokeColor="#228181"
        custom:etvStrokeSize="1dp"/>
```

>Activity

```java
    // set data
    ArrayList<CellInfo> cellInfoList = new ArrayList<CellInfo>();
    ...
    table.setData(cellInfoList);
    ...

    // click
    table.setOnCellClickListener(new EasyTableView.OnCellClickListener() {
        @Override
        public void onCellClick(EasyTableView.CellInfo cellInfo) {
            // your codes.
        }

        @Override
        public void onMergedCellClick(EasyTableView.MergeInfo mergeInfo) {
            // your codes.
        }
    });
    
    // long click
    table.setOnCellLongClickListener(new EasyTableView.OnCellLongClickListener() {
        @Override
        public void onCellLongClick(CellInfo cellInfo) {
            // your codes.
        }
        @Override
        public void onMergedCellLongClick(MergeInfo mergeInfo) {
            // your codes.
        }
    });
    
    // update
    curCellInfo.texts = new String[] {"good", "nice"};
    curCellInfo.textSize = newSize;
    curCellInfo.width = newWidth;
    table.updateData(curCellInfo);
    ...
    mergeInfo.bgColor = 0xffff0000;
    table.updateMergeData(mergeInfo);
    ...
    
    // add & remove
    table.addNewRows(curRow, newRows, height, EasyTableView.ADD_ROWS_TOP);
    ...
    table.addNewLines(curLine, newLines, width, EasyTableView.ADD_LINES_RIGHT);
    ...
    table.removeRows(start, end);
    ...
    table.removeLines(start, end);
    ...
    
    // merge & unmerge
    EasyTableView.MergeInfo mergeInfo = new EasyTableView.MergeInfo();
    mergeInfo.startRow = 0;
    mergeInfo.startLine = 0;
    mergeInfo.endRow = 1;
    mergeInfo.endLine = 1;
    ...
    table.mergeCells(mergeInfo);
    ...
    table.unmergeCells(mergeInfo);
    ...
```

>Decorations

Implement interface EasyDecoration, it has only one method 'draw' that will be invoke during the drawing process of EasyTableView. For now we have several default decorations, see [CircleDecoration](https://github.com/huzenan/EasyTableView/blob/master/library/src/main/java/com/hzn/library/decoration/CircleDecoration.java) and [RangeDecoration](https://github.com/huzenan/EasyTableView/blob/master/library/src/main/java/com/hzn/library/decoration/RangeDecoration.java).

And it is easy to set decorations:
```java
    // bottom decoration, drawing above cells background, and below cells texts
    table.setBottomDecorations(new MyBottomDecoration());
    
    // top decoration, drawing above everything
    table.setTopDecorations(new MyTopDecoration());
```
