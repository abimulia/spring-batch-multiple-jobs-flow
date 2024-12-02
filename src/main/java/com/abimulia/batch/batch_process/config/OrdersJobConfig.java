package com.abimulia.batch.batch_process.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import com.abimulia.batch.batch_process.mapper.OrderFieldSetMapper;
import com.abimulia.batch.batch_process.record.Order;
import com.abimulia.batch.batch_process.writer.SimpleItemWriter;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class OrdersJobConfig {
    public static String[] tokens = new String[] { "order_id", "first_name", "last_name", "email", "cost", "item_id",
            "item_name", "ship_date" };


    @Bean
    public ItemReader<Order> orderItemReader() {
        FlatFileItemReader<Order> orderItemReader = new FlatFileItemReader<Order>();
        orderItemReader.setLinesToSkip(1);
        orderItemReader.setResource(new FileSystemResource("/data/shipped_orders.csv"));
        DefaultLineMapper<Order> lineMapper = new DefaultLineMapper<Order>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames(tokens);
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(new OrderFieldSetMapper()); 
        orderItemReader.setLineMapper(lineMapper);
        return orderItemReader;

    }

    // chunkOrderBasedStep Step #1.1
    @Bean
    public Step chunkOrderBasedStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
            ItemReader<Order> orderItemReader, SimpleItemWriter itemWriter) {
        log.debug("## chunkOrderBasedStep()");
        return new StepBuilder("chunkOrderBasedStep", jobRepository)
                .<Order, Order>chunk(3, transactionManager)
                .reader(orderItemReader)
                .writer(new ItemWriter<Order>() {
                    @Override
                    public void write(Chunk<? extends Order> items) throws Exception {
                        System.out.println(String.format("Received list of size: %s", items.size()));
                        for (Order item : items) {
                            System.out.println("Output: [" + item.firstName() + " - " + item.lastName() + " - "
                                    + item.email() + "]"); // Output to console
                        }
                    }
                }).build();
    }

    // chunkOrderJob Job #1
    @Bean
    public Job chunkOrderJob(JobRepository jobRepository, Step chunkOrderBasedStep) {
        log.debug("### chunkOrderJob()");
        return new JobBuilder("chunkOrderJob", jobRepository)
                .start(chunkOrderBasedStep)
                .build();

    }
}
