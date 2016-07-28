package com.gzzm.chat.data;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.gzzm.chat.db.DataBaseManage;

/**
 * PushMsg 实体类 Wed Jun 15 21:52:14 CST 2016 PX
 */

public class PushMsg {

	public Long _id;
	public Integer createUser;
	public Long createTime;
	public String pushTitle;
	public String pushContent;

	@Override
	public String toString() {
		return "PushMsg [_id=" + _id + ", CreateUser=" + createUser + ", CreateTime=" + createTime + ", PushTitle="
				+ pushTitle + ", pushContent=" + pushContent + "]";
	}

	/* 添加一条消息 */
	public static int savePushMsg(PushMsg pushMsg) throws Exception {
		synchronized (PushMsg.class) {
			String sql = "INSERT INTO PushMsg(_id, CreateUser, CreateTime, PushTitle, pushContent) VALUES(?, ?, ?, ?, ?)";
			return DataBaseManage.getInstance().executeUpdate(sql, pushMsg._id.toString(),
					pushMsg.createUser.toString(), pushMsg.createTime.toString(), pushMsg.pushTitle,
					pushMsg.pushContent);
		}
	}

	/* 更新一条消息 */
	public static int updatePushMsg(PushMsg pushMsg) throws Exception {
		synchronized (PushMsg.class) {
			String sql = "UPDATE PushMsg SET CreateUser=?, CreateTime=?, PushTitle=?, pushContent=? WHERE _id=?";
			return DataBaseManage.getInstance().executeUpdate(sql, pushMsg.createUser.toString(),
					pushMsg.createTime.toString(), pushMsg.pushTitle, pushMsg.pushContent);
		}
	}

	/* 删除一条消息 */
	public static int deletePushMsg(Long _id) throws Exception {
		synchronized (PushMsg.class) {
			String sql = "DELETE FROM PushMsg WHERE _id = ?";
			return DataBaseManage.getInstance().executeUpdate(sql, _id.toString());
		}
	}

	/* 获取一条消息 */
	public static PushMsg getPushMsg(Long _id) throws Exception {
		String sql = "SELECT * FROM PushMsg WHERE _id = ?";
		ResultSet rs = DataBaseManage.getInstance().executeQuery(sql, _id.toString());
		if (rs.next()) {
			PushMsg pushMsg = new PushMsg();
			pushMsg._id = rs.getLong(1);
			pushMsg.createUser = rs.getInt(2);
			pushMsg.createTime = rs.getLong(3);
			pushMsg.pushTitle = rs.getString(4);
			pushMsg.pushContent = rs.getString(5);
			return pushMsg;
		}
		return null;
	}

	/* 获取指定类型的所有消息 */
	public static List<PushMsg> getPushMsgList() throws Exception {
		List<PushMsg> pushMsgs = new ArrayList<>();
		String sql = "SELECT * FROM PushMsg";
		ResultSet rs = DataBaseManage.getInstance().executeQuery(sql);
		while (rs.next()) {
			PushMsg pushMsg = new PushMsg();
			pushMsg._id = rs.getLong(1);
			pushMsg.createUser = rs.getInt(2);
			pushMsg.createTime = rs.getLong(3);
			pushMsg.pushTitle = rs.getString(4);
			pushMsg.pushContent = rs.getString(5);
			pushMsgs.add(pushMsg);
		}
		return pushMsgs;
	}
}
