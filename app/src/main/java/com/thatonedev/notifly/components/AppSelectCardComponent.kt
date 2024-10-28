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

    private var filteredDataSet: JSONArray = dataSet

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
        val appInfo = filteredDataSet.getJSONObject(position)
        viewHolder.appCardName.text = appInfo.getString("name")
        //appInfo.getString("packageName")

        val iconDrawable = appInfo.get("icon") as Drawable
        viewHolder.appCardIcon.setImageDrawable(iconDrawable)

        viewHolder.appCardCheckBox.setOnClickListener {
            (activity as? OnDataPass)?.toggleAppCard(position)
        }
    }

    override fun getItemCount() = filteredDataSet.length()

    fun filter(query: String) {
        filteredDataSet = if (query.isEmpty()) {
            dataSet
        } else {
            val tempList = JSONArray()
            for (i in 0 until dataSet.length()) {
                val appInfo = dataSet.getJSONObject(i)
                if (appInfo.getString("name").contains(query, true)) {
                    tempList.put(appInfo)
                }
            }
            tempList
        }
        notifyDataSetChanged()
    }

    interface OnDataPass {
        fun toggleAppCard(position: Int)
    }
}
