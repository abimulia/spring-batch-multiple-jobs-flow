package com.abimulia.batch.batch_process.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
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
import com.abimulia.batch.batch_process.decider.ReceiptDecider;
import com.abimulia.batch.batch_process.listener.FlowerSelectionStepExecutionListener;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class DeliveryPackageJobConfig {

    /* Billing Section */

    // nestedBillingJobStep Step #3.2
    @Bean
    public Step nestedBillingJobStep(JobRepository jobRepository, Job billingJob) {
        log.debug("### nestedBillingJobStep()");
        return new StepBuilder("nestedBillingJobStep", jobRepository)
                .job(billingJob)
                .build();

    }

    // Step #3.1
    @Bean
    public Step sendInvoiceStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.debug("## sendInvoiceStep()");
        return new StepBuilder("sendInvoiceStep", jobRepository)
                .tasklet(new Tasklet() {

                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
                            throws Exception {
                        log.info("##Invoce Sent");
                        System.out.println("== Invoice is sent to the customer");
                        return RepeatStatus.FINISHED;
                    }

                }, transactionManager) // or .chunk(chunkSize, transactionManager)
                .build();
    }

    // billingJob Job #3
    @Bean
    public Job billingJob(JobRepository jobRepository, Step sendInvoiceStep) {
        log.debug("### billingJob()");
        return new JobBuilder("billingJob", jobRepository)
                .start(sendInvoiceStep)
                .build();

    }
    /* End Billing Section */

    /* Flower Job Section */
    @Bean
    public StepExecutionListener selectFlowerListener() {
        log.debug("## StepExecutionListener()");
        return new FlowerSelectionStepExecutionListener();
    }

    // Step #2.3
    @Bean
    public Step arrangeFlowersStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.debug("## arrangeFlowersStep()");
        return new StepBuilder("arrangeFlowersStep", jobRepository)
                .tasklet(new Tasklet() {

                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
                            throws Exception {
                        log.info("##Arranging flowers");
                        System.out.println("== Arranging flowers for order.");
                        return RepeatStatus.FINISHED;
                    }

                }, transactionManager) // or .chunk(chunkSize, transactionManager)
                .build();
    }

    // Step #2.2
    @Bean
    public Step removeThornsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.debug("## removeThornsStep()");
        return new StepBuilder("removeThornsStep", jobRepository)
                .tasklet(new Tasklet() {

                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
                            throws Exception {
                        log.info("#Remove thorns");
                        System.out.println("== Remove thorns from roses.");
                        return RepeatStatus.FINISHED;
                    }

                }, transactionManager) // or .chunk(chunkSize, transactionManager)
                .build();
    }

    // Step #2.1
    @Bean
    public Step selectFlowersStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.debug("## selectFlowersStep()");
        return new StepBuilder("selectFlowersStep", jobRepository)
                .tasklet(new Tasklet() {

                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
                            throws Exception {
                        log.info("#Gather flowers");
                        System.out.println("== Gathering flowers for order.");
                        return RepeatStatus.FINISHED;
                    }

                }, transactionManager) // or .chunk(chunkSize, transactionManager)
                .listener(selectFlowerListener())
                .build();
    }

    // prepareFlowersJob Job #2
    @Bean
    public Job prepareFlowersJob(JobRepository jobRepository, Step selectFlowersStep, Step removeThornsStep,
            Step arrangeFlowersStep, Flow deliveryFLow) {
        log.debug("### prepareFlowersJob()");
        return new JobBuilder("prepareFlowersJob", jobRepository)
                .start(selectFlowersStep)
                .on("TRIM_REQUIRED").to(removeThornsStep)
                .next(arrangeFlowersStep)
                .from(selectFlowersStep).on("NO_TRIM_REQUIRED")
                .to(arrangeFlowersStep)
                .from(arrangeFlowersStep).on("*").to(deliveryFLow)
                .end()
                .build();

    }
    /* End FLower Job Section */

    // Get Environment Variable to simulate error
    @Value("${GOT_LOST:false}")
    private String GOT_LOST;

    @Bean
    public JobExecutionDecider deliveryDecider() {
        log.debug("JobExecutionDecider");
        return new DeliveryDecider();
    }

    @Bean
    public JobExecutionDecider receiptDecider() {
        log.debug("receiptDecider()");
        return new ReceiptDecider();
    }

    /* Deliver Section */
    // External deliveryFLow Flow #1
    @Bean
    public Flow deliveryFLow(Step driveToAddressStep, Step givePackageToCustomerStep, Step thankStep, Step refundStep,
            Step leaveAtDoorStep) {
        log.debug("## deliveryFLow()");
        return new FlowBuilder<SimpleFlow>("deliveryFlow")
                .start(driveToAddressStep)
                .on("FAILED").fail()
                .from(driveToAddressStep)
                .on("*").to(deliveryDecider())
                .on("PRESENT").to(givePackageToCustomerStep)
                .next(receiptDecider()).on("CORRECT").to(thankStep)
                .from(receiptDecider()).on("INCORRECT").to(refundStep)
                .from(deliveryDecider())
                .on("NOT_PRESENT").to(leaveAtDoorStep)
                .build();
    }

    // Step #1.7
    @Bean
    public Step thankStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.debug("## thankStep()");
        return new StepBuilder("thankStep", jobRepository)
                .tasklet(new Tasklet() {

                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
                            throws Exception {

                        System.out.println("== Thanking the customer.");
                        return RepeatStatus.FINISHED;
                    }

                }, transactionManager) // or .chunk(chunkSize, transactionManager)
                .build();
    }

    // Step #1.6
    @Bean
    public Step refundStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.debug("## refundStep()");
        return new StepBuilder("refundStep", jobRepository)
                .tasklet(new Tasklet() {

                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
                            throws Exception {

                        System.out.println("== Refunding customer money.");
                        return RepeatStatus.FINISHED;
                    }

                }, transactionManager) // or .chunk(chunkSize, transactionManager)
                .build();
    }

    // Step #1.5
    @Bean
    public Step leaveAtDoorStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.debug("## leaveAtDoorStep()");
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

    // Step #1.4
    @Bean
    public Step storePackageStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.debug("## storePackageStep()");
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

    // Step #1.3
    @Bean
    public Step givePackageToCustomerStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.debug("## givePackageToCustomerStep()");
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

    // Step #1.2
    @Bean
    public Step driveToAddressStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.debug("## driveToAddressStep()");
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

    // Step #1.1
    @Bean
    public Step packageItemStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.debug("## packageItemStep()");
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

    // Job Delivery Job #1
    @Bean
    public Job deliverPackageJob(JobRepository jobRepository, Step packageItemStep, Flow deliveryFLow, Step nestedBillingJobStep) {
        log.debug("### deliverPackageJob()");
        return new JobBuilder("deliverPackageJob", jobRepository)
                .start(packageItemStep)
                .on("*").to(deliveryFLow)
                .next(nestedBillingJobStep) //Nested Job
                .end()
                .build();
    }
    /* Delivery Section */
}
