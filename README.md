# README #

# Cloud Simulations #



**Description:**  
This is a cloud simulation framework made on top of [CloudSim Plus](http://cloudsimplus.org//). The project consists of 2 simulations (Load balancing and Network Topology), Unit test cases via [FunSuite](http://www.scalatest.org/getting_started_with_fun_suite), logging mechanism via [Logback](https://logback.qos.ch/) and configuration files management via [Typesafe Conguration Library](https://github.com/lightbend/config). The project is build with [Simple Build Toolkit (SBT)](https://www.scala-sbt.org/1.x/docs/index.html) using [IntelliJ](https://www.jetbrains.com/student/) as the development environment.   


**Project Structure:** 

- **Resource**: Containing config files for LoadBalancing and Network params, VM's, Data Center and Cloudlets and one xml file for logging configurations
    
- **Main**: Consists of driver, simulation and other files and packages which include:
     - Main class driving the 2 simulations
     - Converter class to convert Java code to Scala and vice versa. 
     - Config class to pull parameter from all configuration files. 
     - Helper functions used throughout the simulations.
     - Decorator classes which override Cloudsim classes adding certain behaviour. (Used in load balancing)
            
    
- **Test**: Consist of Test files for checking funtionality of the project.
    
- **[SBT](http://www.scala-sbt.org/0.13/docs/Basic-Def.html)**: This is the configuration file which imports all relevant Jars and resources required to build, run and test the project.


**Simulations:**

- Simulation Load Balancer: 
    -  5 Simulations created for 5 LB types (Hashing, RoundRobin, MinMin, MaxMin and Random).
    -  Generated random cloudlets/tasks to be fed to LB for comparison
    -  Comparison id done w.r.t time for each Load Balancer
    -  Results as follows: MinMin > MaxMin > (Round Robin = Hashing) > Random.
    -  Extra Design Decisions are highlighted in the doc file provided "CS441_Project.docx".
    
- Simulation Network Topology: 
    -  Two datacenters
    -  Multiple Hosts,each supporting space shared VM
    -  Multiple VMs,each supporting time shared cloudlets
    -  Network cloudlets with send and receive task simulating diffusing computation.  

**Execution:**
 The project can be executed either via IntelliJ or SBT.
 
 - For IntelliJ, GIT Clone the repo, open it as an sbt project and execute the Main.scala file inside src > main > scala.
 
 - For SBT, GIT clone the repo and open the path of the folder in sbt shell. Then run 'clean', 'compile', 'run' (to run the project) and 'test' (to run the test cases)
 
 - There is only 1 main file which runs and executes the simulation based on the user input.
 
 **Deployed on docker:** 
    
    In order to run the project via docker, you need to run hte following commands:
    - docker pull smaith2/cloudsim-plus:latest
    - docker run -i smaith2/cloudsim-plus
 
 **Note:** 
 -  The parameters are in the .conf files inside resources folder. You can change them if you wish to do so.
 -  The LogBack logger is implemented at the console as well as the file level. The logs are stored in 'scala-logging.log' within the project folder.
 
 That's It ! :) 