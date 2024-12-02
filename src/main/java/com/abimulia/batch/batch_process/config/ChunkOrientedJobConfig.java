package com.abimulia.batch.batch_process.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.abimulia.batch.batch_process.reader.SimpleItemReader;
import com.abimulia.batch.batch_process.writer.SimpleItemWriter;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class ChunkOrientedJobConfig {

    @Bean
    public SimpleItemReader itemReader() {
        log.debug("## SimpleItemReader()");
        return new SimpleItemReader();
    }

    @Bean
    public SimpleItemWriter itemWriter() {
        log.debug("## SimpleItemWriter()");
        return new SimpleItemWriter();
    }

    // chunkBasedStep Step #1.1
    @Bean
    public Step chunkBasedStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
            SimpleItemReader itemReader, SimpleItemWriter itemWriter) {
        log.debug("## chunkBasedStep()");
        return new StepBuilder("chunkBasedStep", jobRepository)
                .<String, String>chunk(3, transactionManager)
                .reader(itemReader)
                .writer(itemWriter).build();
    }

    // chunkJob Job #1
    @Bean
    public Job chunkJob(JobRepository jobRepository, Step chunkBasedStep) {
        log.debug("### chunkJob()");
        return new JobBuilder("chunkJob", jobRepository)
                .start(chunkBasedStep)
                .build();

    }

}
