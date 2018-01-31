package org.hobbit.smlbenchmark.examples.system;

import org.hobbit.core.components.Component;
import org.hobbit.sdk.ComponentsExecutor;
import org.hobbit.sdk.EnvironmentVariablesWrapper;
import org.hobbit.sdk.JenaKeyValue;
import org.hobbit.sdk.docker.AbstractDockerizer;
import org.hobbit.sdk.docker.RabbitMqDockerizer;
import org.hobbit.sdk.docker.builders.*;
import org.hobbit.sdk.docker.builders.common.PullBasedDockersBuilder;
import org.hobbit.sdk.utils.CommandQueueListener;
import org.hobbit.sdk.utils.commandreactions.MultipleCommandsReaction;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;

import static org.hobbit.sdk.CommonConstants.EXPERIMENT_URI;
import static org.hobbit.smlbenchmark.examples.system.ExampleDockersBuilder.SYSTEM_IMAGE_NAME;
import static org.hobbit.smlbenchmark.examples.system.ExampleDockersBuilder.SYSTEM_URI;

public class StreaMLSystemTest extends EnvironmentVariablesWrapper {
    private RabbitMqDockerizer rabbitMqDockerizer;
    private ComponentsExecutor componentsExecutor;
    private CommandQueueListener commandQueueListener;

    public static final String BENCHMARK_URI = "http://project-hobbit.eu/sml-benchmark-v1/";
    public static final String PROBABILITY_THRESHOLD_INPUT_NAME = BENCHMARK_URI+"probabilityThreshold";
    public static final String DATA_POINT_COUNT_INPUT_NAME = BENCHMARK_URI+"dataPointCount";
    public static final String WINDOW_SIZE_INPUT_NAME = BENCHMARK_URI+"windowSize";
    public static final String TRANSITIONS_COUNT_INPUT_NAME = BENCHMARK_URI+"transitionsCount";
    public static final String MAX_CLUSTER_ITERATIONS_INPUT_NAME = BENCHMARK_URI+"maxClusterIterations";
    public static final String INTERVAL_NANOS_INPUT_NAME = BENCHMARK_URI+"interval";
    public static final String SEED_INPUT_NAME = BENCHMARK_URI+"seed";
    public static final String FORMAT_INPUT_NAME = BENCHMARK_URI+"format";
    public static final String MACHINE_COUNT_INPUT_NAME = BENCHMARK_URI+"machineCount";
    public static final String TIMEOUT_MINUTES_INPUT_NAME = BENCHMARK_URI+"timeoutMinutes";
    public static final String BENCHMARK_MODE_INPUT_NAME = BENCHMARK_URI+"benchmarkMode";

    public static final String BENCHMARK_MODE_DEFAULT = "static"; //"dynamic:<initialMachinesCount>:<dataPointsBetweenMachinesCount>"
    public static final double PROBABILITY_THRESHOLD_DEFAULT = 0.005;
    public static final int DATAPOINTS_COUNT_DEFAULT = 63;
    public static final int MACHINE_COUNT_DEFAULT = 1;
    public static final int WINDOW_SIZE_DEFAULT = 20;
    public static final int TRANSITIONS_COUNT_DEFAULT = 5;
    public static final int MAX_CLUSTER_ITERATIONS_DEFAULT = 50;
    public static final int INTERVAL_NANOS_DEFAULT = 10;
    public static final int SEED_DEFAULT = 123;
    public static final int FORMAT_DEFAULT = 0;  //0=RDF, 1=CSV
    public static final int NO_TIMEOUT = -1;


    String benchmarkImageName = "git.project-hobbit.eu:4567/smirnp/sml-benchmark-v1/benchmark-controller";

    BenchmarkDockerBuilder benchmarkBuilder;
    SystemAdapterDockerBuilder systemAdapterBuilder;

    Component benchmarkController;
    Component systemAdapter;


