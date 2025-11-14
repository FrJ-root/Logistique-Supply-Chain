# Logistique Supply Chain

A Spring Boot application for logistics and supply chain management, built with Java 17 and modern technologies.

## 🚀 Features

- RESTful API for supply chain operations
- PostgreSQL database integration with H2 for development
- Docker containerization support
- API documentation with OpenAPI/Swagger
- Code coverage reporting with JaCoCo
- Object mapping with MapStruct
- Data validation and JPA support

## 🛠️ Tech Stack

- **Framework**: Spring Boot 3.5.7
- **Language**: Java 17
- **Database**: PostgreSQL (production), H2 (development)
- **Build Tool**: Maven
- **Containerization**: Docker & Docker Compose
- **Documentation**: SpringDoc OpenAPI
- **Testing**: JUnit with JaCoCo coverage
- **Object Mapping**: MapStruct
- **Code Simplification**: Lombok

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker and Docker Compose (optional)
- PostgreSQL (for production)

## 🚀 Getting Started

### Local Development

1. **Clone the repository**
```bash
git clone https://github.com/FrJ-root/Logistique-Supply-Chain.git
cd Logistique-Supply-Chain
```

2. **Build the project**
```bash
./mvnw clean install
```

3. **Run the application**
```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

### Using Docker

1. **Build and run with Docker Compose**
```bash
docker-compose up --build
```

This will start both the application and PostgreSQL database.

## 📚 API Documentation

Once the application is running, you can access the API documentation at:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## 🧪 Testing

Run tests with coverage report:
```bash
./mvnw clean test
```

Generate JaCoCo coverage report:
```bash
./mvnw clean verify
```

Coverage reports will be available in `target/site/jacoco/index.html`

## 🐳 Docker

### Build Docker Image
```bash
docker build -t logistics-api .
```

### Run with Docker Compose
```bash
docker-compose up -d
```

## 📁 Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── org/logistics/
│   └── resources/
└── test/
    └── java/
```

## 🔧 Configuration

The application uses Spring Boot's configuration system. Key configuration files:
- `application.properties` - Main application configuration
- `docker-compose.yml` - Docker services configuration
- `Dockerfile` - Container build instructions

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is open source and available under the [MIT License](LICENSE).

## 👨‍💻 Author

**FrJ-root**
- GitHub: [@FrJ-root](https://github.com/FrJ-root)

## 📞 Support

If you have any questions or need help, please open an issue in the GitHub repository.