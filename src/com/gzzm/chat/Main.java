package com.gzzm.chat;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import com.gzzm.chat.service.CoreSocketService;
import com.gzzm.chat.service.PushFrameService;
import com.gzzm.chat.service.UserFrameService;
import com.gzzm.chat.uitl.MsgUtil;
import com.gzzm.chat.uitl.StrUtil;

/**
 * 程序主入口
 */
public class Main {

	/* 保存连接 */
	public static final Map<String, CoreSocketService> SOCKETS = new Hashtable<>();
	/* 记录超时 */
	public static final Map<String, Long> TO_MAP = new Hashtable<>();
	/* 计时器对象 */
	private static final Timer mainTimer = new Timer();
	/* 重复时间 */
	private static final long PERIOD_DELAY_TIME = 3 * 60 * 1000L;
	/* 服务器监听端口 */
	private static final int SERVICE_PORT = 10019;

	/* 发送空消息保持通道打开 */
	private static void doKeepConn() {
		mainTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				List<String> keyList = new ArrayList<>();
				long nowTime = System.currentTimeMillis();
				for (Map.Entry<String, CoreSocketService> entry : SOCKETS.entrySet()) {
					try {
						entry.getValue().sendMessage(MsgUtil.getEmptyMsg(String.valueOf(nowTime)));
					} catch (Exception e) {
						keyList.add(entry.getKey());
						e.printStackTrace();
					}
				}
				for (String key : keyList) {
					SOCKETS.remove(key);
				}
			}
		}, PERIOD_DELAY_TIME, PERIOD_DELAY_TIME);
	}

	/* 删除超时的连接 */
	private static void removeTimeout() {
		mainTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				List<String> keyList = new ArrayList<>();
				long nowTime = System.currentTimeMillis();
				for (Map.Entry<String, Long> entry : TO_MAP.entrySet()) {
					long intervalTime = nowTime - entry.getValue().longValue() - 10000; // 允许10秒误差时间
					if (intervalTime > PERIOD_DELAY_TIME) {
						CoreSocketService su = SOCKETS.get(entry.getKey());
						if (su != null) {
							su.isCanRun = false; // 中断服务线程
							if (su.isAlive())
								su.interrupt();
						}
						keyList.add(entry.getKey());
					}
				}
				// 清除超时连接
				for (String key : keyList) {
					TO_MAP.remove(key);
					SOCKETS.remove(key);
				}
			}
		}, PERIOD_DELAY_TIME * 2, PERIOD_DELAY_TIME * 2);
	}

	/* 初始化控制台输入 */
	private static void openPushPage() {
		new Thread(new Runnable() {

			private boolean isPush(String value) {
				if (value.startsWith("push") && value.contains("-") && value.length() > (value.indexOf("-") + 1)) {
					if (StrUtil.isNumber(value.split("-")[1].replaceAll(" ", ""))) {
						return true;
					}
				}
				return false;
			}

			@SuppressWarnings("static-access")
			@Override
			public void run() {
				while (true) {
					@SuppressWarnings("resource")
					String value = new Scanner(System.in).nextLine();
					if (isPush(value)) {
						PushFrameService.showPushFrame(Integer.parseInt(value.split("-")[1].replaceAll(" ", "")));
					} else if (value.startsWith("user")) {
						UserFrameService.showUserAdd();
					} else if (value.startsWith("linear")) {
						for (Map.Entry<String, CoreSocketService> entry : SOCKETS.entrySet()) {
							System.out.println("在线用户：《 " + entry.getKey() + " : "
									+ entry.getValue().currentThread().getId() + " 》");
						}
					} else {
						System.out.println(value);
					}
				}
			}
		}).start();
	}

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		doKeepConn();
		removeTimeout();
		openPushPage();

		try {
			ServerSocket serverSocket = new ServerSocket(SERVICE_PORT);
			System.out.println("====== service start to finish\t" + InetAddress.getLocalHost().getHostAddress() + "\t"
					+ SERVICE_PORT + "\t======\n");
			while (!serverSocket.isClosed()) {
				new CoreSocketService(serverSocket.accept()).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
