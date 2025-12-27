<div style="text-align: center; padding: 3rem 2rem; background: linear-gradient(to right, #0ea5e9, #2563eb); border-radius: 20px; margin-bottom: 2rem; box-shadow: 0 20px 60px rgba(14, 165, 233, 0.4);">
  <img src="src/main/resources/views/icons/logo_readme_version.png" alt="AuditDoc AI logo" style="width: 120px; height: 120px; margin: 0 auto 1.5rem; display: block; border-radius: 24px; box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3); border: 4px solid rgba(255, 255, 255, 0.2);" />
  <h1 style="margin: 0; font-size: 3.5rem; font-weight: 800; color: #ffffff; letter-spacing: -0.03em; text-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);">AuditDoc AI</h1>
  <p style="margin: 1rem 0 0; font-size: 1.2rem; color: rgba(255, 255, 255, 0.9); font-weight: 500;">Smart document intelligence</p>
  <p style="margin: 0.5rem 0 0; font-size: 1rem; color: rgba(255, 255, 255, 0.75);">Audit, analyze, and synthesize your documents with confidence.</p>
</div>

# AuditDoc AI


[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2+-green.svg)](https://spring.io/projects/spring-boot)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-blue.svg)](https://openjfx.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

AuditDoc AI is an intelligent desktop application that automates document compliance auditing using AI-powered analysis. Built with JavaFX and Spring Boot, it helps organizations streamline their audit processes by automatically detecting issues, generating reports, and tracking project compliance.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Technologies](#technologies)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Project Structure](#project-structure)
- [AI Integration](#ai-integration)
- [Contributing](#contributing)

## 🎯 Overview <a id="overview"></a>

AuditDoc AI transforms traditional document auditing by leveraging artificial intelligence to automatically analyze documents against predefined compliance templates. The platform supports multiple document formats (PDF, Word, Excel) and provides detailed issue tracking, smart recommendations, and comprehensive reporting.

**Key Capabilities:**
- Automated document analysis using AI (Ollama/OpenAI/Gemini)
- Multi-format document support
- Template-based compliance checking
- Real-time audit progress tracking
- Interactive dashboards with AI chatbot assistance
- Comprehensive PDF/Word report generation

## ✨ Features <a id="features"></a>

### Core Functionality
- **🤖 AI-Powered Analysis**: Automatic document review using local or cloud AI models
- **📄 Multi-Format Support**: Process PDF, DOCX, DOC, XLSX, XLS files
- **📊 Dashboard Analytics**: Visual insights into audit status and compliance scores
- **🔍 Issue Detection**: Automated identification of compliance problems with location tracking
- **💬 AI Chatbot Assistant**: Context-aware help system using Gemini API
- **📈 Progress Tracking**: Real-time monitoring of audit completion
- **📑 Report Generation**: Professional PDF/Word reports with customizable templates

### User Management
- **🔐 Secure Authentication**: BCrypt password hashing with JWT-ready architecture
- **👥 Role-Based Access**: Admin, Manager, and User roles
- **📧 Password Recovery**: Email-based password reset flow
- **🔔 Notifications**: Real-time alerts for audit events

### Project Management
- **📂 Project Organization**: Manage multiple projects with dedicated folders
- **📅 Audit Scheduling**: Track upcoming and past audits
- **📝 Audit Templates**: Customizable compliance models per organization
- **📚 Document History**: Complete audit trail with version control

## 🏗️ Architecture <a id="architecture"></a>
```
┌─────────────────────────────────────────────────────────┐
│                    JavaFX Frontend                      │
│  (Controllers, FXML Views, CSS Styling)                │
└──────────────────┬──────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────┐
│              Service Layer (UI Services)                │
│  AuditApiService, FileUploadService, NotificationUI     │
└──────────────────┬──────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────┐
│            Business Logic (Backend Services)            │
│  AuditService, AiAuditService, ReportService, etc.     │
└──────────────────┬──────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────┐
│              Data Access Layer (JPA/Hibernate)          │
│       Repositories + PostgreSQL Database                │
└─────────────────────────────────────────────────────────┘
```

**Design Patterns:**
- **MVC Pattern**: Clean separation between UI and business logic
- **Service Layer Pattern**: Dedicated services for each domain
- **Repository Pattern**: JPA repositories for data access
- **DTO Pattern**: Data transfer between layers
- **Strategy Pattern**: Pluggable AI providers (Ollama, OpenAI, Gemini)

## 🛠️ Technologies <a id="technologies"></a>

### Backend
- **Spring Boot 3.2+** - Application framework
- **Spring Data JPA** - Data persistence
- **Hibernate** - ORM
- **PostgreSQL** (Supabase) - Production database
- **H2** - Development database
- **Spring Mail** - Email notifications

### Frontend
- **JavaFX 21** - Desktop UI framework
- **FXML** - UI markup
- **CSS** - Styling

### AI Integration
- **Ollama** - Local AI inference
- **OpenAI API** - GPT models
- **Google Gemini API** - Chatbot and analysis

### Document Processing
- **Apache PDFBox** - PDF extraction
- **Apache POI** - Word/Excel processing
- **iText PDF** - PDF generation

### Build & Deployment
- **Maven** - Dependency management
- **Lombok** - Boilerplate reduction
- **SLF4J + Logback** - Logging

## 📋 Prerequisites <a id="prerequisites"></a>

Before installing, ensure you have:

- **Java 17 or higher** ([Download](https://www.oracle.com/java/technologies/downloads/))
- **Maven 3.8+** ([Download](https://maven.apache.org/download.cgi))
- **PostgreSQL** (or use the provided Supabase connection)
- **Ollama** (optional, for local AI) ([Install](https://ollama.ai/))
- **Git** for cloning the repository

**System Requirements:**
- OS: Windows 10/11, macOS 10.15+, or Linux
- RAM: 4GB minimum, 8GB recommended
- Storage: 500MB for application + space for documents

## 💻 Installation <a id="installation"></a>

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/auditdoc-ai.git
cd auditdoc-ai
```

### 2. Install Dependencies
```bash
mvn clean install
```

### 3. Setup Database

The application uses PostgreSQL (Supabase). The connection is pre-configured, but you can modify it in `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://your-db-url:6543/postgres
    username: your-username
    password: your-password
```

Tables will be created automatically on first run (`ddl-auto: update`).

### 4. Configure AI Provider

#### Option A: Ollama (Local, Free)

1. Install Ollama from https://ollama.ai/
2. Download a model:
```bash
   ollama pull llama3
```
3. Start Ollama:
```bash
   ollama serve
```

#### Option B: OpenAI (Cloud)

Add your API key to `application.yml`:
```yaml
ai:
  provider: openai
  api:
    key: sk-your-openai-api-key
  model: gpt-3.5-turbo
```

#### Option C: Google Gemini (Cloud)

Add your API key to `application.properties`:
```properties
gemini.apiKey=your-gemini-api-key
gemini.model=gemini-2.0-flash
```

## ⚙️ Configuration <a id="configuration"></a>

### Email Configuration (Gmail)

For password reset functionality, configure SMTP in `application.yml`:
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password  # Generate from Google Account settings
```

**To generate Gmail App Password:**
1. Enable 2-Step Verification in your Google Account
2. Go to Security → App Passwords
3. Generate a new app password
4. Use this password in the configuration

### File Upload Settings

Adjust max file sizes if needed:
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 50MB
```

### AI Model Configuration

Customize AI behavior:
```yaml
ai:
  max-tokens: 2000      # Response length
  temperature: 0.7      # Creativity (0-1)
  simulation:
    mode: auto          # auto/enabled/disabled
```

## 🚀 Running the Application <a id="running-the-application"></a>

### Method 1: Maven
```bash
mvn clean javafx:run
```

### Method 2: IDE (IntelliJ IDEA / Eclipse)

1. Open the project in your IDE
2. Run `Main.java` (located in `src/main/java/com/yourapp/`)
3. The application will launch in maximized mode

### Method 3: Package as JAR
```bash
mvn clean package
java -jar target/auditdoc-ai-1.0.0.jar
```

## 📁 Project Structure <a id="project-structure"></a>
```
auditdoc-ai/
├── src/
│   ├── main/
│   │   ├── java/com/yourapp/
│   │   │   ├── AI/                     # AI integration
│   │   │   │   ├── AiClient.java       # AI provider abstraction
│   │   │   │   ├── AiPromptBuilder.java
│   │   │   │   └── AiResponseParser.java
│   │   │   ├── API/                    # REST controllers (optional)
│   │   │   ├── DAO/                    # JPA repositories
│   │   │   ├── controller/             # JavaFX controllers
│   │   │   │   ├── AuditController.java
│   │   │   │   ├── DashboardController.java
│   │   │   │   ├── LoginController.java
│   │   │   │   └── ...
│   │   │   ├── dto/                    # Data transfer objects
│   │   │   ├── model/                  # JPA entities
│   │   │   │   ├── Audit.java
│   │   │   │   ├── AuditDocument.java
│   │   │   │   ├── AuditIssue.java
│   │   │   │   ├── User.java
│   │   │   │   └── ...
│   │   │   ├── services/               # Business logic
│   │   │   │   ├── AuditService.java
│   │   │   │   ├── AiAuditService.java
│   │   │   │   ├── AuthenticationService.java
│   │   │   │   └── ...
│   │   │   ├── services_UI/            # UI service layer
│   │   │   ├── utils/                  # Utilities
│   │   │   └── Main.java              # Application entry point
│   │   └── resources/
│   │       ├── views/
│   │       │   ├── css/               # Stylesheets
│   │       │   ├── fxml/              # UI layouts
│   │       │   └── icons/             # Images
│   │       ├── application.yml        # Main configuration
│   │       └── application.properties # Additional config
│   └── test/                          # Unit tests
├── uploads/                           # Document storage
├── reports/                           # Generated reports
├── pom.xml                           # Maven configuration
└── README.md
```

### Key Packages

- **`AI/`**: AI provider integration and response parsing
- **`controller/`**: JavaFX UI controllers
- **`services/`**: Core business logic
- **`services_UI/`**: Bridge between UI and backend
- **`model/`**: Database entities
- **`DAO/`**: Data access repositories

## 🤖 AI Integration <a id="ai-integration"></a>

### Supported Providers

| Provider | Type | Use Case | Cost |
|----------|------|----------|------|
| **Ollama** | Local | Development, privacy-focused | Free |
| **OpenAI** | Cloud | Production, best quality | Paid |
| **Gemini** | Cloud | Chatbot, cost-effective | Free tier available |

### How AI Analysis Works

1. **Document Upload**: User uploads documents (PDF, Word, Excel)
2. **Content Extraction**: Text is extracted using Apache libraries
3. **Prompt Construction**: `AiPromptBuilder` creates context-aware prompts
4. **AI Analysis**: Selected provider analyzes content
5. **Response Parsing**: `AiResponseParser` converts JSON to structured issues
6. **Issue Storage**: Results saved to database
7. **Report Generation**: Professional PDF/Word reports created

### Customizing AI Prompts

Edit `AiPromptBuilder.java` to customize analysis prompts:
```java
public String buildPrompt(AuditTemplate template, String documentContent, String documentName) {
    // Modify this method to adjust AI instructions
    prompt.append("Your custom instructions here...");
}
```

### AI Provider Switching

Change providers in `application.yml`:
```yaml
ai:
  provider: ollama    # Options: ollama, openai, gemini
  model: llama3       # Model name
```

Or set `ai.simulation.mode: enabled` for testing without AI.

## 🤝 Contributing <a id="contributing"></a>

We welcome contributions! To contribute:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'Add amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. **Open** a Pull Request
