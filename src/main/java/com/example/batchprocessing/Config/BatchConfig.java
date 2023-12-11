package com.example.batchprocessing.Config;

import com.example.batchprocessing.Entity.Employee;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.data.MongoCursorItemReader;
import org.springframework.batch.item.data.MongoPagingItemReader;
import org.springframework.batch.item.data.builder.MongoPagingItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.support.DatabaseType;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.batch.BatchDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.WritableResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Configuration
public class BatchConfig {

    @Autowired
    private Processor processor;

    @Qualifier("primaryTemplate")
    @Autowired
    MongoTemplate mongoTemplate;

    @Bean
    public Job job(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        return new JobBuilder("myJob", jobRepository)
                .start(step(jobRepository, platformTransactionManager))
                .build();
    }

    @Bean
    public Step step(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder("myStep", jobRepository)
                .<Employee, Employee>chunk(4, platformTransactionManager)
                .reader(read())
                .processor(processor)
                .writer(writer())
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public FlatFileItemWriter<Employee> writer(){
        String[] headers = new String[]{"empId", "empName", "empSalary", "empRole"};
        String format1 = new SimpleDateFormat("yyyy-MM-dd'-'HH-mm-ss-SSS", Locale.forLanguageTag("tr-TR")).format(new Date());
        WritableResource outputResource = new FileSystemResource("output/employees_" + format1 + ".csv");

        return new FlatFileItemWriterBuilder<Employee>()
                .resource(outputResource)
                .name("writer")
                .append(true)
                .lineAggregator(new DelimitedLineAggregator<Employee>() {
                    {
                        setDelimiter(",");
                        setFieldExtractor(new BeanWrapperFieldExtractor<Employee>() {
                            {
                                setNames(headers);
                            }
                        });
                    }
                })
                .build();
    }

    @Bean
    public MongoPagingItemReader<Employee> read() {
        Map<String, Sort.Direction> sort = new HashMap<>();
        sort.put("_id", Sort.Direction.DESC);

        return new MongoPagingItemReaderBuilder<Employee>()
                        .template(mongoTemplate)
                        .name("mongoReader")
                        .query(Query.query(
                                new Criteria().andOperator(
                                        Criteria.where("empId").lte(1000)
                                )
                        ))
                        .collection("employee")
                        .targetType(Employee.class)
                        .sorts(sort).build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor simpleAsyncTaskExecutor = new SimpleAsyncTaskExecutor();
        simpleAsyncTaskExecutor.setConcurrencyLimit(7);
        return simpleAsyncTaskExecutor;
    }

}
