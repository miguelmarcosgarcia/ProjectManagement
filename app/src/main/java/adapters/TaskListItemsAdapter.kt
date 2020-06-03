package adapters

import activities.TaskListActivity
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.projectmanagement.R
import kotlinx.android.synthetic.main.item_task.view.*
import models.Task

open class TaskListItemsAdapter(
    private val context: Context,
    private var list: List<Task>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false)

        //setting viewholder to 70% of width of the screen and the necessary height
        val layoutParams = LinearLayout.LayoutParams(
            (parent.width * 0.7).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT
        )

        layoutParams.setMargins(
            15.toDp().toPx(), 0, (40.toDp()).toPx(), 0)

        view.layoutParams = layoutParams

        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder,
                                  position: Int) {

        //in this case the model is the task
        val model = list[position]
        //ONLY SHOW ADD LIST BUTTON IF THERE'S NO LIST
        if(holder is MyViewHolder){
            if(position.equals(list.size -1 )){
                holder.itemView.tv_add_task_list.visibility=View.VISIBLE
                holder.itemView.ll_task_item.visibility = View.GONE
            }
            else{
                holder.itemView.tv_add_task_list.visibility=View.GONE
                holder.itemView.ll_task_item.visibility = View.VISIBLE
            }

            holder.itemView.tv_task_list_title.text = model.title
            holder.itemView.tv_add_task_list.setOnClickListener{

                holder.itemView.tv_add_task_list.visibility=View.GONE
                holder.itemView.cv_add_task_list_name.visibility = View.VISIBLE

            }
            holder.itemView.ib_close_list_name.setOnClickListener{
                holder.itemView.tv_add_task_list.visibility=View.VISIBLE
                holder.itemView.cv_add_task_list_name.visibility = View.GONE

            }
            holder.itemView.ib_done_list_name.setOnClickListener{
                val listName = holder.itemView.et_task_list_name.text.toString()

                if(listName.isNotEmpty()){
                    if(context is TaskListActivity){
                        context.createTaskList(listName)
                    }
                }else{
                    Toast.makeText(context, "Please enter a List name", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //GET THE DENSITY OF THE SCREEN AND CONVERT IT INTO AN INT SO THAT WE CAN ADJUST THE WIDTH
    //OF THE APP TO SET THE WIDTH OF THE RECYCLERVIEW
    private fun Int.toDp():Int =
        (this/ Resources.getSystem().displayMetrics.density).toInt()

    //GETS PIXELS FROM DENSITY PIXEL
    private fun Int.toPx():Int =
        (this * Resources.getSystem().displayMetrics.density).toInt()

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view)

}