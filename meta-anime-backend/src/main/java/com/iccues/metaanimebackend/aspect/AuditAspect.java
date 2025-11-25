package com.iccues.metaanimebackend.aspect;

import com.iccues.metaanimebackend.annotation.Audit;
import com.iccues.metaanimebackend.common.Response;
import com.iccues.metaanimebackend.service.AuditLogService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
public class AuditAspect {

    @Resource
    private AuditLogService auditLogService;

    @Around("@annotation(com.iccues.metaanimebackend.annotation.Audit)")
    public Object auditLog(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Audit audit = method.getAnnotation(Audit.class);

        Object[] args = joinPoint.getArgs();
        Object beforeData = null;
        Object afterData = null;
        Long entityId = null;

        // 获取请求信息
        String ipAddress = null;
        try {
            ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                ipAddress = getClientIp(request);
            }
        } catch (Exception e) {
            log.debug("Failed to get request context", e);
        }

        try {
            // 执行目标方法
            Object result = joinPoint.proceed();

            // 尝试从返回值中提取实体ID和数据
            if (result != null) {
                if (result instanceof Response<?> response) {
                    afterData = response.getData();
                    entityId = extractEntityId(afterData);
                } else {
                    afterData = result;
                    entityId = extractEntityId(result);
                }
            }

            // 如果没有从返回值中获取到实体ID，尝试从参数中获取
            if (entityId == null) {
                entityId = extractEntityIdFromArgs(args);
            }

            // 记录审计日志
            if (entityId != null) {
                auditLogService.log(
                    audit.operation(),
                    audit.entityType(),
                    entityId,
                    audit.description(),
                    beforeData,
                    afterData,
                    null, // 用户名，后续可以从认证上下文获取
                    ipAddress
                );
            }

            return result;
        } catch (Exception e) {
            log.error("Error in audit aspect", e);
            throw e;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private Long extractEntityId(Object obj) {
        if (obj == null) {
            return null;
        }

        // 尝试常见的ID字段名
        String[] idFieldNames = {"id", "animeId", "mappingId"};
        for (String fieldName : idFieldNames) {
            try {
                var idField = obj.getClass().getDeclaredField(fieldName);
                idField.setAccessible(true);
                Object id = idField.get(obj);
                if (id instanceof Long) {
                    return (Long) id;
                } else if (id instanceof Integer) {
                    return ((Integer) id).longValue();
                }
            } catch (Exception e) {
                // 继续尝试下一个字段名
            }
        }

        // 尝试通过方法获取
        String[] getterNames = {"getId", "getAnimeId", "getMappingId"};
        for (String methodName : getterNames) {
            try {
                var getIdMethod = obj.getClass().getMethod(methodName);
                Object id = getIdMethod.invoke(obj);
                if (id instanceof Long) {
                    return (Long) id;
                } else if (id instanceof Integer) {
                    return ((Integer) id).longValue();
                }
            } catch (Exception e) {
                // 继续尝试下一个方法
            }
        }

        log.debug("Cannot extract entity id from object of type {}", obj.getClass().getName());
        return null;
    }

    private Long extractEntityIdFromArgs(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            } else if (arg instanceof Integer) {
                return ((Integer) arg).longValue();
            } else if (arg != null) {
                Long id = extractEntityId(arg);
                if (id != null) {
                    return id;
                }
            }
        }
        return null;
    }
}
