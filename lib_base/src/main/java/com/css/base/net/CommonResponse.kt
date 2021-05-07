package com.css.base.net

import com.css.base.keep.KeepClass

/**
 * @author Ruis
 * @date 2021/5/6
 */
class CommonResponse<T> : KeepClass {
    var status: Int = 0
    var message: String? = null
    var data: T? = null
}