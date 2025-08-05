@FilterDef(
        name = "tenantFilter",
        parameters = @ParamDef(
                name = "tenantId",
                type = Long.class
        )
)
package diploma.ecommerce.backend.shopbase.model;

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;