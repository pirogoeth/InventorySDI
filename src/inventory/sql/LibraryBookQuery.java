package inventory.sql;

import com.mysql.jdbc.Statement;
import inventory.models.LibraryBook;
import inventory.util.Sql;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LibraryBookQuery extends Connector implements IQueryBase<LibraryBook> {

    /**
     * Static connector instance
     */
    protected static LibraryBookQuery instance = null;

    public static LibraryBookQuery getInstance() {
        return instance;
    }

    public LibraryBookQuery() {
        instance = this;
    }

    public void createTable() {
        // Create the library-book junction
        String query = "CREATE TABLE IF NOT EXISTS `library_book` (" +
                "   `id` int(11) NOT NULL AUTO_INCREMENT," +
                "   `library_id` int(11) NOT NULL," +
                "   `book_id` int(11) NOT NULL," +
                "   `quantity` int(11) NOT NULL," +
                "   `last_modified` timestamp DEFAULT CURRENT_TIMESTAMP," +
                "  PRIMARY KEY (`id`)," +
                "  FOREIGN KEY (`library_id`) REFERENCES library(`id`)" +
                "       ON DELETE CASCADE" +
                "       ON UPDATE CASCADE," +
                "  FOREIGN KEY (`book_id`) REFERENCES book(`id`)" +
                "       ON DELETE CASCADE" +
                "       ON UPDATE CASCADE" +
                ")";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            // Could not create the prepared statement?
            LOG.warn("Could not create `library_book` table!");
            LOG.catching(ex);
        }

        // Create an update-last-modified trigger on normal table
        String triggerQuery = "CREATE TRIGGER `libbook_updateLastModifiedTS` BEFORE UPDATE ON `library_book` " +
                "   FOR EACH ROW SET NEW.last_modified = CURRENT_TIMESTAMP()";

        try {
            PreparedStatement stmt = conn.prepareStatement(triggerQuery);
            stmt.executeUpdate();
        } catch ( SQLException ex ) {
            // Could not create the trigger - maybe it already exists?
            LOG.warn("Could not create trigger `library_book/libbook_updateLastModifiedTS` - maybe it already exists?");
        }
    }

    public boolean create(LibraryBook model) {
        String query = "INSERT INTO `library_book` (" +
                "    `library_id`, `book_id`, `quantity`" +
                "  )" +
                "  VALUES (?, ?, ?)";

        try {
            PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, model.getLibraryId());
            stmt.setInt(2, model.getBookId());
            stmt.setInt(3, model.getQuantity());

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
            LOG.warn("Could not insert into `library_book` table!");
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

    public boolean update(LibraryBook model) {
        String query = "UPDATE `library_book` SET " +
                "  author_id=?," +
                "  book_id=?," +
                "  quantity=?" +
                "  WHERE id=?";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, model.getLibraryId());
            stmt.setInt(2, model.getBookId());
            stmt.setInt(3, model.getQuantity());

            if ( stmt.executeUpdate() == 1 ) {
                // 1 row modified, PERFECT!
                conn.commit();
                return true;
            }
        } catch (SQLException ex) {
            // Could not create the prepared statement?
            LOG.warn("Could not update `library_book` table!");
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

    public boolean delete(LibraryBook model) {
        // Only need to use the model's id for the delete
        String query = "DELETE FROM `library_book` WHERE id=?";

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
            LOG.warn("Could not delete from `library_book` table!");
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

    public ObservableList<LibraryBook> findAll() {
        String query = "SELECT * FROM `library_book`;";
        ObservableList<LibraryBook> all = FXCollections.observableArrayList();

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet r = stmt.executeQuery();

            while ( r.next() ) {
                LibraryBook lb = new LibraryBook(
                        r.getInt("id"),
                        r.getInt("book_id"),
                        r.getInt("library_id"),
                        r.getInt("quantity"),
                        r.getTimestamp("last_modified").toLocalDateTime()
                );
                all.add(lb);
            }
        } catch (SQLException ex) {
            // Could not create the prepared statement?
            LOG.warn("Could not select on `library_book` table!");
            LOG.catching(ex);
        }

        return all;
    }

    public LibraryBook findById(int id) {
        String query = "SELECT * FROM `library_book` WHERE id=?";

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
                LibraryBook lb = new LibraryBook(
                        r.getInt("id"),
                        r.getInt("book_id"),
                        r.getInt("library_id"),
                        r.getInt("quantity"),
                        r.getTimestamp("last_modified").toLocalDateTime()
                );

                return lb;
            }
        } catch (SQLException ex) {
            // Could not create the prepared statement?
            LOG.warn("Could not select on `library_book` table!");
            LOG.catching(ex);
        }

        // If something is not returned already, it's likely something went horribly wrong.
        LOG.error("findById() well out of try/catch - something went wrong?");
        return null;
    }

}


