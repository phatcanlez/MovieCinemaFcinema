# =============================
# Stage 1: Build với Maven
# =============================
FROM maven:3.9.6-eclipse-temurin-22-alpine AS builder

# Tạo thư mục làm việc
WORKDIR /app

# Copy toàn bộ mã nguồn vào container
COPY . .

# Build project
RUN mvn clean package -DskipTests

# =============================
# Stage 2: Runtime (JRE only)
# =============================
FROM eclipse-temurin:17-jre-alpine

# Tạo thư mục làm việc
WORKDIR /app

# Copy file jar từ stage build
COPY --from=builder /app/target/*.jar app.jar

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
