package com.abimulia.batch.batch_process;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

@SpringBootApplication
public class BatchProcessApplication {

	@Bean
	public Step packageItemStep(JobRepository jobRepository,
			PlatformTransactionManager transactionManager) {
		return new StepBuilder("packageItemStep", jobRepository)
				.tasklet(new Tasklet() {

					@Override
					public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
							throws Exception {
						System.out.println("==The item has been packaged.");
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

	public static void main(String[] args) {
		SpringApplication.run(BatchProcessApplication.class, args);
	}

}
