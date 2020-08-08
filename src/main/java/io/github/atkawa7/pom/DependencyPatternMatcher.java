package io.github.atkawa7.pom;

import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;

import java.util.regex.Pattern;

public class DependencyPatternMatcher extends DependencyInfo {

    private final Pattern patternGroupId;

    private final Pattern patternArtifactId;

    private final Pattern patternVersion;

    /**
     * @param dependency
     */
    public DependencyPatternMatcher(Dependency dependency) {

        this(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion(), dependency
                .getType(), dependency.getScope(), dependency.getClassifier());
    }

    /**
     * @param project
     */
    public DependencyPatternMatcher(MavenProject project) {

        this(project.getGroupId(), project.getArtifactId(), project.getVersion(), project
                .getPackaging(), null, null);
    }

    /**
     * @param groupId
     * @param artifactId
     * @param version
     * @param type
     * @param scope
     * @param classifier
     */
    public DependencyPatternMatcher(String groupId, String artifactId, String version, String type,
                                    String scope, String classifier) {

        super(groupId, artifactId, version, type, scope, classifier);
        this.patternGroupId = compileGlob(groupId);
        this.patternArtifactId = compileGlob(artifactId);
        this.patternVersion = compileGlob(version);
    }

    private static Pattern compileGlob(String pattern) {

        if (pattern == null) {
            return null;
        }
        String globPattern = pattern.replace("\\", "\\\\").replace("^", "\\^").replace("+", "\\+")
                .replace("$", "\\$").replace(".", "\\.").replace("(", "\\(").replace("[", "\\[").replace(
                        "{", "\\{").replace("*", ".*").replace("?", ".");
        return Pattern.compile(globPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMatching(String groupId, String artifactId, String version, String type,
                              String scope, String classifier) {

        if (this.patternGroupId != null) {
            if (!this.patternGroupId.matcher(groupId).matches()) {
                return false;
            }
        }
        if (this.patternArtifactId != null) {
            if (!this.patternArtifactId.matcher(artifactId).matches()) {
                return false;
            }
        }
        if (this.patternVersion != null) {
            if (!this.patternVersion.matcher(version).matches()) {
                return false;
            }
        }
        return super.isMatching(null, null, null, type, scope, classifier);
    }

}