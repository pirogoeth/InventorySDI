package inventory.sql;

import com.mysql.jdbc.Statement;
import inventory.models.Audit;
import inventory.util.Sql;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuditQuery extends Connector implements IQueryBase<Audit> {

    /**
     * Static connector instance
     */
    protected static AuditQuery instance = null;

    public static AuditQuery getInstance() {
        return instance;
    }

    public AuditQuery() {
        instance = this;
    }

    public void createTable() {
        String query = "CREATE TABLE IF NOT EXISTS `audit_log` (" +
            "  `id` int(11) not null AUTO_INCREMENT," +
            "  `record_type` char(1) not null default 'a'," +
            "  `record_id` int not null," +
            "  `date_added` TIMESTAMP default CURRENT_TIMESTAMP, " +
            "  `entry_msg` varchar(255) not null," +
            "  PRIMARY KEY (`id`)" +
            "); ";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            int rowCount = stmt.executeUpdate();
            if ( rowCount < 1 ) return;
        } catch (SQLException ex) {
            // Could not create the prepared statement?
            LOG.warn("Could not create `audit_log` table!");
            LOG.catching(ex);
        }

        // Create indexes...
        String[] indexQueries = {
            "create index if not exists idxRecType on audit_log (record_type);",
            "create index if not exists idxRecId on audit_log (record_id);"
        };

        for (String indexQ : indexQueries) {
            try {
                PreparedStatement stmt = conn.prepareStatement(indexQ);
                stmt.executeUpdate();
            } catch (SQLException ex) {
                // Could not execute the index creations????
                LOG.warn("Could not execute index creation: " + indexQ);
                LOG.catching(ex);
            }
        }
    }

    public boolean create(Audit model) {
        String query = "INSERT INTO `audit_log` (" +
            "`record_type`, `record_id`, `date_added`, `entry_msg`)" +
            "VALUES (?, ?, ?, ?);";

        try {
            PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, model.getRecordType());
            stmt.setInt(2, model.getRecordId());
            stmt.setObject(3, model.getEntryDate());
            stmt.setString(4, model.getEntryMessage());

            if ( stmt.executeUpdate() == 0 ) {
                // Insert failed! :(
                return false;
            }

            // Create succeeded, get the id.
            int creationId = Sql.getCreationId(stmt);
            model.setId(creationId);

            conn.commit();

            return true;
        } catch (SQLException ex) {
            // Could not create the prepared statement?
            LOG.warn("Could not insert into `audit_log` table!");
            LOG.catching(ex);
        }

        try {
            conn.rollback();
        } catch ( SQLException ex ) {
            LOG.warn("Error while rolling back transaction");
            LOG.catching(ex);
        }

        // If we make it down here something went horribly wrong.
        LOG.warn("create() fell out of try/catch - something went wrong?");
        return false;
    }

    public boolean update(Audit model) {
        String query = "UPDATE `audit_log` SET " +
            "record_type=?," +
            "record_id=?," +
            "date_added=?," +
            "entry_msg=?," +
            "WHERE id=?";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, model.getRecordType());
            stmt.setInt(2, model.getRecordId());
            stmt.setObject(3, model.getEntryDate());
            stmt.setString(4, model.getEntryMessage());
            stmt.setInt(5, model.getId());

            if ( stmt.executeUpdate() == 1 ) {
                // 1 row modified, PERFECT!
                conn.commit();
                return true;
            }
        } catch (SQLException ex) {
            // Could not create the prepared statement?
            LOG.warn("Could not insert into `audit_log` table!");
            LOG.catching(ex);
        }

        try {
            conn.rollback();
        } catch ( SQLException ex ) {
            LOG.warn("Error while rolling back transaction");
            LOG.catching(ex);
        }

        return false;
    }

    public boolean delete(Audit model) {
        // Only need to use the model's id for the delete
        String query = "DELETE FROM `audit_log` WHERE id=?";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, model.getId());
            if ( stmt.executeUpdate() == 1 ) {
                // Delete success!
                //  XXX - Unset the model id here
                conn.commit();
                return true;
            }
            LOG.info("delete() did not return 1 -- no rows affected?");
        } catch (SQLException ex) {
            // Could not create the prepared statement?
            LOG.warn("Could not delete from `audit_log` table!");
            LOG.catching(ex);
        }

        try {
            conn.rollback();
        } catch ( SQLException ex ) {
            LOG.warn("Error while rolling back transaction");
            LOG.catching(ex);
        }

        return false;
    }

    public ObservableList<Audit> findAll() {
        String query = "SELECT * FROM `audit_log`;";
        ObservableList<Audit> all = FXCollections.observableArrayList();

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet r = stmt.executeQuery();

            while ( r.next() ) {
                Audit a = new Audit();
                a.setId(r.getInt("id"));
                a.setRecordType(r.getString("record_type"));
                a.setRecordId(r.getInt("record_id"));
                a.setEntryDate(r.getDate("date_added"));
                a.setEntryMessage(r.getString("entry_msg"));
                all.add(a);
            }
        } catch (SQLException ex) {
            // Could not create the prepared statement?
            LOG.warn("Could not select on `audit_log` table!");
            LOG.catching(ex);
        }

        return all;
    }

    public Audit findById(int id) {
        String query = "SELECT * FROM `audit_log` WHERE id=?;";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, id);
            ResultSet r = stmt.executeQuery();

            if ( !r.first() ) {
                // No results -- no match
                return null;
            } else {
                // Now at the first row -- id is primary, unique, so only one row max
                // will be returned.
                Audit a = new Audit();
                a.setId(r.getInt("id"));
                a.setRecordType(r.getString("record_type"));
                a.setRecordId(r.getInt("record_id"));
                a.setEntryDate(r.getDate("date_added"));
                a.setEntryMessage(r.getString("entry_msg"));
                return a;
            }
        } catch (SQLException ex) {
            // Could not create the prepared statement?
            LOG.warn("Could not select on `audit_log` table!");
            LOG.catching(ex);
        }

        // If something is not returned already, it's likely something went horribly wrong.
        LOG.error("findById() well out of try/catch - something went wrong?");
        return null;
    }

}

