package io.papermc.hangar.components.webhook.dao;

import io.papermc.hangar.components.webhook.model.WebhookTable;
import java.util.List;
import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

@JdbiRepository
@RegisterConstructorMapper(WebhookTable.class)
public interface WebhookDAO {

    @SqlQuery("SELECT * FROM webhooks WHERE (:event = ANY(events) or '*' = ANY(events)) AND scope = 'global'")
    List<WebhookTable> getGlobalWebhooksForType(String event);
}
