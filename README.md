
# Pom-maven-plugin

Pom-maven-plugin which aims to help with POM tasks in large projects. 

You need to check it out and do "mvn install".

After  that you are ready to use the plugin.

Refactoring POMs

There is a goal for refactoring POMs in a project called "pom:refactor". You should call this goal on your toplevel project.

Examples:

    mvn pom:refactor -DartifactId=myArtifact -DnewVersion=2.1
    This will set the version of all occurrences of "myArtifact" in your POMs to "2.1" including all dependencies that point to the current version.
    mvn pom:refactor -DartifactId=myArtifact -Dversion=* -DnewVersion=2.1
    This will set the version of all occurrences of "myArtifact" in your POMs to "2.1" including all dependencies that point to the current version.
    mvn pom:refactor -DartifactId=myArtifact -Dversion=2.1-SNAPSHOT -DnewVersion=2.1
    This will set the version of all occurrences of "myArtifact" in your POMs where current version is "2.1-SNAPSHOT" to "2.1"
    mvn pom:refactor -DartifactId=* -DnewGroupId=foo.bar
    This will change the groupId of all your modules to "foo.bar" (and fix all according dependencies).
    mvn pom:refactor -DartifactId=myArtifact -DgroupId=foo.bar -DnewScope=test
    This will set the scope of all dependencies on "foo.bar:myArtifact" to "test"

In general, you define a matcher for projects, dependencies, etc. with

    artifactId (required parameter, set to * to match any artifactId)
    groupId (${project.groupId} if omitted)
    version (current version of matching project in reactor if omitted, use * to match any version)
    scope (scope to match in dependencies, any if omitted)
    classifier (classifier to match in dependencies, any if omitted)

For all these parameters you can use glob-patterns (using * as wildcard and ?). Please note that your shell might expand such patterns with the matching files in your current directory, so you might need to escape this properly.

To express what to change for the matching sections, you must specify at least one of

    newArtifactId
    newGroupId
    newVersion
    newScope
    newClassifier

Please also note that the plugin takes care about distribution management and variables. In such case it tries to perform the modification where the actual value is defined. However you might be able to create wired constructs where the strategy will fail. But it should work for regular usecases. You can also set "resolveVariables" to false if you want to disable and allow to replace "${foo}" with "${bar}".

Further important options:

    overwrite (true: overwrite pom.xml files, false: write to target/pom-refactored.xml - default)
    requireToplevel (default is true, if your toplevel-POM on local disc has a parent you need to set to false)
    xmlEncoding (defaults to file.encoding)

