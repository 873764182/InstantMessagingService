package com.gzzm.chat.db;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.gzzm.chat.uitl.ConfigUtil;

/**
 * 数据库连接管理 http://blog.csdn.net/bluesnail216/article/details/15810119
 */
public class DataBaseManage {
	private static DataBaseManage dataBaseManage = null;

	private String conn_url = "";
	private String data_base = "";
	private String conn_param = "";
	private String userName = "";
	private String password = "";

	private Connection conn = null;
	private PreparedStatement preparedStatement = null;
	private Statement statement = null;

	private DataBaseManage() throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		this.initConfig();
		this.initDataBase();
		this.conn = DriverManager.getConnection(conn_url + "/" + data_base + conn_param, userName, password);
	}

	/* 初始化配置信息 */
	private void initConfig() throws Exception {
		conn_url = ConfigUtil.getInstance().getValue("conn_url");
		data_base = ConfigUtil.getInstance().getValue("database");
		conn_param = ConfigUtil.getInstance().getValue("conn_param");
		userName = ConfigUtil.getInstance().getValue("username");
		password = ConfigUtil.getInstance().getValue("password");
	}

	/* 创建数据库表 在MySql5.6上测试通过 要求数据库中存在mysql这个表 */
	private void initDataBase() {
		try {
			Connection c = DriverManager.getConnection(conn_url + "/mysql", userName, password);
			BufferedReader bufread = new BufferedReader(
					new InputStreamReader(new BufferedInputStream(new FileInputStream("res\\chat.sql"))));
			StringBuilder sb = new StringBuilder("");
			String temp = "";
			while ((temp = bufread.readLine()) != null) {
				sb.append(temp).append("\n");
			}
			bufread.close();
			String sbStr = sb.toString().trim();
			if (sbStr.endsWith(";")) {
				sbStr = sbStr.substring(0, sbStr.length() - 1);
			}
			String[] sqlArr = sbStr.split(";");
			Statement s = c.createStatement();
			for (String sql : sqlArr) {
				s.executeUpdate(sql);
				s.clearBatch();
			}
			c.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取数据库连接
	 */
	public static DataBaseManage getInstance() throws Exception {
		if (dataBaseManage == null) {
			synchronized (DataBaseManage.class) {
				if (dataBaseManage == null) {
					dataBaseManage = new DataBaseManage();
				}
			}
		}
		return dataBaseManage;
	}

	/**
	 * 关闭数据库连接
	 */
	public void closeDataBase() throws SQLException {
		if (statement != null) {
			statement.close();
		}
		if (conn != null) {
			conn.close();
		}
	}

	/**
	 * 获取参数化SQL语句执行对象
	 */
	public PreparedStatement getPreparedStatement(String sql) throws SQLException {
		preparedStatement = conn.prepareStatement(sql);
		return preparedStatement;
	}

	/**
	 * 参数化SQL执行操作（只支持String类型参数）
	 */
	public int executeUpdate(String sql, String... params) throws SQLException {
		PreparedStatement ps = getPreparedStatement(sql);
		for (int i = 0; i < params.length; i++) { // 数据库从1开始
			ps.setString(i + 1, params[i]);
		}
		return ps.executeUpdate();
	}

	/**
	 * 参数化SQL查询操作（只支持String类型参数）
	 */
	public ResultSet executeQuery(String sql, String... params) throws SQLException {
		PreparedStatement ps = getPreparedStatement(sql);
		for (int i = 0; i < params.length; i++) { // 数据库从1开始
			ps.setString(i + 1, params[i]);
		}
		return ps.executeQuery();
	}

	/**
	 * 获取普通SQL语句执行对象
	 */
	public Statement getStatement() throws SQLException {
		statement = conn.createStatement();
		return statement;
	}

	/**
	 * 普通SQL执行操作
	 */
	public int executeUpdate(String sql) throws SQLException {
		return getStatement().executeUpdate(sql);
	}

	/**
	 * 普通SQL查询操作
	 */
	public ResultSet executeQuery(String sql) throws SQLException {
		return getStatement().executeQuery(sql);
	}

}
