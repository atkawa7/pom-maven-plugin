package io.github.atkawa7.pom;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Element;

import java.util.List;

@Mojo( name = "refactor", defaultPhase = LifecyclePhase.CLEAN)
public class PomRefactorMojo extends AbstractPomModifyMojo {

    @Parameter(defaultValue = "${newGroupId}")
    private String newGroupId;

    @Parameter(defaultValue = "${newArtifactId}")
    private String newArtifactId;

    @Parameter(defaultValue = "${newVersion}")
    private String newVersion;

    @Parameter(defaultValue = "${newType}")
    private String newType;


    @Parameter(defaultValue = "${newScope}")
    private String newScope;

    @Parameter(defaultValue = "${newClassifier}")
    private String newClassifier;


    @Parameter(defaultValue = "${resolveVariables}")
    private boolean resolveVariables;

    // ----------------------------------------------------------------------
    // Mojo fields
    // ----------------------------------------------------------------------

    /** The new classifier to apply. */
    private DependencyInfo newAttributes;

    protected DependencyInfo getNewAttributes() {

        if (this.newAttributes == null) {
            this.newAttributes = new DependencyInfo(this.newGroupId, this.newArtifactId, this.newVersion,
                    this.newType, this.newScope, this.newClassifier);
        }
        return this.newAttributes;
    }

    protected void updateDependencies(List<Dependency> dependencyList,
                                      ProjectContainer projectContainer, String tagname) throws MojoExecutionException,
            MojoFailureException {

        for (Dependency dependency : dependencyList) {
            if (getMatcher().isMatching(dependency)) {
                DependencyInfo delta = getNewAttributes().getDiff(dependency);
                if (!delta.isEmpty()) {
                    getLog().info(
                            "Changing dependency " + new DependencyInfo(dependency) + " to "
                                    + delta.toDiffString());
                    // try to find dependency in XML
                    List<Element> dependencyElementList;
                    if (ProjectContainer.XML_TAG_DEPENDENCIES.equals(tagname)) {
                        dependencyElementList = projectContainer.getPomDependenciesList();
                    } else if (ProjectContainer.XML_TAG_DEPENDENCY_MANAGEMENT.equals(tagname)) {
                        dependencyElementList = projectContainer.getPomDependencyManagementList();
                    } else {
                        throw new MojoExecutionException("Internal Error: Unknown dependency tagname '"
                                + tagname + "'!");
                    }
                    boolean notFound = true;
                    for (Element elementDependency : dependencyElementList) {
                        DependencyInfo dependencyInfo = projectContainer.createDependencyInfo(
                                elementDependency, this.resolveVariables);
                        getLog().debug("Checking dependency '" + dependencyInfo + "' ...");
                        if (dependencyInfo.isMatching(dependency)) {
                            getLog().debug("Found dependency in pom.xml: " + dependencyInfo);
                            notFound = false;
                            if (delta.getGroupId() != null) {
                                updateValue(elementDependency, ProjectContainer.XML_TAG_GROUPID, projectContainer,
                                        delta.getGroupId());
                            }
                            if (delta.getArtifactId() != null) {
                                updateValue(elementDependency, ProjectContainer.XML_TAG_ARTIFACTID,
                                        projectContainer, delta.getArtifactId());
                            }
                            if (delta.getVersion() != null) {
                                updateValue(elementDependency, ProjectContainer.XML_TAG_VERSION, projectContainer,
                                        delta.getVersion());
                            }
                            if (delta.getType() != null) {
                                updateValue(elementDependency, ProjectContainer.XML_TAG_TYPE, projectContainer,
                                        delta.getType());
                            }
                            if (delta.getScope() != null) {
                                updateValue(elementDependency, ProjectContainer.XML_TAG_SCOPE, projectContainer,
                                        delta.getScope());
                            }
                            if (delta.getClassifier() != null) {
                                updateValue(elementDependency, ProjectContainer.XML_TAG_CLASSIFIER,
                                        projectContainer, delta.getClassifier());
                            }
                        }
                    }
                    if (notFound) {
                        getLog().warn("Dependency NOT found in pom.xml");
                    }
                }
            }
        }
    }

