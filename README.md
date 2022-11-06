# Java Maven Library Quickstart
[![Build Status]([![Build Status](https://dev.azure.com/hpcodeway/OneCloud/_apis/build/status/Quickstarts/onecloud.java-library-quickstart?repoName=onecloud%2Fjava-library-quickstart&branchName=master)](https://dev.azure.com/hpcodeway/OneCloud/_build/latest?definitionId=9077&repoName=onecloud%2Fjava-library-quickstart&branchName=master))
<a href="https://sq.corp.hpicloud.net/dashboard?id=onecloud_java-library-quickstart.unit">
<img src="https://docs.sonarqube.org/latest/images/SonarQubeIcon.svg" width="80">
</a>

# About this quickstart

* Runs and publishes unit tests and test coverage
* Sonar scans with gated builds
* Publishes artifacts to Nexus repositories

# Using this quickstart

## Creating a new Java Maven Library with Nexus

If the user does not have Nexus set up in his Codeway project yet, the user could set it up by referring the below link 

[Setting up Nexus](https://rndwiki.inc.hpicorp.net/confluence/display/softwareathp/How+to+integrate+ADO+to+Nexus)

Once the user has setup Nexus, the user can then click on 'Use this template' in the github repository of `java-maven-library-quickstart` and create a repository out of that template.

```xml
    <repositories>
        <repository>
            <id>nexus-it</id>
            <!-- Update this URL with your project/team Nexus repository -->
            <url>https://nexus-int.corp.hpicloud.net/repository/'your nexus repository'/</url>
            <releases>
                <enabled>true</enabled>
            </releases>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
    </repositories>
 ```

# Further reading

* [Java Library v1](https://pages.github.azc.ext.hp.com/codeway/templates/docs/v1.0/pipeline-templates/onecloud-java-library-v1.html)
* [Anatomy of Codeway Template](https://rndwiki.inc.hpicorp.net/confluence/pages/viewpage.action?pageId=1160910093)
* [Setting up a project in Codeway](https://rndwiki.inc.hpicorp.net/confluence/display/softwareathp/Setup+a+new+project+in+CodeWay)
* [Maven](http://maven.apache.org/guides/introduction/introduction-to-the-pom.html)

# How to get help

* [CodeWay wiki](https://rndwiki.inc.hpicorp.net/confluence/display/softwareathp/HP+CodeWay)
* [Support](https://rndwiki.inc.hpicorp.net/confluence/display/softwareathp/Support+and+SLAs)
* [CodeWay support teams channel](https://teams.microsoft.com/l/channel/19%3a77e2e2b481f546729871fee3efbe0e63%40thread.skype/Support?groupId=62118f2d-d3e3-4d3f-bda5-f480137333de&tenantId=ca7981a2-785a-463d-b82a-3db87dfc3ce6)
* [CodeWay Yammer group](https://www.yammer.com/hp.com/?show_login=true#/threads/inGroup?type=in_group&feedId=5283897344&view=all)
# mysql-aws
# mysql-aws
# mysql-aws
# mysql-aws
