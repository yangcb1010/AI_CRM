<div align="center">
  <img src="https://github.com/WuKongOpenSource/Wukong-AICRM/raw/main/docs/assets/feature-summary/logo.png" width="104" alt="Wukong-AICRM Logo" />
  <h1>Wukong AICRM</h1>
  <p><strong>Salesforce + ChatGPT: AI-enabled CRM for sales, customer success, and business execution</strong></p>

  <p>
    <a href="LICENSE"><img src="https://img.shields.io/badge/License-Noncommercial%20Use-blue.svg" alt="License" /></a>
    <a href="LICENSE"><img src="https://img.shields.io/badge/Commercial%20Use-Authorization%20Required-orange.svg" alt="Commercial Use" /></a>
    <a href="https://github.com/WuKongOpenSource/Wukong-AICRM/stargazers"><img src="https://img.shields.io/github/stars/WuKongOpenSource/Wukong-AICRM?style=flat" alt="GitHub Stars" /></a>
    <a href="https://github.com/WuKongOpenSource/Wukong-AICRM/pulls"><img src="https://img.shields.io/badge/PRs-welcome-brightgreen.svg" alt="PRs Welcome" /></a>
  </p>

  <p>
    <a href="https://www.72crm.ai/"><strong>Live Demo</strong></a>
    |
    <a href="#quick-start"><strong>Quick Start</strong></a>
    |
    <a href="#features"><strong>Features</strong></a>
    |
    <a href="https://bbs.72crm.com#/forum/detail/2069232286842191872"><strong>Community</strong></a>
    |
    <a href="https://github.com/WuKongOpenSource/Wukong-AICRM/blob/main/README.md"><strong>中文</strong></a>
    |
    <strong>English</strong>
  </p>
</div>

<p align="center">
  <img src="https://github.com/WuKongOpenSource/Wukong-AICRM/raw/main/docs/assets/feature-summary/readme-hero.svg" alt="Wukong AICRM product hero" />
</p>

