package com.intention.android.goodmask.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.intention.android.goodmask.R
import com.intention.android.goodmask.activity.MainActivity
import com.intention.android.goodmask.fragment.HomeFragment
import com.intention.android.goodmask.model.NoticeData

class NotificationAdapter(
    private val context: Context,
    var datas: MutableList<NoticeData>,
    var activity: FragmentActivity?
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        public val title: TextView = itemView.findViewById(R.id.title)
        public val content: TextView = itemView.findViewById(R.id.content)
        public val time : TextView = itemView.findViewById(R.id.time)
        public val canvas : ConstraintLayout = itemView.findViewById(R.id.canvas)

        fun bind(item: NoticeData, context: Context, position: Int) {
            title.text = item.title
            content.text = item.content
            time.text = item.time.toString()
            item.read = true

            itemView.setOnClickListener {
                // Room 통해서, 로그 파일 데이터 read 속성 수정
                canvas.background = ContextCompat.getDrawable(context, R.drawable.selector_bg)

                val main = activity as MainActivity
                main.binding.bnvMain.selectedItemId = R.id.frag_homeground
                main.replaceFragment(HomeFragment())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.notification_item_layout,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position],context, position)
    }

    override fun getItemCount(): Int = datas.size
}