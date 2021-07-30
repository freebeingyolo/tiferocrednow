package com.css.wondercorefit.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.css.base.uibase.BaseFragment
import com.css.service.data.CourseDate
import com.css.service.utils.SystemBarHelper
import com.css.wondercorefit.adapter.CourseRecycleAdapter
import com.css.wondercorefit.databinding.FragmentCourseBinding
import com.css.wondercorefit.viewmodel.CourseViewModel


class CourseFragment : BaseFragment<CourseViewModel, FragmentCourseBinding>() {
    private val TAG = "CourseFragment"
    private var toast: Toast? = null
    var mData = ArrayList<CourseDate>()
    lateinit var mAdapter: CourseRecycleAdapter
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        SystemBarHelper.immersiveStatusBar(activity, 0f)
        SystemBarHelper.setHeightAndPadding(activity, mViewBinding?.topView)
        mAdapter = CourseRecycleAdapter(mData)
        mViewBinding?.courseRecycle?.layoutManager = GridLayoutManager(activity, 2)
        mViewBinding?.courseRecycle?.adapter = mAdapter
        mAdapter.setOnItemClickListener {
            // 点击item
//            openUrl(it.mallLink)
        }
        initRecycle()
    }

    private fun initRecycle() {
        mViewModel.getCourseInfo()
    }

    override fun registorUIChangeLiveDataCallBack() {
        super.registorUIChangeLiveDataCallBack()
        mViewModel.courseData.observe(viewLifecycleOwner, {
            mData.addAll(it)
            mAdapter.setItems(mData)
        })
    }

    override fun initViewBinding(
        inflater: LayoutInflater,
        viewGroup: ViewGroup?
    ): FragmentCourseBinding = FragmentCourseBinding.inflate(inflater, viewGroup, false)

    override fun initViewModel(): CourseViewModel =
        ViewModelProvider(this).get(CourseViewModel::class.java)
//    inner class RecycleAdapter : RecyclerView.Adapter<RecycleAdapter.VideoHolder>() {
//
//        inner class VideoHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//            val videoTitle: TextView = itemView.findViewById(R.id.video_index)
//            val videoPoster: ImageView = itemView.findViewById(R.id.video_poster)
//
//            init {
//                itemView.setOnClickListener {
//                    gotoPlayerActivity()
//                }
//            }
//
//            @SuppressLint("MissingPermission")
//            private fun gotoPlayerActivity() {
//                val manager: ConnectivityManager = activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//                when {
//                    manager.activeNetworkInfo != null -> {
//                        startIntent()
//                    }
//                    else -> {
//                        if (toast == null) {
//                            toast = Toast.makeText(context, "网络请求失败，请确认网络环境是否正常", Toast.LENGTH_SHORT)
//                        }else {
//                            toast?.setText("网络请求失败，请确认网络环境是否正常")
//                            toast?.duration = Toast.LENGTH_SHORT
//                        }
//                        toast?.show()
//                    }
//                }
//            }
//
//            private fun startIntent(){
//                val recyclerIntent = Intent(context, CoursePlayActivity::class.java)
//                var bundle = Bundle()
//                bundle.putInt("position", bindingAdapterPosition)
//                recyclerIntent.putExtras(bundle)
//                startActivity(recyclerIntent)
//            }
//
//        }
//
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder {
//            return VideoHolder(
//                LayoutInflater.from(parent.context).inflate(
//                    R.layout.item_view_video,
//                    parent,
//                    false
//                )
//            )
//        }
//
//        override fun getItemCount() = CourseViewModel.urls.size
//
//        override fun onBindViewHolder(holder: VideoHolder, position: Int) {
//            holder.run {
//                videoPoster.setImageResource(CourseViewModel.picture[position])
//                videoTitle.text = CourseViewModel.name[position]
//            }
//
//        }
//    }
}