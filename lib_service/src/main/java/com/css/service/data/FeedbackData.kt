package com.css.service.data

/**
 * 意见反馈
 *
{反馈记录
"id": 1,
"phone": "15011831642",
"feedbackContent": "我反馈了一条内容啊",
"feedbackId": 0,
"feedbackTime": null,
"feedbackUserId": 0,
"replyContent": null,
"replyUserId": 0,
"replyTime": null,
"feedbackStatus": "已反馈",
"feedbackDate": "2021-07-14 10:17"
}
 */
data class FeedbackData(
    //用户唯一标识
    var feedbackUserId: Int = 0,
    //意见反馈id（默认提交不需要传id,针对已反馈问题时，需要传id）
    var feedbackId: Int = 0,
    //报障人的手机号
    var phone: String = "",
    //反馈内容
    var feedbackContent: String = "",
    //问题出现的时间yyyy-MM-dd HH:mm:ss，（提交时候不需要）
    var feedbackDate: String = "",
//    //后台id
    var id: Int=-1,
//    //后台字段
//    var isDel: String="",
    //回复状态：已反馈/收到回复（提交时候不需要）
    var feedbackStatus: String = "",

    )