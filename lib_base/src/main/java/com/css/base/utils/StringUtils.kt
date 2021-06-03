package com.css.base.utils

import android.content.Context
import android.text.InputFilter
import android.text.Spanned
import android.widget.EditText
import android.widget.Toast
import java.util.regex.Matcher
import java.util.regex.Pattern

class StringUtils {
    companion object {
        fun lengthFilter(
            context: Context?, editText: EditText,
            max_length: Int, err_msg: String?
        ) {
            val filters: Array<InputFilter?> = arrayOfNulls<InputFilter>(2)
            filters[0] = object : InputFilter.LengthFilter(max_length) {
                override fun filter(
                    source: CharSequence, start: Int, end: Int,
                    dest: Spanned, dstart: Int, dend: Int
                ): CharSequence? {
                    //获取字符个数(一个中文算2个字符)
                    val destLen = getCharacterNum(dest.toString())
                    val sourceLen = getCharacterNum(source.toString())
                    if (destLen + sourceLen > max_length) {
                        Toast.makeText(context, err_msg, Toast.LENGTH_SHORT).show()
                        return ""
                    }
                    return source
                }
            }
            filters[1] = InputFilter { source, start, end, dest, dstart, dend ->
                val speChat =
                    "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？'\'-_\"]"
                val pattern: Pattern = Pattern.compile(speChat)
                val matcher: Matcher = pattern.matcher(source.toString())
                if (matcher.find()) "" else null
            }
            editText.filters = filters
        }

        /**
         *
         * @param content
         * @return
         */
        fun getCharacterNum(content: String?): Int {
            return if (content == "" || null == content) {
                0
            } else {
                content.length + getChineseNum(content)
            }
        }

        /**
         *
         * @param content
         * @return
         */
        fun getCheckSymbol(content: String): Boolean {
            val speChat = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！￥……&*（）——+|{}【】‘；：”“’。，、？'\'-_\"]"
            val pattern: Pattern = Pattern.compile(speChat)
            val matcher: Matcher = pattern.matcher(content)
            return matcher.find()
        }

        /**
         *
         * @param content
         * @return
         */
        fun getCheckSymbol2(content: String): Boolean {
            val speChat1 = "[a-zA-Z]"
            val speChat2 = "[\u4e00-\u9fa5]"
            return Pattern.compile(speChat1).matcher(content).find() && Pattern.compile(speChat2)
                .matcher(content).find()

        }

        /**
         * 计算字符串的中文长度
         * @param s
         * @return
         */
        fun getChineseNum(s: String): Int {
            var num = 0
            val myChar = s.toCharArray()
            for (i in myChar.indices) {
                if (myChar[i].toByte().toChar() != myChar[i]) {
                    num++
                }
            }
            return num
        }
    }
}