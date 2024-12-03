package com.abimulia.batch.batch_process.config;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import com.abimulia.batch.batch_process.mapper.OrderFieldSetMapper;
import com.abimulia.batch.batch_process.mapper.OrderRowMapper;
import com.abimulia.batch.batch_process.record.Order;
import com.abimulia.batch.batch_process.writer.SimpleItemWriter;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class OrdersJobConfig {
    public static String[] tokens = new String[] { "order_id", "first_name", "last_name", "email", "cost", "item_id",
            "item_name", "ship_date" };

    public static String ORDER_SQL = "select order_id, first_name, last_name, email, cost, item_id, item_name, ship_date "
            + "from SHIPPED_ORDER order by order_id";

    @Bean
    public PagingQueryProvider orderQueryProvider(DataSource dataSource) throws Exception {
        SqlPagingQueryProviderFactoryBean factory = new SqlPagingQueryProviderFactoryBean();
        factory.setSelectClause("select order_id, first_name, last_name, email, cost, item_id, item_name, ship_date");
        factory.setFromClause("from SHIPPED_ORDER");
        factory.setSortKey("order_id");
        factory.setDataSource(dataSource);
        return factory.getObject();
    }

    // JDBC Reader
    @Bean
    public ItemReader<Order> jdbcPagingItemReader(DataSource dataSource, PagingQueryProvider orderQueryProvider) {
        return new JdbcPagingItemReaderBuilder<Order>()
                .dataSource(dataSource)
                .name("jdbcPagintItemReader")
                .pageSize(10)
                .queryProvider(orderQueryProvider)
                .rowMapper(new OrderRowMapper())
                .build();

    }

    // chunkOrderBasedStep Step #1.1
    @Bean
    public Step chunkOrderBasedStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
            ItemReader<Order> jdbcPagingItemReader, SimpleItemWriter itemWriter) {
        log.debug("## chunkOrderBasedStep()");
        return new StepBuilder("chunkOrderBasedStep", jobRepository)
                .<Order, Order>chunk(3, transactionManager)
                .reader(jdbcPagingItemReader)
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
