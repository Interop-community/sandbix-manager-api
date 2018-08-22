# Set nginx base image
FROM nginx

# File Author / Maintainer
MAINTAINER Travis Cummings

# Copy custom configuration file from the current directory
COPY nginx.conf /etc/nginx
RUN mkdir /etc/nginx/sites-available
RUN chmod 777 /etc/nginx/sites-available
RUN mkdir /etc/nginx/sites-enabled

# sandman-api
COPY sandman-api.conf /etc/nginx/sites-available
RUN ln -sf /etc/nginx/sites-available/sandman-api.conf /etc/nginx/sites-enabled/sandman-api.conf
EXPOSE 12000

# sandman-api-debug
COPY sandman-api-debug.conf /etc/nginx/sites-available
RUN ln -sf /etc/nginx/sites-available/sandman-api-debug.conf /etc/nginx/sites-enabled/sandman-api-debug.conf
EXPOSE 12005
