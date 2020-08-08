package io.github.atkawa7.pom;

import org.apache.maven.project.MavenProject;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;

public class ProjectId {

    /** @see #getGroupId() */
    private final String groupId;

    /** @see #getArtifactId() */
    private final String artifactId;

    /**
     * @param project
     */
    public ProjectId(MavenProject project) {

        this(project.getGroupId(), project.getArtifactId());
    }

    /**
     * @param groupId
     * @param artifactId
     */
    public ProjectId(String groupId, String artifactId) {

        super();
        this.groupId = groupId;
        this.artifactId = artifactId;
    }

    /**
     * @return the groupId
     */
    public String getGroupId() {

        return this.groupId;
    }

    /**
     * @return the artifactId
     */
    public String getArtifactId() {

        return this.artifactId;
    }

    public boolean isMatching(String groupId, String artifactId, String version, String type,
                              String scope, String classifier) {

        if ((this.groupId == null) || (groupId == null) || (this.groupId.equals(groupId))) {
            if ((this.artifactId == null) || (artifactId == null) || (this.artifactId.equals(artifactId))) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see #equals(Object)
     */
    public final boolean isMatching(Dependency dependency) {

        return isMatching(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion(),
                dependency.getType(), dependency.getScope(), dependency.getClassifier());
    }

    /**
     * @see #equals(Object)
     */
    public final boolean isMatching(MavenProject dependency) {

        return isMatching(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion(),
                dependency.getPackaging(), null, null);
    }

    public boolean isEmpty() {

        if (this.groupId != null) {
            return false;
        }
        if (this.artifactId != null) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {

        if (other == null) {
            return false;
        }
        if (other.getClass() != ProjectId.class) {
            return false;
        }
        ProjectId pid = (ProjectId) other;
        if (this.groupId == null) {
            if (pid.groupId != null) {
                return false;
            }
        } else if (!this.groupId.equals(pid.groupId)) {
            return false;
        }
        if (this.artifactId == null) {
            if (pid.artifactId != null) {
                return false;
            }
        } else if (!this.artifactId.equals(pid.artifactId)) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {

        int hash = 0;
        if (this.groupId != null) {
            hash = this.groupId.hashCode();
        }
        if (this.artifactId != null) {
            hash = hash + this.artifactId.hashCode();
        }
        return hash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        toString(sb);
        return sb.toString();
    }

    public void toString(StringBuilder sb) {

        sb.append(this.groupId);
        sb.append(':');
        sb.append(this.artifactId);
    }

}
