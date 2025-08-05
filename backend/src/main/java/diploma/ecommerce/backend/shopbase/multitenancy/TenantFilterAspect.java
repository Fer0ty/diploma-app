package diploma.ecommerce.backend.shopbase.multitenancy;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@Order(0)
public class TenantFilterAspect {

    @Autowired
    private EntityManager entityManager;

    @Pointcut("execution(public * diploma.ecommerce.backend.shopbase.repository..*(..))")
    public void repositoryMethods() {
    }

    @Around("repositoryMethods()")
    public Object enableTenantFilterIfNeeded(ProceedingJoinPoint joinPoint) throws Throwable {

        Session session = null;
        Long tenantId = TenantContext.getTenantId();

        if (entityManager != null && entityManager.isOpen()) {
            try {
                session = entityManager.unwrap(Session.class);
            } catch (Exception e) {
                log.trace(
                        "Could not unwrap Hibernate Session (perhaps no active transaction/session?) for method: {}. " +
                                "Proceeding without filter.",
                        joinPoint.getSignature().getName()
                );
                return joinPoint.proceed();
            }
        } else {
            log.trace(
                    "EntityManager closed or null, skipping filter logic for method: {}. Proceeding without filter.",
                    joinPoint.getSignature().getName()
            );
            return joinPoint.proceed();
        }

        try {
            if (tenantId != null) {
                log.trace(
                        "Setting tenantFilter parameter to tenantId: {} for method: {}",
                        tenantId, joinPoint.getSignature().toShortString()
                );
                session.enableFilter("tenantFilter")
                        .setParameter("tenantId", tenantId);
            } else {
                if (session.getEnabledFilter("tenantFilter") != null) {
                    log.trace(
                            "Disabling tenantFilter because tenantId is null for method: {}",
                            joinPoint.getSignature().toShortString()
                    );
                    session.disableFilter("tenantFilter");
                } else {
                    log.trace(
                            "TenantId is null and tenantFilter is already disabled for method: {}",
                            joinPoint.getSignature().toShortString()
                    );
                }
            }
        } catch (Exception e) {
            log.error(
                    "Error setting/disabling Hibernate tenant filter (tenantId: {}): {}",
                    tenantId, e.getMessage(), e
            );
            throw new RuntimeException("Failed to manage tenant filter state", e);
        }
        return joinPoint.proceed();
    }
}
