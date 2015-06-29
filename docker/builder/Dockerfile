FROM williamyeh/java8
ENV HTTP_PROXY="http://__proxy__"
ENV HTTPS_PROXY="http://__proxy__"
ENV GRADLE_OPTS="-Dorg.gradle.daemon=true \
                 -Dhttp.proxyHost=__proxyHost__ \
                 -Dhttp.proxyPort=__proxyPort__ \
                 -Dhttp.proxyUser=__proxyUser__ \
                 -Dhttp.proxyPassword=__proxyPassword__ \
                 -Dhttps.proxyHost=__proxyHost__ \
                 -Dhttps.proxyPort=__proxyPort__ \
                 -Dhttps.proxyUser=__proxyUser__ \
                 -Dhttps.proxyPassword=__proxyPassword__"
ENV GRADLE_USER_HOME=/source/.gradle
ENV PATH=.:$PATH
VOLUME ["/source"]
WORKDIR /source
CMD ["bash"]
