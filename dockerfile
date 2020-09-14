FROM jenkins/jenkins:lts
LABEL name="trial headless chrome on mark59/jenkins"

RUN whoami
USER root

RUN apt-get update && apt-get clean

# RUN /usr/local/bin/install-plugins.sh docker-slaves github-branch-source:1.8

# COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
# RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt

# install all plugins
# COPY plugins.txt /var/jenkins_home/plugins.txt
# RUN chmod +x /usr/local/bin/install-plugins.sh
# RUN xargs /usr/local/bin/install-plugins.sh  < /var/jenkins_home/plugins.txt


# Set the Chrome repo.
RUN wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - \
    && echo "deb http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google.list

# Install Chrome.
RUN apt-get update && apt-get -y install google-chrome-stable


USER jenkins









