package dinhnguyen.filza.file.manager.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ZoomableImageView extends View {
    
    private static final float MIN_SCALE = 0.5f;
    private static final float MAX_SCALE = 5.0f;
    private static final float INITIAL_SCALE = 1.0f;
    
    private Bitmap bitmap;
    private Matrix matrix = new Matrix();
    private ScaleGestureDetector scaleDetector;
    
    // Zoom and pan properties
    private float scaleFactor = INITIAL_SCALE;
    private float focusX, focusY;
    private float translateX = 0f, translateY = 0f;
    private float lastTouchX, lastTouchY;
    private boolean isZooming = false;
    
    // Touch handling
    private static final int INVALID_POINTER_ID = -1;
    private int activePointerId = INVALID_POINTER_ID;
    
    // Highlighting properties
    private Paint highlightPaint;
    private Path currentHighlightPath;
    private List<Path> highlightPaths = new ArrayList<>();
    private boolean isHighlightMode = false;
    
    public ZoomableImageView(Context context) {
        super(context);
        init();
    }
    
    public ZoomableImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public ZoomableImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        scaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        
        // Initialize highlight paint
        highlightPaint = new Paint();
        highlightPaint.setColor(Color.YELLOW);
        highlightPaint.setAlpha(128); // 50% transparency
        highlightPaint.setStrokeWidth(20f);
        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setStrokeJoin(Paint.Join.ROUND);
        highlightPaint.setStrokeCap(Paint.Cap.ROUND);
        highlightPaint.setAntiAlias(true);
        
        currentHighlightPath = new Path();
    }
    
    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        resetTransform();
        invalidate();
    }
    
    public void setHighlightMode(boolean enabled) {
        this.isHighlightMode = enabled;
    }
    
    public void clearHighlights() {
        highlightPaths.clear();
        currentHighlightPath.reset();
        invalidate();
    }
    
    public void resetTransform() {
        matrix.reset();
        scaleFactor = INITIAL_SCALE;
        translateX = 0f;
        translateY = 0f;
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (bitmap == null) return;
        
        canvas.save();
        
        // Apply transformations
        canvas.translate(translateX, translateY);
        canvas.scale(scaleFactor, scaleFactor, focusX, focusY);
        
        // Draw the bitmap
        canvas.drawBitmap(bitmap, 0, 0, null);
        
        // Draw highlights
        for (Path path : highlightPaths) {
            canvas.drawPath(path, highlightPaint);
        }
        
        // Draw current highlight path
        if (currentHighlightPath != null && !currentHighlightPath.isEmpty()) {
            canvas.drawPath(currentHighlightPath, highlightPaint);
        }
        
        canvas.restore();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        
        if (isHighlightMode) {
            return handleHighlightTouch(event);
        } else {
            return handleZoomTouch(event);
        }
    }
    
    private boolean handleZoomTouch(MotionEvent event) {
        final int action = event.getAction();
        
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                final float x = event.getX();
                final float y = event.getY();
                
                lastTouchX = x;
                lastTouchY = y;
                activePointerId = event.getPointerId(0);
                break;
            }
            
            case MotionEvent.ACTION_MOVE: {
                if (activePointerId != INVALID_POINTER_ID && !isZooming) {
                    final int pointerIndex = event.findPointerIndex(activePointerId);
                    final float x = event.getX(pointerIndex);
                    final float y = event.getY(pointerIndex);
                    
                    final float dx = x - lastTouchX;
                    final float dy = y - lastTouchY;
                    
                    translateX += dx;
                    translateY += dy;
                    
                    lastTouchX = x;
                    lastTouchY = y;
                    
                    invalidate();
                }
                break;
            }
            
            case MotionEvent.ACTION_UP: {
                activePointerId = INVALID_POINTER_ID;
                isZooming = false;
                break;
            }
            
            case MotionEvent.ACTION_CANCEL: {
                activePointerId = INVALID_POINTER_ID;
                isZooming = false;
                break;
            }
            
            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) 
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = event.getPointerId(pointerIndex);
                
                if (pointerId == activePointerId) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    lastTouchX = event.getX(newPointerIndex);
                    lastTouchY = event.getY(newPointerIndex);
                    activePointerId = event.getPointerId(newPointerIndex);
                }
                break;
            }
        }
        
        return true;
    }
    
    private boolean handleHighlightTouch(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        
        // Convert screen coordinates to bitmap coordinates
        float bitmapX = (x - translateX) / scaleFactor;
        float bitmapY = (y - translateY) / scaleFactor;
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentHighlightPath.reset();
                currentHighlightPath.moveTo(bitmapX, bitmapY);
                break;
                
            case MotionEvent.ACTION_MOVE:
                currentHighlightPath.lineTo(bitmapX, bitmapY);
                break;
                
            case MotionEvent.ACTION_UP:
                currentHighlightPath.lineTo(bitmapX, bitmapY);
                
                // Save the highlight path
                Path savedPath = new Path(currentHighlightPath);
                highlightPaths.add(savedPath);
                
                // Clear current path
                currentHighlightPath.reset();
                break;
        }
        
        invalidate();
        return true;
    }
    
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            isZooming = true;
            scaleFactor *= detector.getScaleFactor();
            
            // Limit scale factor
            scaleFactor = Math.max(MIN_SCALE, Math.min(scaleFactor, MAX_SCALE));
            
            focusX = detector.getFocusX();
            focusY = detector.getFocusY();
            
            invalidate();
            return true;
        }
    }
    
    public float getScaleFactor() {
        return scaleFactor;
    }
    
    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = Math.max(MIN_SCALE, Math.min(scaleFactor, MAX_SCALE));
        invalidate();
    }
} 