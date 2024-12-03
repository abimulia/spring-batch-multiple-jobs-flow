# Batch Process

[![Spring Boot v3.3.5](https://img.shields.io/badge/Java-SpringBoot-green)](https://spring.io/)
[![License](http://img.shields.io/:license-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0.html)
[![Spring Batch v5](https://img.shields.io/badge/Spring-Batch-red)](https://docs.spring.io/spring-batch/docs/5.0.x/reference/html/)

This project created by `start.spring.io` contain [Spring Boot](https://spring.io/) version 3.3.5.

Batch Service project, using spring-batch to do batch processing.
This will show how to use spring-batch framework to run batch process.


## Development server

Run `mvn spring-boot:run` for a dev server. 

To run job with parameter need to package the project using maven build with goals clean package
Then run the created jar with the following command  

`java -jar .\batch-process-1.0.0.jar "item=shoes" "run.date=2024-12-25,java.time.LocalDate"`

`java -jar .\batch-process-1.0.0.jar "item=shoes" "run.date=2024-12-17,java.time.LocalDate"`

to simulate error got lost set the environment variable

`$env:GOT_LOST = "true"` then to switch back again `$env:GOT_LOST = "false"` for normal run 

additional setting for another condition is 

`$env:IS_PRESENT = "true"` to simulate customer is present and  
`$env:IS_PRESENT = "false"` to simulate customer is not present

to run the flower job  
`java -jar "-Dspring.batch.job.name=prepareFlowersJob" .\batch-process-1.0.0.jar "type=roses" "run.date=2024-12-16,java.time.LocalDate"`

to run the order job  
`java -jar "-Dspring.batch.job.name=chunkOrderJob" .\batch-process-1.0.0.jar "run.date=2024-12-19,java.time.LocalDate"`

When there are multiple jobs we need to specify one default job name in the application.properties  
`spring.batch.job.name=deliverPackageJob`

or you'll get this error  
![image](https://github.com/user-attachments/assets/b342c12c-1189-4157-8ea7-128096465096)

so when you run without specifying job name the default job will be running 

![image](https://github.com/user-attachments/assets/e8ceef44-b558-47b1-bcd2-6d885303d557)

to run a specific job you need to specify the job just add the job name in the command 

![image](https://github.com/user-attachments/assets/e7540102-c362-43e8-b634-2413cc8f6cfd)
