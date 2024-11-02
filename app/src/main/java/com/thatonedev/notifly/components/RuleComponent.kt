package com.thatonedev.notifly.components

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thatonedev.notifly.MainActivity
import com.thatonedev.notifly.R
import org.json.JSONArray

class RuleComponent(private val activity: Activity, private val dataSet: JSONArray) : RecyclerView.Adapter<RuleComponent.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ruleName: TextView = view.findViewById(R.id.rule_name)
        val ruleSwitch: Switch = view.findViewById(R.id.rule_switch)
        val ruleCard: CardView = view.findViewById(R.id.rule_card)
        val ruleDelete: ImageButton = view.findViewById(R.id.rule_delete_button)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.component_rule, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.ruleName.text = dataSet.getJSONObject(position).getString("name").toString()
        viewHolder.ruleSwitch.isChecked = dataSet.getJSONObject(position).getBoolean("active")


        viewHolder.ruleSwitch.setOnCheckedChangeListener { _, isChecked ->
            (activity as? OnDataPass)?.activateRule(position, isChecked)
        }

        viewHolder.ruleCard.setOnClickListener {
            (activity as? OnDataPass)?.editRule(position)
        }

        viewHolder.ruleDelete.setOnClickListener {
            (activity as? OnDataPass)?.deleteRule(position)
        }
    }

    override fun getItemCount() = dataSet.length()

    interface OnDataPass {
        fun editRule(ruleId: Int)
        fun activateRule(ruleId: Int, active: Boolean)
        fun deleteRule(ruleId: Int)
    }
}