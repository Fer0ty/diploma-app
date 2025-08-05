package diploma.ecommerce.backend.shopbase.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tenants")
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tenant_id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "subdomain", unique = true)
    private String subdomain;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "ozon_api_key", length = 500)
    private String ozonApiKey;

    @Column(name = "ozon_client_id", length = 500)
    private String ozonClientId;

    @Column(name = "ozon_warehouse_id")
    private Long ozonWarehouseId;

    @Column(name = "ozon_sync_enabled")
    private boolean ozonSyncEnabled = false;

    @Column(name = "ozon_last_sync_at")
    private LocalDateTime ozonLastSyncAt;

    @Column(name = "wildberries_api_key", length = 500)
    private String wildberriesApiKey;

    @Column(name = "wildberries_warehouse_id")
    private Long wildberriesWarehouseId;

    @Column(name = "wildberries_sync_enabled")
    private boolean wildberriesSyncEnabled = false;

    @Column(name = "wildberries_last_sync_at")
    private LocalDateTime wildberriesLastSyncAt;

    @Column(name = "yookassa_idempotency_key", length = 500)
    private String yookassaIdempotencyKey;

    @Column(name = "yookassa_secret_key", length = 500)
    private String yookassaSecretKey;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id")
    private Theme theme;
}