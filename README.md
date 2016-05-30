JIRA Plugin
===========
[![Build Status](https://api.travis-ci.org/SonarQubeCommunity/sonar-jira.svg)](https://travis-ci.org/SonarQubeCommunity/sonar-jira)


## Description
This plugin connects SonarQube to Altassian JIRA in various ways.

* **Widget JIRA Issues:**
SonarQube retrieves the number of issues associated with a project from JIRA. It then reports on the total number of issues and distribution by priority.

* **Link a SonarQube issue to a JIRA ticket:**
This feature allows you to link a SonarQube issue to a JIRA issue by creating a new JIRA ticket for the SonarQube issue.
When logged in, you should find the "Link to JIRA" action available on any issue.
After you press "Link to JIRA", a new review comment is added on the issue: you can see the link to the newly-created JIRA ticket.

## Requirements

Plugin | 1.0 | 1.1 | 1.2
------ | --- | --- | ---
Jira 3.x | :white_check_mark: | :white_check_mark: | :white_check_mark:
Jira 4.x | :white_check_mark: | :white_check_mark: | :white_check_mark:
Jira 5.x | :white_check_mark: | :white_check_mark: | :white_check_mark:
Jira 6.x | :x: | :x: | :white_check_mark:
	
## Usage

#### Mandatory Properties

At the project or global level go to Settings > JIRA and set the "sonar.jira.url", "sonar.jira.login.secured" and "sonar.jira.password.secured" properties.

**Security note for SonarQube 3.4.0 to 3.6.3 included**

For the *.secured properties to be read during the project analysis, it is necessary to set the "sonar.login" and "sonar.password" properties to the credentials of a user that is both:
* System administrator
* And project administrator on the project that is being analyzed

Example:
> sonar-runner -Dsonar.login=admin -Dsonar.password=admin

#### To Display Data on the JIRA Issues Widget

1. Log in to your JIRA instance and create a filter. Mark it as favorite. You may also want to share this filter with your team.
2. Specify the "sonar.jira.url.param" property for the project or module: this is the name of an issue filter that you have previously created on JIRA (see the [JIRA documentation for more on issue filters](https://confluence.atlassian.com/pages/viewpage.action?pageId=284367607)).

#### To Create JIRA Issues

Set the "sonar.jira.project.key" property that is the key of the JIRA project for which this SonarQube plugin will generate tickets

Note that you can also configure the issue type, the component, the priorities. Browse the JIRA setting page on the web interface for a complete list.

**Connecting to JIRA through HTTPS**

If you must connect to your JIRA instance through HTTPS, and a certificate is required for this connection, then you must import the certificate into the CA store of the JRE that runs SonarQube.

## FAQ

* Q: I see the following warning in SonarQube logs when using the JIRA plugin:

> [WARN] [18:32:24.426] Unable to find required classes (javax.activation.DataHandler and javax.mail.internet.MimeMultipart). Attachment support is disabled.

* A: This is a message from the Axis SOAP client we are using to talk to your JIRA. You can safely ignore this message. Attachment support is not used anyway.


* Q: I have configured my JIRA instance so that some fields are mandatory. Because SonarQube does not populate these fields, issue creation fails. How can I make it work?
* A: The "sonar.jira.issue.component.id" property can be defined at project level. For other fields the suggested workaround is to create a new issue type in JIRA (for example "Quality issue") and set the "sonar.jira.issue.type.id" property accordingly.
