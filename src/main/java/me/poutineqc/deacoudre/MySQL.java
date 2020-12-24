package me.poutineqc.deacoudre;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQL {

	private DeACoudre plugin;
	private Configuration config;
	
	private String host;
	private int port;
	private String database;
	private String user;
	private String password;

	private Connection connection;

	public MySQL() {
		connection = null;
	}

	public MySQL(DeACoudre plugin) {
		this.plugin = plugin;
		this.config = plugin.getConfiguration();
		
		this.host = config.host;
		this.port = config.port;
		this.database = config.database;
		this.user = config.user;
		this.password = config.password;

		connect();
	}

	public void updateInfo(DeACoudre plugin) {
		this.plugin = plugin;
		this.config = plugin.getConfiguration();
		
		this.host = config.host;
		this.port = config.port;
		this.database = config.database;
		this.user = config.user;
		this.password = config.password;
		
		connect();
	}

	public void connect() {
		try {
			connection = DriverManager.getConnection(
					"jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true", user, password);
		} catch (SQLException e) {
			plugin.getLogger().info("[MySQL] The connection to MySQL couldn't be made! reason: " + e.getMessage());
		}
	}

	public void close() {
		try {
			if (connection != null) {
				connection.close();
				plugin.getLogger().info("[MySQL] The connection to MySQL is ended successfully!");
			}
		} catch (SQLException e) {
			plugin.getLogger().info("[MySQL] The connection couldn't be closed! reason: " + e.getMessage());
		}
	}

	public void update(String qry) {
		try {
			PreparedStatement st = connection.prepareStatement(qry);
			st.execute();
			st.close();
		} catch (SQLException e) {
			connect();
			System.err.println(e);
		}
	}

	public boolean hasConnection() {
		return connection != null;
	}

	public ResultSet query(String qry) {
		ResultSet rs = null;
		try {
			PreparedStatement st = connection.prepareStatement(qry);
			rs = st.executeQuery();
		} catch (SQLException e) {
			connect();
			System.err.println(e);
		}
		return rs;
	}
}