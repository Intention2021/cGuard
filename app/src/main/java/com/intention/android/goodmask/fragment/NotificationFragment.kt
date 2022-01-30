package com.intention.android.goodmask.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.intention.android.goodmask.adapter.NotificationAdapter
import com.intention.android.goodmask.databinding.FragNotificationBinding
import com.intention.android.goodmask.model.NoticeData

class NotificationFragment : Fragment() {
    private var _binding: FragNotificationBinding? = null
    private val binding get() = _binding!!
    lateinit var notificationList : RecyclerView
    lateinit var notificationAdapter: NotificationAdapter

    val simpleCallback  = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT){
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            var position = viewHolder.adapterPosition
            binding.notificationList.adapter?.notifyItemRemoved(position)
            notificationData.removeAt(position)
        }

    }

    val itemTouchHelper = ItemTouchHelper(simpleCallback)

    // Room 로그 데이터 내 저장
    public var notificationData = mutableListOf<NoticeData>(
        NoticeData(title = "경고", "미세먼지 농도가 매우 나쁨으로 감지되었습니다. 팬 속도를 조절하시려면 메시지를 클릭해주십시오.", null, false),
        NoticeData(title = "경고", content = "미세먼지 농도가 나쁨으로 감지되었습니다. 팬 속도를 조절하시려면 메시지를 클릭해주십시오.", time = null, read = false),
        NoticeData(title = "경고", content = "현 위치의 미세먼지 농도가 나쁨으로 감지되었습니다. 팬 속도를 조절하시려면 메시지를 클릭해주십시오.", time = null, read = false)
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragNotificationBinding.inflate(inflater, container, false)
        val view = binding.root

        notificationList = binding.notificationList
        itemTouchHelper.attachToRecyclerView(notificationList)
        notificationRecycler(binding.listCover)

        return view
    }

    public fun notificationRecycler(view: LinearLayout) {
        notificationAdapter = NotificationAdapter(view.context, notificationData, activity)
        notificationList.adapter = notificationAdapter

        notificationAdapter.datas = notificationData
        notificationAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}