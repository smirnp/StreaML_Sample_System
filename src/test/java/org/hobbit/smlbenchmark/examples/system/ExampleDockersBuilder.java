package org.hobbit.smlbenchmark.examples.system;


import org.hobbit.sdk.docker.BuildBasedDockerizer;
import org.hobbit.sdk.docker.builders.common.DynamicDockerFileBuilder;

/**
 * @author Pavel Smirnov
 */

public class ExampleDockersBuilder extends DynamicDockerFileBuilder {

    public static String GIT_REPO_PATH = "git.project-hobbit.eu:4567/smirnp/";
    //public static String GIT_REPO_PATH = "";
    public static String PROJECT_NAME = "sml-v1-example-system/";
    public static final String SYSTEM_URI = "http://project-hobbit.eu/"+PROJECT_NAME;

    //use these constants within BenchmarkController

    public static final String SYSTEM_IMAGE_NAME = GIT_REPO_PATH+PROJECT_NAME +"system-adapter";

    public ExampleDockersBuilder(Class runnerClass, String imageName) throws Exception {
        super("ExampleDockersBuilder");
        imageName(imageName);
        //user-friendly name for searching in logs
        containerName(runnerClass.getSimpleName());
        //temp docker file will be created there
        buildDirectory(".");
        //should be packaged will all dependencies (via 'mvn package' command)
        jarFilePath("target/sml-example-system-1.0.jar");
        //will be placed in temp dockerFile
        dockerWorkDir("/usr/src/"+PROJECT_NAME);
        //will be placed in temp dockerFile
        runnerClass(org.hobbit.core.run.ComponentStarter.class, runnerClass);
    }

}
