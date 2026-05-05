package doctor.Repositories.Implements;

import doctor.Models.Entities.LuotXemHoSoBacSi;
import doctor.Repositories.Interfaces.LuotXemHoSoBacSiRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class LuotXemHoSoBacSiRepositoryImpl extends BaseRepositoryImpl<LuotXemHoSoBacSi, Integer>
        implements LuotXemHoSoBacSiRepository {
    public LuotXemHoSoBacSiRepositoryImpl(EntityManager entityManager) {
        super(entityManager, LuotXemHoSoBacSi.class);
    }
}
