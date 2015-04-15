import com.worksap.timachine.Executor;
import com.worksap.timachine.model.Migration;
import com.worksap.timachine.model.Options;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.reflections.Reflections;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Run migration.
 * <p>Parameters:  use -D to specify</p>
 * <p>to : to version of migration (INIT as the first version)</p>
 */
@Mojo(name = "migrate", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
@Execute(phase = LifecyclePhase.COMPILE)
public class MigrateMojo extends AbstractMojo {

    @Parameter(required = true)
    private String packageName;
    @Parameter(property = "project.runtimeClasspathElements")
    private List<String> elements;

    @Parameter(property = "project.build.outputDirectory")
    private String outDir;

    @Parameter(required = true)
    private String executor;

    @Parameter(property = "to")
    private String to;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        Thread.currentThread().setContextClassLoader(getClassLoader());
        Reflections reflections = new Reflections(packageName);
        Set<Class<?>> migrations = reflections.getTypesAnnotatedWith(Migration.class);
        List<Class<?>> list = new ArrayList<>(migrations);
        Collections.sort(list, new Comparator<Class<?>>() {
            @Override
            public int compare(Class<?> o1, Class<?> o2) {
                return o1.getSimpleName().compareTo(o2.getSimpleName());
            }
        });
        try {
            Executor executorImpl = (Executor) Thread.currentThread().getContextClassLoader().loadClass(executor).newInstance();
            Options options = new Options();
            options.setToVersion(to);
            executorImpl.execute(options, list);
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to initialize dependencies.", e);
        }
    }

    private ClassLoader getClassLoader() throws MojoExecutionException {
        elements.add(outDir);
        try {
            URL urls[] = new URL[elements.size()];

            for (int i = 0; i < elements.size(); ++i) {
                urls[i] = new File((String) elements.get(i)).toURI().toURL();
            }
            return new URLClassLoader(urls, getClass().getClassLoader());
        } catch (Exception e)//gotta catch em all
        {
            throw new MojoExecutionException("Couldn't create a classloader.", e);
        }
    }

}
