package inventory.sql;

import com.mysql.jdbc.Statement;
import inventory.models.Book;
import inventory.util.Sql;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class BookQuery extends Connector implements IQueryBase<Book> {

    /**
     * Static connector instance
     */
    protected static BookQuery instance = null;

    public static BookQuery getInstance() {
        return instance;
    }

    public BookQuery() {
        instance = this;
    }

    public void createTable() {
        // Create the table
        String query = "CREATE TABLE IF NOT EXISTS `books` (" +
                "  `id` int(11) NOT NULL AUTO_INCREMENT," +
                "  `title` varchar(100) NOT NULL," +
                "  `publisher` varchar(100) NOT NULL," +
                "  `date_published` date NOT NULL," +
                "  `summary` text NOT NULL," +
                "  `author_id` int(11) NOT NULL," +
                "  `last_modified` timestamp DEFAULT CURRENT_TIMESTAMP," +
                "  PRIMARY KEY (`id`)," +
                "  FOREIGN KEY (`author_id`) REFERENCES authors(`id`)" +
                ") ";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            // Could not create the prepared statement?
            LOG.warn("Could not create `books` table!");
            LOG.catching(ex);
        }

        // XXX - Add trigger creation code here!
//        String trigger = "CREATE TRIGGER";
    }

    public boolean create(Book model) {
        String query = "INSERT INTO `books` (" +
                "`title`, `publisher`, `date_published`, `summary`, `author_id`)" +
                "VALUES (?, ?, ?, ?, ?);";

        try {
            PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, model.getTitle());
            stmt.setString(2, model.getPublisher());
            stmt.setObject(3, model.getPublishDate());
            stmt.setString(4, model.getSummary());
            stmt.setInt(5, model.getAuthorId());

            if ( stmt.executeUpdate() == 0 ) {
                // Insert failed! :(
                return false;
            }

            // Create succeeded, get the id.
            int creationId = Sql.getCreationId(stmt);
            model.setId(creationId);

            return true;
        } catch (SQLException ex) {
            // Could not create the prepared statement?
            LOG.warn("Could not insert into `books` table!");
            LOG.catching(ex);
        }

        // If we make it down here something went horribly wrong.
        LOG.warn("create() fell out of try/catch - something went wrong?");
        return false;
    }

    public boolean update(Book model) {
        String query = "UPDATE `books` SET " +
                "title=?," +
                "publisher=?," +
                "date_published=?," +
                "summary=?," +
                "author_id=?" +
                "WHERE id=?";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, model.getTitle());
            stmt.setString(2, model.getPublisher());
            stmt.setObject(3, model.getPublishDate());
            stmt.setString(4, model.getSummary());
            stmt.setInt(5, model.getAuthorId());
            stmt.setInt(6, model.getId());

            if ( stmt.executeUpdate() == 1 ) {
                // 1 row modified, PERFECT!
                return true;
            }
        } catch (SQLException ex) {
            // Could not create the prepared statement?
            LOG.warn("Could not insert into `books` table!");
            LOG.catching(ex);
        }

        return false;
    }

    public boolean delete(Book model) {
        // Only need to use the model's id for the delete
        String query = "DELETE FROM `books` WHERE id=?";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, model.getId());
            if ( stmt.executeUpdate() == 1 ) {
                // Delete success!
                // Unset the model's id.
                model.setId(-1);
                return true;
            }
            LOG.warn("delete() did not return 1 -- no rows affected?");
        } catch (SQLException ex) {
            // Could not create the prepared statement?
            LOG.warn("Could not delete from `books` table!");
            LOG.catching(ex);
        }

        return false;
    }

    public ObservableList<Book> findAll() {
        String query = "SELECT * FROM `books`;";
        ObservableList<Book> all = FXCollections.observableArrayList();

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet r = stmt.executeQuery();

            while ( r.next() ) {
                Book b = new Book(
                        r.getInt("id"),
                        r.getString("title"),
                        r.getString("publisher"),
                        r.getDate("date_published"),
                        r.getString("summary"),
                        r.getInt("author_id"),
                        LocalDateTime.from(r.getDate("last_modified").toInstant())
                );
                all.add(b);
            }
        } catch (SQLException ex) {
            // Could not create the prepared statement?
            LOG.warn("Could not select on `books` table!");
            LOG.catching(ex);
        }

        return all;
    }

    public Book findById(int id) {
        String query = "SELECT * FROM `books` WHERE id=?;";

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
                Book b = new Book(
                        r.getInt("id"),
                        r.getString("title"),
                        r.getString("publisher"),
                        r.getDate("date_published"),
                        r.getString("summary"),
                        r.getInt("author_id"),
                        LocalDateTime.from(r.getTimestamp("last_modified").toInstant())
                );

                return b;
            }
        } catch (SQLException ex) {
            // Could not create the prepared statement?
            LOG.warn("Could not select on `books` table!");
            LOG.catching(ex);
        }

        // If something is not returned already, it's likely something went horribly wrong.
        LOG.error("findById() well out of try/catch - something went wrong?");
        return null;
    }

}
