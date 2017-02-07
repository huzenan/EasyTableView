package com.hzn.easytableview;

import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

import java.text.ParseException;

/**
 * 路径解析类，用于生成路径对象Path
 * Created by huzn on 2016/12/19.
 */
public class EasyPathParser {

    private String pathString;
    private int len;
    private int curIndex;
    private char last = '!'; // 标志开头

    private int argNum;
    private int curNum;

    private float[] args = new float[6];

    private volatile static EasyPathParser instance;

    private EasyPathParser() {
    }

    public static EasyPathParser getInstance() {
        if (null == instance) {
            synchronized (EasyPathParser.class) {
                if (null == instance) {
                    instance = new EasyPathParser();
                }
            }
        }
        return instance;
    }

    /**
     * 解析一段路径字符串并返回一个路径对象Path<br>
     * 目前支持的指令有：<br>
     * m、M：相当于执行Path.moveTo，后面带2个参数，分别为x和y坐标；例如："m50,50"<br>
     * l、L：相当于执行Path.lineTo，后面带2个参数，分别为x和y坐标；例如："l28.8,33.3"<br>
     * a、A：相当于执行Path.addArc，后面带6个参数，分别为left，top，right，bottom，startAngle和sweepAngle；例如："a-50,-50,50,50,45,359.9"<br>
     * o、O：相当于执行Path.addCircle，后面带4个参数，分别为圆心的x和y坐标、半径、方向，方向为1表示CW，2表示CCW(详见{@link Path.Direction})；例如："l30,30"<br>
     * q、Q：相当于执行Path.quadTo，二次贝塞尔曲线，后面带4个参数，(详见{@link Path#quadTo(float, float, float, float)})；例如："q50,50,100,0"<br>
     * c、C：相当于执行Path.cubicTo，三次贝塞尔曲线，后面带6个参数，(详见{@link Path#cubicTo(float, float, float, float, float, float)})；例如："q50,50,100,0"<br>
     * z、Z：相当于执行Path.close，不带参数，例如："z"<br>
     * 其中，指令为小写时的坐标为相对于父容器原点的坐标，大写时为绝对坐标；当parent参数为空时，按绝对坐标解析。
     *
     * @param str    要解析的路径字符串，每一种指令之间用空格来间隔，相同指令之间无需再指定指令字符，例如："m100,100 l150,150 100,150 z"
     * @param parent 父容器视图对象
     * @param factor 实际大小与指定大小的比例，用于正确生成路径位置，例如实际绘制大小为600x400，指定大小为300x200，此时factor为2
     * @return Path 解析完成后返回的路径对象
     * @throws ParseException 发生解析错误时抛出的异常
     */
    public Path parsePath(String str, View parent, float factor) throws ParseException {
        Path path = new Path();
        pathString = str.trim();
        len = pathString.length();

        curIndex = 0;
        while (curIndex < len) {
            curNum = 0;
            char ch = pathString.charAt(curIndex);

            // 跳过空格
            while (ch == ' ') {
                if (++curIndex < len)
                    ch = pathString.charAt(curIndex);
            }

            // 是否为同一个指令
            if (last != '!' && ('0' <= ch && ch <= '9' || ch == '-')) {
                ch = last;
                --curIndex;
            }

            switch (ch) {
                // moveTo
                case 'M':
                case 'm':
                    argNum = 2;
                    last = 'm';
                    nextToken();
                    if (ch == 'M' && null != parent) {
                        last = 'M';
                        args[0] -= parent.getX();
                        args[1] -= parent.getY();
                    }
                    path.moveTo(args[0] * factor, args[1] * factor);
                    break;

                // lineTo
                case 'L':
                case 'l':
                    argNum = 2;
                    last = 'l';
                    nextToken();
                    if (ch == 'L' && null != parent) {
                        last = 'L';
                        args[0] -= parent.getX();
                        args[1] -= parent.getY();
                    }
                    path.lineTo(args[0] * factor, args[1] * factor);
                    break;

                // addArc
                case 'A':
                case 'a':
                    argNum = 6;
                    last = 'a';
                    nextToken();
                    if (ch == 'A' && null != parent) {
                        last = 'A';
                        args[0] -= parent.getX();
                        args[1] -= parent.getY();
                        args[2] -= parent.getX();
                        args[3] -= parent.getY();
                    }
                    RectF oval = new RectF();
                    oval.left = args[0] * factor;
                    oval.top = args[1] * factor;
                    oval.right = args[2] * factor;
                    oval.bottom = args[3] * factor;
                    path.addArc(oval, args[4], args[5]);
                    break;

                // addCircle
                case 'O':
                case 'o':
                    argNum = 4;
                    last = 'o';
                    nextToken();
                    if (ch == 'O' && null != parent) {
                        last = 'O';
                        args[0] -= parent.getX();
                        args[1] -= parent.getY();
                    }
                    Path.Direction dir = args[3] == 1 ? Path.Direction.CW : Path.Direction.CCW;
                    path.addCircle(args[0] * factor, args[1] * factor, args[2] * factor, dir);
                    break;

                // quadTo
                case 'Q':
                case 'q':
                    argNum = 4;
                    last = 'q';
                    nextToken();
                    if (ch == 'Q' && null != parent) {
                        last = 'Q';
                        args[0] -= parent.getX();
                        args[1] -= parent.getY();
                        args[2] -= parent.getX();
                        args[3] -= parent.getY();
                    }
                    path.quadTo(args[0] * factor, args[1] * factor, args[2] * factor, args[3] * factor);
                    break;

                // cubicTo
                case 'C':
                case 'c':
                    argNum = 6;
                    last = 'c';
                    nextToken();
                    if (ch == 'C' && null != parent) {
                        last = 'C';
                        args[0] -= parent.getX();
                        args[1] -= parent.getY();
                        args[2] -= parent.getX();
                        args[3] -= parent.getY();
                        args[4] -= parent.getX();
                        args[5] -= parent.getY();
                    }
                    path.cubicTo(args[0] * factor, args[1] * factor, args[2] * factor, args[3] * factor, args[4] * factor, args[5] * factor);
                    break;

                // close
                case 'Z':
                case 'z':
                    last = 'z';
                    ++curIndex;
                    path.close();
                    break;

                default:
                    throw new ParseException("expected command character", curIndex);
            }
        }

        return path;
    }

    private void nextToken() throws ParseException {
        boolean isStart = true;
        boolean seenDot = false;
        StringBuilder num = new StringBuilder();

        ++curIndex;
        while (curIndex < len) {
            char ch = pathString.charAt(curIndex);
            if (ch == '-' && !isStart)
                throw new ParseException("unexpected '-'", curIndex);
            else if (ch == '.' && (isStart || seenDot))
                throw new ParseException("unexpected '.'", curIndex);

            isStart = false;
            if ('0' <= ch && ch <= '9' || ch == '-') {
                num.append(ch);
            } else if (!seenDot && ch == '.') {
                seenDot = true;
                num.append(ch);
            } else {
                consumeNum(num.toString());
                isStart = true;
                seenDot = false;
                num = new StringBuilder();

                if (curNum == argNum)
                    break;

                if (ch != ',')
                    throw new ParseException("expected ','", curIndex);
            }
            ++curIndex;
        }

        if (curIndex == len)
            consumeNum(num.toString());
    }

    private void consumeNum(String num) {
        ++curNum;
        args[curNum - 1] = Float.valueOf(num);
    }
}
