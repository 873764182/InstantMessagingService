package com.gzzm.chat.data;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.gzzm.chat.db.DataBaseManage;

/**
 * 数据基础对象
 */
public class BaseData {

	/* 数据ID （判断消息是否发送成功时用到） */
	public Long dataId;
	/* 数据类型 */
	public Integer dataType;
	/* 数据内容 */
	public String dataContent;

	public BaseData() {
	}

	public BaseData(Integer dataType, String dataContent) {
		this.dataType = dataType;
		this.dataContent = dataContent;
	}

	public BaseData(Long dataId, Integer dataType, String dataContent) {
		this.dataId = dataId;
		this.dataType = dataType;
		this.dataContent = dataContent;
	}

	@Override
	public String toString() {
		return "BaseData [dataId=" + dataId + ", dataType=" + dataType + ", dataContent=" + dataContent + "]";
	}

	/* 添加一条消息 */
	public static int saveBaseData(BaseData baseData) throws Exception {
		synchronized (BaseData.class) {
			String sql = "INSERT INTO BaseData(dataId, dataType, dataContent) VALUES(?, ?, ?)";
			return DataBaseManage.getInstance().executeUpdate(sql, baseData.dataId.toString(),
					baseData.dataType.toString(), baseData.dataContent);
		}
	}

	/* 删除一条消息 */
	public static int deleteBaseData(Long _id) throws Exception {
		synchronized (BaseData.class) {
			String sql = "DELETE FROM BaseData WHERE dataId = ?";
			return DataBaseManage.getInstance().executeUpdate(sql, _id.toString());
		}
	}

	/* 获取一条消息 */
	public static BaseData getBaseData(Long _id) throws Exception {
		String sql = "SELECT * FROM BaseData WHERE dataId = ?";
		ResultSet rs = DataBaseManage.getInstance().executeQuery(sql, _id.toString());
		if (rs.next()) {
			BaseData baseData = new BaseData();
			baseData.dataId = rs.getLong(1);
			baseData.dataType = rs.getInt(2);
			baseData.dataContent = rs.getString(3);
			return baseData;
		}
		return null;
	}

	/* 获取指定类型的所有消息 */
	public static List<BaseData> getBaseDataList(String dataType) throws Exception {
		List<BaseData> baseDatas = new ArrayList<>();
		String sql = "SELECT * FROM BaseData WHERE dataType=? ORDER BY dataId LIMIT 0,20 ";
		ResultSet rs = DataBaseManage.getInstance().executeQuery(sql, dataType);
		while (rs.next()) {
			BaseData baseData = new BaseData();
			baseData.dataId = rs.getLong(1);
			baseData.dataType = rs.getInt(2);
			baseData.dataContent = rs.getString(3);
			baseDatas.add(baseData);
		}
		return baseDatas;
	}

	/* 更新一条消息 */
	public static int updateBaseData(BaseData baseData) throws Exception {
		synchronized (BaseData.class) {
			String sql = "UPDATE BaseData SET dataId=?, dataType=?, dataContent=? WHERE dataId=?";
			return DataBaseManage.getInstance().executeUpdate(sql, baseData.dataId.toString(),
					baseData.dataType.toString(), baseData.dataContent);
		}
	}
}
