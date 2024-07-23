# Stage 1: Build the frontend
FROM node:20.6.1-slim AS frontend-builder
WORKDIR /frontend
COPY ./package*.json ./
RUN npm install --force
COPY . .
RUN npm run build
# Final stage: Create the final image for frontend
FROM node:20.6.1-slim
WORKDIR /app/frontend
COPY --from=frontend-builder /frontend ./
EXPOSE 3000
CMD ["npm", "run", "dev"]