package com.slicequeue.springboot.batch.hibernate.batch;

import org.hibernate.SessionFactory;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * 배치 잡에 사용할 TransactionManger 를 커스터마이징함
 * 스프링 배치는 기본적으로 사용하는 TransactionManger 로 DataSourceTransactionManager 제공하나
 * 여기서는 일반적인 DataSource 커낵션과 하이버네이트 세션을 아우르는 TransactionManager 가 필요하므로!
 * HibernateTransactionManager 를 구성한다!
 */
public class HibernateBatchConfigurer extends DefaultBatchConfigurer {

    private DataSource dataSource;
    private SessionFactory sessionFactory;
    private PlatformTransactionManager transactionManager;

    public HibernateBatchConfigurer(DataSource dataSource,
                                    EntityManagerFactory entityManagerFactory) {
        this.dataSource = dataSource;
        this.sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        this.transactionManager = new HibernateTransactionManager(this.sessionFactory);
    }

    @Override
    public PlatformTransactionManager getTransactionManager() {
        return transactionManager; // getter 오버라잇! 해서 여기서 구성한 HibernateTransactionManager 객체로 반환
        // 새 트랜잭션 매니저를 적절한 곳에 spring batch 가 사용할 것!
    }
}
