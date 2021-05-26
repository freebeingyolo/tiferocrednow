package com.css.service.bus

/**
 * @author huwencheng
 * @date 2020/9/23
 */
class EventMessage<T> {

    var message: Int = 0
    var t: T? = null

    constructor(message: Int, t: T?) {
        this.message = message
        this.t = t
    }

    constructor(message: Int) {
        this.message = message
    }

    /*code对应数字直接往上叠加，不要跳跃*/
    object Code {
        const val MAIN_INDEX_BACK = 1000
    }

}