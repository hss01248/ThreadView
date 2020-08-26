# Android线程治理

# 内存占用评估

> 结论: 对内存的影响不大

```java
private void createThreads() {
    //内存占用只增大了1M不到
    for (int i = 0; i < 100; i++) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"memory-"+i).start();
    }
}
```

初始

![WeChatWorkScreenshot_1ee8229a-3f1d-48aa-bd75-0ada7196b9dd](http://hss01248.tech/uPic/2020-08-26-09-52-52-WeChatWorkScreenshot_1ee8229a-3f1d-48aa-bd75-0ada7196b9dd.png)

创建100个线程后

![WeChatWorkScreenshot_7bca3096-9f33-4e61-895e-db61f1c310bb](http://hss01248.tech/uPic/2020-08-26-09-53-09-WeChatWorkScreenshot_7bca3096-9f33-4e61-895e-db61f1c310bb.png)

再创建100个线程后

![WeChatWorkScreenshot_2cf5e9ec-1c1c-4663-9fe9-c96921272b0c](http://hss01248.tech/uPic/2020-08-26-09-53-20-WeChatWorkScreenshot_2cf5e9ec-1c1c-4663-9fe9-c96921272b0c.png)

# 线程可视化:

![image-20200826101126954](http://hss01248.tech/uPic/2020-08-26-10-11-27-image-20200826101126954.png)

![image-20200826101150054](http://hss01248.tech/uPic/2020-08-26-10-11-50-image-20200826101150054.png)