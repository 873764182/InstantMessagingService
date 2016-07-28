package com.gzzm.chat.data;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.gzzm.chat.db.DataBaseManage;

/**
 * PushUser 实体类 Wed Jun 15 21:52:25 CST 2016 PX
 */

public class PushUser {

	public Long _id;
	public Long createTime;
	public Integer userId;
	public Long pushId;

	public PushUser() {
	}

	public PushUser(Long createTime, Integer userId, Long pushId) {
		this.createTime = createTime;
		this.userId = userId;
		this.pushId = pushId;
	}

	@Override
	public String toString() {
		return "PushUser [_id=" + _id + ", CreateTime=" + createTime + ", UserId=" + userId + ", PushId=" + pushId
				+ "]";
	}

	/* 添加一条消息 */
	public static int savePushUser(PushUser pushUser) throws Exception {
		synchronized (PushUser.class) {
			String sql = "INSERT INTO PushUser(CreateTime, UserId, PushId) VALUES(?, ?, ?)";
			return DataBaseManage.getInstance().executeUpdate(sql, pushUser.createTime.toString(),
					pushUser.userId.toString(), pushUser.pushId.toString());
		}
	}

	/* 更新一条消息 */
	public static int updatePushUser(PushUser pushUser) throws Exception {
		synchronized (PushUser.class) {
			String sql = "UPDATE PushUser SET CreateTime=?, UserId=?, PushId=? WHERE _id=?";
			return DataBaseManage.getInstance().executeUpdate(sql, pushUser.createTime.toString(),
					pushUser.userId.toString(), pushUser.pushId.toString(), pushUser._id.toString());
		}
	}

	/* 删除一条消息 */
	public static int deletePushUser(Long _id) throws Exception {
		synchronized (PushUser.class) {
			String sql = "DELETE FROM PushUser WHERE _id = ?";
			return DataBaseManage.getInstance().executeUpdate(sql, _id.toString());
		}
	}

	/* 删除一条消息 */
	public static int deletePushUserByUserIdAndPushId(Integer userId, Long pushId) throws Exception {
		synchronized (PushUser.class) {
			String sql = "DELETE FROM PushUser WHERE (UserId = ? AND PushId = ?)";
			return DataBaseManage.getInstance().executeUpdate(sql, userId.toString(), pushId.toString());
		}
	}

	/* 获取一条消息 */
	public static PushUser getPushUser(Long _id) throws Exception {
		String sql = "SELECT * FROM PushUser WHERE _id = ?";
		ResultSet rs = DataBaseManage.getInstance().executeQuery(sql, _id.toString());
		if (rs.next()) {
			PushUser pushUser = new PushUser();
			pushUser._id = rs.getLong(1);
			pushUser.createTime = rs.getLong(2);
			pushUser.userId = rs.getInt(3);
			pushUser.pushId = rs.getLong(4);
			return pushUser;
		}
		return null;
	}

	/* 获取一条消息 */
	public static PushUser getPushUser(Integer userId, Long pushId) throws Exception {
		String sql = "SELECT * FROM PushUser WHERE (UserId = ? AND PushId = ?)";
		ResultSet rs = DataBaseManage.getInstance().executeQuery(sql, userId.toString(), pushId.toString());
		if (rs.next()) {
			PushUser pushUser = new PushUser();
			pushUser._id = rs.getLong(1);
			pushUser.createTime = rs.getLong(2);
			pushUser.userId = rs.getInt(3);
			pushUser.pushId = rs.getLong(4);
			return pushUser;
		}
		return null;
	}

	/* 获取指定用户的所有消息 */
	public static List<PushUser> getPushUserList(Integer userId) throws Exception {
		List<PushUser> pushUsers = new ArrayList<>();
		String sql = "SELECT * FROM PushUser WHERE UserId = ?";
		ResultSet rs = DataBaseManage.getInstance().executeQuery(sql, userId.toString());
		while (rs.next()) {
			PushUser pushUser = new PushUser();
			pushUser._id = rs.getLong(1);
			pushUser.createTime = rs.getLong(2);
			pushUser.userId = rs.getInt(3);
			pushUser.pushId = rs.getLong(4);
			pushUsers.add(pushUser);
		}
		return pushUsers;
	}
}
