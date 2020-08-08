package io.github.atkawa7.pom;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractPomModifyMojo extends AbstractPomMojo {

    @Parameter(defaultValue = "${groupId}")
    private String groupId;

    @Parameter(defaultValue = "${artifactId}")
    private String artifactId;

    @Parameter(defaultValue = "${version}")
    private String version;

    @Parameter(defaultValue = "${type}")
    private String type;

    @Parameter(defaultValue = "${scope}")
    private String scope;

    @Parameter(defaultValue = "${classifier}")
    private String classifier;


    @Parameter(defaultValue = "${file.encoding}")
    private String xmlEncoding;

    @Parameter(defaultValue = "${overwrite}")
    private boolean overwrite;

    private DependencyPatternMatcher matcher;

    private final Map<ProjectId, ProjectContainer> projectContainerMap;

    public AbstractPomModifyMojo() {

        super();
        this.projectContainerMap = new HashMap<ProjectId, ProjectContainer>();
    }

    /**
     * @return the overwrite
     */
    public boolean isOverwrite() {

        return this.overwrite;
    }

    /**
     * @return the matcher
     */
    public DependencyPatternMatcher getMatcher() {

        return this.matcher;
    }

    public ProjectContainer getReactorProject(String groupId, String artifactId) {

        ProjectId pid = new ProjectId(groupId, artifactId);
        ProjectContainer reactorProject = this.projectContainerMap.get(pid);
        return reactorProject;
    }

    /**
     * {@inheritDoc}
     */
    public void execute() throws MojoExecutionException, MojoFailureException {

        super.execute();
        this.matcher = new DependencyPatternMatcher(this.groupId, this.artifactId, this.version,
                this.type, this.scope, this.classifier);
        // pass 1: search matching modules in reactor...
        for (MavenProject module : this.reactorProjects) {
            ProjectContainer parent = null;
            if (module.getParent() != null) {
                MavenProject parentProject = module.getParent();
                ProjectId parentId = new ProjectId(parentProject);
                parent = this.projectContainerMap.get(parentId);
                if (parent == null) {
                    getLog().debug("Parent project '" + parentId + "' NOT in reactor.");
                }
            }
            ProjectContainer container = new ProjectContainer(module, parent, getLog());
            this.projectContainerMap.put(container.getId(), container);
            // getLog().info("Matching " + module);
        }
        // pass 2: modify modules...
        for (MavenProject module : this.reactorProjects) {
            ProjectId pid = new ProjectId(module);
            ProjectContainer container = this.projectContainerMap.get(pid);
            getLog().warn("Executing project container");
            execute(container);
        }
        // pass 3: save modified POMs...
        for (MavenProject module : this.reactorProjects) {
            ProjectId pid = new ProjectId(module);
            ProjectContainer container = this.projectContainerMap.get(pid);
            container.save(this.xmlEncoding, this.overwrite);
        }
    }

    protected void execute(ProjectContainer projectContainer) throws MojoExecutionException,
            MojoFailureException {
        getLog().debug("Inside execute method ");
        if(projectContainer==null){
            getLog().debug("Project container is null ");
        }else{
            if(projectContainer.getId() !=null){
                getLog().debug("Processing project " + projectContainer.getId() + "...");
            }
            else{
                getLog().debug("Processing project null ...");
            }

        }


    }

}