import android.util.Log


object LogUtils {
    private lateinit var currentThread: Array<StackTraceElement>
    private var tagName: String? = null
    private var msgT: String? = null
    private var msgC: String? = null
    private var callTraceStack: String? = null
    var curLogLevel: Int = Log.VERBOSE

    @Synchronized
    fun initTrace(tag: String?, msg: String, vararg isPrintStack: Int) {
        val isPrintStackOne = if (isPrintStack.isNotEmpty()) isPrintStack[0] else 10
        currentThread = Thread.currentThread().stackTrace
        var curentIndex = 0
        // vm调用栈中此方法所在index：2：VMStack.java:-2:getThreadStackTrace()<--Thread.java:737:getStackTrace()<--
        for (i in 2..10) {
            if (currentThread[i].className != javaClass.simpleName) {
                curentIndex = i;
                break
            }
        }
        val className = currentThread[curentIndex].fileName
        val endIndex = className.lastIndexOf(".")
        tagName = if (endIndex < 0) className else className.substring(0, endIndex)
        msgT = msg
        msgC = ""
        if (isPrintStackOne > 0) {
            val sb = StringBuilder()
            sb.append("[")
            var i = curentIndex
            while (i < curentIndex + isPrintStackOne && i < currentThread.size) {
                if (i > 0) sb.append("<--")
                sb.append(currentThread[i].fileName).append(":")
                    .append(currentThread[i].lineNumber).append("#")
                    .append(currentThread[i].methodName).append("()")
                i++
            }
            sb.append("]")
            callTraceStack = sb.toString()
            msgC += callTraceStack
        }
    }

    fun e(msg: String, printStack: Boolean) {
        e(msg, if (printStack) 105 else 0)
    }

    fun w(msg: String, printStackNum: Boolean) {
        w(msg, if (printStackNum) 105 else 0)
    }

    fun d(msg: String, printStackNum: Boolean) {
        d(msg, if (printStackNum) 105 else 0)
    }

    fun v(msg: String, printStackNum: Boolean) {
        v(msg, if (printStackNum) 105 else 0)
    }

    fun i(msg: String, printStackNum: Boolean) {
        i(msg, if (printStackNum) 105 else 0)
    }

    fun e(msg: String, vararg printStackNum: Int) {
        e(null, msg, *printStackNum)
    }

    fun w(msg: String, vararg printStackNum: Int) {
        w(null, msg, *printStackNum)
    }

    fun d(msg: String, vararg printStackNum: Int) {
        d(null, msg, *printStackNum)
    }

    fun v(msg: String, vararg printStackNum: Int) {
        v(null, msg, *printStackNum)
    }

    fun i(msg: String, vararg printStackNum: Int) {
        i(null, msg, *printStackNum)
    }

    fun e(tag: String?, msg: String, vararg printStackNum: Int) {
        if (curLogLevel > Log.ERROR) {
            return
        }
        initTrace(tag, msg, if (printStackNum.isNotEmpty()) printStackNum[0] else 0)
        Log.e(tag ?: tagName, msgT + msgC)
    }

    fun w(tag: String?, msg: String, vararg printStackNum: Int) {
        if (curLogLevel > Log.WARN) {
            return
        }
        initTrace(tag, msg, if (printStackNum.isNotEmpty()) printStackNum[0] else 0)
        Log.w(tag ?: tagName, msgT + msgC)
    }

    fun d(tag: String?, msg: String, vararg printStackNum: Int) {
        if (curLogLevel > Log.DEBUG) {
            return
        }
        initTrace(tag, msg, if (printStackNum.isNotEmpty()) printStackNum[0] else 0)
        Log.d(tag ?: tagName, msgT + msgC)
    }

    fun v(tag: String?, msg: String, vararg printStackNum: Int) {
        if (curLogLevel > Log.VERBOSE) {
            return
        }
        initTrace(tag, msg, if (printStackNum.isNotEmpty()) printStackNum[0] else 0)
        Log.v(tag ?: tagName, msgT + msgC)
    }

    fun i(tag: String?, msg: String, vararg printStackNum: Int) {
        if (curLogLevel > Log.INFO) {
            return
        }
        initTrace(tag, msg, if (printStackNum.isNotEmpty()) printStackNum[0] else 0)
        Log.i(tag ?: tagName, msgT + msgC)
    }
}