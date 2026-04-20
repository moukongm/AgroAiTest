package com.main.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class SideBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var letters: Array<String> = arrayOf(
        "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
        "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
    )
        set(value) {
            field = value
            invalidate()
        }

    private var selectedLetter: String? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    private var onLetterSelectedListener: OnLetterSelectedListener? = null

    interface OnLetterSelectedListener {
        fun onLetterSelected(letter: String)
        fun onLetterReleased()
    }

    fun setOnLetterSelectedListener(listener: OnLetterSelectedListener) {
        this.onLetterSelectedListener = listener
    }

    @JvmName("setSideBarLetters")
    fun setLetters(letters: Array<String>) {
        this.letters = letters
    }

    fun setSelectedLetter(letter: String?) {
        if (selectedLetter != letter) {
            selectedLetter = letter
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val viewWidth = width
        val viewHeight = height
        if (viewWidth <= 0 || viewHeight <= 0) return

        val letterCount = letters.size
        val cellHeight = viewHeight.toFloat() / letterCount

        paint.textSize = (cellHeight * 0.55f).coerceAtMost(14f * resources.displayMetrics.density)
        val textX = viewWidth / 2f

        for (i in letters.indices) {
            val textY = cellHeight * i + cellHeight / 2f - (paint.descent() + paint.ascent()) / 2f
            val letter = letters[i]

            if (letter == selectedLetter) {
                paint.color = 0xFF00AAD0.toInt()
                paint.isFakeBoldText = true
            } else {
                paint.color = 0xFF999999.toInt()
                paint.isFakeBoldText = false
            }

            canvas.drawText(letter, textX, textY, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val letter = getLetterAtY(event.y)
                if (letter != null && letter != selectedLetter) {
                    selectedLetter = letter
                    invalidate()
                    onLetterSelectedListener?.onLetterSelected(letter)
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                onLetterSelectedListener?.onLetterReleased()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun getLetterAtY(y: Float): String? {
        val viewHeight = height.toFloat()
        if (viewHeight <= 0) return null

        val cellHeight = viewHeight / letters.size
        val index = (y / cellHeight).toInt()

        if (index in letters.indices) {
            return letters[index]
        }
        return null
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val minHeight = suggestedMinimumHeight
        val height = resolveSizeAndState(minHeight, heightMeasureSpec, 0)
        val width = resolveSizeAndState(dpToPx(36f), widthMeasureSpec, 0)
        setMeasuredDimension(width, height)
    }

    private fun dpToPx(dp: Float): Int {
        return (dp * resources.displayMetrics.density + 0.5f).toInt()
    }
}
