package com.abimulia.batch.batch_process.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.abimulia.batch.batch_process.decider.DeliveryDecider;

@Configuration
public class DeliveryPackageJobConfig {
    // Get Environment Variable to simulate error
    @Value("${GOT_LOST:false}")
    private String GOT_LOST;

    @Bean
    public JobExecutionDecider deliveryDecider() {
        return new DeliveryDecider();
    }

    // Step #5
    @Bean
    public Step leaveAtDoorStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager) {
        return new StepBuilder("leaveAtDoorStep", jobRepository)
                .tasklet(new Tasklet() {

                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
                            throws Exception {

                        System.out.println("== Leaving the package at the door.");
                        return RepeatStatus.FINISHED;
                    }

                }, transactionManager) // or .chunk(chunkSize, transactionManager)
                .build();
    }

    // Step #4
    @Bean
    public Step storePackageStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager) {
        return new StepBuilder("storePackageStep", jobRepository)
                .tasklet(new Tasklet() {

                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
                            throws Exception {

                        System.out.println("== Storing the package while the customer address is located.");
                        return RepeatStatus.FINISHED;
                    }

                }, transactionManager) // or .chunk(chunkSize, transactionManager)
                .build();
    }

    // Step #3
    @Bean
    public Step givePackageToCustomerStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager) {
        return new StepBuilder("givePackageToCustomerStep", jobRepository)
                .tasklet(new Tasklet() {

                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
                            throws Exception {

                        System.out.println("== Given the package to the customer.");
                        return RepeatStatus.FINISHED;
                    }

                }, transactionManager) // or .chunk(chunkSize, transactionManager)
                .build();
    }

    // Step #2
    @Bean
    public Step driveToAddressStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager) {
        return new StepBuilder("driveToAddressStep", jobRepository)
                .tasklet(new Tasklet() {

                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
                            throws Exception {
                        if (GOT_LOST.equalsIgnoreCase("true")) {
                            throw new RuntimeException("Got lost driving to the address");
                        }

                        System.out.println("== Successfully arrived at the address.");
                        return RepeatStatus.FINISHED;
                    }

                }, transactionManager) // or .chunk(chunkSize, transactionManager)
                .build();
    }

    // Step #1
    @Bean
    public Step packageItemStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager) {
        return new StepBuilder("packageItemStep", jobRepository)
                .tasklet(new Tasklet() {

                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
                            throws Exception {
                        String item = chunkContext.getStepContext().getJobParameters().get("item").toString();
                        String date = chunkContext.getStepContext().getJobParameters().get("run.date").toString();
                        System.out.println(String.format("== The %s has been packaged on %s.", item, date));
                        return RepeatStatus.FINISHED;
                    }

                }, transactionManager) // or .chunk(chunkSize, transactionManager)
                .build();
    }

    // Job Delivery
    @Bean
    public Job deliverPackageJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("deliverPackageJob", jobRepository)
                .start(packageItemStep(jobRepository, transactionManager))
                .next(driveToAddressStep(jobRepository, transactionManager))
                    .on("FAILED").to(storePackageStep(jobRepository, transactionManager))
                .from(driveToAddressStep(jobRepository, transactionManager))
                    .on("*").to(deliveryDecider())
                        .on("PRESENT").to(givePackageToCustomerStep(jobRepository, transactionManager))
                    .from(deliveryDecider())
                        .on("NOT_PRESENT").to(leaveAtDoorStep(jobRepository, transactionManager))
                .end()
                .build();
    }
}
