package com.gzzm.chat.service;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import com.gzzm.chat.Main;
import com.gzzm.chat.data.PushMsg;
import com.gzzm.chat.data.PushUser;
import com.gzzm.chat.data.Users;
import com.gzzm.chat.uitl.MsgUtil;
import com.gzzm.chat.uitl.StrUtil;

/**
 * 显示推送消息面板
 */
public class PushFrameService {
	private static Frame frame = null;
	private static Integer userId = -1; // 操作员

	private TextField textField;
	private TextArea textArea;
	private Button button;

	public synchronized static void showPushFrame(Integer userId) {
		PushFrameService.userId = userId;
		if (frame == null) {
			new PushFrameService();
		}
		frame.setVisible(true);
	}

	public PushFrameService() {
		frame = new Frame("推送消息");
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				frame.setVisible(false);
			}
		});
		frame.add(getTitleView(), BorderLayout.NORTH);
		frame.add(getContentView(), BorderLayout.CENTER);
		frame.add(getButtonView(), BorderLayout.SOUTH);
		frame.pack();
	}

	/* 获取标题输入框 */
	private Component getTitleView() {
		Panel titlePanel = new Panel();
		titlePanel.setLayout(new BorderLayout());
		textField = new TextField(60);
		textField.addTextListener(e -> {
			int len = textField.getText().length();
			if (len > 0) {
				button.setLabel("Start push");
			}
		});
		titlePanel.add(new Label("title:"), BorderLayout.NORTH);
		titlePanel.add(textField, BorderLayout.CENTER);
		return titlePanel;
	}

	/* 获取内容输入框 */
	private Component getContentView() {
		Panel contentPanel = new Panel();
		contentPanel.setLayout(new BorderLayout());
		textArea = new TextArea();
		contentPanel.add(new Label("content:"), BorderLayout.NORTH);
		contentPanel.add(textArea, BorderLayout.CENTER);
		return contentPanel;
	}

	/* 获取按钮 */
	private Component getButtonView() {
		Panel buttonPanel = new Panel();
		button = new Button("Start push");
		button.addActionListener(e -> {
			doPusgMessage();
		});
		buttonPanel.add(button);
		return buttonPanel;
	}

	/* 开始推送 */
	private void doPusgMessage() {
		try {
			String title = textField.getText();
			String content = textArea.getText();
			if (StrUtil.isEmpty(title) || StrUtil.isEmpty(content)) {
				System.err.println("Title and content is not null !");
				return;
			}

			PushMsg pm = new PushMsg();
			pm._id = MsgUtil.getBaseDataId();
			pm.createTime = System.currentTimeMillis();
			pm.createUser = userId;
			pm.pushTitle = title;
			pm.pushContent = content;

			List<Users> users = Users.getUsersList();
			if (users != null && users.size() > 0) {
				for (Users u : users) {
					CoreSocketService su = Main.SOCKETS.get(u._id.toString());
					if (su != null) {
						su.sendPushMsg(u._id.intValue(), pm);
					} else {
						PushUser.savePushUser(new PushUser(System.currentTimeMillis(), u._id.intValue(), pm._id));
					}
				}
			}
			// 保存到记录
			if (PushMsg.getPushMsg(pm._id) == null) {
				PushMsg.savePushMsg(pm);
			}

			textField.setText("");
			textArea.setText("");
			button.setLabel("Push ok");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
