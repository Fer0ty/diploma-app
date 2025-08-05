package diploma.ecommerce.backend.shopbase.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"tenant", "product"})
@Entity
@Table(name = "product_ozon_mapping",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"tenant_id", "product_id"}),
                @UniqueConstraint(columnNames = {"tenant_id", "ozon_product_id"})
        }
)
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class ProductOzonMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mapping_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "ozon_product_id", nullable = false)
    private Long ozonProductId;

    @Column(name = "ozon_sku", nullable = false)
    private Long ozonSku;

    @Column(name = "ozon_fbo_sku")
    private Long ozonFboSku;

    @Column(name = "ozon_fbs_sku")
    private Long ozonFbsSku;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "last_price_sync")
    private LocalDateTime lastPriceSync;

    @Column(name = "last_stock_sync")
    private LocalDateTime lastStockSync;

    @Column(name = "sync_status", length = 20)
    @Enumerated(EnumType.STRING)
    private SyncStatus syncStatus = SyncStatus.PENDING;

    @Column(name = "sync_error", columnDefinition = "TEXT")
    private String syncError;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum SyncStatus {
        PENDING, SYNCING, SYNCED, ERROR
    }
}