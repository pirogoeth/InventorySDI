package inventory.sql;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;

abstract class Connector {

    protected static Logger LOG = LogManager.getLogger(Connector.class);

    /**
     * Database connection
     */
    static Connection conn;

    public static void setConnection(Connection c) {
        conn = c;
    }
}
