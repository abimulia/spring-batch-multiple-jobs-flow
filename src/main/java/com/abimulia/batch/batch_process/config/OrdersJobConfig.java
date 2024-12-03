package com.abimulia.batch.batch_process.config;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
import org.springframework.batch.item.support.builder.CompositeItemProcessorBuilder;
import org.springframework.batch.item.validator.BeanValidatingItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.abimulia.batch.batch_process.exception.OrderProcessingException;
import com.abimulia.batch.batch_process.listener.CustomRetryListener;
import com.abimulia.batch.batch_process.listener.CustomSkipListener;
import com.abimulia.batch.batch_process.mapper.OrderRowMapper;
import com.abimulia.batch.batch_process.record.Order;
import com.abimulia.batch.batch_process.record.TrackedOrder;
import com.abimulia.batch.processor.FreeShippingItemProcessor;
import com.abimulia.batch.processor.TrackedOrderItemProcessor;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class OrdersJobConfig {

        private static String[] names = new String[] { "orderId", "firstName", "lastName", "email", "cost", "itemId",
                        "itemName", "shipDate" };

        @SuppressWarnings("unused")
        private static String[] tokens = new String[] { "order_id", "first_name", "last_name", "email", "cost",
                        "item_id",
                        "item_name", "ship_date" };

        @SuppressWarnings("unused")
        private static String ORDER_SQL = "select order_id, first_name, last_name, email, cost, item_id, item_name, ship_date "
                        + "from SHIPPED_ORDER order by order_id";

        // private static String INSERT_ORDER_SQL = "insert into "
        // + "SHIPPED_ORDER_OUTPUT(order_id, first_name, last_name, email, item_id,
        // item_name, cost, ship_date)"
        // + "
        // values(:orderId,:firstName,:lastName,:email,:itemId,:itemName,:cost,:shipDate)";
        public static String INSERT_ORDER_SQL = "insert into "
                        + "TRACKED_ORDER(order_id, first_name, last_name, email, item_id, item_name, cost, ship_date, tracking_number, free_shipping)"
                        + " values(:order.orderId,:order.firstName,:order.lastName,:order.email,:order.itemId,:order.itemName,:order.cost,:order.shipDate,:trackingNumber, :freeShipping)";

        @Bean
        public TaskExecutor taskExecutor() {
                ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
                executor.setCorePoolSize(2);
                executor.setMaxPoolSize(10);
                return executor;
        }

        @Bean
        public CustomRetryListener customRetryListener() {
                return new CustomRetryListener();
        }

        @Bean
        public CustomSkipListener customOrderSkipListener() {
                return new CustomSkipListener();
        }

        @Bean
        public ItemProcessor<TrackedOrder, TrackedOrder> freeShippingItemProcessor() {
                return new FreeShippingItemProcessor();
        }

        @Bean
        public ItemProcessor<Order, TrackedOrder> compositeItemPrcessor(
                        ItemProcessor<Order, Order> orderValidatingItemProcessor,
                        ItemProcessor<Order, TrackedOrder> trackedOrderItemProcessor,
                        ItemProcessor<TrackedOrder, TrackedOrder> freeShippingItemProcessor) {
                return new CompositeItemProcessorBuilder<Order, TrackedOrder>()
                                .delegates(orderValidatingItemProcessor, trackedOrderItemProcessor,
                                                freeShippingItemProcessor)
                                .build();
        }

        @Bean
        public ItemProcessor<Order, TrackedOrder> trackedOrderItemProcessor() {
                return new TrackedOrderItemProcessor();
        }

        @Bean
        public ItemProcessor<Order, Order> orderValidatingItemProcessor() throws Exception {
                BeanValidatingItemProcessor<Order> orderItemPocessor = new BeanValidatingItemProcessor<Order>();
                orderItemPocessor.setFilter(true);
                return orderItemPocessor;
        }

        // JsonFileItemWriter
        @Bean
        public ItemWriter<TrackedOrder> jsonFileItemWriter() {
                return new JsonFileItemWriterBuilder<TrackedOrder>()
                                .jsonObjectMarshaller(new JacksonJsonObjectMarshaller<TrackedOrder>())
                                .resource(new FileSystemResource(
                                                "/data/shipped_tracked_orders.output-"
                                                                + LocalDateTime.now()
                                                                                .format(DateTimeFormatter.ofPattern(
                                                                                                "yyyyMMdd-HHmm"))
                                                                + ".json"))
                                .name("jsonFileItemWriter")
                                .build();
        }

        // JdbcBatchItemWriter
        @Bean
        public ItemWriter<TrackedOrder> jdbcBatchItemWriter(DataSource dataSource) {
                return new JdbcBatchItemWriterBuilder<TrackedOrder>()
                                .dataSource(dataSource)
                                .sql(INSERT_ORDER_SQL)
                                .beanMapped()
                                .build();
        }

        // Flatfile Item writer
        @Bean
        public ItemWriter<Order> flatFileItemWriter() {
                FlatFileItemWriter<Order> flatFileItemWriter = new FlatFileItemWriter<Order>();
                flatFileItemWriter
                                .setResource(new FileSystemResource(
                                                "/data/shipped_orders_output"
                                                                + LocalDateTime.now()
                                                                                .format(DateTimeFormatter.ofPattern(
                                                                                                "yyyyMMdd-HHmm"))
                                                                + ".csv"));

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
                factory.setSelectClause(
                                "select order_id, first_name, last_name, email, cost, item_id, item_name, ship_date");
                factory.setFromClause("from SHIPPED_ORDER");
                factory.setSortKey("order_id");
                factory.setDataSource(dataSource);
                return factory.getObject();
        }

        // JDBC Reader
        @Bean
        public ItemReader<Order> jdbcPagingItemReader(DataSource dataSource, PagingQueryProvider orderQueryProvider)
                        throws Exception {
                return new JdbcPagingItemReaderBuilder<Order>()
                                .dataSource(dataSource)
                                .name("jdbcPagintItemReader")
                                .pageSize(10)
                                .queryProvider(orderQueryProvider)
                                .rowMapper(new OrderRowMapper())
                                .saveState(false) //when we use multi-threaded step we are no longer able to to restart the job in a successful state.
                                .build();

        }

        // chunkOrderBasedStep Step #1.1
        @Bean
        public Step chunkOrderBasedStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                        ItemReader<Order> jdbcPagingItemReader,
                        ItemProcessor<Order, TrackedOrder> trackedOrderItemProcessor,
                        ItemProcessor<Order, TrackedOrder> compositeItemPrcessor,
                        CustomRetryListener customRetryListener,
                        ItemWriter<TrackedOrder> jdbcBatchItemWriter,
                        TaskExecutor taskExecutor)
                        throws Exception {
                log.debug("## chunkOrderBasedStep()");
                return new StepBuilder("chunkOrderBasedStep", jobRepository)
                                .<Order, TrackedOrder>chunk(3, transactionManager)
                                .reader(jdbcPagingItemReader)
                                .processor(compositeItemPrcessor)
                                .faultTolerant()
                                .retry(OrderProcessingException.class)
                                .retryLimit(5)
                                .listener(customRetryListener)
                                .writer(jdbcBatchItemWriter)
                                .taskExecutor(taskExecutor)
                                .build();
        }

        // chunkOrderJob Job #1
        @Bean
        public Job chunkOrderJob(JobRepository jobRepository, Step chunkOrderBasedStep) throws Exception {
                log.debug("### chunkOrderJob()");
                return new JobBuilder("chunkOrderJob", jobRepository)
                                .start(chunkOrderBasedStep)
                                .build();

        }
}
