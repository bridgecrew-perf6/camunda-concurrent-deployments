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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ContextConfiguration(initializers = PostgresqlCreateDuplicateDeploymentTwoDatasourcesTests.DockerPostgresDataSourceInitializer.class)
@ActiveProfiles({"twodatasources"})
public class PostgresqlCreateDuplicateDeploymentTwoDatasourcesTests extends PreventDuplicateDeploymentsTests {

    public static PostgreSQLContainer<?> postgresDBContainer = new PostgreSQLContainer(DockerImageName.parse("postgres:12"));
    static {
        postgresDBContainer.start();
    }

    public static class DockerPostgresDataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    applicationContext,
                    "spring.datasource.primary.url=" + postgresDBContainer.getJdbcUrl(),
                    "spring.datasource.primary.username=" + postgresDBContainer.getUsername(),
                    "spring.datasource.primary.password=" + postgresDBContainer.getPassword(),
                    "spring.datasource.camunda.url=" + postgresDBContainer.getJdbcUrl(),
                    "spring.datasource.camunda.username=" + postgresDBContainer.getUsername(),
                    "spring.datasource.camunda.password=" + postgresDBContainer.getPassword()
            );
        }
    }

    @Test
    public void multipleConcurrentDeploymentsForSameFlowResultsInOneDeployment() throws BrokenBarrierException, InterruptedException {
        this.tryToCreateMultipleDuplicateDeploymentsAtOnce();;
    }
}
