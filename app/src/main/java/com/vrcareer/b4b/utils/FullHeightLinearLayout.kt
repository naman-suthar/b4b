package com.vrcareer.b4b.utils

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout


class FullHeightLinearLayout : LinearLayout {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(
            widthMeasureSpec, MeasureSpec.makeMeasureSpec(
                (parent as View).getMeasuredHeight(), MeasureSpec.EXACTLY
            )
        )
    }
}