package com.slicequeue.springboot.batch.hibernate;


import com.slicequeue.springboot.batch.hibernate.domain.Customer;
import org.hibernate.SessionFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.HibernatePagingItemReader;
import org.springframework.batch.item.database.builder.HibernatePagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.persistence.EntityManagerFactory;
import java.util.Collections;

//@EnableBatchProcessing
//@SpringBootApplication
public class HibernatePagingJob {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public JobParametersValidator validator() {
        return new DefaultJobParametersValidator(
                new String[]{"city"},
                new String[]{"run.id"}
        );
    }

    @Bean
    @StepScope
    public HibernatePagingItemReader<Customer> customerHibernatePagingItemReader(
            EntityManagerFactory entityManagerFactory,
            @Value("#{jobParameters['city']}") String city
    ) {
        return new HibernatePagingItemReaderBuilder<Customer>()
                .name("customerHibernatePagingItemReader")
                .sessionFactory(entityManagerFactory.unwrap(SessionFactory.class))  // 아이템 리더에 사용할 세션 팩토리 설정
                .queryString("from Customer where city = :city")                    // 하이버네이트 쿼리 옵션1: 스프링 구성
//                .queryName(...)       // 하이버네이트 쿼리 옵션2: 하이버네이트 구성에 포함된 네임드 하이버네이트 쿼리를 참조함
//                .queryProvider(...)   // 하이버네이트 쿼리 옵션3: 하이버네이트 쿼리(HQL)를 프로그래밍으로 빌드하는 기능 제공
//                .nativeQuery(...)     // 하이버네이트 쿼리 옵션4: 네이티브 SQL 쿼리를 실행한 뒤 결과를 하이버네티으로 매핑하는데 사용
                .parameterValues(Collections.singletonMap("city", city))
                .pageSize(5)
                .build();
    }

    @Bean
    public ItemWriter<Customer> itemWriter() {
        return (items) -> items.forEach(System.out::println);
    }

    @Bean
    public Step copyFileStep() {
        return this.stepBuilderFactory.get("copyFileStep")
                .<Customer, Customer>chunk(10)
                .reader(customerHibernatePagingItemReader(null, null))
                .writer(itemWriter())
                .build();
    }

    @Bean
    public Job job() {
        return this.jobBuilderFactory.get("job-hibernate-paging")
                .validator(validator())
                .incrementer(new RunIdIncrementer())
                .start(copyFileStep())
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(HibernatePagingJob.class, "city=Bellevue");
    }
}
