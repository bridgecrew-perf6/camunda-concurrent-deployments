package com.example.workflow;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.BrokenBarrierException;

@ContextConfiguration(initializers = PostgresqlCreateDuplicateDeploymentTests.DockerPostgresDataSourceInitializer.class)
@ActiveProfiles({"onedatasource"})
public class PostgresqlCreateDuplicateDeploymentTests extends PreventDuplicateDeploymentsTests {

    public static PostgreSQLContainer<?> postgresDBContainer = new PostgreSQLContainer(DockerImageName.parse("postgres:12"));
    static {
        postgresDBContainer.start();
    }

    public static class DockerPostgresDataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    applicationContext,
                    "spring.datasource.url=" + postgresDBContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgresDBContainer.getUsername(),
                    "spring.datasource.password=" + postgresDBContainer.getPassword()
            );
        }
    }

    @Test
    public void multipleConcurrentDeploymentsForSameFlowResultsInOneDeployment() throws BrokenBarrierException, InterruptedException {
        this.tryToCreateMultipleDuplicateDeploymentsAtOnce();;
    }
}
