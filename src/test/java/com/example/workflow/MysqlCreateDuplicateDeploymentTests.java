package com.example.workflow;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.BrokenBarrierException;


@ContextConfiguration(initializers = MysqlCreateDuplicateDeploymentTests.DockerPostgresDataSourceInitializer.class)
public class MysqlCreateDuplicateDeploymentTests extends PreventDuplicateDeploymentsTests {

    public static MySQLContainer<?> mySQLContainer = new MySQLContainer<>(DockerImageName.parse("mysql"));
    static {
        mySQLContainer.start();
    }

    public static class DockerPostgresDataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    applicationContext,
                    "spring.datasource.url=" + mySQLContainer.getJdbcUrl(),
                    "spring.datasource.username=" + mySQLContainer.getUsername(),
                    "spring.datasource.password=" + mySQLContainer.getPassword()
            );
        }
    }

    @Test
    public void multipleConcurrentDeploymentsForSameFlowResultsInOneDeployment() throws BrokenBarrierException, InterruptedException {
        this.tryToCreateMultipleDuplicateDeploymentsAtOnce();;
    }

}
