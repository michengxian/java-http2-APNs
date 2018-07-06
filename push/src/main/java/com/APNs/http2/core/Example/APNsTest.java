package com.APNs.http2.core.Example;

import com.APNs.http2.core.error.ErrorDispatcher;
import com.APNs.http2.core.error.ErrorListener;
import com.APNs.http2.core.error.ErrorModel;
import com.APNs.http2.core.manager.ApnsServiceManager;
import com.APNs.http2.core.model.ApnsConfig;
import com.APNs.http2.core.model.Payload;
import com.APNs.http2.core.model.PushNotification;
import com.APNs.http2.core.service.ApnsService;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class APNsTest {

    public static void main (String[] args) throws FileNotFoundException {

        //p12文件的路径
        String certificatePath = "/Users/biostime/Documents/java-http2-APNs/push/src/main/webapp/bcenterProduction.p12";

        String deviceToken = "9043c7ca7e92eb7f99969ae87d0205e71097d25153cb347cba91a7609e659311";

        InputStream inputStream = new FileInputStream(certificatePath);

        ApnsConfig config = new ApnsConfig();
        config.setName("name1");// 推送服务名称
        config.setDevEnv(false);// 是否是开发环境
        config.setKeyStore(inputStream);// 证书
        config.setPassword("123456");// 证书密码
        config.setPoolSize(5);// 线程池大小
        config.setTimeout(3000);// TCP连接超时时间
        config.setTopic("com.biostime.business.center");// 标题,即证书的bundleID

        ApnsService service= ApnsServiceManager.createService(config);

        ErrorDispatcher.getInstance().addListener(new ErrorListener() {
            @Override
            public void handle(ErrorModel errorModel) {
                System.out.print("收到错误监听:" + errorModel);

            }
        });

        Payload payload = new Payload();
        payload.setAlert("你的交流圈话题“交流圈入口没那么深了”被测试账号回复啦");
        payload.setBadge(1); // iphone应用图标上小红圈上的数值
        payload.setSound("default");
        payload.addParam("module","person_msg");
        payload.addParam("sm","BBSN");
        payload.addParam("redirectType","native");
        payload.addParam("title","你的交流圈话题“交流圈入口没那么深了”被测试账号回复啦1234567891134s啊啊时间啊贷记卡理解拉丝打开圣诞节阿拉斯加快点开始阿斯利康简单阿克索德撒娇的垃圾大伞你的交流圈话题“交流圈入口没那么深了”被测试账号回复啦1234567891134s啊啊时间啊贷记卡理解拉丝打开圣诞节阿拉斯加快点开始阿斯利康简单阿克索德撒娇的垃圾大伞你的交流圈话题“交流圈入口没那么深了”被测试账号回复啦1234567891134s啊啊时间啊贷记卡理解拉丝打开圣诞节阿拉斯加快点开始阿斯利康简单阿克索德撒娇的垃圾大伞你的交流圈话题“交流圈入口没那么深了”被测试账号回复啦1234567891134s啊啊时间啊贷记卡理解拉丝打开圣诞节阿拉斯加快点开始阿斯利康简单阿克索德撒娇的垃圾大伞");
        payload.addParam("url","http://test-01.biostime.us/merchant_h5/topicGroup/html5/topicDetail.html?topicId=164");


        PushNotification notification = new PushNotification();
        notification.setPayload(payload);
        notification.setToken(deviceToken);


        service = ApnsServiceManager.getService(config.getName());
        service.sendNotification(notification);// 异步发送
//        boolean result = service.sendNotificationSynch(notification);// 同步发送

    }

}
