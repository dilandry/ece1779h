package ca.utoronto.ece1779.database;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.tomcat.dbcp.dbcp.cpdsadapter.DriverAdapterCPDS;
import org.apache.tomcat.dbcp.dbcp.datasources.SharedPoolDataSource;

public class DatabaseConnection {

    private volatile SharedPoolDataSource dbcp = null;
    private static volatile DatabaseConnection db = null;

    /**
     * Create a singleton object for DatabaseConnection, and return that object every time.
     * Uses the double-lock mechanisms for multi-threaded environment.
     *
     * Implementation obtained from Wikipedia page on Double-Locking
     *
     * @return DatabaseConnection singleton object
     */
    public static DatabaseConnection getInstance() {
        DatabaseConnection result = db;
        if (db == null) {
            synchronized (DatabaseConnection.class) {
                db = new DatabaseConnection();
                if (result == null) {
                    db = result = new DatabaseConnection();
                }
            }
        }
        return result;
    }

    private DatabaseConnection() {
        try {
            // force the driver to register itself
            Class.forName("com.mysql.jdbc.Driver");

            //Initialize connection pool
            DriverAdapterCPDS ds = new DriverAdapterCPDS();
            ds.setDriver("com.mysql.jdbc.Driver");
            ds.setUrl("jdbc:mysql://ece1779winter2015db.cf2zhhwzx2tf.us-east-1.rds.amazonaws.com:3306/ece1779group26");

            ds.setUser("group26");
            ds.setPassword("0982390126");

            dbcp = new SharedPoolDataSource();
            dbcp.setConnectionPoolDataSource(ds);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Return a connection to the database
     *
     * @return Connection
     */
    public Connection getConnection() {
        try {
            return getInstance().dbcp.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
