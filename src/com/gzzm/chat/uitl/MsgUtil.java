package com.gzzm.chat.uitl;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.gzzm.chat.data.BaseData;
import com.gzzm.chat.data.ServicePushData;
import com.gzzm.chat.data.MsgData;

public class MsgUtil {

	public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
	public static final Type BASE_DATA = new TypeToken<BaseData>() {
	}.getType();
	public static final Type MSG_DATA = new TypeToken<MsgData>() {
	}.getType();
	public static final Type IPD = new TypeToken<ServicePushData>() {
	}.getType();

	/* 获取数据ID 时间戳+两位随机数 */
	public static long getBaseDataId() {
		String ran = String.valueOf((int) ((Math.random() * 100)));
		if (ran.length() <= 1) {
			ran = "0" + ran;
		}
		return Long.valueOf(System.currentTimeMillis() + ran);
	}

	/* 获取基础消息对象 */
	public static BaseData getBaseDataMsg(Integer dataType, String content) {
		return new BaseData(getBaseDataId(), dataType, content);
	}

	/* 获取一条空消息 */
	public static BaseData getEmptyMsg(String msg) {
		return getBaseDataMsg(100000, msg);
	}

	/* 获取一条回复消息 */
	public static BaseData getSendOkMsg(String msg) {
		return getBaseDataMsg(100001, msg);
	}

	/* 获取一条登陆消息 */
	public static BaseData getLoginMsg(String msg) {
		return getBaseDataMsg(100002, msg);
	}

	/* 获取一条推送消息 */
	public static BaseData getPushMsg(String msg) {
		return getBaseDataMsg(100003, msg);
	}

	/* 获取用户列表 */
	public static BaseData getUserList(String msg) {
		return getBaseDataMsg(100004, msg);
	}

	/* 获取聊天记录 */
	public static BaseData getChatRecordMsg(String msg) {
		return getBaseDataMsg(100005, msg);
	}

	/* 获取通道测试消息 */
	public static BaseData getTestConnMsg(String msg) {
		return getBaseDataMsg(100006, msg);
	}

	/* 获取一条聊天消息 */
	public static BaseData getChatMsg(String msg) {
		return getBaseDataMsg(100010, msg);
	}
}
