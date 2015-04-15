package com.worksap.timachine;

import com.worksap.timachine.migrations.*;
import com.worksap.timachine.model.Options;
import com.worksap.timachine.spi.TransactionManager;
import com.worksap.timachine.spi.VersionProvider;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.StandardOutputStreamLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by liuyang on 14-10-8.
 */
public class TimachineTest {

    private static final List<Class<?>> migrations;


    static {
        migrations = new ArrayList<>();
        migrations.add(M20141106171530.class);
        migrations.add(M20141106171531.class);
        migrations.add(M20141106171532.class);
        migrations.add(M20141106171533WithSomeName.class);
    }

    @Rule
    public final StandardOutputStreamLog log = new StandardOutputStreamLog();

    private static final String PACKAGE = "jp.co.worksap.timachine.migrations";
    private TransactionManager transactionManager = new TransactionManager() {
        @Override
        public void begin() {
        }

        @Override
        public void commit() {
        }

        @Override
        public void rollback() {
        }

        @Override
        public void close() {
            // TODO Auto-generated method stub

        }
    };

    private static class FakeVersionProvider implements VersionProvider {

        private List<String> executedVersions;

        @Override
        public List<String> executedVersions() {
            return executedVersions;
        }

        @Override
        public void updateVersion(List<String> executedVersions) {
            this.executedVersions = executedVersions;
        }
    }

    private FakeVersionProvider versionProvider = new FakeVersionProvider();

    @Test
    public void testUp() throws Exception {
        versionProvider.updateVersion(Arrays.asList("M20141106171530", "M20141106171531"));
        Executor executor = new Executor(transactionManager, versionProvider);
        Options options = new Options();
        executor.execute(options, migrations);
        assertEquals("up 3\nup 4\n", log.getLog());
    }

    @Test
    public void testUpWithMissedMigration() throws Exception {
        versionProvider.updateVersion(Arrays.asList("M20141106171530", "M20141106171532"));
        Executor executor = new Executor(transactionManager, versionProvider);
        Options options = new Options();
        executor.execute(options, migrations);
        assertEquals("up 2\nup 4\n", log.getLog());
    }


    @Test
    public void testUpFromInit() throws Exception {
        versionProvider.updateVersion(new ArrayList<String>());
        Executor executor = new Executor(transactionManager, versionProvider);
        Options options = new Options();
        executor.execute(options, migrations);
        assertEquals("up 1\nup 2\nup 3\nup 4\n", log.getLog());
    }

    @Test
    public void testUpIrrevocable() throws Exception {
        migrations.add(M20141106171534.class);
        try {
            Executor executor = new Executor(transactionManager, versionProvider);
            Options options = new Options();
            try {
                executor.execute(options, migrations);
            } catch (RuntimeException e) {
                assertEquals("Class does not contain a method annotated with \"@Down\" M20141106171534", e.getMessage());
            }
        } finally {
            migrations.remove(M20141106171534.class);
        }
    }

    @Test
    public void testDown() throws Exception {
        versionProvider.updateVersion(Arrays.asList("M20141106171530", "M20141106171531", "M20141106171532", "M20141106171533"));
        Executor executor = new Executor(transactionManager, versionProvider);
        Options options = new Options();
        options.setToVersion("M20141106171531");
        executor.execute(options, migrations);
        assertEquals("down 4\ndown 3\n", log.getLog());
    }

    @Test
    public void testDownWithMissedMigration() throws Exception {
        versionProvider.updateVersion(Arrays.asList("M20141106171530", "M20141106171531", "M20141106171533"));
        Executor executor = new Executor(transactionManager, versionProvider);
        Options options = new Options();
        options.setToVersion("M20141106171531");
        executor.execute(options, migrations);
        assertEquals("down 4\n", log.getLog());
    }

    @Test
    public void testDownToInit() throws Exception {
        versionProvider.updateVersion(Arrays.asList("M20141106171530", "M20141106171531", "M20141106171532", "M20141106171533"));
        Executor executor = new Executor(transactionManager, versionProvider);
        Options options = new Options();
        options.setToVersion("INIT");
        executor.execute(options, migrations);
        assertEquals("down 4\ndown 3\ndown 2\ndown 1\n", log.getLog());
    }
}
