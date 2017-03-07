package inventory.sql;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import inventory.app.InventoryProps;
import inventory.controller.WaitingPane;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class JdbcLoader {

    // Root logger for the application
    private static Logger LOG = LogManager.getLogger(JdbcLoader.class);

    // Singleton instance
    private static JdbcLoader instance = null;

    public static JdbcLoader getInstance() {
        if ( instance == null ) {
            return new JdbcLoader();
        }

        return instance;
    }

    // Mysql DataSource
    private MysqlDataSource dataSource = null;

    // Datasource connection
    private Connection dsConn = null;

    public JdbcLoader() {
        Properties props = InventoryProps.getInstance().getProps();
        this.dataSource = new MysqlDataSource();
        this.dataSource.setUrl(props.getProperty("jdbc_url"));
        this.dataSource.setUser(props.getProperty("jdbc_username"));
        this.dataSource.setPassword(props.getProperty("jdbc_password"));

        this.connect();
    }

    public void connect() {
        // Show the waiting pane..
        WaitingPane.display();
        WaitingPane.setDetail("Connecting to database..");

        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws Exception {
                try {
                    open();
                } catch (Exception ex) {
                    // This actually shouldn't happen here...
                    LOG.fatal("Could not open database connection!");
                    LOG.catching(ex);

                    // Retry connection..
                    WaitingPane.setDetail("Could not connect, retrying...");
                    try {
                        Thread.sleep(2000L);
                    } catch (InterruptedException iex) {
                        LOG.catching(ex);
                    }
                    connect();
                }

                LOG.info("Database connection opened!");
                Platform.runLater(() -> WaitingPane.hide());
                return null;
            }
        };
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private void open() throws Exception {
        if ( this.dsConn != null ) {
            // Connection is already opened...?
            throw new Exception("Can't open already opened connection!");
        }

        LOG.debug("Opening database connection..");

        try {
            this.dsConn = this.dataSource.getConnection();
        } catch (SQLException ex) {
            // Invalid database credentials?
            LOG.catching(ex);
        }

        Class<? extends Connector>[] queryClasses = new Class[]{AuthorQuery.class, AuditQuery.class, BookQuery.class};

        for (Class<? extends Connector> cls : queryClasses) {
            // Set the connection object in the IQueryBase<T> + Connector classes.
            Method setConn = cls.getMethod("setConnection", Connection.class);
            setConn.invoke(null, this.dsConn);

            // Ensure that all Connector / IQueryBase<T> classes have their tables created
            Constructor cons = cls.getConstructor();
            IQueryBase<?> inst = (IQueryBase<?>) cons.newInstance();
            LOG.debug("Ensuring tables exist for IQueryBase: " + inst.getClass().getSimpleName());
            inst.createTable();
        }

        // All further database actions will be transactions.
        dsConn.setAutoCommit(false);
    }

    public void close() {
        try {
            this.dsConn.close();
        } catch (SQLException ex) {
            // Invalid database credentials?
            LOG.catching(ex);
        }
    }

    public Connection getConnection() {
        return this.dsConn;
    }

}
