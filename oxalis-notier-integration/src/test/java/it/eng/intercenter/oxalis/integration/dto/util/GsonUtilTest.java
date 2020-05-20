package it.eng.intercenter.oxalis.integration.dto.util;

import com.google.gson.Gson;
import it.eng.intercenter.oxalis.integration.util.GsonUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author Manuel Gozzi
 */
public class GsonUtilTest {

    private List<String> hashCodes_getInstance = new ArrayList<>();

    private List<String> hashCodes_getPrettyPrintedInstance = new ArrayList<>();

    @Test
    public void getInstance() throws InterruptedException {

        // Running 1000 threads simultaneously in order to check thread safe
        int threadCount = 1000;
        List<GsonGetInstance> tasks = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            tasks.add(new GsonGetInstance());
        }
        Executors.newFixedThreadPool(threadCount).invokeAll(tasks);
        boolean doubleInstancesFound = hashCodes_getInstance.stream()
                .anyMatch(hashCode -> Collections.frequency(hashCodes_getInstance, hashCode) > 1);
        Assert.assertFalse(doubleInstancesFound);

    }

    @Test
    public void getPrettyPrintedInstance() throws InterruptedException {

        // Running 1000 threads simultaneously in order to check thread safe
        int threadCount = 1000;
        List<GsonGetPrettyPrintedInstance> tasks = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            tasks.add(new GsonGetPrettyPrintedInstance());
        }
        Executors.newFixedThreadPool(threadCount).invokeAll(tasks);
        boolean doubleInstancesFound = hashCodes_getPrettyPrintedInstance.stream()
                .anyMatch(hashCode -> Collections.frequency(hashCodes_getPrettyPrintedInstance, hashCode) > 1);
        Assert.assertFalse(doubleInstancesFound);

    }

    class GsonGetInstance implements Callable<GsonGetInstance> {

        @Override
        public GsonGetInstance call() {
            hashCodes_getInstance.add(GsonUtil.getInstance().hashCode() + "");
            return null;
        }
    }

    class GsonGetPrettyPrintedInstance implements Callable<GsonGetPrettyPrintedInstance> {

        @Override
        public GsonGetPrettyPrintedInstance call() {
            hashCodes_getPrettyPrintedInstance.add(GsonUtil.getPrettyPrintedInstance().hashCode() + "");
            return null;
        }

    }

}
