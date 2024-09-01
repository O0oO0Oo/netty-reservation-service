package org.server.rsaga.common.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "mysql")
@EnableConfigurationProperties
@EnableTransactionManagement
public class DataSourceConfig {
    private MySQLInstance master;
    private List<MySQLInstance> slaves;

    @Bean
    public DataSource dataSource() {
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("master", master.getDataSource());
        slaves.forEach(
                slave -> targetDataSources.put("replica", slave.getDataSource())
        );

        MasterReplicaRoutingDataSource replicaRoutingDataSource = new MasterReplicaRoutingDataSource();
        replicaRoutingDataSource.setTargetDataSources(targetDataSources);
        replicaRoutingDataSource.setDefaultTargetDataSource(master.getDataSource());

        return replicaRoutingDataSource;
    }

    @Data
    public static class MySQLInstance{
        private String driverClassName;
        private String url;
        private String username;
        private String password;

        DataSource getDataSource() {
            HikariDataSource dataSource = new HikariDataSource();
            dataSource.setDriverClassName(this.driverClassName);
            dataSource.setJdbcUrl(this.url);
            dataSource.setUsername(this.username);
            dataSource.setPassword(this.password);

            return dataSource;
        }
    }
}
