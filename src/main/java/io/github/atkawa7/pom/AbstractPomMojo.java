package io.github.atkawa7.pom;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.util.List;

public abstract class AbstractPomMojo extends AbstractMojo {


    @Parameter(defaultValue="${project}", readonly=true, required=true)
    private MavenProject project;


    @Parameter(defaultValue="${reactorProjects}")
    protected List<MavenProject> reactorProjects;

    @Parameter(defaultValue="${requireToplevel}")
    protected boolean requireToplevel;

    protected MavenProject findReactorProject(DependencyInfo info) throws MojoFailureException {

        MavenProject result = null;
        for (MavenProject module : this.reactorProjects) {
            if (info.isMatching(module)) {
                if (result != null) {
                    throw new MojoFailureException("ambiguous reactor search for " + info + "!\nFound "
                            + new DependencyInfo(result) + " and " + new DependencyInfo(module));
                }
                result = module;
            }
        }
        return result;
    }

    protected MavenProject requireReactorProject(DependencyInfo info) throws MojoFailureException {

        MavenProject result = findReactorProject(info);
        if (result == null) {
            throw new MojoFailureException("Could NOT find project " + info + " in reactor");
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Starting executing");
        if(this.project  == null ){
            getLog().warn("Project is null");
        }
        else {
            if (this.project.getParent() != null) {
                if (this.requireToplevel) {
                    throw new MojoFailureException(
                            "You have to invoke this plugin on your top-level POM!\nHowever you have a parent "
                                    + new DependencyInfo(this.project.getParent())
                                    + ".\nUse -DrequireToplevel=false to continue.");
                } else {
                    getLog().warn("Maven was NOT executed on top-level POM!");
                    getLog().info("Continue since ${requireToplevel} is false...");
                }
            }
        }

    }
}