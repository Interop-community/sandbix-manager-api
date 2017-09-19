package org.hspconsortium.sandboxmanagerapi;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.sql.DataSource;

@Component
public class BootstrappingFlywayMigration implements FlywayMigrationStrategy {

    private DataSource dataSource;

    private DataSourceProperties dataSourceProperties;

    @Inject
    public BootstrappingFlywayMigration setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        return this;
    }

    @Inject
    public void setDataSourceProperties(DataSourceProperties dataSourceProperties) {
        this.dataSourceProperties = dataSourceProperties;
    }

    @Override
    public void migrate(Flyway flyway) {
        flyway.setDataSource(dataSource);
        flyway.setSchemas(
                dataSourceProperties.getUrl().substring(
                        dataSourceProperties.getUrl().lastIndexOf("/") + 1));
        flyway.migrate();
    }
}
