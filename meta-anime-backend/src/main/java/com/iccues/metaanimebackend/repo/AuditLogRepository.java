package com.iccues.metaanimebackend.repo;

import com.iccues.metaanimebackend.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * 根据实体类型和实体ID查询审计日志
     */
    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            AuditLog.EntityType entityType, Long entityId);

    /**
     * 根据操作类型查询审计日志
     */
    Page<AuditLog> findByOperationType(AuditLog.OperationType operationType, Pageable pageable);

    /**
     * 根据实体类型查询审计日志
     */
    Page<AuditLog> findByEntityType(AuditLog.EntityType entityType, Pageable pageable);

    /**
     * 根据时间范围查询审计日志
     */
    Page<AuditLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    /**
     * 查询所有审计日志（分页）
     */
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
