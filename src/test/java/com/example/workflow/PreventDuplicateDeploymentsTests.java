package com.example.workflow;

import com.example.workflow.helper.FlowDeploymentWorker;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {Application.class})
public class PreventDuplicateDeploymentsTests {
    @Autowired
    private RepositoryService _repositoryService;

    public void tryToCreateMultipleDuplicateDeploymentsAtOnce() throws BrokenBarrierException, InterruptedException {
        String flowId = "MyTestFlow";

        List<ProcessDefinition> processDefinitionsBeforeLoad = _repositoryService
                .createProcessDefinitionQuery()
                .processDefinitionKey(flowId)
                .list();
        assertTrue(processDefinitionsBeforeLoad.size() == 0);

        int threadCount = 50;
        CyclicBarrier cyclicBarrier = new CyclicBarrier(threadCount + 1);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        List<FlowDeploymentWorker> workers = new ArrayList<>();
        IntStream.range(0, threadCount).forEach((num) -> {
            workers.add(new FlowDeploymentWorker("Worker-" + num, cyclicBarrier, countDownLatch, _repositoryService));
        });
        workers.forEach(worker -> worker.start());

        //kickoff workers
        cyclicBarrier.await();

        // wait until all workers are finished
        countDownLatch.await();

        List<ProcessDefinition> processDefinitions = _repositoryService
                .createProcessDefinitionQuery()
                .processDefinitionKey(flowId)
                .list();
        long distinctVersions = processDefinitions.stream().map(ProcessDefinition::getVersion).distinct().count();
        assertTrue(distinctVersions > 0);
        assertEquals(distinctVersions, processDefinitions.size());
    }
}
