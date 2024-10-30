package com.thatonedev.notifly.components

import android.annotation.SuppressLint
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
        viewHolder.appCardCheckBox.isChecked = appInfo.getBoolean("selected")
        //appInfo.getString("packageName")

        val iconDrawable = appInfo.get("icon") as Drawable
        viewHolder.appCardIcon.setImageDrawable(iconDrawable)

        viewHolder.appCardCheckBox.setOnClickListener {
            appInfo.put("selected", viewHolder.appCardCheckBox.isChecked)
            (activity as? OnDataPass)?.toggleAppCard(appInfo.getString("packageName"), viewHolder.appCardCheckBox.isChecked)
        }

        /*viewHolder.appCardCheckBox.setOnCheckedChangeListener { _, isChecked ->
            appInfo.put("selected", isChecked)
            (activity as? OnDataPass)?.toggleAppCard(appInfo.getString("packageName"), isChecked)
        }*/
    }

    override fun getItemCount() = filteredDataSet.length()

    @SuppressLint("NotifyDataSetChanged")
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
        fun toggleAppCard(packageName: String, selected: Boolean)
    }
}
