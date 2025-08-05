FROM node:18-alpine as build
WORKDIR /app

COPY package*.json ./

RUN npm install

COPY public ./public
COPY src ./src
COPY .env ./

ENV PUBLIC_URL=/admin
ENV REACT_APP_API_BASE_URL=/api/v1

RUN npm run build

FROM nginx:alpine
COPY --from=build /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

RUN find /usr/share/nginx/html -type f > /usr/share/nginx/html/files.txt

RUN sed -i 's|access_log .*;|access_log /var/log/nginx/access.log;|' /etc/nginx/conf.d/default.conf \
    && echo "error_log /var/log/nginx/error.log debug;" >> /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]