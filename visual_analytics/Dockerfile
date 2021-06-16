# Stage 1
# FROM node:10-alpine as build-step
# RUN mkdir -p /app
# WORKDIR /app
# COPY package.json /app
# RUN npm install
# COPY . /app
#RUN npm run build --prod 

# Stage 2
# FROM nginx:1.17.1-alpine
# RUN rm /usr/share/nginx/html/*
# COPY --from=build-step /app/dist/visual_analytics /usr/share/nginx/html

FROM nginx:1.17.1-alpine
RUN rm /usr/share/nginx/html/*
COPY ./dist/visual_analytics /usr/share/nginx/html