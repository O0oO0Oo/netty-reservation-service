package org.jmeter.step_1_normal_state.post_user10000;

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

/**
 * 유저 10000명
 * 1 ~ 1000 번의 유저 1번 아이템 예약 요청
 * 1001 ~ 2000 번의 유저 2번 아이템 예약 요청
 * ....
 * 9001 ~ 10000 번의 유저 10번 아이템 예약 요청
 *
 * 1 ~ 10번 아이템은 각 5개씩 총 50개가 있다.
 *
 *
 */
public class Main {
    public static void main(String[] args) throws IOException {

        JMeterUtils.setJMeterHome("E:/apache-jmeter-5.6.3/apache-jmeter-5.6.3");
        JMeterUtils.loadJMeterProperties("E:/apache-jmeter-5.6.3/apache-jmeter-5.6.3/bin/jmeter.properties");
        JMeterUtils.initLocale();

        StandardJMeterEngine jmeter = new StandardJMeterEngine();

        TestPlan testPlan = new TestPlan("Concurrent Transaction Test Plan");

        CSVDataSet csvDataSet = new CSVDataSet();
        String csvFilePath = Main.class.getResource("/post_user10000.csv").getPath();
        csvDataSet.setProperty("filename", csvFilePath);
        csvDataSet.setProperty("variableNames", "user_id,businessId,itemId,quantity");
        csvDataSet.setProperty("delimiter", ",");
        csvDataSet.setProperty("recycle", "true");
        csvDataSet.setProperty("stopThread", "false");
        csvDataSet.setProperty("shareMode", "shareMode.all");

        HTTPSamplerProxy registerHttpSampler = new HTTPSamplerProxy();
        registerHttpSampler.setDomain("localhost");
        registerHttpSampler.setPort(8080);
        registerHttpSampler.setPath("/user/${user_id}/reservation");
        registerHttpSampler.setMethod("POST");

        String jsonBody = "{ \"businessId\": \"${businessId}\", \"itemId\": \"${itemId}\", \"quantity\": \"${quantity}\" }";
        registerHttpSampler.addNonEncodedArgument("", jsonBody, "=");
        registerHttpSampler.setPostBodyRaw(true);

        HeaderManager headerManager = new HeaderManager();
        headerManager.add(new Header("Content-Type", "application/json"));
        registerHttpSampler.setHeaderManager(headerManager);

        LoopController loopController = new LoopController();
        loopController.setLoops(1);
        loopController.addTestElement(registerHttpSampler);
        loopController.setFirst(true);
        loopController.initialize();

        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setName("ConcurrentTransactionTest");
        threadGroup.setNumThreads(10000);
        threadGroup.setRampUp(1);
        threadGroup.setSamplerController(loopController);

        HashTree testPlanTree = new ListedHashTree();
        HashTree threadGroupTree = testPlanTree.add(testPlan);
        threadGroupTree.add(threadGroup);
        threadGroupTree.add(csvDataSet);
        threadGroupTree.add(registerHttpSampler);

        Summariser summer = null;
        String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
        if (summariserName.length() > 0) {
            summer = new Summariser(summariserName);
        }

        String logFile = "concurrent_transaction_test_results.jtl";
        ResultCollector logger = new ResultCollector(summer);
        logger.setFilename(logFile);
        testPlanTree.add(testPlanTree.getArray()[0], logger);

        SaveService.saveTree(testPlanTree, new FileOutputStream("concurrent_transaction_test.jmx"));

        jmeter.configure(testPlanTree);
        jmeter.run();
    }
}