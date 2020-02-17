package com.abtasty.flagship_demo.app.utils

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.min


class PZImageView : AppCompatImageView {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}



    companion object {

        enum class STATE {INI, ZOOM, MOVE}
    }

    var state = STATE.INI
    var newMatrix = Matrix()
    var minScale = 1f
    var maxScale = 5f
    var pointEnd = PointF()
    var pointStart = PointF()
    var mat =  floatArrayOf()
    var mViewWidth: Int = 0
    var mViewHeight: Int = 0
    var mSaveScale = 1f
    var mOrigWidth: Float = 0.toFloat()
    var mOrigHeight: Float = 0.toFloat()
    var oldMeasuredWidth: Int = 0
    var oldMeasuredHeight: Int = 0
    var mScaleDetector: ScaleGestureDetector? = null


    init {
        super.setClickable(true)
        mScaleDetector = ScaleGestureDetector(context, ScaleListener())
        newMatrix = Matrix()
        mat = FloatArray(9)
        imageMatrix = newMatrix
        scaleType = ScaleType.MATRIX
        setOnTouchListener { v, event ->
            mScaleDetector?.onTouchEvent(event)
            val mCurrentPoint = PointF(event.x, event.y)
            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    pointEnd.set(mCurrentPoint)
                    pointStart.set(pointEnd)
                    state = STATE.MOVE
                }

                MotionEvent.ACTION_MOVE -> if (state == STATE.MOVE) {
                    val deltaX = mCurrentPoint.x - pointEnd.x
                    val deltaY = mCurrentPoint.y - pointEnd.y
                    val fixTransX =
                        getFixDragTrans(deltaX, mViewWidth.toFloat(), mOrigWidth * mSaveScale)
                    val fixTransY =
                        getFixDragTrans(deltaY, mViewHeight.toFloat(), mOrigHeight * mSaveScale)
                    newMatrix.postTranslate(fixTransX, fixTransY)
                    fixTrans()
                    pointEnd.set(mCurrentPoint.x, mCurrentPoint.y)
                }

                MotionEvent.ACTION_UP -> {
                    state = STATE.INI
                    val xDiff = Math.abs(mCurrentPoint.x - pointStart.x).toInt()
                    val yDiff = Math.abs(mCurrentPoint.y - pointStart.y).toInt()
                    if (xDiff < 5 && yDiff < 5)
                        performClick()
                }

                MotionEvent.ACTION_POINTER_UP -> state = STATE.INI
            }
            imageMatrix = newMatrix
            invalidate()
            true
        }

    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            state = STATE.ZOOM
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            var mScaleFactor = detector.scaleFactor
            val origScale = mSaveScale
            mSaveScale *= mScaleFactor
            if (mSaveScale > maxScale) {
                mSaveScale = maxScale
                mScaleFactor = maxScale / origScale
            } else if (mSaveScale < minScale) {
                mSaveScale = minScale
                mScaleFactor = minScale / origScale
            }

            if (mOrigWidth * mSaveScale <= mViewWidth || mOrigHeight * mSaveScale <= mViewHeight)
                newMatrix.postScale(
                    mScaleFactor,
                    mScaleFactor,
                    (mViewWidth / 2).toFloat(),
                    (mViewHeight / 2).toFloat()
                )
            else
                newMatrix.postScale(mScaleFactor, mScaleFactor, detector.focusX, detector.focusY)

            fixTrans()
            return true
        }
    }

    private fun fixTrans() {
        newMatrix.getValues(mat)
        val transX = mat[Matrix.MTRANS_X]
        val transY = mat[Matrix.MTRANS_Y]
        val fixTransX = getFixTrans(transX, mViewWidth.toFloat(), mOrigWidth * mSaveScale)
        val fixTransY = getFixTrans(transY, mViewHeight.toFloat(), mOrigHeight * mSaveScale)
        if (fixTransX != 0f || fixTransY != 0f)
            newMatrix.postTranslate(fixTransX, fixTransY)
    }

    private fun getFixTrans(trans: Float, viewSize: Float, contentSize: Float): Float {
        val minTrans: Float
        val maxTrans: Float
        if (contentSize <= viewSize) {
            minTrans = 0f
            maxTrans = viewSize - contentSize
        } else {
            minTrans = viewSize - contentSize
            maxTrans = 0f
        }

        if (trans < minTrans)
            return -trans + minTrans
        return if (trans > maxTrans) -trans + maxTrans else 0f

    }

    private fun getFixDragTrans(delta: Float, viewSize: Float, contentSize: Float): Float {
        return if (contentSize <= viewSize) {
            0f
        } else delta
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mViewWidth = MeasureSpec.getSize(widthMeasureSpec)
        mViewHeight = MeasureSpec.getSize(heightMeasureSpec)
        if (oldMeasuredHeight == mViewWidth && oldMeasuredHeight == mViewHeight || mViewWidth == 0 || mViewHeight == 0)
            return
        oldMeasuredHeight = mViewHeight
        oldMeasuredWidth = mViewWidth
        if (mSaveScale == 1f) {

            val scale: Float
            val mDrawable = drawable
            if (mDrawable == null || mDrawable.intrinsicWidth == 0 || mDrawable.intrinsicHeight == 0)
                return
            val bmWidth = mDrawable.intrinsicWidth
            val bmHeight = mDrawable.intrinsicHeight
            val scaleX = mViewWidth.toFloat() / bmWidth.toFloat()
            val scaleY = mViewHeight.toFloat() / bmHeight.toFloat()
            scale = min(scaleX, scaleY)
            newMatrix.setScale(scale, scale)

            var redundantYSpace = mViewHeight.toFloat() - scale * bmHeight.toFloat()
            var redundantXSpace = mViewWidth.toFloat() - scale * bmWidth.toFloat()
            redundantYSpace /= 2.toFloat()
            redundantXSpace /= 2.toFloat()
            newMatrix.postTranslate(redundantXSpace, redundantYSpace)
            mOrigWidth = mViewWidth - 2 * redundantXSpace
            mOrigHeight = mViewHeight - 2 * redundantYSpace
            imageMatrix = newMatrix
        }
        fixTrans()
    }
}