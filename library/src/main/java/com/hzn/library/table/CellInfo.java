package com.hzn.library.table;

/**
 * Information of a table cell.
 * <br/>
 * Created by huzn on 2017/9/27.
 */
public class CellInfo {
    /**
     * Not drawing the cell, can be used to hide the cell
     * and data of the cell will not be cleared.
     */
    public static final int TYPE_NONE = 0;
    /**
     * Normal type.
     */
    public static final int TYPE_NORMAL = 1;
    /**
     * Button type.
     */
    public static final int TYPE_BUTTON = 2;
    /**
     * Type of cell.
     */
    public int type = TYPE_NORMAL;
    /**
     * Tag of cell.
     */
    public Object tag = null;
    /**
     * Row of cell.
     */
    public int row = -1;
    /**
     * Line of cell.
     */
    public int line = -1;
    /**
     * Starting x-coordinate of cell,
     * access with {@link #getStartX()}.
     */
    float startX = 0.0f;
    /**
     * Starting y-coordinate of cell,
     * access with {@link #getStartY()}.
     */
    float startY = 0.0f;
    /**
     * Width of cell, if less than 0, width will be
     * automatically set by table's mode.
     */
    public float width = -1.0f;
    /**
     * Height of cell, if less than 0, height will be
     * automatically set by table's mode.
     */
    public float height = -1.0f;
    /**
     * Background color of cell, only if this is set will
     * the background of the cell be drew, otherwise
     * the background of the cell is the background of the table.
     */
    public int bgColor = 0;
    /**
     * Text color of cell, this will override {@link #textColors},
     * text will be drew with Color.BLACK by default.
     */
    public int textColor = 0;
    /**
     * Text colors, denotes color of each text line, should be set
     * to the same size of {@link #texts}.
     */
    public int[] textColors = null;
    /**
     * Size of text, this will override {@link #textSizes},
     * text will be drew with 14sp by default.
     */
    public int textSize = -1;
    /**
     * Text sizes, denotes size of each text line, should be set
     * to the same size of {@link #texts}.
     */
    public int[] textSizes = null;
    /**
     * Texts, multiple lines.
     */
    public String[] texts = null;

    public CellInfo() {
    }

    public CellInfo(
            int type,
            Object tag,
            int row,
            int line,
            float width,
            float height,
            int bgColor,
            int textColor,
            int[] textColors,
            int textSize,
            int[] textSizes,
            String[] texts) {
        this.type = type;
        this.tag = tag;
        this.row = row;
        this.line = line;
        this.width = width;
        this.height = height;
        this.bgColor = bgColor;
        this.textColor = textColor;
        this.textColors = textColors;
        this.textSize = textSize;
        this.textSizes = textSizes;
        this.texts = texts;
    }

    public float getStartX() {
        return startX;
    }

    public float getStartY() {
        return startY;
    }
}
