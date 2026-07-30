# Sử dụng môi trường Maven kèm Java 21
FROM maven:3.9-eclipse-temurin-21

# Tạo thư mục làm việc
WORKDIR /app

# Copy toàn bộ mã nguồn vào container
COPY . .

# Mở cổng 8080 (cổng mặc định của Spring Boot)
EXPOSE 8080

# Chạy ứng dụng bằng lệnh của Maven
CMD ["mvn", "spring-boot:run"]