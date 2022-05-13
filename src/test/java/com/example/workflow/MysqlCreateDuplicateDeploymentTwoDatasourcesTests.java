package com.example.workflow;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.BrokenBarrierException;

@ContextConfiguration(initializers = MysqlCreateDuplicateDeploymentTwoDatasourcesTests.DockerPostgresDataSourceInitializer.class)
@ActiveProfiles({"twodatasources"})
public class MysqlCreateDuplicateDeploymentTwoDatasourcesTests extends PreventDuplicateDeploymentsTests {

    public static MySQLContainer<?> mySQLContainer = new MySQLContainer<>(DockerImageName.parse("mysql"));
    static {
        mySQLContainer.start();
    }

    public static class DockerPostgresDataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    applicationContext,
                    "spring.datasource.primary.url=" + mySQLContainer.getJdbcUrl(),
                    "spring.datasource.primary.username=" + mySQLContainer.getUsername(),
                    "spring.datasource.primary.password=" + mySQLContainer.getPassword(),
                    "spring.datasource.camunda.url=" + mySQLContainer.getJdbcUrl(),
                    "spring.datasource.camunda.username=" + mySQLContainer.getUsername(),
                    "spring.datasource.camunda.password=" + mySQLContainer.getPassword()
            );
        }
    }

    @Test
    public void multipleConcurrentDeploymentsForSameFlowResultsInOneDeployment() throws BrokenBarrierException, InterruptedException {
        this.tryToCreateMultipleDuplicateDeploymentsAtOnce();;
    }

}
