package com.css.ble.ui.view//package com.css.base.uibase

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView


open class SimpleRecyclerViewAdapter<T>(
    private var itemDatas: List<T>?,
    private val defaultLayout: Int,
    private val brId: Int
) : RecyclerView.Adapter<SimpleRecyclerViewAdapter.BaseViewHolder?>() {

    open fun getItemLayout(itemData: T): Int {
        return defaultLayout
    }

    open fun addListener(root: View?, itemData: T, position: Int) {}

    open fun onItemDatasChanged(newItemDatas: List<T>?) {
        itemDatas = newItemDatas
        notifyDataSetChanged()
    }

    protected open fun onItemRangeChanged(newItemDatas: List<T>?, positionStart: Int, itemCount: Int) {
        itemDatas = newItemDatas
        notifyItemRangeChanged(positionStart, itemCount)
    }

    protected open fun onItemRangeInserted(newItemDatas: List<T>?, positionStart: Int, itemCount: Int) {
        itemDatas = newItemDatas
        notifyItemRangeInserted(positionStart, itemCount)
    }

    protected open fun onItemRangeRemoved(newItemDatas: List<T>?, positionStart: Int, itemCount: Int) {
        itemDatas = newItemDatas
        notifyItemRangeRemoved(positionStart, itemCount)
    }

    override fun getItemViewType(position: Int): Int {
        return getItemLayout(itemDatas!![position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding =
            DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), viewType, parent, false)
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.binding.setVariable(brId, itemDatas!![position])
        holder.binding.executePendingBindings()
    }

    override fun getItemCount(): Int = itemDatas?.size ?: 0

    class BaseViewHolder(var binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)

    companion object{
        fun Demo() {
//    val adapter: SimpleRecyclerViewAdapter<MailType> = object :
//        SimpleRecyclerViewAdapter<MailType?>(MailTypeModel.getInstance().getDatas(), R.layout.item_mail_type, BR.mailType) {
//        fun addListener(root: View, itemData: MailType?, position: Int) {
//            root.findViewById<View>(R.id.textView).setOnClickListener {
//                Toast.makeText(
//                    getActivity(),
//                    "textView clicked!",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//            root.findViewById<View>(R.id.imageView).setOnLongClickListener {
//                Toast.makeText(getActivity(), "imageView long clicked!", Toast.LENGTH_SHORT).show()
//                true
//            }
//        }
//    }
        }
    }

}

