package org.jmeter.orchestration_saga_request_1000;


import org.apache.jmeter.config.CSVDataSet;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;

import java.io.FileOutputStream;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {

        // JMeter 설정
        JMeterUtils.setJMeterHome("E:/apache-jmeter-5.6.3");
        JMeterUtils.loadJMeterProperties("E:/apache-jmeter-5.6.3/bin/jmeter.properties");
        JMeterUtils.initLocale();

        // JMeter 엔진 생성
        StandardJMeterEngine jmeter = new StandardJMeterEngine();

        // 테스트 플랜 생성
        TestPlan testPlan = new TestPlan("Concurrent Transaction Test Plan");

        // CSV DataSet 설정
        CSVDataSet csvDataSet = new CSVDataSet();
        String csvFilePath = Main.class.getResource("/request_user1000.csv").getPath();
        csvDataSet.setProperty("filename", csvFilePath);
        csvDataSet.setProperty("variableNames", "paymentType,userId,businessId,reservableItemId,reservableTimeId,requestQuantity");
        csvDataSet.setProperty("delimiter", ",");
        csvDataSet.setProperty("recycle", "true");  // CSV 파일 끝까지 반복해서 읽기
        csvDataSet.setProperty("stopThread", "false"); // 스레드가 멈추지 않도록 설정
        csvDataSet.setProperty("shareMode", "shareMode.all");

        // HTTP 샘플러 설정
        HTTPSamplerProxy registerHttpSampler = new HTTPSamplerProxy();
        registerHttpSampler.setDomain("192.168.35.191");
        registerHttpSampler.setPort(8080);
        registerHttpSampler.setPath("/reservations");
        registerHttpSampler.setMethod("POST");

        // JSON 요청 본문 설정
        String jsonBody = "{ \"paymentType\": \"${paymentType}\", \"userId\": \"${userId}\", \"businessId\": \"${businessId}\", \"reservableItemId\": \"${reservableItemId}\", \"reservableTimeId\": \"${reservableTimeId}\", \"requestQuantity\": \"${requestQuantity}\" }";
        registerHttpSampler.addNonEncodedArgument("", jsonBody, "=");
        registerHttpSampler.setPostBodyRaw(true);

        // HTTP 헤더 설정
        HeaderManager headerManager = new HeaderManager();
        headerManager.add(new Header("Content-Type", "application/json"));
        registerHttpSampler.setHeaderManager(headerManager);

        // 루프 컨트롤러 설정
        LoopController loopController = new LoopController();
        loopController.setLoops(1);
        loopController.addTestElement(registerHttpSampler);
        loopController.setFirst(true);
        loopController.initialize();

        // 스레드 그룹 설정
        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setName("ConcurrentTransactionTest");
        threadGroup.setNumThreads(10);
        threadGroup.setRampUp(1);
        threadGroup.setSamplerController(loopController);

        // 테스트 플랜 트리 구성
        HashTree testPlanTree = new ListedHashTree();
        HashTree threadGroupTree = testPlanTree.add(testPlan);
        threadGroupTree.add(threadGroup);
        threadGroupTree.add(csvDataSet);
        threadGroupTree.add(registerHttpSampler);

        // 요약 보고서 및 결과 수집기 설정
        Summariser summer = null;
        String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
        if (summariserName.length() > 0) {
            summer = new Summariser(summariserName);
        }

        String logFile = "concurrent_transaction_test_results.jtl";
        ResultCollector logger = new ResultCollector(summer);
        logger.setFilename(logFile);
        testPlanTree.add(testPlanTree.getArray()[0], logger);

        // JMX 파일로 테스트 플랜 저장 (선택사항)
        SaveService.saveTree(testPlanTree, new FileOutputStream("concurrent_transaction_test.jmx"));

        // 테스트 실행
        jmeter.configure(testPlanTree);
        jmeter.run();
    }
}
