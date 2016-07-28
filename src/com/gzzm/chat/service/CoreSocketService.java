package com.gzzm.chat.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.gzzm.chat.Main;
import com.gzzm.chat.data.BaseData;
import com.gzzm.chat.data.MsgData;
import com.gzzm.chat.data.PushMsg;
import com.gzzm.chat.data.PushUser;
import com.gzzm.chat.data.ServicePushData;
import com.gzzm.chat.data.Users;
import com.gzzm.chat.uitl.ConfigUtil;
import com.gzzm.chat.uitl.MsgUtil;
import com.gzzm.chat.uitl.StrUtil;

/**
 * 处理Socket连接的对象
 */
public class CoreSocketService extends Thread {

	/* 限制线程数量 */
	private final ExecutorService exeSer = Executors.newFixedThreadPool(5);
	/* 连接对象 */
	private volatile Socket socket = null;
	/* 消息读取对象 */
	private volatile BufferedReader bufferedReader = null;
	/* 消息内容 */
	private volatile StringBuilder stringBuilder = null;
	/* 用户ID */
	private volatile Integer userId = -1;
	/* 登陆计时 */
	private volatile Timer loginTimer = null;
	/* 消息发送次数 */
	private volatile Integer pushNumber = 1;
	/* 消息重发延迟 */
	private volatile Long pushDelay = 5 * 1000L;
	/* 关闭线程标记 */
	public volatile boolean isCanRun = true;

	public CoreSocketService(Socket socket) {
		this.socket = socket;
	}

