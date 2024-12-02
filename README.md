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
Then run the created jar 
` java -jar .\batch-process-1.0.0.jar "item=shoes" "run.date=2024-12-25,java.time.LocalDate"`

to simulate error got lost set the environment variable
`$env:GOT_LOST = "true"` then to switch back again `$env:GOT_LOST = "false"` for normal run