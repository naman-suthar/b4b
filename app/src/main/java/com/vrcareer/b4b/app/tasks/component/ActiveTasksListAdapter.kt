package com.vrcareer.b4b.app.tasks.component

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vrcareer.b4b.R
import com.vrcareer.b4b.model.TaskItem
import com.vrcareer.b4b.utils.TaskEarningType

class ActiveTasksListAdapter(context: Context, var activeTasksList:List<TaskItem>, val onEarnButtonClicked: (TaskItem) -> Unit)
    :RecyclerView.Adapter<ActiveTasksListAdapter.TaskItemViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskItemViewHolder {
       val view = LayoutInflater.from(parent.context).inflate(R.layout.single_active_job_list_item,parent,false)
        return TaskItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskItemViewHolder, position: Int) {
        val currTask = activeTasksList[position]
        holder.taskTitle.text = currTask.task_title
        holder.taskTagline.text = currTask.task_tagline
        holder.taskPriceTagline.text = currTask.price_tagline ?: "Price tagline here"
      /*  if (currTask.price_type == TaskEarningType.Percentage.type){
            holder.tvEarnMoney.text = "${currTask.task_earning_price}%"
        }else{
            holder.tvEarnMoney.text = "\u20B9 ${currTask.task_earning_price}"
        }*/

        holder.btnEarn.setOnClickListener {
            onEarnButtonClicked(currTask)
        }
    }

    override fun getItemCount(): Int = activeTasksList.size

    class TaskItemViewHolder(view: View): RecyclerView.ViewHolder(view){
        val imgLog: ImageView = view.findViewById(R.id.img_logo_active_job)
        val taskTitle: TextView = view.findViewById(R.id.tv_active_job_title)
        val taskTagline: TextView = view.findViewById(R.id.tv_active_job_task_tagline)
//        val tvEarnMoney: TextView = view.findViewById(R.id.tv_earn_from_task)
        val btnEarn: Button = view.findViewById(R.id.btn_earn_task)
        val taskPriceTagline: TextView = view.findViewById(R.id.tv_active_job_task_price_tagline)
    }
}