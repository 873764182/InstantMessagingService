package com.gzzm.chat.service;

import java.awt.Button;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.gzzm.chat.data.Users;
import com.gzzm.chat.uitl.StrUtil;

/**
 * 添加用户
 */
public class UserFrameService {
	private static Frame frame = null;

	public synchronized static void showUserAdd() {
		if (frame == null) {
			new UserFrameService();
		}
		frame.setVisible(true);
	}

	public UserFrameService() {
		frame = new Frame("添加用户");
		frame.setLayout(new GridLayout(5, 1, 10, 10));
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				frame.setVisible(false);
			}
		});

		Label msgL = new Label("");

		Panel panelId = new Panel();
		Label userIdL = new Label("User  Id");
		TextField userIdTF = new TextField(20);
		panelId.add(userIdL);
		panelId.add(userIdTF);

		Panel panelName = new Panel();
		Label userNameL = new Label("UserName");
		TextField userNmaeTF = new TextField(20);
		panelName.add(userNameL);
		panelName.add(userNmaeTF);

		Panel panelPass = new Panel();
		Label passwordL = new Label("Password");
		TextField passwordTF = new TextField(20);
		panelPass.add(passwordL);
		panelPass.add(passwordTF);

		Panel panelBtn = new Panel();
		Button button = new Button(" ADD USER ");
		button.addActionListener(e -> {
			doAddUser(userIdTF, userNmaeTF, passwordTF, button, msgL);
		});
		panelBtn.add(button);

		userNmaeTF.addTextListener(e -> {
			String text = userNmaeTF.getText();
			if (!StrUtil.isEmpty(text)) {
				button.setLabel(" ADD USER ");
			}
		});

		frame.add(panelId);
		frame.add(panelName);
		frame.add(panelPass);
		frame.add(panelBtn);
		frame.add(msgL);

		frame.pack();
	}

	/* 执行添加 */
	private void doAddUser(TextField userIdTF, TextField userNmaeTF, TextField passwordTF, Button button, Label msgL) {
		String userId = userIdTF.getText();
		String userName = userNmaeTF.getText();
		String password = passwordTF.getText();
		if (StrUtil.isEmpty(userId) || StrUtil.isEmpty(userName) || StrUtil.isEmpty(password)) {
			return;
		}
		if (!StrUtil.isNumber(userId)) {
			msgL.setText("UserId is not number");
			return; // ID必须是数值类型
		}
		Users user = new Users();
		user._id = Long.parseLong(userId);
		user.userName = userName;
		user.passWord = password;
		user.inLinear = 0;
		try {
			if (Users.getUsers(Long.parseLong(userId)) != null) {
				msgL.setText("Error UserId already exists");
				return;
			}
			if (Users.getUsers(userName) != null) {
				msgL.setText("Error UserName already exists");
				return;
			}
			if (Users.saveUsers(user) > 0) {
				Users u = Users.getUsers(userName);
				if (u != null) {
					userIdTF.setText("");
					userNmaeTF.setText("");
					passwordTF.setText("");
					button.setLabel("ADD OK");
					msgL.setText("USER_ID: " + u._id + "  PASS: " + u.passWord);
				}
			} else {
				button.setLabel("ERROR");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
