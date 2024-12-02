package com.abimulia.batch.batch_process.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.abimulia.batch.batch_process.listener.FlowerSelectionStepExecutionListener;

@Configuration
public class PrepareFLowersJobConfig {

    @Bean
    public StepExecutionListener selectFlowerListener() {
        return new FlowerSelectionStepExecutionListener();
    }

    // Step #3
    @Bean
    public Step arrangeFlowersStep(JobRepository flowerRepository,
            PlatformTransactionManager flowerTManager) {
        return new StepBuilder("arrangeFlowersStep", flowerRepository)
                .tasklet(new Tasklet() {

                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
                            throws Exception {
                        System.out.println("== Arranging flowers for order.");
                        return RepeatStatus.FINISHED;
                    }

                }, flowerTManager) // or .chunk(chunkSize, transactionManager)
                .build();
    }

    // Step #2
    @Bean
    public Step removeThornsStep(JobRepository flowerRepository,
            PlatformTransactionManager flowerTManager) {
        return new StepBuilder("removeThornsStep", flowerRepository)
                .tasklet(new Tasklet() {

                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
                            throws Exception {
                        System.out.println("== Remove thorns from roses.");
                        return RepeatStatus.FINISHED;
                    }

                }, flowerTManager) // or .chunk(chunkSize, transactionManager)
                .build();
    }

    // Step #1
    @Bean
    public Step selectFlowersStep(JobRepository flowerRepository,
            PlatformTransactionManager flowerTManager) {
        return new StepBuilder("selectFlowersStep", flowerRepository)
                .tasklet(new Tasklet() {

                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
                            throws Exception {
                        System.out.println("== Gathering flowers for order.");
                        return RepeatStatus.FINISHED;
                    }

                }, flowerTManager) // or .chunk(chunkSize, transactionManager)
                .listener(selectFlowerListener())
                .build();
    }

    @Bean
    Job prepareFlowersJob(JobRepository flowerRepository, PlatformTransactionManager flowerTManager) {
        return new JobBuilder("prepareFlowersJob", flowerRepository)
                .start(selectFlowersStep(flowerRepository, flowerTManager))
                .on("TRIM_REQUIRED").to(removeThornsStep(flowerRepository, flowerTManager))
                .next(arrangeFlowersStep(flowerRepository, flowerTManager))
                .from(selectFlowersStep(flowerRepository, flowerTManager)).on("NO_TRIM_REQUIRED")
                .to(arrangeFlowersStep(flowerRepository, flowerTManager))
                .end()
                .build();

    }

}
