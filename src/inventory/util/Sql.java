package inventory.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Some common SQL utilities.
 */
public class Sql {

    protected static Logger LOG = LogManager.getLogger(Sql.class);

    public static int getCreationId(PreparedStatement stmt) {
        try ( ResultSet genKeys = stmt.getGeneratedKeys() ) {
            if ( genKeys.next() ) {
                // Column 1 should *always* be the id
                return genKeys.getInt(1);
            } else {
                // Could not get id... did the create fail?
                LOG.warn("Could not obtain creation id - did create fail?");
                return -1;
            }
        } catch (SQLException e) {
            LOG.catching(e);
            LOG.error("Could not get created id from generated keys!");
        }

        return -1;
    }

}
