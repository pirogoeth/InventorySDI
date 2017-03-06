package inventory.sql;

import java.util.List;

public interface IQueryBase<T> {

    void createTable();
    boolean create(T model);
    boolean update(T model);
    boolean delete(T model);
    List<T> findAll();
    T findById(int id);
}
