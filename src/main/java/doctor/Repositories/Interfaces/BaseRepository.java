package doctor.Repositories.Interfaces;

import java.util.List;
import java.util.Optional;

public interface BaseRepository<T, ID> {
    T insert(T entity);

    Optional<T> selectById(ID id);

    List<T> selectAll();

    T update(T entity);

    void deleteById(ID id);
}

