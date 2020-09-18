FROM mark59-jenkins-base
LABEL name="ci for mark59"

COPY ./JENKINSHOME /var/jenkins_home/

USER root

RUN chmod -R 777 /var/jenkins_home/

USER jenkins
