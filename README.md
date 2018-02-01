# StreaML Open Challenge

Here you can find some helpful sourcecodes which should ease your integration into the HOBBIT platform and participation in the [StreaML Open Challenge](https://project-hobbit.eu/open-challenges/streaml-open-challenge). 

The sourcecodes use the [HOBBIT Java SDK](https://github.com/hobbit-project/java-sdk-example) and should allow challenge participants to debug their systems using benchmark workload locally without having a running platform instance. 
The benchmarking system can be tested as pure java code or/and being packed (automatically, by demand) into docker image. 
Finally the docker image of the system may be uploaded into the [HOBBIT online platform](http://master.project-hobbit.eu) (as described [here](https://github.com/hobbit-project/platform/wiki/Push-a-docker-image) and [here](https://github.com/hobbit-project/platform/wiki/System-meta-data-file)) and executed there under the online StreaML v1 Benchmark, which is used for the challenge.

# Usage
## Before you start
1) Make sure that docker (v17 and later) is installed (or install it by `sudo curl -sSL https://get.docker.com/ | sh`)
2) Make sure that maven (v3 and later) is installed (or install it by `sudo apt-get install maven`)
3) Clone this repository (`git clone https://github.com/smirnp/StreaML_Sample_System.git`)
4) Open the cloned repository in any IDE you like. 
5) Make sure that hobbit-java-sdk dependency (declared in [pom.xml](https://github.com/hobbit-project/java-sdk-example/blob/master/pom.xml)) is installed into your local maven repository (or install it by executing the `mvn validate` command)

## How to create a system for existing benchmark
1) Find the `SystemAdapter.java` and use it as a basic HOBBIT-compatible implementation of your future system. Run the `checkHealth()` method from the `StreaMLSystemTest.java` to test/debug your system as pure java code. More details about the design of HOBBIT-compatible system adapters can be found [here](https://github.com/hobbit-project/platform/wiki/Develop-a-system-adapter-in-Java) and [here](https://github.com/hobbit-project/platform/wiki/Develop-a-system-adapter).
2) To build docker image you for the system you have to configure values in the `ExampleDockersBuilder.java`, package your code into jar file (`mvn package -DskipTests=true`) and execute the `buildImages()` from the `SampleSystemTest.java`. Image building is automatic, but  is on-demand, i.e. you have to check the actuality and rebuild images (inc. rebuilding jar file) by your own.
3) To run the docker image of your system you have to switch the value of the (`systemAdapter`) variable in `StreaMLSystemTest.java`. All internal logs from containers will be provided. You can skip logs output from benchmark by adding the '.skipLogsReading()' for PullBasedDockersBuilder within BenchmarkDockersBuilder.
4) To upload your image of the system into the HOBBIT platform please follow the standard procedure (decribed [here](https://github.com/hobbit-project/platform/wiki/Push-a-docker-image) and [here](https://github.com/hobbit-project/platform/wiki/System-meta-data-file)).

## Benchmark-sensitive information
All the benchmark-sensitive information for your system.ttl file you may find in the example [system.ttl](https://github.com/smirnp/StreaML_Sample_System/blob/master/system.ttl) file. You have to change only the following things: label, comment and imageName.

## FAQ
Feel free to ask any questions regading the StreaML Open Challenge under the [Issues](https://github.com/smirnp/StreaML_Sample_System/issues) tab.
