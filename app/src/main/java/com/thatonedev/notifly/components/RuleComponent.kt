package com.thatonedev.notifly.components

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.thatonedev.notifly.R
import org.json.JSONArray

class RuleComponent(private val activity: Activity, private val dataSet: JSONArray) : RecyclerView.Adapter<RuleComponent.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ruleName: TextView = view.findViewById(R.id.rule_name)
        val ruleSwitch: Switch = view.findViewById(R.id.rule_switch)
        val ruleCard: CardView = view.findViewById(R.id.rule_card)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.component_rule, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.ruleName.text = dataSet.getJSONObject(position).getString("name").toString()
        viewHolder.ruleSwitch.isChecked = dataSet.getJSONObject(position).getBoolean("active")

        viewHolder.ruleCard.setOnClickListener {
            (activity as? OnDataPass)?.editRule(position)
        }
    }

    override fun getItemCount() = dataSet.length()

    interface OnDataPass {
        fun editRule(ruleId: Int)
    }
}