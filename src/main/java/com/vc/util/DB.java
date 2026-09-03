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

            // Railway MySQL fallback variables
            if (dbUrl == null || dbUrl.isBlank()) {
                dbUrl = System.getenv("MYSQL_URL");
            }

            if (dbUrl == null || dbUrl.isBlank()) {
                dbUrl = System.getenv("MYSQL_PUBLIC_URL");
            }

            if (dbUrl == null || dbUrl.isBlank()) {
                throw new IllegalStateException(
                    "Database URL is not configured. Set DB_URL, MYSQL_URL, or MYSQL_PUBLIC_URL."
                );
            }

            dbUrl = dbUrl.replace("\"", "")
                         .replace("'", "")
                         .trim();

            // Convert mysql:// to jdbc:mysql:// if necessary
            if (dbUrl.startsWith("mysql://")) {
                dbUrl = "jdbc:" + dbUrl;
            }

            HikariConfig config = new HikariConfig();

            config.setJdbcUrl(dbUrl);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");

            if (dbUser != null && !dbUser.isBlank()) {
                config.setUsername(dbUser);
            }

            if (dbPassword != null && !dbPassword.isBlank()) {
                config.setPassword(dbPassword);
            }

            /*
             * Railway / MySQL connection settings
             */
            config.addDataSourceProperty(
                "allowPublicKeyRetrieval",
                "true"
            );

            config.addDataSourceProperty(
                "useSSL",
                "false"
            );

            config.addDataSourceProperty(
                "serverTimezone",
                "UTC"
            );

            config.addDataSourceProperty(
                "characterEncoding",
                "UTF-8"
            );

            config.addDataSourceProperty(
                "useUnicode",
                "true"
            );

            /*
             * HikariCP
             */
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);

            config.setConnectionTimeout(10000);
            config.setIdleTimeout(60000);
            config.setMaxLifetime(1800000);

            /*
             * Prepared statement cache
             */
            config.addDataSourceProperty(
                "cachePrepStmts",
                "true"
            );

            config.addDataSourceProperty(
                "prepStmtCacheSize",
                "250"
            );

            config.addDataSourceProperty(
                "prepStmtCacheSqlLimit",
                "2048"
            );

            dataSource = new HikariDataSource(config);

        } catch (Exception e) {

            throw new ExceptionInInitializerError(
                "Failed to initialize Hikari Connection Pool: "
                + e.getMessage()
            );
        }
    }

    private DB() {
        // Prevent creating DB objects
    }

    public static Connection getConnection()
            throws SQLException {

        return dataSource.getConnection();
    }

    public static void shutdown() {

        if (dataSource != null &&
            !dataSource.isClosed()) {

            dataSource.close();
        }
    }
}
