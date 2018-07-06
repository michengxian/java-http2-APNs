# java-http2-APNs
java-http2推送给iPhone


>`APNs`是苹果推送通知服务，在设备与苹果的推送通知服务器会保持常连接状态。当你想发送一个推送通知给某个用户的`iPhone`上的应用程序时，你可以使用`APNs` 发送一个推送消息给目标设备上已安装的某个应用程序。

## 流程
1. 当在`iPhone`上第一次打开某个应用时，`iOS`设备会跟`APNs Service`索要`deviceToken`,
2. 应用程序将上一步得到的`deviceToken`发送给后台，后台保存好当前用户的`deviceToken`
3. 服务端向`APNs Service`发送消息
4. `APNs Service`将消息发送给`iPhone`应用程序

***

## iOS
```
- (void)application:(UIApplication*)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData*)deviceToken
{
    NSString *deviceTokenString = [NSString stringWithFormat:@"%@", deviceToken];
    NSString *string = [deviceTokenString stringByReplacingOccurrencesOfString:@" " withString:@""];
    string = [string stringByReplacingOccurrencesOfString:@"<" withString:@""];
    string = [string stringByReplacingOccurrencesOfString:@">" withString:@""];
    DLog(@"------------------获取deviceToken--------\n%@",string);
}
```

```
- (void)application:(UIApplication*)application didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:(void(^)(UIBackgroundFetchResult))completionHandler
{
    //在这里对收到的消息进行相应的处理
    DLog(@"-------------------iOS7以后，消息推送成功后的回调");
}
```

***

## java 基于二进制

>`pom.xml`加入`maven`依赖

```
<dependency>
  <groupId>org.bouncycastle</groupId>
  <artifactId>bcprov-jdk16</artifactId>
  <version>1.45</version>
</dependency>
<dependency>
  <groupId>commons-io</groupId>
  <artifactId>commons-io</artifactId>
  <version>2.0.1</version>
</dependency>
<dependency>
  <groupId>commons-lang</groupId>
  <artifactId>commons-lang</artifactId>
  <version>2.5</version>
</dependency>
<dependency>
  <groupId>org.apache.logging.log4j</groupId>
  <artifactId>log4j-slf4j-impl</artifactId>
  <scope>runtime</scope>
</dependency>
<dependency>
  <groupId>com.github.fernandospr</groupId>
  <artifactId>javapns-jdk16</artifactId>
  <version>2.2.1</version>
</dependency>
```

```
import javapns.devices.Device;
import javapns.devices.implementations.basic.BasicDevice;
import javapns.notification.AppleNotificationServerBasicImpl;
import javapns.notification.PushNotificationManager;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushedNotification;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ApnsSend {

    public static void main(String[] args) throws Exception
    {
        //生产
        Boolean isProduction=true;


        String deviceToken;
        String alert;//push的内容
        int badge ;//图标小红圈的数值
        String sound;////铃音
        if (isProduction){//生产
            deviceToken = "6541acccc39dc8ebe810fe844286684877ea2844c927acfb931507b52d61d966";//生产
            alert = "生产证书推送";//push的内容
            badge = 10;
            sound = "default";
        }
        else {//测试
            deviceToken = "e90440a91269e2a409f898f4176460ab693d8799d6f3deab822c20125e9e8286";//生产
            alert = "测试证书推送";//push的内容
            badge = 10;
            sound = "default";
        }


        List<String> tokens = new ArrayList<String>();
        tokens.add(deviceToken);
        String certificatePath = "/Users/biostime/Desktop/tuisong/push1.p12";
        String certificatePassword = "123456";//此处注意导出的证书密码不能为空因为空密码会报错
        boolean sendCount = true;

        try
        {
            PushNotificationPayload payLoad = new PushNotificationPayload();
            payLoad.addAlert(alert); // 消息内容
            payLoad.addBadge(badge); // iphone应用图标上小红圈上的数值
            if (!StringUtils.isBlank(sound))
            {
                payLoad.addSound(sound);//铃音
            }
            PushNotificationManager pushManager = new PushNotificationManager();
            //true：表示的是产品发布推送服务 false：表示的是产品测试推送服务
            pushManager.initializeConnection(new AppleNotificationServerBasicImpl(certificatePath, certificatePassword, isProduction));
            List<PushedNotification> notifications = new ArrayList<PushedNotification>();
            // 发送push消息
            if (sendCount)
            {
                Device device = new BasicDevice();
                device.setToken(tokens.get(0));
                PushedNotification notification = pushManager.sendNotification(device, payLoad, true);
                notifications.add(notification);
            }
            else
            {
                List<Device> device = new ArrayList<Device>();
                for (String token : tokens)
                {
                    device.add(new BasicDevice(token));
                }
                notifications = pushManager.sendNotifications(payLoad, device);
            }
            List<PushedNotification> failedNotifications = PushedNotification.findFailedNotifications(notifications);
            List<PushedNotification> successfulNotifications = PushedNotification.findSuccessfulNotifications(notifications);
            int failed = failedNotifications.size();
            int successful = successfulNotifications.size();

            System.out.print("failed:"+failed+"\n"+"success:"+successful);
            pushManager.stopConnection();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
```
不过，以上方法会存在一个问题，你会发现，当你发送的内容大于256字节的时候，会发现推送失败，所以就有了下面的方法

***

## 基于`HTTP/2`

见`git`地址

[java-http2-APNs](https://github.com/michengxian/java-http2-APNs)

在`com.APNs.http2.core.Example`文件中

```
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
        payload.setAlert("通知");
        payload.setBadge(1); // iphone应用图标上小红圈上的数值
        payload.setSound("default");
        payload.addParam("module","person_msg");
        payload.addParam("sm","BBSN");
        payload.addParam("redirectType","native");
        payload.addParam("title","你的交流圈话题");
        payload.addParam("url","http://test-01.biostime.us/merchant_h5/topicGroup/html5/topicDetail.html?topicId=164");

       PushNotification notification = new PushNotification();
        notification.setPayload(payload);
        notification.setToken(deviceToken);

       service = ApnsServiceManager.getService(config.getName());
        service.sendNotification(notification);// 异步发送
//        boolean result = service.sendNotificationSynch(notification);// 同步发送
    }
```

用该方法，可大大的增加消息内容的长度。4K大小，基本上够用了。


[博客地址](https://github.com/michengxian/java-http2-APNs/tree/master)


