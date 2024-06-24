FROM node:20.12.2-alpine AS build-stage
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine AS run-stage
COPY nginx.conf /etc/nginx/nginx.conf
COPY --from=build-stage /app/dist/book-network-frontend/browser /usr/share/nginx/html
EXPOSE 80
