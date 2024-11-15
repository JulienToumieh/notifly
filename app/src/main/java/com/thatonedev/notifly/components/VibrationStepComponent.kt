package com.thatonedev.notifly.components

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.setPadding
import com.thatonedev.notifly.R


class VibrationStepComponent : LinearLayout {
    private var vibrationStepValue: TextView? = null
    private var vibrationStepCard: CardView? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.component_vibration_step, this, true)
        vibrationStepValue = findViewById(R.id.vibration_step_value)
        vibrationStepCard = findViewById(R.id.vibration_step_card)

    }

    @SuppressLint("PrivateResource")
    fun setProps(value: String, isVibration: Boolean, cardWeight: Float) {

        vibrationStepValue?.text = value

        vibrationStepValue?.viewTreeObserver?.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                vibrationStepValue?.viewTreeObserver?.removeOnPreDrawListener(this)

                val parentWidth = (vibrationStepValue?.parent as? View)?.width ?: 0

                val textWidth = vibrationStepValue?.paint?.measureText(vibrationStepValue?.text.toString())?.toInt() ?: 0

                if (textWidth > parentWidth) {
                    vibrationStepValue?.visibility = View.GONE
                } else {
                    vibrationStepValue?.visibility = View.VISIBLE
                }

                return true
            }
        })

        vibrationStepValue?.text = value

        val typedValue = TypedValue()
        val typedValue2 = TypedValue()
        if (isVibration) {
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true)
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue2, true)
        } else {
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorBackgroundFloating, typedValue, true)
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnBackground, typedValue2, true)
        }
        vibrationStepValue?.setTextColor(typedValue2.data)
        vibrationStepCard?.setCardBackgroundColor(typedValue.data)

        val layoutParams = this.layoutParams ?: LayoutParams(0, LayoutParams.WRAP_CONTENT).apply {
            weight = cardWeight
        }
        this.layoutParams = layoutParams
        this.requestLayout()
    }



}
