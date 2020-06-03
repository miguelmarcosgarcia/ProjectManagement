package adapters

import android.content.Context
import android.graphics.ColorSpace
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projectmanagement.R
import kotlinx.android.synthetic.main.item_board.view.*
import models.Board
import java.security.AccessControlContext

open class BoardItemsAdapter (private val context: Context,
                              private var list: ArrayList<Board>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.item_board,
                    parent,
                    false))

    }

    //AMOUNT OF LIST ITEMS IN THE DB
    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        if(holder is MyViewHolder){
            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_board_place_holder)
                .into(holder.itemView.iv_board_image)

            holder.itemView.tv_name.text = model.name
            holder.itemView.tv_created_by.text = "Created by: ${model.createdBy}"

            holder.itemView.setOnClickListener{
                if(onClickListener!=null){
                    onClickListener!!.onClick(position, model)
                }
            }
        }
    }
    interface OnClickListener{
        fun onClick(position: Int, model: Board)
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    private  class MyViewHolder(view: View): RecyclerView.ViewHolder(view)
}