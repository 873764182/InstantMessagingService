package com.gzzm.chat.data;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.gzzm.chat.db.DataBaseManage;

public class MsgData {

	/* 消息编号 */
	public Long _id;
	/* 发送者 */
	public Integer fromUserId;
	/* 接收者 */
	public Integer toUserId;
	/* 创建时间 */
	public Long createTime;
	/* 消息类型 */
	public Integer msgType;
	/* 消息内容 */
	public String content;
	/* 是否已读 0.已读, 1.未读 */
	public Integer isRead;
	/* 其他备注 */
	public String otherNote;

	@Override
	public String toString() {
		return "MsgData [_id=" + _id + ", fromUserId=" + fromUserId + ", toUserId=" + toUserId + ", createTime="
				+ createTime + ", msgType=" + msgType + ", content=" + content + ", otherNote=" + otherNote
				+ ", isRead=" + isRead + "]";
	}

	/* 添加一条消息 */
	public static int saveMsgData(MsgData msgData) throws Exception {
		String sql = "INSERT INTO "
				+ "MsgData(FromUserId, ToUserId, CreateTime, MsgType, Content, IsRead, OtherNote) VALUES(?, ?, ?, ?, ?, ?, ?)";
		return DataBaseManage.getInstance().executeUpdate(sql, msgData.fromUserId.toString(),
				msgData.toUserId.toString(), msgData.createTime.toString(), msgData.msgType.toString(), msgData.content,
				msgData.isRead.toString(), msgData.otherNote);
	}

	/* 删除一条消息 */
	public static int deleteMsgData(Long _id) throws Exception {
		String sql = "DELETE FROM MsgData WHERE _id = ?";
		return DataBaseManage.getInstance().executeUpdate(sql, _id.toString());
	}

	/* 获取一条消息 */
	public static MsgData getMsgData(Long _id) throws Exception {
		String sql = "SELECT * FROM MsgData WHERE _id = ?";
		ResultSet rs = DataBaseManage.getInstance().executeQuery(sql, _id.toString());
		if (rs.next()) {
			MsgData msgData = new MsgData();
			msgData._id = rs.getLong(1);
			msgData.fromUserId = rs.getInt(2);
			msgData.toUserId = rs.getInt(3);
			msgData.createTime = rs.getLong(4);
			msgData.msgType = rs.getInt(5);
			msgData.content = rs.getString(6);
			msgData.isRead = rs.getInt(7);
			msgData.otherNote = rs.getString(8);
			return msgData;
		}
		return null;
	}

	/* 获取所有消息 */
	public static List<MsgData> getMsgDataList() throws Exception {
		List<MsgData> msgDatas = new ArrayList<>();
		String sql = "SELECT * FROM MsgData ORDER BY CreateTime ";
		ResultSet rs = DataBaseManage.getInstance().executeQuery(sql);
		while (rs.next()) {
			MsgData msgData = new MsgData();
			msgData._id = rs.getLong(1);
			msgData.fromUserId = rs.getInt(2);
			msgData.toUserId = rs.getInt(3);
			msgData.createTime = rs.getLong(4);
			msgData.msgType = rs.getInt(5);
			msgData.content = rs.getString(6);
			msgData.isRead = rs.getInt(7);
			msgData.otherNote = rs.getString(8);
			msgDatas.add(msgData);
		}
		return msgDatas;
	}

	/* 更新一条消息 */
	public static int updateMsgData(MsgData msgData) throws Exception {
		String sql = "UPDATE MsgData SET FromUserId=?, ToUserId=?, CreateTime=?, MsgType=?, Content=?, IsRead=?, OtherNote=? WHERE _id=?";
		return DataBaseManage.getInstance().executeUpdate(sql, msgData.fromUserId.toString(),
				msgData.toUserId.toString(), msgData.createTime.toString(), msgData.msgType.toString(), msgData.content,
				msgData.isRead.toString(), msgData.otherNote, msgData._id.toString());
	}

	/* 获取用户未读消息 */
	public static List<MsgData> getUserNoReadMsg(Integer _id) throws Exception {
		List<MsgData> msgDatas = new ArrayList<>();
		String sql = "SELECT * FROM MsgData WHERE ToUserId=? AND IsRead=0 ORDER BY CreateTime ";
		ResultSet rs = DataBaseManage.getInstance().executeQuery(sql, _id.toString());
		while (rs.next()) {
			MsgData msgData = new MsgData();
			msgData._id = rs.getLong(1);
			msgData.fromUserId = rs.getInt(2);
			msgData.toUserId = rs.getInt(3);
			msgData.createTime = rs.getLong(4);
			msgData.msgType = rs.getInt(5);
			msgData.content = rs.getString(6);
			msgData.isRead = rs.getInt(7);
			msgData.otherNote = rs.getString(8);
			msgDatas.add(msgData);
		}
		return msgDatas;
	}

	/* 获取用户记录消息 */
	public static List<MsgData> getUserChatRecordMsg(Integer fromUserId, Integer toUserId) throws Exception {
		List<MsgData> msgDatas = new ArrayList<>();
		String sql = "SELECT * FROM MsgData WHERE (FromUserId=? AND ToUserId=?) OR (FromUserId=? AND ToUserId=?) ORDER BY CreateTime LIMIT 0,20 ";
		ResultSet rs = DataBaseManage.getInstance().executeQuery(sql, fromUserId.toString(), toUserId.toString(),
				toUserId.toString(), fromUserId.toString());
		while (rs.next()) {
			MsgData msgData = new MsgData();
			msgData._id = rs.getLong(1);
			msgData.fromUserId = rs.getInt(2);
			msgData.toUserId = rs.getInt(3);
			msgData.createTime = rs.getLong(4);
			msgData.msgType = rs.getInt(5);
			msgData.content = rs.getString(6);
			msgData.isRead = rs.getInt(7);
			msgData.otherNote = rs.getString(8);
			msgDatas.add(msgData);
		}
		return msgDatas;
	}

}
