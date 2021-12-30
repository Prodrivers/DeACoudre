package me.poutineqc.deacoudre;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.*;
import java.util.Optional;

@Singleton
public class MySQL {
	private Configuration config;

	private String host;
	private int port;
	private String database;
	private String user;
	private String password;

	private Connection connection;

	@Inject
	public MySQL(DeACoudre plugin) {
		this.config = plugin.getConfiguration();

		if(config.mysql) {
			this.host = config.host;
			this.port = config.port;
			this.database = config.database;
			this.user = config.user;
			this.password = config.password;

			connect();
		}
	}

	public void updateInfo(DeACoudre plugin) {
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
		} catch(SQLException e) {
			Log.info("[MySQL] The connection to MySQL couldn't be made! reason: " + e.getMessage());
		}
	}

	public void close() {
		try {
			if(connection != null) {
				connection.close();
				Log.info("[MySQL] The connection to MySQL is ended successfully!");
			}
		} catch(SQLException e) {
			Log.info("[MySQL] The connection couldn't be closed! reason: " + e.getMessage());
		}
	}

	public Optional<PreparedStatement> getPreparedStatement(String qry) {
		try {
			return Optional.of(connection.prepareStatement(qry));
		} catch(SQLException e) {
			connect();
		}
		return Optional.empty();
	}

	public void update(String qry) {
		try {
			PreparedStatement st = connection.prepareStatement(qry);
			st.execute();
			st.close();
		} catch(SQLException e) {
			connect();
			Log.severe("Error on SQL update.", e);
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
		} catch(SQLException e) {
			connect();
			Log.severe("Error on SQL query.", e);
		}
		return rs;
	}
}