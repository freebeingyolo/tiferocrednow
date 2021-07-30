1. //解决Android低版本，对list排序引起的错误

  ```
  val query: List<String> = request.url.query!!.split("&")
  Collections.sort(query) { str1, str2 -> // 按首字母升序排
   str2.compareTo(str1)
  }
  ```

  抛出异常消息

  ```
  Process: com.css.ble, PID: 11046
  java.lang.UnsupportedOperationException
  at java.util.AbstractList.set(AbstractList.java:681)
  at java.util.AbstractList$FullListIterator.set(AbstractList.java:143)
  at java.util.Collections.sort(Collections.java:1909)
  at com.css.base.net.interceptor.HeaderInterceptor.signatures(HeaderInterceptor.kt:69)
  ```

  原因：split返回的是不可变的list，对list进行sort操作，在低版本的Android系统会抛出UnsupportedOperationException

  解决：

  ```
  val query: List<String> = ArrayList(request.url.query!!.split("&"))
  ```

  

  