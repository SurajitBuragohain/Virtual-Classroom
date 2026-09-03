package com.vc.util;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public final class DB {

    private static final HikariDataSource dataSource;

    static {
        try {
            String dbUrl = System.getenv("DB_URL");
            String dbUser = System.getenv("DB_USER");
            String dbPassword = System.getenv("DB_PASSWORD");

            if (dbUrl == null || dbUrl.isBlank()) {
                throw new IllegalStateException(
                    "DB_URL environment variable is not configured"
                );
            }

            if (dbUser == null || dbUser.isBlank()) {
                throw new IllegalStateException(
                    "DB_USER environment variable is not configured"
                );
            }

            if (dbPassword == null || dbPassword.isBlank()) {
                throw new IllegalStateException(
                    "DB_PASSWORD environment variable is not configured"
                );
            }

            // Remove accidental DB_URL= prefix
            if (dbUrl.startsWith("DB_URL=")) {
                dbUrl = dbUrl.substring("DB_URL=".length());
            }

            // Railway MYSQL_URL is mysql://...
            // MySQL JDBC driver requires jdbc:mysql://...
            if (dbUrl.startsWith("mysql://")) {
                dbUrl = "jdbc:" + dbUrl;
            }

            // Remove accidental quotes
            dbUrl = dbUrl.replace("\"", "").trim();

            HikariConfig config = new HikariConfig();

            config.setJdbcUrl(dbUrl);
            config.setUsername(dbUser);
            config.setPassword(dbPassword);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");

            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);

            config.setConnectionTimeout(10000);
            config.setIdleTimeout(60000);
            config.setMaxLifetime(1800000);

            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);

        } catch (Exception e) {
            throw new ExceptionInInitializerError(
                "Failed to initialize Hikari Connection Pool: "
                + e.getMessage()
            );
        }
    }

    private DB() {
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}