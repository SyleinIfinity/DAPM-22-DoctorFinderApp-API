package doctor.Repositories.Implements;

import doctor.Repositories.Interfaces.BaseRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

public abstract class BaseRepositoryImpl<T, ID> implements BaseRepository<T, ID> {
    protected final EntityManager entityManager;
    private final Class<T> entityClass;

    protected BaseRepositoryImpl(EntityManager entityManager, Class<T> entityClass) {
        this.entityManager = entityManager;
        this.entityClass = entityClass;
    }

    @Override
    @Transactional
    public T insert(T entity) {
        entityManager.persist(entity);
        entityManager.flush();
        return entity;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<T> selectById(ID id) {
        return Optional.ofNullable(entityManager.find(entityClass, id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> selectAll() {
        String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e";
        return entityManager.createQuery(jpql, entityClass).getResultList();
    }

    @Override
    @Transactional
    public T update(T entity) {
        T merged = entityManager.merge(entity);
        entityManager.flush();
        return merged;
    }

    @Override
    @Transactional
    public void deleteById(ID id) {
        T existing = entityManager.find(entityClass, id);
        if (existing != null) {
            entityManager.remove(existing);
            entityManager.flush();
        }
    }
}

