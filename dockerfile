FROM jenkins/jenkins
LABEL name="trial headless chrome on mark59/jenkins"

# ENV http_proxy http://my.proxy.corp:8888
# ENV https_proxy http://my.proxy.corp:8888

USER root
RUN echo "Australia/Melbourne" > /etc/timezone && dpkg-reconfigure -f noninteractive tzdata

# install Jenkins plugins one at a time (better chance to work) 
RUN /usr/local/bin/install-plugins.sh build-timeout 
RUN /usr/local/bin/install-plugins.sh email-ext 
RUN /usr/local/bin/install-plugins.sh git 
RUN /usr/local/bin/install-plugins.sh greenballs 
RUN /usr/local/bin/install-plugins.sh htmlpublisher
RUN /usr/local/bin/install-plugins.sh jobConfigHistory 
RUN /usr/local/bin/install-plugins.sh log-parser 
RUN /usr/local/bin/install-plugins.sh nodelabelparameter
RUN /usr/local/bin/install-plugins.sh timestamper
RUN /usr/local/bin/install-plugins.sh parameterized-trigger

RUN apt-get update && apt-get clean

# Set the Chrome repo.
RUN wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - \
    && echo "deb http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google.list

# Install Chrome.
RUN apt-get update && apt-get -y install google-chrome-stable

# Install mpstat command  
RUN apt-get update && apt-get -y install sysstat

USER jenkins

