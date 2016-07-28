package com.gzzm.chat.data;

import java.util.List;

/**
 * 服务器对外推送接口数据模型
 */
public class ServicePushData {
	/* 要接收推送的用户ID集合 */
	public List<Users> users;
	/* 要推送的消息 */
	public PushMsg pushMsg;
}
