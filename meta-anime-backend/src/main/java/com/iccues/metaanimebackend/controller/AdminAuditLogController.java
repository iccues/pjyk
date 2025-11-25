package com.iccues.metaanimebackend.controller;

import com.iccues.metaanimebackend.common.Response;
import com.iccues.metaanimebackend.entity.AuditLog;
import com.iccues.metaanimebackend.service.AuditLogService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/api/admin/audit-log")
@ResponseBody
@Slf4j
public class AdminAuditLogController {

    @Resource
    private AuditLogService auditLogService;

    /**
     * 分页查询所有审计日志
     */
    @GetMapping("/list")
    public Response<Page<AuditLog>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.debug("Getting audit logs, page={}, pageSize={}", page, pageSize);
        PageRequest pageRequest = PageRequest.of(page, pageSize);
        Page<AuditLog> auditLogs = auditLogService.getAllAuditLogs(pageRequest);
        return Response.ok(auditLogs);
    }

    /**
     * 根据实体类型和ID查询审计日志
     */
    @GetMapping("/entity/{entityType}/{entityId}")
    public Response<List<AuditLog>> getEntityAuditLogs(
            @PathVariable AuditLog.EntityType entityType,
            @PathVariable Long entityId) {
        log.debug("Getting audit logs for {} id={}", entityType, entityId);
        List<AuditLog> auditLogs = auditLogService.getEntityAuditLogs(entityType, entityId);
        return Response.ok(auditLogs);
    }

    /**
     * 根据操作类型查询审计日志
     */
    @GetMapping("/operation/{operationType}")
    public Response<Page<AuditLog>> getByOperationType(
            @PathVariable AuditLog.OperationType operationType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.debug("Getting audit logs for operation type={}, page={}, pageSize={}",
                  operationType, page, pageSize);
        PageRequest pageRequest = PageRequest.of(page, pageSize);
        Page<AuditLog> auditLogs = auditLogService.getByOperationType(operationType, pageRequest);
        return Response.ok(auditLogs);
    }

    /**
     * 根据实体类型查询审计日志
     */
    @GetMapping("/entity-type/{entityType}")
    public Response<Page<AuditLog>> getByEntityType(
            @PathVariable AuditLog.EntityType entityType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.debug("Getting audit logs for entity type={}, page={}, pageSize={}",
                  entityType, page, pageSize);
        PageRequest pageRequest = PageRequest.of(page, pageSize);
        Page<AuditLog> auditLogs = auditLogService.getByEntityType(entityType, pageRequest);
        return Response.ok(auditLogs);
    }

    /**
     * 根据时间范围查询审计日志
     */
    @GetMapping("/time-range")
    public Response<Page<AuditLog>> getByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.debug("Getting audit logs from {} to {}, page={}, pageSize={}",
                  start, end, page, pageSize);
        PageRequest pageRequest = PageRequest.of(page, pageSize);
        Page<AuditLog> auditLogs = auditLogService.getByTimeRange(start, end, pageRequest);
        return Response.ok(auditLogs);
    }
}
