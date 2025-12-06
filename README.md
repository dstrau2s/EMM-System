# EMM System - Enterprise Mobility Management

## 📊 Project Structure

Complete enterprise system for managing mobile devices with Java client and SQL database.

## 🚀 Quick Start

### 1. Database Setup
Run the SQL scripts in `database/` folder in order:
- `01_create_database.sql`
- `02_insert_test_data.sql` 
- `03_create_views.sql`
- `04_create_procedures.sql`
- `05_create_trigger.sql`

### 2. Java Client
```bash
cd emm-java-client
mvn clean compile
mvn exec:java -Dexec.mainClass="de.emm.demo.EMMDatabaseManager"

EMM-System/
├── database/           # Complete SQL database schema
├── emm-java-client/    # Java console application
└── README.md           # This file

🛠️ Technologies

- Database: SQL Server
- Backend: Java 11+
- Build Tool: Maven
- Database Scripts: 5 complete SQL files

📊 Features
- Device management (smartphones, laptops, tablets)
- Employee assignment tracking
- Mobile contract management
- Compliance checking
- Audit logging
- Stored procedures for business logic

🔧 Requirements
- SQL Server (Express edition works)
- Java JDK 11 or higher
- Maven 3.6+

🤝 Contributing
- Fork the repository
- Create a feature branch
- Commit your changes
- Push to the branch
- Open a Pull Request

📜 License
- MIT License - see LICENSE file