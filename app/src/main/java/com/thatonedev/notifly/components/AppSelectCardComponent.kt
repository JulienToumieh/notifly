package com.thatonedev.notifly.components

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.thatonedev.notifly.R
import org.json.JSONArray


class AppSelectCardComponent(private val activity: Activity, private val dataSet: JSONArray) : RecyclerView.Adapter<AppSelectCardComponent.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appCardName: TextView = view.findViewById(R.id.app_card_name)
        val appCardCheckBox: CheckBox = view.findViewById(R.id.app_card_checkbox)
        val appCardIcon: ImageView = view.findViewById(R.id.app_card_icon)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.component_app_select_card, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val appInfo = dataSet.getJSONObject(position)
        viewHolder.appCardName.text = appInfo.getString("name")

        // Get the drawable icon
        val iconDrawable = appInfo.get("icon") as Drawable
        viewHolder.appCardIcon.setImageDrawable(iconDrawable)

        viewHolder.appCardCheckBox.setOnClickListener {
            // Handle checkbox click event
            // For example, you can pass the position back to the activity or fragment
            // Notify the activity or fragment about the toggle
            (activity as? OnDataPass)?.toggleAppCard(position)
        }
    }


    override fun getItemCount() = dataSet.length()

    interface OnDataPass {
        fun toggleAppCard(position: Int)
    }
}