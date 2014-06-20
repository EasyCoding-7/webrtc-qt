package com.video.data;


public class Value {
	
	/**
	 * 服务器ID和Port
	 */
//	public final static String BackstageIPPort = "tcp://192.168.60.198:5555";
//	public final static String BackstageIPPort = "tcp://192.168.0.185:5555";
	public final static String BackstageIPPort = "tcp://222.174.213.185:5555";
//	public final static String BackstageIPPort = "tcp://192.168.40.191:5555";
//	public final static String BackstageIPPort = "tcp://218.56.11.182:5555";

	/**
	 * 设备服务器名称
	 */
	public final static String DeviceBackstageName = "Backstage";
	
	/**
	 * 报警服务器名称
	 */
	public final static String AlarmBackstageName = "Alarmstage";
	
	/**
	 * 终端服务初始化值
	 */
	public final static String IceServersValue = "[{\"uri\":\"stun:222.174.213.185:5389\"},{\"uri\":\"turn:222.174.213.185:5766\"}]";
	
	/**
	 * 服务器上比较版本的xml文件地址
	 */
	public final static String ApkXmlPath = "http://222.174.213.185:8088/plugin/VideoMonitorXml.xml";
	
	/**
	 * 服务的包名类名
	 */
	public final static String BackstageServicePackage = "com.video.service.BackstageService";
	
	/**
	 * 搜索本地设备的广播
	 */
	public final static String SEARCH_LOCAL_DEVICE_ACTION = "Value.search_local_device";
	/**
	 * 接收报警消息的广播
	 */
	public final static String BACKSTAGE_MESSAGE_ACTION = "Value.backstage_message";
	/**
	 * 通道是否被打开回调的广播
	 */
	public static final String TUNNEL_REQUEST_ACTION = "Value.Player.TunnelRequest";
	
	/**
	 * 超时时间
	 */
	public final static int REQ_TIME_6S = 6000;
	public final static int REQ_TIME_10S = 10000;
	public final static int REQ_TIME_30S = 30000;
	
	/**
	 * 是否需要请求报警记录的标志
	 */
	public static boolean isLoginSuccess = false;
	
	/**
	 * 是否需要请求终端列表的标志
	 */
	public static boolean isNeedReqTermListFlag = true;
	
	/**
	 * 是否需要请求报警记录的标志
	 */
	public static boolean isNeedReqAlarmListFlag = true;
	
	/**
	 * 请求报警记录的数据
	 */
	public static int requstAlarmCount = 5;
	
	/**
	 * 报警语音是否播放标志
	 */
	public static boolean isPlayMp3 = false;
	
	/**
	 * 是否手动退出软件
	 */
	public static boolean isManulLogout = false;
	
	/**
	 * 心跳失败标志
	 */
	public static boolean beatHeartFailFlag = false;
	
	/**
	 * 通道是否打开的标志
	 */
	public static boolean isTunnelOpened = false;
	
	/**
	 * 是否是共享的用户
	 */
	public static boolean isSharedUser = false;
	
	/**
	 * 实时界面请求报警消息
	 */
	public static boolean ownFragmentRequestAlarmFlag = false;
	
	/**
	 * 初始化应用程序的全局变量
	 */
	public static void resetValues() {
		isLoginSuccess = false;
		isNeedReqTermListFlag = true;
		isNeedReqAlarmListFlag = true;
		requstAlarmCount = 5;
		isPlayMp3 = false;
		isManulLogout = true;
		beatHeartFailFlag = false;
		isTunnelOpened = false;
		isSharedUser = false;
		ownFragmentRequestAlarmFlag = false;
	}
}
