package doctor.Repositories.Implements;

import doctor.Models.Entities.LichSuTimKiemBacSi;
import doctor.Repositories.Interfaces.LichSuTimKiemBacSiRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class LichSuTimKiemBacSiRepositoryImpl extends BaseRepositoryImpl<LichSuTimKiemBacSi, Integer>
        implements LichSuTimKiemBacSiRepository {
    public LichSuTimKiemBacSiRepositoryImpl(EntityManager entityManager) {
        super(entityManager, LichSuTimKiemBacSi.class);
    }
}
