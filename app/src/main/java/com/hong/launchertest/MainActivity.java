package com.hong.launchertest;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity
    implements DragSource, DropTarget, View.OnLongClickListener{

    private static final String TAG = "MainActivity";

    private DragLayer mDragLayer;
    private TextView mTextView;
    private CellLayout mCellLayout;

    private DragController mDragController;

    private final Canvas mCanvas = new Canvas();

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.hello_tv);
        mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
        mCellLayout = (CellLayout) findViewById(R.id.cell_layout);

        mDragController = new DragController(this, mDragLayer);
        mDragController.addDropTarget(this);
        mDragLayer.setup(mDragController);

        mTextView.setOnLongClickListener(this);
        int i = 0;
        for(int x=0; x<3; x++){
            for(int y=0; y<4; y++){
                TextView tv= new TextView(this);
                Drawable da = getDrawable(R.mipmap.ic_launcher_round);
                tv.setText(String.valueOf(i));
                da.setBounds(0, 0, da.getIntrinsicWidth(), da.getIntrinsicHeight());
                tv.setCompoundDrawables(null,da,null,null);
                tv.setOnLongClickListener(this);
                CellLayout.LayoutParams lp = new CellLayout.LayoutParams(y, x, 1, 1);
                mCellLayout.addViewToCellLayout(tv, i,i, lp, true);
                i++;
            }
        }

    }

    @Override
    public boolean onLongClick(View v) {

        AtomicInteger padding = new AtomicInteger(2);
        final Bitmap b = createDragBitmap(v, padding);

        CellLayout.LayoutParams lp = (CellLayout.LayoutParams)v.getLayoutParams();
        Log.d(TAG, "onLongClick: " + lp.x + " " + lp.y);

        DragView dv = mDragController.startDrag(b, lp.x, lp.y, this, null,
                DragController.DRAG_ACTION_MOVE, new Point(), new Rect(), 1);

        return false;
    }

    public Bitmap createDragBitmap(View v, AtomicInteger expectedPadding) {
        Bitmap b;

        int padding = expectedPadding.get();
        if (v instanceof TextView) {
            Drawable d = getTextViewIcon((TextView) v);
            Rect bounds = getDrawableBounds(d);
            b = Bitmap.createBitmap(bounds.width() + padding,
                    bounds.height() + padding, Bitmap.Config.ARGB_8888);
            expectedPadding.set(padding - bounds.left - bounds.top);
        } else {
            b = Bitmap.createBitmap(
                    v.getWidth() + padding, v.getHeight() + padding, Bitmap.Config.ARGB_8888);
        }

        mCanvas.setBitmap(b);
        drawDragView(v, mCanvas, padding);
        mCanvas.setBitmap(null);

        return b;
    }

    private static void drawDragView(View v, Canvas destCanvas, int padding) {
        final Rect clipRect = new Rect();
        v.getDrawingRect(clipRect);

        destCanvas.save();
        if (v instanceof TextView) {
            Drawable d = getTextViewIcon((TextView) v);
            Rect bounds = getDrawableBounds(d);
            clipRect.set(0, 0, bounds.width() + padding, bounds.height() + padding);
            destCanvas.translate(padding / 2 - bounds.left, padding / 2 - bounds.top);
            d.draw(destCanvas);
        }
        destCanvas.restore();
    }

    private static Rect getDrawableBounds(Drawable d) {
        Rect bounds = new Rect();
        d.copyBounds(bounds);
        if (bounds.width() == 0 || bounds.height() == 0) {
            bounds.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        } else {
            bounds.offsetTo(0, 0);
        }

        return bounds;
    }

    /**
     * Returns the drawable for the given text view.
     */
    public static Drawable getTextViewIcon(TextView tv) {
        final Drawable[] drawables = tv.getCompoundDrawables();
        for (int i = 0; i < drawables.length; i++) {
            if (drawables[i] != null) {
                return drawables[i];
            }
        }
        return null;
    }

    //----------DragSource---------
    @Override
    public boolean supportsFlingToDelete() {
        return false;
    }

    @Override
    public boolean supportsAppInfoDropTarget() {
        return false;
    }

    @Override
    public boolean supportsDeleteDropTarget() {
        return false;
    }

    @Override
    public float getIntrinsicIconScaleFactor() {
        return 0;
    }

    @Override
    public void onFlingToDeleteCompleted() {

    }

    @Override
    public void onDropCompleted(View target, DragObject d, boolean isFlingToDelete, boolean success) {

    }
    //----------DragSource----------


    //----------DropTarget----------
    @Override
    public boolean isDropEnabled() {
        return true;
    }

    @Override
    public void onDrop(DragObject dragObject) {
        float[] f = dragObject.getVisualCenter(null);
        Log.d(TAG, "onDrop: " + f[0] + " " + f[1]);

        int[] resultSpan = new int[2];
        int[] targetCell = new int[2];
        mCellLayout.performReorder( (int)f[0], (int)f[1], 1,
                1, 1, 1, dragObject.dragView, targetCell, resultSpan, CellLayout.MODE_DRAG_OVER);

        dragObject.dragView.remove();
    }

    @Override
    public void onDragEnter(DragObject dragObject) {

    }

    @Override
    public void onDragOver(DragObject dragObject) {
//        Log.d(TAG, "onDragOver: " + dragObject);
        float[] f = dragObject.getVisualCenter(null);
        Log.d(TAG, "onDrop: " + f[0] + " " + f[1]);

        int[] resultSpan = new int[2];
        int[] targetCell = new int[2];
        mCellLayout.performReorder( (int)f[0], (int)f[1], 1,
                1, 1, 1, dragObject.dragView, targetCell, resultSpan, CellLayout.MODE_DRAG_OVER);

//        dragObject.dragView.remove();
    }

    @Override
    public void onDragExit(DragObject dragObject) {

    }

    @Override
    public void onFlingToDelete(DragObject dragObject, PointF vec) {

    }

    @Override
    public boolean acceptDrop(DragObject dragObject) {
        return true;
    }

    @Override
    public void prepareAccessibilityDrop() {

    }

    @Override
    public void getHitRectRelativeToDragLayer(Rect outRect) {
        mDragLayer.getHitRect(outRect);
    }

    @Override
    public void getLocationInDragLayer(int[] loc) {

    }

    @Override
    public int getLeft() {
        return 0;
    }

    @Override
    public int getTop() {
        return 0;
    }
    //----------DropTarget----------
}
