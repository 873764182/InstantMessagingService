package com.gzzm.chat.data;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.gzzm.chat.db.DataBaseManage;

/**
 * Users 实体类 Sun Jun 12 10:54:21 CST 2016 PX
 */

public class Users {

	/* 用户编号 */
	public Long _id;
	/* 用户名 */
	public String userName;
	/* 用户密码 */
	public String passWord;
	/* 是否在线 (0.否, 1.是) */
	public Integer inLinear;

	public Users() {
	}

	public Users(Long _id, String userName, String passWord, Integer inLinear) {
		super();
		this._id = _id;
		this.userName = userName;
		this.passWord = passWord;
		this.inLinear = inLinear;
	}

	@Override
	public String toString() {
		return "Users [_id=" + _id + ", userName=" + userName + ", passWord=" + passWord + ", otherNote=" + inLinear
				+ "]";
	}

	/* 添加一条用户 */
	public static int saveUsers(Users user) throws Exception {
		String sql = "INSERT INTO Users(_id, UserName, PassWord, InLinear) VALUES(?, ?, ?, ?)";
		return DataBaseManage.getInstance().executeUpdate(sql, user._id.toString(), user.userName, user.passWord,
				user.inLinear.toString());
	}

	/* 删除一条用户 */
	public static int deleteUsers(Long _id) throws Exception {
		String sql = "DELETE FROM Users WHERE _id = ?";
		return DataBaseManage.getInstance().executeUpdate(sql, _id.toString());
	}

	/* 获取一条用户 */
	public static Users getUsers(Long _id) throws Exception {
		String sql = "SELECT * FROM Users WHERE _id = ?";
		ResultSet rs = DataBaseManage.getInstance().executeQuery(sql, _id.toString());
		if (rs.next()) {
			Users user = new Users();
			user._id = rs.getLong(1);
			user.userName = rs.getString(2);
			user.passWord = rs.getString(3);
			user.inLinear = rs.getInt(4);
			return user;
		}
		return null;
	}

	/* 获取一条用户 */
	public static Users getUsers(String name) throws Exception {
		String sql = "SELECT * FROM Users WHERE UserName = ?";
		ResultSet rs = DataBaseManage.getInstance().executeQuery(sql, name);
		if (rs.next()) {
			Users user = new Users();
			user._id = rs.getLong(1);
			user.userName = rs.getString(2);
			user.passWord = rs.getString(3);
			user.inLinear = rs.getInt(4);
			return user;
		}
		return null;
	}

	/* 获取所有用户 */
	public static List<Users> getUsersList() throws Exception {
		List<Users> users = new ArrayList<>();
		String sql = "SELECT * FROM Users";
		ResultSet rs = DataBaseManage.getInstance().executeQuery(sql);
		while (rs.next()) {
			Users user = new Users();
			user._id = rs.getLong(1);
			user.userName = rs.getString(2);
			user.passWord = rs.getString(3);
			user.inLinear = rs.getInt(4);
			users.add(user);
		}
		return users;
	}

	/* 更新一条用户 */
	public static int updateUsers(Users user) throws Exception {
		String sql = "UPDATE Users SET userName=?, passWord=?, inLinear=? WHERE _id=?";
		return DataBaseManage.getInstance().executeUpdate(sql, user.userName, user.passWord, user.inLinear.toString(),
				user._id.toString());
	}

}