	/* 初始化参数 */
	private void initConfig() {
		pushNumber = Integer.parseInt(ConfigUtil.getInstance().getValue("send_number").trim()); // 推送未读消息次数
		if (loginTimer != null) {
			loginTimer.cancel();
		}
		loginTimer = new Timer(); // 考虑到用户重连的情况
		loginTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				isCanRun = false; // 如果连接上后s内用户不登陆成功将中断这条线程
			}
		}, 20 * 1000);
	}

	@Override
	public void run() {
		initConfig();
		try {
			while (isCanRun && isRunConnect()) {
				bufferedReader = getBufferedReader();
				stringBuilder = new StringBuilder("");
				String readerLine = "";
				while (!StrUtil.isEmpty((readerLine = bufferedReader.readLine()))) {
					stringBuilder.append(readerLine).append("\n");
				}
				String message = stringBuilder.toString();
				if (!StrUtil.isEmpty(message)) {
					exeSer.execute(new DisposeMessage(this, message)); // 收到消息
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* 对接收到的消息进行预处理 */
	public void disposeMessage(String message) throws Exception {
		BaseData baseData = MsgUtil.GSON.fromJson(message, MsgUtil.BASE_DATA); // 会出现丢包数据不完整
		int type = baseData.dataType.intValue();
		switch (type) {
		case 100000: // 空消息保持通道打开
			doKeepConn(baseData);
			break;
		case 100001: // 得知消息是否发送成功（dataContent：数据ID）
			doSendMsgOK(baseData);
			break;
		case 100002: // 用户登录（dataContent：用户ID）
			doUserLogin(baseData);
			break;
		case 100003: // 消息推送（dataContent：推送的消息与要推送的用户ID）
			onPushMsg(baseData);
			break;
		case 100004: // 返回用户列表（在线的用一个属性标记）
			doUserList(baseData);
			break;
		case 100005: // 获取用户聊天记录
			doChatRecord(baseData);
			break;
		case 100006: // 检测Socket通道状态
			doTestConnMsg(baseData);
			break;
		case 100007: // 对外推送接口
			doServicePush(baseData);
			break;
		case 100010: // 普通消息（可以根据msgType判断消息类型 注意不是dataType）
			disposeMsgBody(baseData);
			break;
		default:
			break;
		}
		// 空消息与消息的成功回调都不回复
		if (type != 100000 && type != 100001) {
			sendMessage(MsgUtil.getSendOkMsg(baseData.dataId.toString())); // 告诉对方消息被接收了
		}
	}

	/* 保持通道打开且更新超时时间 */
	private void doKeepConn(BaseData baseData) {
		// 用客户端传上来的ID更新在线时间 避免被当作超时连接删除
		Main.TO_MAP.put(baseData.dataContent.trim(), System.currentTimeMillis());
	}

	/* 处理消息发送成功回调 要求客户端不论收到什么消息都要回传消息的dataId 不回传消息将被视为失败消息 会继续推送（不包括空消息） */
	private void doSendMsgOK(BaseData baseData) throws Exception {
		BaseData.deleteBaseData(Long.parseLong(baseData.dataContent));// 删除数据库临时表的对应数据
	}

	/* 处理用户登录 */
	private void doUserLogin(BaseData baseData) throws Exception {
		String[] userAndPws = baseData.dataContent.split("&"); // 隔开账号密码
		if (!StrUtil.isNumber(userAndPws[0])) {
			sendMessage(MsgUtil.getLoginMsg("-1"));
			return;
		}
		userId = Integer.valueOf(userAndPws[0]);
		Users user = Users.getUsers(userId.longValue());
		if (user != null && userAndPws[1].equals(user.passWord)) {

			loginTimer.cancel(); // 用户登录成功

			// 保存登陆信息
			CoreSocketService css = Main.SOCKETS.get(userId.toString());
			if (css != null) {
				css.isCanRun = false;
				if (css.isAlive())
					css.interrupt();
			}
			Main.SOCKETS.put(userId.toString(), this);

			// 返回空消息避免客户端多次重连
			sendMessage(MsgUtil.getEmptyMsg(String.valueOf(System.currentTimeMillis())));

			Thread.sleep(100);//

			// 返回用户数据
			sendMessage(MsgUtil.getLoginMsg(MsgUtil.GSON.toJson(user)));

			new Thread(new Runnable() {

				@Override
				public void run() {
					for (int i = 0; i < pushNumber; i++) {
						try {
							Thread.sleep(pushDelay); // 两次推送的时间差

							// 推送所有未读推送
							List<PushUser> pushUsers = PushUser.getPushUserList(userId);
							if (pushUsers != null && pushUsers.size() > 0) {
								for (PushUser p : pushUsers) {
									sendPushMsg(userId, PushMsg.getPushMsg(p.pushId));
									Thread.sleep(500);// 不能让线程太快 解决每次只能推两条的BUG
								}
							}

							// 推送所有未读聊天消息
							List<MsgData> msgs = MsgData.getUserNoReadMsg(userId);
							if (msgs != null && msgs.size() > 0) {
								for (MsgData md : msgs) {
									sendMessage(MsgUtil.getChatMsg(MsgUtil.GSON.toJson(md)));
									// 更改为已读状态
									md.isRead = 1;
									MsgData.updateMsgData(md);
									Thread.sleep(500);// 不能让线程太快解决每次只能推两条的BUG
								}
							}

							// 推送所有发送失败的聊天信息
							List<BaseData> baseDatas = BaseData.getBaseDataList("100010");
							if (baseDatas != null && baseDatas.size() > 0) {
								for (BaseData bd : baseDatas) {
									MsgData msgData = MsgUtil.GSON.fromJson(bd.dataContent, MsgUtil.MSG_DATA);
									if (msgData.toUserId.intValue() == userId.intValue()) {
										sendMessage(MsgUtil.getChatMsg(bd.dataContent));
										BaseData.deleteBaseData(bd.dataId);// 要删除原来的数据因为新发送又会保存新的记录
										Thread.sleep(500);// 不能让线程太快解决每次只能推两条的BUG
									}
								}
							}

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}).start();

		} else {
			sendMessage(MsgUtil.getLoginMsg(MsgUtil.GSON.toJson(new Users(-1L, "-1", "-1", -1)))); // 返回空用户代表验证失败
		}
	}

	/* 处理消息推送 来到这里说明消息已经推送到了当前用户 返回：PushId */
	private void onPushMsg(BaseData baseData) throws Exception {
		/* 删除临时记录 不删除下次还会继续给用户推送 */
		String[] strArr = baseData.dataContent.split("&");
		PushUser.deletePushUserByUserIdAndPushId(Integer.parseInt(strArr[0]), Long.parseLong(strArr[1]));
	}

	/* 对外服务器推送接口 注意该接口会超时 所以必须尽快发送消息 (超时了也会继续执行任务到结束) */
	private void doServicePush(BaseData baseData) throws Exception {
		ServicePushData ipd = MsgUtil.GSON.fromJson(baseData.dataContent, MsgUtil.IPD);
		if (ipd.users == null || ipd.users.size() <= 0) {
			ipd.users = Users.getUsersList();
		}
		if (ipd.users != null && ipd.users.size() > 0) {
			for (Users u : ipd.users) {
				CoreSocketService su = Main.SOCKETS.get(u._id.toString());
				if (su != null) {
					su.sendPushMsg(u._id.intValue(), ipd.pushMsg);
				} else {
					PushUser.savePushUser(new PushUser(System.currentTimeMillis(), u._id.intValue(), ipd.pushMsg._id));
				}
			}
		}
		if (PushMsg.getPushMsg(ipd.pushMsg._id) == null) {
			PushMsg.savePushMsg(ipd.pushMsg); // 保存到记录
		}
	}

	/* 返回用户列表 */
	private void doUserList(BaseData baseData) throws Exception {
		List<Users> users = new ArrayList<>();
		users.addAll(Users.getUsersList());
		for (Users user : users) {
			for (Map.Entry<String, CoreSocketService> entry : Main.SOCKETS.entrySet()) {
				if (entry.getKey().equals(user._id.toString())) {
					user.inLinear = 1;
				} else {
					user.inLinear = 0;
				}
			}
		}
		sendMessage(MsgUtil.getUserList(MsgUtil.GSON.toJson(users)));
	}

	/* 获取聊天记录 */
	private void doChatRecord(BaseData baseData) throws Exception {
		String[] p = baseData.dataContent.split("&");
		List<MsgData> mds = MsgData.getUserChatRecordMsg(Integer.valueOf(p[0]), Integer.valueOf(p[1]));
		if (mds != null) {
			sendMessage(MsgUtil.getChatRecordMsg(MsgUtil.GSON.toJson(mds)));
		}
	}

	/* 响应客户端检测Socket通道 */
	private void doTestConnMsg(BaseData baseData) throws Exception {
		sendMessage(MsgUtil.getTestConnMsg(String.valueOf(System.currentTimeMillis())));
	}

	/* 处理普通消息体 派发消息到指定用户 */
	private void disposeMsgBody(BaseData baseData) throws Exception {
		// 解析消息
		MsgData msgData = MsgUtil.GSON.fromJson(baseData.dataContent, MsgUtil.MSG_DATA);
		msgData.createTime = System.currentTimeMillis(); // 设置消息时间
		// 查找用户
		CoreSocketService su = Main.SOCKETS.get(msgData.toUserId.toString());
		if (su != null) {
			msgData.isRead = 1;
		} else {
			msgData.isRead = 0; // 标记为未读(有用户消息但是用户不在线则认为是未读)
		}
		// 保存聊天记录到数据库
		MsgData.saveMsgData(msgData);
		if (su != null) // 消息要在保存到数据库后发送
			su.sendMessage(MsgUtil.getChatMsg(MsgUtil.GSON.toJson(msgData)));
	}

	/* 获取输入流 */
	private BufferedReader getBufferedReader() throws IOException {
		return new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
	}

	/* 获取输出流 */
	private PrintWriter getPrintWriter() throws IOException {
		return new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
	}

	// --------------------------------------------------------对外公开方法

	/* Socket是否正常连接 只能判断自己的状态 */
	public boolean isRunConnect() {
		return (socket.isConnected() && socket.isBound() && !socket.isClosed());
	}

	/* 发送推送消息 */
	public void sendPushMsg(Integer uid, PushMsg pushMsg) throws Exception {
		if (StrUtil.isEmpty(uid.toString()) || pushMsg == null)
			return;
		// 记录接收用户
		if (PushUser.getPushUser(uid, pushMsg._id) == null) {
			PushUser.savePushUser(new PushUser(System.currentTimeMillis(), userId, pushMsg._id));
		}
		// 推送消息
		sendMessage(MsgUtil.getPushMsg(MsgUtil.GSON.toJson(pushMsg)));
	}

	/* 发送消息 */
	public void sendMessage(BaseData baseData) throws Exception {
		if (baseData.dataType.intValue() == 100000 || baseData.dataType.intValue() == 100001) {
			getPrintWriter().println(MsgUtil.GSON.toJson(baseData).replaceAll("\n", "\r") + "\n"); // 非重要消息直接发送
		} else {
			if (BaseData.getBaseData(baseData.dataId) == null) {
				BaseData.saveBaseData(baseData); // 保存消息发送记录
				exeSer.execute(new SendTask(baseData, getPrintWriter(), pushNumber, pushDelay)); // 发送
			}
		}
	}

	/* 消息接收派发线程 */
	static class DisposeMessage implements Runnable {
		private CoreSocketService css = null;
		private String message = "";

		public DisposeMessage(CoreSocketService css, String message) {
			this.css = css;
			this.message = message;
		}

		@Override
		public void run() {
			try {
				css.disposeMessage(message);
				System.out.println(Thread.currentThread().getId() + "-收:\n" + message);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/* 消息发送任务 */
	static class SendTask implements Runnable {
		private BaseData baseData = null;
		private String stringValue = null;
		private PrintWriter printWriter = null;
		private int sn = 1;
		private long pushDelay = 1000;

		public SendTask(BaseData baseData, PrintWriter printWriter, int sn, long pushDelay) {
			this.baseData = baseData;
			this.stringValue = getSendMsgString(baseData);
			this.printWriter = printWriter;
			this.sn = sn;
			this.pushDelay = pushDelay;
		}

		/* 转化要发送的消息 */
		private String getSendMsgString(BaseData baseData) {
			return MsgUtil.GSON.toJson(baseData).replaceAll("\n", "\r") + "\n";
		}

		@Override
		public void run() {
			try {
				for (int i = 0; i < sn; i++) {
					BaseData bd = BaseData.getBaseData(baseData.dataId); // 必须从数据库拿数据判断数据是否发送完成
					if (bd != null) {
						printWriter.println(stringValue);
						System.out.println(Thread.currentThread().getId() + "-发:\n" + stringValue);
						Thread.sleep(pushDelay); // 重发延迟
					} else {
						return;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
