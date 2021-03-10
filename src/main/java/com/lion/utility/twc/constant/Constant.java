package com.lion.utility.twc.constant;

import io.netty.util.AttributeKey;

/**
 * 常量
 * 
 * @author lion
 */
public class Constant {
	/**
	 * 编码
	 */
	public final static String ENCODING = "UTF-8";
	/**
	 * 加密模式
	 */
	public final static String SENCRYPT_MODE = "AES/CBC/PKCS5Padding";
	/**
	 * 数据传输加密iv
	 */
	public final static String SENCRYPT_IV = "669.a()_$%^12ASe";
	/**
	 * twc重连时间间隔，单位：秒
	 */
	public final static int TWC_RETRY_SECOND = 3;
	/**
	 * twc链接超时默认秒数
	 */
	public final static int TWC_CONNECTTIMEOUT_SECOND_DEFAULT = 3;
	/**
	 * twc连接超时检测时间间隔默认秒数
	 */
	public final static int TWC_CONNECTTIMEOUT_CHECK_INTERVALSECOND_DEFAULT = 60;
	/**
	 * twc连接超时次数默认阈值
	 */
	public final static int TWC_CONNECTTIMEOUT_THRESHOLD_DEFAULT = 3;
	/**
	 * twc连接超时，达到阈值，禁止请求的时间间隔默认秒数
	 */
	public final static int TWC_CONNECTTIMEOUT_FORBID_INTERVALSECOND_DEFAULT = 300;
	/**
	 * twc读取超时默认秒数
	 */
	public final static int TWC_READTIMEOUT_SECOND_DEFAULT = 10;
	/**
	 * twc最大接收消息默认字节数
	 */
	public final static int TWC_MESSAGERECIEVE_MAXLENGTH_DEFAULT = 5 * 1024 * 1024;
	/**
	 * twc加密默认秘钥（必须16位）
	 */
	public final static String TWC_ENCRYPTKEY_DEFAULT = "2000lion8000king";
	/**
	 * 当输出内容大于等于此值时进行压缩，默认值
	 */
	public final static int TWC_COMPRESSMINLENGTH_DEFAULT = 1024;
	/**
	 * twc客户端集群容错-失败自动切换尝试次数（不包含首次），默认值
	 */
	public final static int TWC_FAILOVERRETRIES_DEFAULT = 2;

	/**
	 * twc业务线程池-默认线程个数
	 */
	public final static int TWC_BIZTHREAD_TOTAL_DEFAULT = 16;

	/**
	 * 客户端默认io线程数
	 */
	public final static int TWC_CLIENT_IOTHREADS_DEFAULT = 4;
	/**
	 * 客户端默认连接空闲秒数
	 */
	public final static int TWC_CLIENT_IDLETIMESECONDS_DEFAULT = 60;
	/**
	 * 服务端默认Acceptor线程数
	 */
	public final static int TWC_SERVER_ACCEPTORTHREADS_DEFAULT = 2;
	/**
	 * 服务端默认io线程数
	 */
	public final static int TWC_SERVER_IOTHREADS_DEFAULT = 4;

	/**
	 * twc集群服务注册发现zk命名空间
	 */
	public final static String TWC_REGISTER_ZK_NAMESPACE = "TWCServerDiscovery";
	/**
	 * twc集群服务注册发现zk基础路径--server（用于服务发现）
	 */
	public final static String TWC_SERVER_REGISTER_ZK_BASEPATH = "/Discovery_Server";
	/**
	 * twc集群服务注册发现zk基础路径--client（用于记录有多少client进行连接）
	 */
	public final static String TWC_CLIENT_REGISTER_ZK_BASEPATH = "/Discovery_Client";
	/**
	 * zk重试策略-等待重试间隔（毫秒）
	 */
	public final static int ZK_RETRYPOLICY_BASESLEEPTIMEMS = 1000;
	/**
	 * zk重试策略-最大重试次数
	 */
	public final static int ZK_RETRYPOLICY_MAXRETRIES = 3;

	/**
	 * twc connect 标识（netty属性）
	 */
	public final static AttributeKey<String> TWC_CLIENT_CONNECT_ID = AttributeKey.valueOf("TWCConnectID");

	/**
	 * 日志输出等级-输入，输出，异常
	 */
	public final static int LOGLEVEL_INOUTERROR = 1;
	/**
	 * 日志输出等级-输入，异常
	 */
	public final static int LOGLEVEL_INERROR = 2;
	/**
	 * 日志输出等级-异常
	 */
	public final static int LOGLEVEL_ERROR = 3;

	/**
	 * 心跳方法标识
	 */
	public final static String METHODID_HEARTBEAT = "twc_heartbeat";
	/**
	 * 客户端/管理端注册方法标识
	 */
	public final static String METHODID_REGISTER = "twc_register";
	/**
	 * 获取客户端标识列表标识
	 */
	public final static String METHODID_GETCLIENTIDS = "twc_getClientIds";

	/**
	 * 消息类型-请求
	 */
	public final static int MESSAGE_TYPE_REQUEST = 1;
	/**
	 * 消息类型-响应
	 */
	public final static int MESSAGE_TYPE_RESPONSE = 2;
	/**
	 * 消息类型-管理端请求
	 */
	public final static int MESSAGE_TYPE_MANAGEMENTREQUEST = 11;

	/**
	 * 管理端请求类型-所有client
	 */
	public final static int MANAGEREQUEST_TYPE_ALLCLIENTID = 101;
	/**
	 * 管理端请求类型-指定client列表
	 */
	public final static int MANAGEREQUEST_TYPE_CLIENTIDS = 102;

	/**
	 * 响应类型-单个执行
	 */
	public final static int RESPONSE_TYPE_SINGLE = 1;
	/**
	 * 响应类型-批量执行
	 */
	public final static int RESPONSE_TYPE_BATCH = 2;
}
