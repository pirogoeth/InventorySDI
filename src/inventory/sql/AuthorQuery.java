package inventory.sql;

import com.mysql.jdbc.Statement;
import inventory.models.Author;
import inventory.util.Sql;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class AuthorQuery extends CachingConnector<Author> implements IQueryBase<Author> {

    /**
     * Static connector instance
     */
    protected static AuthorQuery instance = null;

    public static AuthorQuery getInstance() {
        return instance;
    }

    public AuthorQuery() {
        instance = this;
    }

    public void createTable() {
        String query = "CREATE TABLE IF NOT EXISTS `author` (" +
            "  `id` int(11) NOT NULL AUTO_INCREMENT," +
            "  `first_name` varchar(100) NOT NULL," +
            "  `last_name` varchar(100) NOT NULL," +
            "  `dob` date NOT NULL," +
            "  `gender` char(1) DEFAULT NULL," +
            "  `web_site` varchar(100) DEFAULT NULL," +
            "  `last_modified` timestamp DEFAULT CURRENT_TIMESTAMP," +
            "  PRIMARY KEY (`id`)" +
            ") ";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            // Could not create the prepared statement?
            LOG.warn("Could not create `author` table!");
            LOG.catching(ex);
        }

        String triggerQuery = "CREATE TRIGGER `updateLastModifiedTS` BEFORE UPDATE ON `author` " +
            "   FOR EACH ROW SET NEW.last_modified = CURRENT_TIMESTAMP()";

        try {
            PreparedStatement stmt = conn.prepareStatement(triggerQuery);
            stmt.executeUpdate();
        } catch ( SQLException ex ) {
            // Could not create the trigger - maybe it already exists?
            LOG.warn("Could not create trigger `updateLastModifiedTS` - maybe it already exists?");
        }
    }

    public boolean create(Author model) {
        String query = "INSERT INTO `author` (" +
            "`first_name`, `last_name`, `dob`, `gender`, `web_site`)" +
            "VALUES (?, ?, ?, ?, ?);";

        try {
            PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, model.getFirstName());
            stmt.setString(2, model.getLastName());
            stmt.setObject(3, model.getBirthDate());
            stmt.setString(4, String.valueOf(model.getGender().asChar()));
            stmt.setString(5, model.getSiteUrl());

            if ( stmt.executeUpdate() == 0 ) {
                // Insert failed! :(
                return false;
            }

            // Create succeeded, get the id.
            int creationId = Sql.getCreationId(stmt);
            model.setId(creationId);

            // Add the new model to the cache
            this.cacheItem(model);

            conn.commit();

            return true;
        } catch (SQLException ex) {
            // Could not create the prepared statement?
            LOG.warn("Could not insert into `author` table!");
            LOG.catching(ex);
        }

        // If we make it down here something went horribly wrong.
        LOG.warn("create() fell out of try/catch - something went wrong?");
        return false;
    }

    public boolean update(Author model) {
        String query = "UPDATE `author` SET " +
            "first_name=?," +
            "last_name=?," +
            "dob=?," +
            "gender=?," +
            "web_site=?" +
            "WHERE id=?";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, model.getFirstName());
            stmt.setString(2, model.getLastName());
            stmt.setObject(3, model.getBirthDate());
            stmt.setString(4, String.valueOf(model.getGender().asChar()));
            stmt.setString(5, model.getSiteUrl());
            stmt.setInt(6, model.getId());

            if ( stmt.executeUpdate() == 1 ) {
                // 1 row modified, PERFECT!
                conn.commit();

                // Invalidate the cache.
                this.invalidate();

                return true;
            }
        } catch (SQLException ex) {
            // Could not create the prepared statement?
            LOG.warn("Could not insert into `author` table!");
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

    public boolean delete(Author model) {
        // Only need to use the model's id for the delete
        String query = "DELETE FROM `author` WHERE id=?";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, model.getId());
            if ( stmt.executeUpdate() == 1 ) {
                // Delete success!
                // Unset the model's id.
                model.setId(-1);

                conn.commit();

                // Invalidate the cache.
                this.invalidate();

                return true;
            }
            LOG.info("delete() did not return 1 -- no rows affected?");
        } catch (SQLException ex) {
            // Could not create the prepared statement?
            LOG.warn("Could not delete from `author` table!");
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

    public ObservableList<Author> findAll() {
        if ( this.isDirty() ) {
            this.ensureCacheClean();
            return this.findAll();
        } else {
            return this.stream()
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
        }
    }

    public Author findById(int id) {
        if ( this.isDirty() ) {
            this.ensureCacheClean();
            return this.findById(id);
        } else {
            try {
                return this.filter(a -> a.getId() == id).findFirst().get();
            } catch (NoSuchElementException e) {
                return null;
            }
        }
    }

    protected void updateCache() {
        String query = "SELECT * FROM `author`";
        List<Author> all = new ArrayList<>();

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet r = stmt.executeQuery();

            while ( r.next() ) {
                Author a = new Author();
                a.setId(r.getInt("id"));
                a.setFirstName(r.getString("first_name"));
                a.setLastName(r.getString("last_name"));
                a.setBirthDate(r.getDate("dob"));
                a.setGender(Author.Gender.fromChar(r.getString("gender").charAt(0)));
                a.setSiteUrl(r.getString("web_site"));
                all.add(a);
            }
        } catch (SQLException ex) {
            // Could not create the prepared statement?
            LOG.warn("Could not select on `author` table!");
            LOG.catching(ex);
        }

        this.setCache(all);
    }

}
