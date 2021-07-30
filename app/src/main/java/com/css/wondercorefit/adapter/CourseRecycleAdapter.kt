package com.css.wondercorefit.adapter

import com.css.ble.ui.view.BaseBindingAdapter
import com.css.service.data.CourseDate
import com.css.wondercorefit.R
import com.css.wondercorefit.databinding.ItemViewVideoBinding

class CourseRecycleAdapter(mData: List<CourseDate>): BaseBindingAdapter<CourseDate, ItemViewVideoBinding>(mData) {

    private var mItemClickListener: ((CourseDate) -> Unit)? = null
    fun setOnItemClickListener(listener: ((CourseDate) -> Unit)?) {
        mItemClickListener = listener
    }

/*    class VideoHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoTitle: TextView = itemView.findViewById(R.id.video_index)
        val videoPoster: ImageView = itemView.findViewById(R.id.video_poster)

        init {
            itemView.setOnClickListener {
                gotoPlayerActivity()
            }
        }

        @SuppressLint("MissingPermission")
        private fun gotoPlayerActivity() {
            val manager: ConnectivityManager = activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            when {
                manager.activeNetworkInfo != null -> {
                    startIntent()
                }
                else -> {
                    if (toast == null) {
                        toast = Toast.makeText(context, "网络请求失败，请确认网络环境是否正常", Toast.LENGTH_SHORT)
                    }else {
                        toast?.setText("网络请求失败，请确认网络环境是否正常")
                        toast?.duration = Toast.LENGTH_SHORT
                    }
                    toast?.show()
                }
            }
        }

        private fun startIntent(){
            val recyclerIntent = Intent(context, CoursePlayActivity::class.java)
            var bundle = Bundle()
            bundle.putInt("position", bindingAdapterPosition)
            recyclerIntent.putExtras(bundle)
            startActivity(recyclerIntent)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder {
        return VideoHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_view_video,
                parent,
                false
            )
        )
    }

    override fun getItemCount() = CourseViewModel.urls.size

    override fun onBindViewHolder(holder: VideoHolder, position: Int) {
        holder.run {
            videoPoster.setImageResource(CourseViewModel.picture[position])
            videoTitle.text = CourseViewModel.name[position]
        }

    }*/

    override fun getLayoutResId(viewType: Int): Int = R.layout.item_view_video

    override fun onBindItem(binding: ItemViewVideoBinding, item: CourseDate, position: Int) {
        binding.courseData = item
        binding.clCourse.setOnClickListener {
            mItemClickListener?.invoke(item)
        }

        binding.executePendingBindings()
    }
}