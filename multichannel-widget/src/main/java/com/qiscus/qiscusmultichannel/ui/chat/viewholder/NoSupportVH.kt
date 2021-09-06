package com.qiscus.qiscusmultichannel.ui.chat.viewholder

import android.view.View
import com.qiscus.qiscusmultichannel.QiscusMultichannelWidgetColor
import com.qiscus.qiscusmultichannel.QiscusMultichannelWidgetConfig
import com.qiscus.qiscusmultichannel.R
import com.qiscus.sdk.chat.core.data.model.QMessage
import kotlinx.android.synthetic.main.item_message_not_supported_mc.view.*

/**
 * Created on : 17/02/20
 * Author     : arioki
 * Name       : Yoga Setiawan
 * GitHub     : https://github.com/arioki
 */

class NoSupportVH(
    itemView: View,
    config: QiscusMultichannelWidgetConfig,
    color: QiscusMultichannelWidgetColor
) : BaseViewHolder(itemView, config, color) {

    override fun bind(comment: QMessage) {
        super.bind(comment)
        itemView.tv_chat.text = itemView.context.getString(R.string.qiscus_type_not_support_mc)
    }

}