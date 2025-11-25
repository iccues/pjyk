package com.iccues.metaanimebackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iccues.metaanimebackend.entity.AuditLog;
import com.iccues.metaanimebackend.repo.AuditLogRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class AuditLogService {

    @Resource
    private AuditLogRepository auditLogRepository;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 记录审计日志（使用新事务，确保即使主事务回滚也能记录）
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(AuditLog.OperationType operationType,
                    AuditLog.EntityType entityType,
                    Long entityId,
                    String description,
                    Object beforeData,
                    Object afterData,
                    String userName,
                    String ipAddress) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setOperationType(operationType);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setDescription(description);
            auditLog.setUserName(userName);
            auditLog.setIpAddress(ipAddress);

            if (beforeData != null) {
                auditLog.setBeforeData(objectMapper.writeValueAsString(beforeData));
            }

            if (afterData != null) {
                auditLog.setAfterData(objectMapper.writeValueAsString(afterData));
            }

            auditLogRepository.save(auditLog);
            log.debug("Audit log recorded: {} {} on {} id={}",
                      operationType, entityType, entityType.name().toLowerCase(), entityId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize audit log data", e);
        } catch (Exception e) {
            log.error("Failed to save audit log", e);
        }
    }

    /**
     * 查询指定实体的审计日志
     */
    public java.util.List<AuditLog> getEntityAuditLogs(AuditLog.EntityType entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
    }

    /**
     * 分页查询所有审计日志
     */
    public Page<AuditLog> getAllAuditLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    /**
     * 根据操作类型查询审计日志
     */
    public Page<AuditLog> getByOperationType(AuditLog.OperationType operationType, Pageable pageable) {
        return auditLogRepository.findByOperationType(operationType, pageable);
    }

    /**
     * 根据实体类型查询审计日志
     */
    public Page<AuditLog> getByEntityType(AuditLog.EntityType entityType, Pageable pageable) {
        return auditLogRepository.findByEntityType(entityType, pageable);
    }

    /**
     * 根据时间范围查询审计日志
     */
    public Page<AuditLog> getByTimeRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return auditLogRepository.findByCreatedAtBetween(start, end, pageable);
    }
}
