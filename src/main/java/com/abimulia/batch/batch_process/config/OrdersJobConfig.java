package com.abimulia.batch.batch_process.config;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import com.abimulia.batch.batch_process.mapper.OrderItemPreparedStatementSetter;
import com.abimulia.batch.batch_process.mapper.OrderRowMapper;
import com.abimulia.batch.batch_process.record.Order;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class OrdersJobConfig {

    private static String[] names = new String[] { "orderId", "firstName", "lastName", "email", "cost", "itemId",
			"itemName", "shipDate" };

    private static String[] tokens = new String[] { "order_id", "first_name", "last_name", "email", "cost", "item_id",
            "item_name", "ship_date" };

    private static String ORDER_SQL = "select order_id, first_name, last_name, email, cost, item_id, item_name, ship_date "
            + "from SHIPPED_ORDER order by order_id";

    private static String INSERT_ORDER_SQL = "insert into "
			+ "SHIPPED_ORDER_OUTPUT(order_id, first_name, last_name, email, item_id, item_name, cost, ship_date)"
			+ " values(:orderId,:firstName,:lastName,:email,:itemId,:itemName,:cost,:shipDate)";

    //JdbcBatchItemWriter
    @Bean
    public ItemWriter<Order> jdbcBatchItemWriter(DataSource dataSource){
        return new JdbcBatchItemWriterBuilder<Order>()
            .dataSource(dataSource)
            .sql(INSERT_ORDER_SQL)
            .beanMapped()
            .build();
    }

    // Flatfile Item writer
    @Bean
    public ItemWriter<Order> flatFileItemWriter(){
        FlatFileItemWriter<Order> flatFileItemWriter = new FlatFileItemWriter<Order>();
        flatFileItemWriter.setResource(new FileSystemResource("/data/shipped_orders_output"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"))+".csv"));
        
        DelimitedLineAggregator<Order> delimitedLineAggregator = new DelimitedLineAggregator<Order>();
        delimitedLineAggregator.setDelimiter(",");

        BeanWrapperFieldExtractor<Order> fieldExtractor = new BeanWrapperFieldExtractor<Order>();
        fieldExtractor.setNames(names);
        delimitedLineAggregator.setFieldExtractor(fieldExtractor);

        flatFileItemWriter.setLineAggregator(delimitedLineAggregator);

        return flatFileItemWriter;



    }


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
            ItemReader<Order> jdbcPagingItemReader, ItemWriter<Order> jdbcBatchItemWriter) {
        log.debug("## chunkOrderBasedStep()");
        return new StepBuilder("chunkOrderBasedStep", jobRepository)
                .<Order, Order>chunk(3, transactionManager)
                .reader(jdbcPagingItemReader)
                .writer(jdbcBatchItemWriter).build();
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