    public void init(boolean useCachedImages) throws Exception {

        rabbitMqDockerizer = RabbitMqDockerizer.builder().build();

        setupCommunicationEnvironmentVariables(rabbitMqDockerizer.getHostName(), "session_"+String.valueOf(new Date().getTime()));
        setupBenchmarkEnvironmentVariables(EXPERIMENT_URI, createBenchmarkParameters());
        setupGeneratorEnvironmentVariables(1,1);
        setupSystemEnvironmentVariables(SYSTEM_URI, createSystemParameters());

        benchmarkBuilder = new BenchmarkDockerBuilder(new PullBasedDockersBuilder(benchmarkImageName));
        systemAdapterBuilder = new SystemAdapterDockerBuilder(new ExampleDockersBuilder(SystemAdapter.class, SYSTEM_IMAGE_NAME).useCachedImage(useCachedImages).init());

        benchmarkController = benchmarkBuilder.build();


        //Here you can switch between dockerized (by default) and pure java code of your system
        //systemAdapter = new SystemAdapter();
        systemAdapter = systemAdapterBuilder.build();
    }

    @Test
    @Ignore
    public void buildImages() throws Exception {
        init(false);
        ((AbstractDockerizer)systemAdapter).prepareImage();
    }

    @Test
    public void checkHealth() throws Exception {

        Boolean useCachedImages = true;

        init(useCachedImages);

        commandQueueListener = new CommandQueueListener();
        componentsExecutor = new ComponentsExecutor(commandQueueListener, environmentVariables);

        rabbitMqDockerizer.run();

        commandQueueListener.setCommandReactions(
                new MultipleCommandsReaction(componentsExecutor, commandQueueListener)
                        .systemContainerId(systemAdapterBuilder.getImageName())
        );

        componentsExecutor.submit(commandQueueListener);
        commandQueueListener.waitForInitialisation();

        componentsExecutor.submit(benchmarkController);
        componentsExecutor.submit(systemAdapter, systemAdapterBuilder.getImageName());

        commandQueueListener.waitForTermination();

        //rabbitMqDockerizer.stop();
        Assert.assertFalse(componentsExecutor.anyExceptions());

    }

    public JenaKeyValue createBenchmarkParameters() {

        JenaKeyValue kv = new JenaKeyValue();
        kv.setValue(BENCHMARK_MODE_INPUT_NAME, BENCHMARK_MODE_DEFAULT);
        kv.setValue(TIMEOUT_MINUTES_INPUT_NAME, NO_TIMEOUT);
        kv.setValue(DATA_POINT_COUNT_INPUT_NAME, DATAPOINTS_COUNT_DEFAULT);
        kv.setValue(MACHINE_COUNT_INPUT_NAME, MACHINE_COUNT_DEFAULT);
        kv.setValue(PROBABILITY_THRESHOLD_INPUT_NAME, PROBABILITY_THRESHOLD_DEFAULT);
        kv.setValue(WINDOW_SIZE_INPUT_NAME, WINDOW_SIZE_DEFAULT);
        kv.setValue(TRANSITIONS_COUNT_INPUT_NAME, TRANSITIONS_COUNT_DEFAULT);
        kv.setValue(MAX_CLUSTER_ITERATIONS_INPUT_NAME, MAX_CLUSTER_ITERATIONS_DEFAULT);
        kv.setValue(INTERVAL_NANOS_INPUT_NAME, INTERVAL_NANOS_DEFAULT);
        kv.setValue(SEED_INPUT_NAME, SEED_DEFAULT);
        kv.setValue(FORMAT_INPUT_NAME, FORMAT_DEFAULT);


        return kv;
    }

    private static JenaKeyValue createSystemParameters(){
        JenaKeyValue kv = new JenaKeyValue();
        kv.setValue(PROBABILITY_THRESHOLD_INPUT_NAME, PROBABILITY_THRESHOLD_DEFAULT);
        kv.setValue(WINDOW_SIZE_INPUT_NAME, WINDOW_SIZE_DEFAULT);
        kv.setValue(TRANSITIONS_COUNT_INPUT_NAME, TRANSITIONS_COUNT_DEFAULT);
        kv.setValue(MAX_CLUSTER_ITERATIONS_INPUT_NAME, MAX_CLUSTER_ITERATIONS_DEFAULT);
        kv.setValue(FORMAT_INPUT_NAME, FORMAT_DEFAULT);
        return kv;
    }
}
