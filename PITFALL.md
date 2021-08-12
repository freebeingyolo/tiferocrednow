# 技术点

1. 自定义属性	

   - 如何自定义属性
     attrs.xml  --> 自定义View中解析（Typed）
     fraction：百分数

     属性定义时可以指定多种类型值

     

   - 自定义属性需要解析多个，比如color既支持"#ff0000"又支持"@color/red"呢？

     

# 注意点

后端这边上传限制文件格式，还有大小是300m





# 遗留问题

1. 查看App使用的资源，删除无效资源(20210811)
2. App内存泄漏检测、CPU、内存、耗电量、流量使用统计(20210811)
3. Activity嵌套Fragment，会出现最上面的布局与状态栏重叠问题（数据统计页面）  (20210811)



# 坑

1. LiveData的Transformations.map()引起的LiveData若没被观察，那他的值不会更新

   ```
   val stateObsrv: LiveData<State> by lazy { BusMutableLiveData(State.disconnected) }
   val connectStateTxt = Transformations.map(stateObsrv) {}
   
   var state: State
   set(value) {
   	(stateObsrv as MutableLiveData).value = value //如果connectStateTxt没有被订阅，那么connectStateTxt的值不会被修改
   	BondDeviceData.getDeviceStateLiveData().value = Pair(deviceType.alias, connectStateTxt.value)//此处connectStateTxt.value永远为null
   }
   
   原因：
   Transformations.map()的LiveData只有在Active的时候才会被赋值
   ```

   

2. HTTP请求参数带有汉字

   

3. 自定义属性没有recycle()

   ```
   try {
       for (i in 0 until ta.indexCount) {
           when (val id = ta.getIndex(i)) {
               R.styleable.ElectricityView_progress -> {
                   val p = ta.getInteger(id, 0)
                   setProgress(p)
               }
               R.styleable.ElectricityView_lowPowerColor -> {
   
               }
           }
       }
   } finally {
       ta.recycle()
   }
   ```

4. //解决Android低版本，对list排序引起的错误

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

  

  

