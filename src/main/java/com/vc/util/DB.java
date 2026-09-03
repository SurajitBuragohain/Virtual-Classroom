package com.vc.util;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public final class DB {

    private static final HikariDataSource dataSource;

    static {
        try {
            HikariConfig config = new HikariConfig();

            
            config.setJdbcUrl(
                "jdbc:mysql://localhost:3306/virtual_classroom"
                + "?useSSL=false"
                + "&allowPublicKeyRetrieval=true"
                + "&serverTimezone=Asia/Kolkata"
                + "&characterEncoding=UTF-8"
                + "&useUnicode=true"
            );

            config.setUsername("root");
            config.setPassword("@Surajit123");

            config.setDriverClassName(
                "com.mysql.cj.jdbc.Driver"
            );

            /*
             * HikariCP settings
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

    /**
     * Get a connection from HikariCP.
     */
    public static Connection getConnection()
            throws SQLException {

        return dataSource.getConnection();
    }

    /**
     * Close the HikariCP pool.
     */
    public static void shutdown() {

        if (dataSource != null &&
            !dataSource.isClosed()) {

            dataSource.close();
        }
    }
}