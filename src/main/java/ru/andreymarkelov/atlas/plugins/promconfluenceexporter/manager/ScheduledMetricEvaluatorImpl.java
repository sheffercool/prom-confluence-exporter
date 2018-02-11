package ru.andreymarkelov.atlas.plugins.promconfluenceexporter.manager;

import com.atlassian.confluence.security.login.LoginManager;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Thread.MIN_PRIORITY;
import static java.util.concurrent.Executors.defaultThreadFactory;
import static java.util.concurrent.Executors.newScheduledThreadPool;

public class ScheduledMetricEvaluatorImpl implements ScheduledMetricEvaluator, DisposableBean, InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(ScheduledMetricEvaluator.class);

    private static final String ATTACHMENT_SQL = "SELECT sum(LONGVAL) FROM contentproperties cp JOIN content c ON cp.contentid = c.contentid WHERE c.contenttype = 'ATTACHMENT' AND cp.propertyname = 'FILESIZE'";

    private final SessionFactory sessionFactory;
    private final LoginManager loginManager;
    private final AtomicLong totalAttachmentSize;

    public ScheduledMetricEvaluatorImpl(
            SessionFactory sessionFactory,
            LoginManager loginManager) {
        this.sessionFactory = sessionFactory;
        this.loginManager = loginManager;
        this.totalAttachmentSize = new AtomicLong(0);
    }

    /**
     * Scheduled executor to grab metrics.
     */
    private ScheduledExecutorService executorService = newScheduledThreadPool(2, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = defaultThreadFactory().newThread(r);
            thread.setPriority(MIN_PRIORITY);
            return thread;
        }
    });

    @Override
    public long getTotalAttachmentSize() {
        return totalAttachmentSize.get();
    }

    @Override
    public void destroy() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

    @Override
    public void afterPropertiesSet() {
        executorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                calculateTotalAttachmentSize();
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    public void calculateTotalAttachmentSize() {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            try (Statement statement = session.connection().createStatement(); ResultSet rs = statement.executeQuery(ATTACHMENT_SQL)) {
                if (rs.next()) {
                    totalAttachmentSize.set(rs.getInt(1));
                }
            }
            transaction.commit();
        } catch (Throwable th) {
            if (transaction != null) {
                try {
                    transaction.rollback();
                } catch (HibernateException ex) {
                    log.error("Cannot rollback hibernate transaction", ex);
                }
            }
            log.error("Error get attachment size from SQL", th);
        } finally {
            if (transaction != null) {
                try {
                    transaction.commit();
                } catch (HibernateException ex) {
                    log.error("Error commit hibernate transaction", ex);
                }
            }

            if (session != null) {
                try {
                    session.close();
                } catch (HibernateException ex) {
                    log.error("Cannot close hibernate session", ex);
                }
            }
        }
    }
}
