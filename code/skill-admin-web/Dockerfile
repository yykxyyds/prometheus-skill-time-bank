# 第一阶段：构建
FROM node:20-alpine AS build
ARG NPM_REGISTRY=https://registry.npmjs.org
WORKDIR /app
COPY package*.json ./
RUN npm config set registry ${NPM_REGISTRY} && npm install
COPY . .
RUN npm run build

# 第二阶段：运行
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
