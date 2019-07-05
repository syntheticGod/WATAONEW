
package me.wowtao.pottery.gl;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import me.wowtao.pottery.Wowtao;
import me.wowtao.pottery.type.WTMode;
import me.wowtao.pottery.utils.PotteryTextureManager;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

public class GLView extends GLViewBottom {

    private static final int LEFT = 1;
    private static final int RIGHT = 2;

    private float startX = 0, startY = 0;

    /**
     * the view type
     *
     * @see WTMode
     */
    private WTMode mode = WTMode.VIEW;

    private int startSide;

    public void setFingerPoint(View fingerPoint) {
        this.fingerPoint = fingerPoint;
    }

    private View fingerPoint;


    /**
     * this construction is used for construct from xml;
     */
    public GLView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public boolean onTouchEvent(final MotionEvent event) {
        final float width = getWidth();
        final float height = getHeight();

        int action = event.getAction();

        if (GLView.this.mode == WTMode.SHAPE) {
            shape(event, width, height);
        } else if (GLView.this.mode == WTMode.INTERACT_VIEW) {
            handleChangeGesture(event);
        } else if (GLView.this.mode == WTMode.INTERACT_VIEW_AND_DECORATE) {
            handleViewAndDecorate(event, width, height, action);
        }
        return true;
    }

    private int isInteractViewDecorateStatus = 0;
    private void handleViewAndDecorate(MotionEvent event, float width, float height, int action) {
        if (action == MotionEvent.ACTION_DOWN) {
            lastX = startX = event.getX();
            lastY = startY = event.getY();
            isInteractViewDecorateStatus = 0;
        } else if (action == MotionEvent.ACTION_MOVE) {
            float deltaX = event.getX() - lastX;
            float deltaY = event.getY() - lastY;
            if (isInteractViewDecorateStatus == 0) {
                float dx = event.getX() - startX;
                float dy = event.getY() - startY;
                float d = (float) sqrt(dx * dx + dy * dy);
                if (d > 10) {
                    if (abs(deltaX * 1.1) < abs(deltaY)) {
                        isInteractViewDecorateStatus = 2;
                    } else {
                        isInteractViewDecorateStatus = 1;
                    }
                }
            } else if (isInteractViewDecorateStatus == 1) {
                Wowtao.getGlManager().changeGesture(deltaX, 0);
            } else if (isInteractViewDecorateStatus == 2) {
                PotteryTextureManager.preDecorate(event.getY(), height);
                isInteractViewDecorateStatus = 3;
            } else if (isInteractViewDecorateStatus == 3) {
                PotteryTextureManager.tempDecorate(event.getY(), height);
            }
            lastX = event.getX();
            lastY = event.getY();
        } else if (action == MotionEvent.ACTION_UP) {
            if (isInteractViewDecorateStatus == 0) {
                PotteryTextureManager.preDecorate(event.getY(), height);
                PotteryTextureManager.finalDecorate(event.getX(), event.getY(), height, width);
            } else if (isInteractViewDecorateStatus == 2) {
                PotteryTextureManager.preDecorate(event.getY(), height);
                PotteryTextureManager.finalDecorate(event.getX(), event.getY(), height, width);
            } else if (isInteractViewDecorateStatus == 3) {
                PotteryTextureManager.finalDecorate(event.getX(), event.getY(), height, width);
            }
        }
    }

    protected void shape(MotionEvent event, float width, float height) {
        moveFingerPoint(event);
        int action = event.getAction();
        if (action == MotionEvent.ACTION_MOVE) {
            float X = event.getX();
            float Y = event.getY();
            if (startSide == LEFT) {
                if (X < getWidth() / 2) {
                    Wowtao.getGlManager().reshape(lastX, lastY, X, Y, width, height);
                } else {
                    Wowtao.getGlManager().reshape(X, lastY, lastX, Y, width, height);
                }
            } else {
                if (X < getWidth() / 2) {
                    Wowtao.getGlManager().reshape(X, lastY, lastX, Y, width, height);
                } else {
                    Wowtao.getGlManager().reshape(lastX, lastY, X, Y, width, height);
                }
            }
            lastX = X;
            lastY = Y;
        }
    }

    private void moveFingerPoint(MotionEvent event) {
        float drawPointX;
        float drawPointY;
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            drawPointX = event.getX();
            drawPointY = event.getY();
            if (drawPointX < getWidth() / 2) {
                this.startSide = LEFT;
            } else {
                this.startSide = RIGHT;
            }
            fingerPoint.setVisibility(VISIBLE);
            fingerPoint.setX(drawPointX - fingerPoint.getWidth() / 2);
            fingerPoint.setY(drawPointY - fingerPoint.getHeight() / 2);
        } else if (action == MotionEvent.ACTION_MOVE) {
            drawPointX = event.getX();
            drawPointY = event.getY();
            if (this.startSide == LEFT && drawPointX > getWidth() / 2) {
                drawPointX = getWidth() / 2;
            } else if (this.startSide == RIGHT && drawPointX < getWidth() / 2) {
                drawPointX = getWidth() / 2;
            }
            fingerPoint.setX(drawPointX - fingerPoint.getWidth() / 2);
            fingerPoint.setY(drawPointY - fingerPoint.getHeight() / 2);
        } else if (action == MotionEvent.ACTION_UP) {
            fingerPoint.setVisibility(GONE);
        }
    }

    @Override
    GLRendererBottom getRenderer() {
        return new GLRenderer();
    }

    public void setMode(WTMode mode) {
        this.mode = mode;
    }

}


