package jp.co.worksap.timachine;

import jp.co.worksap.timachine.model.Down;
import jp.co.worksap.timachine.model.Migration;
import jp.co.worksap.timachine.model.Up;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by liuyang on 14-10-10.
 */
public class Migrations {

    private static final Logger LOGGER = LoggerFactory.getLogger(Migrations.class);

    @Getter
    private final List<String> versions;

    private final Map<String, MigrationMetaData> migrationsMap;

    public Migrations(List<Class<?>> migrationClasses) {
        versions = new ArrayList<>();
        migrationsMap = new HashMap<>();
        for (Class<?> migration : migrationClasses) {
            MigrationMetaData metaData = validate(migration);
            if (migrationsMap.containsKey(metaData.getVersion())) {
                throw new RuntimeException("Duplicated version " + metaData.getVersion());
            }
            versions.add(metaData.getVersion());
            migrationsMap.put(metaData.getVersion(), metaData);
        }
        Collections.sort(versions);
        for (String version : versions) {
            LOGGER.info("Preparing to execute migration: " + migrationsMap.get(version).getClazz().getSimpleName());
        }
    }

    private MigrationMetaData validate(Class<?> migration) {
        MigrationMetaData metaData = new MigrationMetaData();
        metaData.setClazz(migration);
        String name = migration.getSimpleName();
        String version = MigrationNameResolver.validateVersionOf(name);
        metaData.setVersion(version);
        if (!migration.isAnnotationPresent(Migration.class)) {
            throw new RuntimeException("Annotation \"Migration\" does not exist on class: " + migration.getName());
        }

        List<Method> upMethods = new ArrayList<>();
        List<Method> downMethods = new ArrayList<>();
        for (final Method method : migration.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Up.class)) {
                upMethods.add(method);
            }
            if (method.isAnnotationPresent(Down.class)) {
                downMethods.add(method);
            }
        }
        if (upMethods.size() == 0) {
            throw new RuntimeException("Class does not contain a method annotated with \"@Up\"" + migration.getName());
        }
        if (upMethods.size() > 1) {
            throw new RuntimeException("Class contains multiple methods annotated with \"@Up\"" + migration.getName());
        }
        if (downMethods.size() > 1) {
            throw new RuntimeException("Class contains multiple methods annotated with \"@Down\"" + migration.getName());
        }
        metaData.setUp(upMethods.get(0));
        if (downMethods.size() == 1) {
            metaData.setDown(downMethods.get(0));
        }
        return metaData;
    }

    public MigrationMetaData migration(String name) {
        return migrationsMap.get(name);
    }
}
