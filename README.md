# pom-augmenter-for-coverage-check #

**pom-augmenter-for-coverage-check** is a automated command line application to analyze a maven project's POM files to add ***jacoco line coverage check rule*** if **jacoco-prepare-agent** is invoked in any of the parent or child pom file.

##Getting Started
###Restrictions
This application will search for an execution with **prepare-agent** goal in **jacoco-maven-plugin** and adds **default-check**  goal with the required line coverage threshold.
As the application starts with the parent POM, all the child modules should be included under the `<modules>` tag.
###Prerequisites
JRE for java 8
###Usage
Run compiled jar file in the target folder.

```bash
java -jar prepare-line-check-0.1-SNAPSHOT.jar ${PATH_TO_PROJECT} ${COVERAGE_THRESHOLD} ${COVERAGE_PER_TYPE}
```
* **${PATH_TO_PROJECT}**
    : Path to the folder where your parent POM exists 
* **${COVERAGE_THRESHOLD}** : Line coverage threshold value(between 0 to 1) to fail the build  
* **${COVERAGE_PER_TYPE}** : In which way unit test coverage check is calculated. BUNDLE, PACKAGE, CLASS, SOURCEFILE or METHOD

