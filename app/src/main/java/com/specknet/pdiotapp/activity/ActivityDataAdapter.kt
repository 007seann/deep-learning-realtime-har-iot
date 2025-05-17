package com.specknet.pdiotapp.activity

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.specknet.pdiotapp.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ActivityDataAdapter(private var activityDataList: Array<ActivityData>) : RecyclerView.Adapter<ActivityDataAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateView: TextView
        val activityView: TextView
        init {
            dateView = itemView.findViewById(R.id.date_view)
            activityView = itemView.findViewById(R.id.activity_view)
        }
    }

    class ActivityDiffCallBack(
        private val oldList: Array<ActivityData>,
        private val newList: Array<ActivityData>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList[oldItemPosition].date == newList[newItemPosition].date

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList[oldItemPosition] == newList[newItemPosition]
    }

    fun updateData(newData: Array<ActivityData>) {
        val diffResult = DiffUtil.calculateDiff(ActivityDiffCallBack(activityDataList, newData))
        activityDataList = newData
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = activityDataList[position]
        holder.dateView.text = formatDate(item.date)
        if (item.subActivity == "") holder.activityView.text = item.activity
        else holder.activityView.text = "${item.activity}, ${item.subActivity}"
    }

    override fun getItemCount(): Int {
        return activityDataList.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun formatDate(date: LocalDateTime): String {
        val format = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
        return format.format(date)
    }
}