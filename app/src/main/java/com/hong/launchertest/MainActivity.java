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
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity
    implements DragSource, DropTarget, View.OnLongClickListener{

    private static final String TAG = "MainActivity";

    private DragLayer mDragLayer;
    private View mView;
    private CellLayout mCellLayout;
    private Bitmap mOutlineBitmap;

    private DragController mDragController;

    private final Canvas mCanvas = new Canvas();

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
        mCellLayout = (CellLayout) findViewById(R.id.cell_layout);

        mDragController = new DragController(this, mDragLayer);
        mDragController.addDropTarget(this);
        mDragLayer.setup(mDragController);

        int i = 0;
        for(int x=0; x<2; x++){
            for(int y=0; y<4; y++){
                TextView tv= new TextView(this);
                Drawable da = getDrawable(R.mipmap.ic_launcher_round);
                tv.setText(String.valueOf(i));
                tv.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                da.setBounds(0, 0, da.getIntrinsicWidth(), da.getIntrinsicHeight());
                tv.setCompoundDrawables(null,da,null,null);
                tv.setOnLongClickListener(this);
                CellLayout.LayoutParams lp = new CellLayout.LayoutParams(y, x, 1, 1);
                mCellLayout.addViewToCellLayout(tv, i,i, lp, true);
                i++;
            }
        }
        WidgetLayout widgetLayout = new WidgetLayout(this);
        widgetLayout.setOnLongClickListener(this);
        CellLayout.LayoutParams lp = new CellLayout.LayoutParams(0, 2, 1, 2);
        mCellLayout.addViewToCellLayout(widgetLayout, i,i, lp, true);

    }

    @Override
    public boolean onLongClick(View v) {

        mCellLayout.prepareChildForDrag(v);
        v.setVisibility(View.INVISIBLE);
        mView =  v;

        AtomicInteger padding = new AtomicInteger(2);
        final Bitmap b = createDragBitmap(v, padding);
        mOutlineBitmap = b;

        CellLayout.LayoutParams lp = (CellLayout.LayoutParams)v.getLayoutParams();
        Log.d(TAG, "onLongClick: " + lp.x + " " + lp.y);

        DragView dv = mDragController.startDrag(b, lp.x, lp.y, this, null,
                DragController.DRAG_ACTION_MOVE, new Point(), new Rect(), 1);

        return false;
    }

    public Bitmap createDragBitmap(View v, AtomicInteger expectedPadding) {
        Bitmap b;

        int padding = expectedPadding.get();
        b = Bitmap.createBitmap(
                v.getWidth() + padding, v.getHeight() + padding, Bitmap.Config.ARGB_8888);

        mCanvas.setBitmap(b);
        drawDragView(v, mCanvas, padding);
        mCanvas.setBitmap(null);

        return b;
    }

    private static void drawDragView(View v, Canvas destCanvas, int padding) {
        final Rect clipRect = new Rect();
        v.getDrawingRect(clipRect);

        destCanvas.save();

        destCanvas.translate(-v.getScrollX() + padding / 2, -v.getScrollY() + padding / 2);
        destCanvas.clipRect(clipRect, Region.Op.REPLACE);
        v.draw(destCanvas);

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

        CellLayout.LayoutParams lp = (CellLayout.LayoutParams) mView.getLayoutParams();

        int[] resultSpan = new int[2];
        int[] targetCell = new int[2];
	    targetCell =  mCellLayout.performReorder( (int)dragObject.x, (int)dragObject.y, 1,
                1, lp.cellHSpan, lp.cellVSpan, mView, targetCell, resultSpan, CellLayout.MODE_ON_DROP);
	    Log.d(TAG, "onDrop: targetCell " + targetCell[0] + " " + targetCell[1]);
	    lp.cellX = lp.tmpCellX = targetCell[0];
	    lp.cellY = lp.tmpCellY = targetCell[1];
        lp.isLockedToGrid = true;

        dragObject.dragView.remove();
        mView.setVisibility(View.VISIBLE);
        mCellLayout.clearDragOutlines();
    }

    @Override
    public void onDragEnter(DragObject dragObject) {

    }

    @Override
    public void onDragOver(DragObject dragObject) {
//        Log.d(TAG, "onDragOver: " + dragObject);
        float[] f = dragObject.getVisualCenter(null);
//        Log.d(TAG, "onDrop: " + f[0] + " " + f[1]);
        CellLayout.LayoutParams lp = (CellLayout.LayoutParams) mView.getLayoutParams();

        int[] resultSpan = new int[2];
        int[] targetCell = new int[2];
        mCellLayout.performReorder( (int)dragObject.x, (int)dragObject.y, 1,
                1, lp.cellHSpan, lp.cellVSpan,  mView, targetCell, resultSpan, CellLayout.MODE_DRAG_OVER);

        mCellLayout.visualizeDropLocation(null, mOutlineBitmap,
                1, 1,
                targetCell[0], targetCell[1], resultSpan[0], resultSpan[1], false,
                null, null);

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