    protected void updateValue(Element containerElement, String valueTagname,
                               ProjectContainer projectContainer, String newValue) throws MojoExecutionException,
            MojoFailureException {

        Element elementValue = DomUtilities.getChildElement(containerElement, valueTagname);
        if (elementValue != null) {
            String currentValue = elementValue.getTextContent();
            if (currentValue.startsWith(ProjectContainer.PROPERTY_PREFIX)) {
                String propertyName = currentValue.substring(ProjectContainer.PROPERTY_PREFIX.length(),
                        currentValue.length() - ProjectContainer.PROPERTY_SUFFIX.length());
                if (propertyName.startsWith(ProjectContainer.PROPERTY_PREFIX_POM)
                        || propertyName.startsWith(ProjectContainer.PROPERTY_PREFIX_PROJECT)) {
                    getLog().debug("Keeping internal property '" + propertyName + "' ...");
                } else {
                    projectContainer.updatePropertyValue(propertyName, newValue);
                }
            } else {
                String oldValue = elementValue.getTextContent();
                elementValue.setTextContent(newValue);
                getLog().debug(
                        "Updating '" + valueTagname + "' of '" + projectContainer.getId() + "' from '"
                                + oldValue + "' to '" + newValue + "'.");
                projectContainer.setModified();
            }
        }

    }

    protected void updateProject(ProjectContainer projectContainer, MavenProject project,
                                 boolean parent) throws MojoExecutionException, MojoFailureException {

        if (getMatcher().isMatching(project)) {
            DependencyInfo delta = getNewAttributes().getDiff(project);
            if (!delta.isEmpty()) {
                String source;
                if (parent) {
                    source = "parent";
                } else {
                    source = "project";
                }
                getLog().info("Changing " + source + " " + project.getId() + " to " + delta.toDiffString());
                Element elementProject = projectContainer.getPomDocument().getDocumentElement();
                if (parent) {
                    elementProject = DomUtilities.getChildElement(elementProject,
                            ProjectContainer.XML_TAG_PARENT);
                }
                if (delta.getGroupId() != null) {
                    updateValue(elementProject, ProjectContainer.XML_TAG_GROUPID, projectContainer, delta
                            .getGroupId());
                }
                if (delta.getArtifactId() != null) {
                    updateValue(elementProject, ProjectContainer.XML_TAG_ARTIFACTID, projectContainer, delta
                            .getArtifactId());
                }
                if (delta.getVersion() != null) {
                    updateValue(elementProject, ProjectContainer.XML_TAG_VERSION, projectContainer, delta
                            .getVersion());
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void execute(ProjectContainer projectContainer) throws MojoExecutionException,
            MojoFailureException {


        if (getNewAttributes().isEmpty()) {
            getLog().debug("New Parameters are --->"+ toString());
            throw new MojoExecutionException(
                    "At least one of the new*-parameters (newVersion, newArtifactId, newGroupId, newScope or newClassifier) have to be configured!");
        }
        if(projectContainer!=null){
            super.execute(projectContainer);
            MavenProject project = projectContainer.getProject();
            updateProject(projectContainer, project, false);
            MavenProject parent = project.getParent();
            if (parent != null) {
                updateProject(projectContainer, parent, true);
            }
            updateDependencies(project.getDependencies(), projectContainer,
                    ProjectContainer.XML_TAG_DEPENDENCIES);
            DependencyManagement dependencyManagement = project.getDependencyManagement();
            if (dependencyManagement != null) {
                updateDependencies(dependencyManagement.getDependencies(), projectContainer,
                        ProjectContainer.XML_TAG_DEPENDENCY_MANAGEMENT);
            }
        }

    }

    @Override
    public String toString() {
        return "PomRefactorMojo{" +
                "newGroupId='" + newGroupId + '\'' +
                ", newArtifactId='" + newArtifactId + '\'' +
                ", newVersion='" + newVersion + '\'' +
                ", newType='" + newType + '\'' +
                ", newScope='" + newScope + '\'' +
                ", newClassifier='" + newClassifier + '\'' +
                ", resolveVariables=" + resolveVariables +
                ", newAttributes=" + newAttributes +
                '}';
    }
}