| Quick links |
| :--- |
| [Try It Now](#try-it-now) \| [Features](#features) \| [Screenshots](#screenshots) \| [Demo Scenarios](#demo-scenarios) \| [Architecture](#architecture) \| [Quick Start](#quick-start) \| [Configuration](#configuration) |

---

## English Edition

Wukong AICRM is a modern CRM platform that combines customer records, communication history, task scheduling, knowledge base, and AI-assisted operations into one workflow. It helps teams improve response speed, collaboration, and sales execution quality.

## Try It Now

Wukong AICRM is available as a cloud demo and local install.

| Experience | Address / Account | Notes |
| :--- | :--- | :--- |
| **Live Demo** | [https://www.72crm.ai/](https://www.72crm.ai/) | Register a cloud account to try it |
| **Trial Account** | Create a new account on the cloud platform | For testing and evaluation |
| **Help & Discussion** | [Community Forum](https://bbs.72crm.com#/forum/detail/2069232286842191872) | Report issues and share ideas |

> **Tip:** The public demo contains sample data, so you can log in directly to try core features.

## Features

| Module | Core Value |
| :--- | :--- |
| **AI Assistant** | Ask business questions, get guidance, and execute actions from a unified conversation entry. |
| **Knowledge Base + RAG** | Upload manuals, contracts, meeting notes, and reuse documents for accurate Q&A and summaries. |
| **Smart Customer Management** | Centralize customer information, contacts, follow-ups, and automatically surface intent signals. |
| **AI Task Automation** | Convert conversations into structured tasks with priorities, reminders, and owners. |
| **Team Collaboration** | Sync customer updates, task assignments, and knowledge in real time across teams. |

## Technology Stack

- **Backend**: Java 21 + Spring Boot 3.x + Spring AI + PostgreSQL + Redis + MinIO
- **Frontend**: Vue 3 + TypeScript + Element Plus + Tailwind CSS
- **Deployment**: Docker Compose with full production-ready environment setup

## Screenshots

| AI Assistant | Customer Profile |
| :---: | :---: |
| ![AI Assistant](https://github.com/WuKongOpenSource/Wukong-AICRM/raw/main/docs/assets/feature-summary/03-ai-assistant-screen.png) | ![Customer Profile](https://github.com/WuKongOpenSource/Wukong-AICRM/raw/main/docs/assets/feature-summary/07-customer-profile-screen.png) |

| Task Management | Knowledge Base |
| :---: | :---: |
| ![Task Management](https://github.com/WuKongOpenSource/Wukong-AICRM/raw/main/docs/assets/feature-summary/09-task-management-screen.png) | ![AI Knowledge Base](https://github.com/WuKongOpenSource/Wukong-AICRM/raw/main/docs/assets/feature-summary/17-ai-knowledge-base-screen.png) |

## Demo Scenarios

### Sales Follow-Up

View daily priority customers, overdue tasks, and risk alerts at a glance. In customer details, AI summarizes records and follows history to suggest next best actions.

![Sales Follow-up](https://github.com/WuKongOpenSource/Wukong-AICRM/raw/main/docs/assets/feature-summary/n01-Sales%20Follow-up.png)

### Supervisor Collaboration

Managers can identify stalled opportunities and overdue tasks quickly, then coordinate pre-sales, success, or executives at key points.

![Supervisor Collaboration](https://github.com/WuKongOpenSource/Wukong-AICRM/raw/main/docs/assets/feature-summary/n02-Supervisor%20Collaboration.png)

### Customer Success

Customer success teams can proactively flag risk through interaction frequency and support issues, then arrange follow-ups and internal coordination before escalation.

![Customer Success](https://github.com/WuKongOpenSource/Wukong-AICRM/raw/main/docs/assets/feature-summary/n03-Customer%20Success.png)

### Knowledge Reuse

Turn high-quality responses, templates, proposals, and winning patterns into reusable company knowledge. New team members can reuse proven wording and playbooks consistently.

![Knowledge Reuse](https://github.com/WuKongOpenSource/Wukong-AICRM/raw/main/docs/assets/feature-summary/n04-Knowledge%20Reuse.png)

### System Architecture

AI Assistant connects customer management, knowledge retrieval, and task execution into one unified operating loop.

![Overall Architecture](https://github.com/WuKongOpenSource/Wukong-AICRM/raw/main/docs/assets/feature-summary/n05-Overall%20Architecture.png)

```text
┌───────────────┐          ┌───────────────────┐
│    Frontend   │◀───────▶│  Backend Service  │
└───────────────┘          └─────────┬─────────┘
                                     │
         ┌───────────────┬───────────┴───────────┬───────────────┐
         │               │                       │               │
  ┌───────────────┐ ┌───────────────┐     ┌───────────────┐ ┌───────────────┐
  │ PostgreSQL DB  │ │     Redis     │     │     MinIO     │ │   AI Gateway  │
  └───────────────┘ └───────────────┘     └───────────────┘ └───────────────┘
```

## Architecture

- **Backend**: Java 21, Spring Boot 3.x, Spring AI
- **Frontend**: Vue 3, TypeScript, Element Plus, Tailwind CSS, Vite
- **Deployment**: Docker Compose for one-click startup

### Backend Tech Stack

| Technology | Version | Purpose |
| :--- | :--- | :--- |
| Java | 21 | Programming Language |
| Spring Boot | 3.3.12 | Application Framework |
| Spring AI | 1.0.0 | AI/LLM Integration |
| PostgreSQL | 17 | Main Database |
| MyBatis-Plus | 3.5.7 | ORM Framework |
| Redis | - | Cache & Session |
| MinIO | - | Object Storage |

### Frontend Tech Stack

| Technology | Version | Purpose |
| :--- | :--- | :--- |
| Vue | 3.4 | Frontend Framework |
| TypeScript | 5.5 | Type Safety |
| Element Plus | 2.8 | UI Library |
| Pinia | 2.2 | State Management |
| Tailwind CSS | 3.4 | Utility-first Styles |
| Vite | 5.4 | Build Tool |

## Project Structure

```text
wk_ai_crm/
  backend/                 # Spring Boot backend project
    src/main/java/         # Java source
    src/main/resources/    # Config files
    pom.xml               # Maven config
  frontend/                # Vue frontend project
    src/                  # Frontend source
    package.json          # npm config
  docker/                  # Docker deployment files
    docker-compose.yaml   # Compose entry
  nginx/                   # Nginx config
  docs/                    # Docs
  LICENSE                  # License
  README.md                # Project README
```

## Quick Start

We recommend Docker one-click deployment first. Use manual source installation for development.

### Option 1: Docker One-Click Installation

Requirements:

- Docker
- Docker Compose

```bash
git clone https://github.com/WuKongOpenSource/Wukong-AICRM.git
cd Wukong-AICRM/docker
docker-compose up -d
```

Access the system at `http://localhost`.

### Option 2: Local Source Installation

Requirements:

- JDK 21+
- Node.js 18+
- Maven 3.8+
- PostgreSQL 17
- Redis 6+

1. Clone the repository

```bash
git clone https://github.com/WuKongOpenSource/Wukong-AICRM.git
cd Wukong-AICRM
```

2. Start backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend runs at: `http://localhost:8088`  
API docs: `http://localhost:8088/doc.html`

3. Start frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs at: `http://localhost:5173`

Then update `backend/src/main/resources/application.yml` for database, AI key, and required services.

## Configuration Guide

Primary configuration file: `backend/src/main/resources/application.yml`

### Database

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/wk_ai_crm
    username: postgres
    password: your_password
```

### Redis

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: your_password
      database: 7
```

### AI Service

```yaml
spring:
  ai:
    openai:
      api-key: your_api_key
      base-url: https://api.openai.com/v1/
      chat:
        options:
          model: gpt-4
```

### MinIO

```yaml
minio:
  enabled: true
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket: ai-crm
```

### WeKnora

```yaml
weknora:
  enabled: true
  base-url: http://localhost:8080/api/v1
  api-key: your_api_key
  knowledge-base-id: your_kb_id
```

## API Docs

After the backend starts:

```text
http://localhost:8088/doc.html
```

## FAQ

**Q: Which AI models are supported?**  
A: Any OpenAI-compatible API model is supported, including OpenAI GPT, DeepSeek, Ollama and others. Configure your model provider and API key in Backend -> System Settings -> API/AI.

**Q: Is data safe for commercial use?**  
A: The system can be fully self-hosted. Customer, task, and interaction data are all stored in your own infrastructure.

**Q: How to get help?**  
A: Use our [Community Forum](https://bbs.72crm.com) or create an issue in GitHub.

## Roadmap

- AI assistant workflow enhancement
- More AI task automation and prioritization
- Automated AI email drafting
- OAuth-based external integrations
- Enhanced data migration tools
- More modules and performance upgrades

## Welcome Contributions

- Report issues via [GitHub Issues](https://github.com/WuKongOpenSource/Wukong-AICRM/issues)
- Submit pull requests
- Improve docs and translations
- Share ideas in the community

## Useful Links

- Official Website: [https://www.72crm.com/](https://www.72crm.com/)
- Product Site: [https://www.72crm.ai/](https://www.72crm.ai/)
- Download: [https://www.wukongcrm.com/](https://www.wukongcrm.com/)
- Forum: [https://bbs.72crm.com/](https://bbs.72crm.com/)
- GitHub: [https://github.com/WuKongOpenSource/Wukong-AICRM](https://github.com/WuKongOpenSource/Wukong-AICRM)
- Gitee: [https://gitee.com/organizations/wukongcrm/projects](https://gitee.com/organizations/wukongcrm/projects)

## License

Wukong AICRM source code is open for learning, research, evaluation, and non-commercial use. Commercial deployment, hosted services, commercial derivative products, plugin/agent marketplace collaboration, and brand usage require separate commercial authorization.

Please read [LICENSE](LICENSE), [LICENSE.en.md](LICENSE.en.md), [NOTICE](NOTICE), and [TRADEMARKS.md](TRADEMARKS.md).

## Contact Us

If you have suggestions or need enterprise support, you can contact us via Issue or pull request.

<div align="center">
  <h2>Love this project?</h2>
  <p><strong>If Wukong AICRM helps you, please give us a ⭐️ Star.</strong></p>
  <p>
    <a href="https://github.com/WuKongOpenSource/Wukong-AICRM">GitHub</a> |
    <a href="https://www.72crm.ai/">Website</a> |
    <a href="https://bbs.72crm.com">Community</a>
  </p>
</div>
