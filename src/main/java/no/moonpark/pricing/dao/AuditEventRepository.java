package no.moonpark.pricing.dao;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import no.moonpark.pricing.model.entity.AuditEventEntity;

public interface AuditEventRepository extends JpaRepository<AuditEventEntity, Long> {

    Page<AuditEventEntity> findByActor(String actor, Pageable pageable);

    Page<AuditEventEntity> findByAction(String action, Pageable pageable);
}
