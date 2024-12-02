package com.abimulia.batch.batch_process.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class DeliveryPackageJobConfig {

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

    @Bean
    public Job deliverPackageJob(JobRepository jobRepository, Step packageItemStep) {
        return new JobBuilder("deliverPackageJob", jobRepository)
                .start(packageItemStep)
                .build();
    }
}
