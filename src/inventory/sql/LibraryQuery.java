package inventory.sql;

import com.mysql.jdbc.Statement;
import inventory.models.Library;
import inventory.util.Sql;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LibraryQuery extends Connector implements IQueryBase<Library> {

    /**
     * Static connector instance
     */
    protected static LibraryQuery instance = null;

    public static LibraryQuery getInstance() {
        return instance;
    }

    public LibraryQuery() {
        instance = this;
    }

    public void createTable() {
        // Create the normal library lookup table
        String query = "CREATE TABLE IF NOT EXISTS `library` (" +
                "  `id` int(11) NOT NULL AUTO_INCREMENT," +
                "  `name` varchar(100) NOT NULL," +
                "  `last_modified` timestamp DEFAULT CURRENT_TIMESTAMP," +
                "  PRIMARY KEY (`id`)" +
                ") ";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            // Could not create the prepared statement?
            LOG.warn("Could not create `library` table!");
            LOG.catching(ex);
        }

        // Create an update-last-modified trigger on normal table
        String triggerQuery = "CREATE TRIGGER `lib_updateLastModifiedTS` BEFORE UPDATE ON `library` " +
                "   FOR EACH ROW SET NEW.last_modified = CURRENT_TIMESTAMP()";

        try {
            PreparedStatement stmt = conn.prepareStatement(triggerQuery);
            stmt.executeUpdate();
        } catch ( SQLException ex ) {
            // Could not create the trigger - maybe it already exists?
            LOG.warn("Could not create trigger `library/lib_updateLastModifiedTS` - maybe it already exists?");
        }
    }

    public boolean create(Library model) {
        String query = "INSERT INTO `library` (" +
                "  `name`)" +
                "  VALUES (?);";

        try {
            PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, model.getName());

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
            LOG.warn("Could not insert into `library` table!");
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

    public boolean update(Library model) {
        String query = "UPDATE `library` SET " +
                "  name=?" +
                "  WHERE id=?";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, model.getName());
            stmt.setInt(2, model.getId());

            if ( stmt.executeUpdate() == 1 ) {
                // 1 row modified, PERFECT!
                conn.commit();
                return true;
            }
        } catch (SQLException ex) {
            // Could not create the prepared statement?
            LOG.warn("Could not update `library` table!");
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

    public boolean delete(Library model) {
        // Only need to use the model's id for the delete
        String query = "DELETE FROM `library` WHERE id=?";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, model.getId());
            if ( stmt.executeUpdate() == 1 ) {
                // Delete success!
                // Unset the model's id.
                model.setId(-1);
                conn.commit();
                return true;
            }
            LOG.warn("delete() did not return 1 -- no rows affected?");
        } catch (SQLException ex) {
            // Could not create the prepared statement?
            LOG.warn("Could not delete from `library` table!");
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

    public ObservableList<Library> findAll() {
        String query = "SELECT * FROM `library`;";
        ObservableList<Library> all = FXCollections.observableArrayList();

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet r = stmt.executeQuery();

            while ( r.next() ) {
                Library l = new Library(
                        r.getInt("id"),
                        r.getString("name"),
                        r.getTimestamp("last_modified").toLocalDateTime()
                );
                all.add(l);
            }
        } catch (SQLException ex) {
            // Could not create the prepared statement?
            LOG.warn("Could not select on `library` table!");
            LOG.catching(ex);
        }

        return all;
    }

    public Library findById(int id) {
        String query = "SELECT * FROM `library` WHERE id=?";

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
                Library l = new Library(
                        r.getInt("id"),
                        r.getString("name"),
                        r.getTimestamp("last_modified").toLocalDateTime()
                );

                return l;
            }
        } catch (SQLException ex) {
            // Could not create the prepared statement?
            LOG.warn("Could not select on `library` table!");
            LOG.catching(ex);
        }

        // If something is not returned already, it's likely something went horribly wrong.
        LOG.error("findById() well out of try/catch - something went wrong?");
        return null;
    }

}


