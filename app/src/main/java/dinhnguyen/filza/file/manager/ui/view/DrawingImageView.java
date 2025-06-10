package dinhnguyen.filza.file.manager.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DrawingImageView extends View {
    
    public enum DrawingMode {
        DRAW, HIGHLIGHT, ERASER, ZOOM
    }
    
    private Bitmap originalBitmap;
    private Bitmap drawingBitmap; // Separate layer for drawings only
    private Canvas drawingCanvas;
    
    private DrawingMode currentMode = DrawingMode.ZOOM;
    
    // Drawing properties
    private Paint drawPaint;
    private Paint highlightPaint;
    private Paint eraserPaint;
    private Path currentPath;
    private List<Path> paths = new ArrayList<>();
    private List<Paint> paints = new ArrayList<>();
    
    // Zoom and pan properties
    private ScaleGestureDetector scaleDetector;
    private Matrix matrix = new Matrix();
    private float scaleFactor = 1.0f;
    private float focusX, focusY;
    private float translateX = 0f, translateY = 0f;
    private float lastTouchX, lastTouchY;
    private boolean isZooming = false;
    
    // Touch handling
    private static final int INVALID_POINTER_ID = -1;
    private int activePointerId = INVALID_POINTER_ID;
    
    public DrawingImageView(Context context) {
        super(context);
        init();
    }
    
    public DrawingImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public DrawingImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Initialize drawing paint
        drawPaint = new Paint();
        drawPaint.setColor(Color.RED);
        drawPaint.setStrokeWidth(8f);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        drawPaint.setAntiAlias(true);
        
        // Initialize highlight paint
        highlightPaint = new Paint();
        highlightPaint.setColor(Color.YELLOW);
        highlightPaint.setAlpha(128); // 50% transparency
        highlightPaint.setStrokeWidth(20f);
        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setStrokeJoin(Paint.Join.ROUND);
        highlightPaint.setStrokeCap(Paint.Cap.ROUND);
        highlightPaint.setAntiAlias(true);
        
        // Initialize eraser paint
        eraserPaint = new Paint();
        eraserPaint.setStrokeWidth(40f);
        eraserPaint.setStyle(Paint.Style.STROKE);
        eraserPaint.setStrokeJoin(Paint.Join.ROUND);
        eraserPaint.setStrokeCap(Paint.Cap.ROUND);
        eraserPaint.setAntiAlias(true);
        eraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        
        // Initialize scale detector
        scaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        
        // Initialize current path
        currentPath = new Path();
    }
    
    public void setBitmap(Bitmap bitmap) {
        this.originalBitmap = bitmap;
        createDrawingBitmap();
        fitImageToScreen();
        invalidate();
    }
    
    private void createDrawingBitmap() {
        if (originalBitmap == null) return;
        
        // Create a transparent bitmap for drawings only
        drawingBitmap = Bitmap.createBitmap(originalBitmap.getWidth(), 
                                          originalBitmap.getHeight(), 
                                          Bitmap.Config.ARGB_8888);
        drawingCanvas = new Canvas(drawingBitmap);
        // Don't draw the original bitmap here - keep it separate
    }
    
    private void fitImageToScreen() {
        if (originalBitmap == null || getWidth() == 0 || getHeight() == 0) {
            // If view hasn't been laid out yet, we'll fit in onLayout
            return;
        }
        
        float viewWidth = getWidth();
        float viewHeight = getHeight();
        float imageWidth = originalBitmap.getWidth();
        float imageHeight = originalBitmap.getHeight();
        
        // Calculate scale to fit image within view bounds
        float scaleX = viewWidth / imageWidth;
        float scaleY = viewHeight / imageHeight;
        float scale = Math.min(scaleX, scaleY);
        
        // Center the image
        float scaledImageWidth = imageWidth * scale;
        float scaledImageHeight = imageHeight * scale;
        float translateX = (viewWidth - scaledImageWidth) / 2;
        float translateY = (viewHeight - scaledImageHeight) / 2;
        
        // Apply the transformation
        this.scaleFactor = scale;
        this.translateX = translateX;
        this.translateY = translateY;
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        
        // Fit image to screen when layout is complete
        if (changed && originalBitmap != null) {
            fitImageToScreen();
        }
    }
    
    public void setDrawingMode(DrawingMode mode) {
        this.currentMode = mode;
    }
    
    public void resetTransform() {
        fitImageToScreen();
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (originalBitmap == null) return;
        
        // Apply transformations
        canvas.save();
        canvas.translate(translateX, translateY);
        canvas.scale(scaleFactor, scaleFactor);
        
        // First, draw the original bitmap
        canvas.drawBitmap(originalBitmap, 0, 0, null);
        
        // Then, draw the drawing layer on top
        if (drawingBitmap != null) {
            canvas.drawBitmap(drawingBitmap, 0, 0, null);
        }
        
        // Draw current path if exists
        if (currentPath != null && !currentPath.isEmpty()) {
            Paint currentPaint = getCurrentPaint();
            canvas.drawPath(currentPath, currentPaint);
        }
        
        canvas.restore();
    }
    
    private Paint getCurrentPaint() {
        switch (currentMode) {
            case DRAW:
                return drawPaint;
            case HIGHLIGHT:
                return highlightPaint;
            case ERASER:
                return eraserPaint;
            default:
                return drawPaint;
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        
        if (currentMode == DrawingMode.ZOOM) {
            return handleZoomTouch(event);
        } else {
            return handleDrawingTouch(event);
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
                    
                    translateX += x - lastTouchX;
                    translateY += y - lastTouchY;
                    
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
    
    private boolean handleDrawingTouch(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        
        // Convert screen coordinates to bitmap coordinates
        float bitmapX = (x - translateX) / scaleFactor;
        float bitmapY = (y - translateY) / scaleFactor;
        
        // Check if the touch is within bitmap bounds
        if (bitmapX < 0 || bitmapX > originalBitmap.getWidth() || 
            bitmapY < 0 || bitmapY > originalBitmap.getHeight()) {
            return true;
        }
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentPath.reset();
                currentPath.moveTo(bitmapX, bitmapY);
                break;
                
            case MotionEvent.ACTION_MOVE:
                currentPath.lineTo(bitmapX, bitmapY);
                break;
                
            case MotionEvent.ACTION_UP:
                currentPath.lineTo(bitmapX, bitmapY);
                
                // Save the path and paint
                Path savedPath = new Path(currentPath);
                Paint savedPaint = new Paint(getCurrentPaint());
                
                paths.add(savedPath);
                paints.add(savedPaint);
                
                // Draw on the drawing bitmap (not the original)
                drawingCanvas.drawPath(savedPath, savedPaint);
                
                // Clear current path
                currentPath.reset();
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
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f));
            
            invalidate();
            return true;
        }
    }
    
    public Bitmap getEditedBitmap() {
        if (originalBitmap == null) return null;
        
        // Create a new bitmap with original image + drawings
        Bitmap result = Bitmap.createBitmap(originalBitmap.getWidth(), 
                                          originalBitmap.getHeight(), 
                                          Bitmap.Config.ARGB_8888);
        Canvas resultCanvas = new Canvas(result);
        
        // Draw original image first
        resultCanvas.drawBitmap(originalBitmap, 0, 0, null);
        
        // Draw the drawing layer on top
        if (drawingBitmap != null) {
            resultCanvas.drawBitmap(drawingBitmap, 0, 0, null);
        }
        
        return result;
    }
    
    public void setStrokeWidth(float width) {
        drawPaint.setStrokeWidth(width);
        highlightPaint.setStrokeWidth(width * 2.5f); // Highlight is thicker
        eraserPaint.setStrokeWidth(width * 5f); // Eraser is even thicker
    }
    
    public void setDrawColor(int color) {
        drawPaint.setColor(color);
    }
    
    public void setHighlightColor(int color) {
        highlightPaint.setColor(color);
    }
    
    public int getDrawColor() {
        return drawPaint.getColor();
    }
    
    public int getHighlightColor() {
        return highlightPaint.getColor();
    }
} 