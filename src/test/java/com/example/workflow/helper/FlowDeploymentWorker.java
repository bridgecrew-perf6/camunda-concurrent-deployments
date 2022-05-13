package com.example.workflow.helper;

import org.camunda.bpm.engine.RepositoryService;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class FlowDeploymentWorker extends Thread{
    private CyclicBarrier _barrier;
    private CountDownLatch _latch;
    private final RepositoryService _repositoryService;

    public FlowDeploymentWorker(String name, CyclicBarrier cyclicBarrier, CountDownLatch latch, RepositoryService repositoryService) {
        _barrier = cyclicBarrier;
        _latch = latch;
        _repositoryService = repositoryService;
        setName(name);
    }

    @Override
    public void run() {
        try {
            _barrier.await();
            String tenant = "concurrency";
            String flowId = "MyTestFlow";
            String resourceName = flowId + ".bpmn";
            String flowContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" id=\"Definitions_09ad3x7\" targetNamespace=\"http://bpmn.io/schema/bpmn\" xmlns:modeler=\"http://camunda.org/schema/modeler/1.0\" modeler:executionPlatform=\"Camunda Platform\" modeler:executionPlatformVersion=\"7.15.0\">\n" +
                    "  <bpmn:process id=\"MyTestFlow\" isExecutable=\"true\">\n" +
                    "    <bpmn:startEvent id=\"StartEvent_1\" />\n" +
                    "  </bpmn:process>\n" +
                    "  <bpmndi:BPMNDiagram id=\"BPMNDiagram_1\">\n" +
                    "    <bpmndi:BPMNPlane id=\"BPMNPlane_1\" bpmnElement=\"Process_0hoss66\">\n" +
                    "      <bpmndi:BPMNShape id=\"_BPMNShape_StartEvent_2\" bpmnElement=\"StartEvent_1\">\n" +
                    "        <dc:Bounds x=\"179\" y=\"159\" width=\"36\" height=\"36\" />\n" +
                    "      </bpmndi:BPMNShape>\n" +
                    "    </bpmndi:BPMNPlane>\n" +
                    "  </bpmndi:BPMNDiagram>\n" +
                    "</bpmn:definitions>";
            _repositoryService.createDeployment()
                    .tenantId(tenant)
                    .source(tenant)
                    .name(flowId)
                    .addString(resourceName, flowContent)
                    .enableDuplicateFiltering(true)
                    .deployWithResult();
        } catch (IllegalStateException | InterruptedException | BrokenBarrierException e) {
            System.err.println("Failed loading BPMN file: " + e.getMessage());
        } finally {
            _latch.countDown();
        }
    }
}
