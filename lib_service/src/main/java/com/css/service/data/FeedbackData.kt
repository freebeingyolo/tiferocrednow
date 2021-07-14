package com.css.service.data

/**
 * 意见反馈
 * {
"feedbackContent": "string",
"feedbackDate": "string",
"feedbackTime": "string",
"id": 0,
"isDel": "string",
"replyAccountId": "string",
"userId": 0
}
 */
data class FeedbackData(
    //反馈内容
    var feedbackContent: String="",
    //问题出现的日期
    var feedbackDate: String="",
    //问题出现的时间
    var feedbackTime: String="",
    //后台id
    var id: Int=-1,
    //后台字段
    var isDel: String="",
    //回复id，如果有值就是服务台回复标识，没有值就是用户反馈标识
    var replyAccountId: String="",
    //用户唯一标识
    var userId: Int=-1,
)