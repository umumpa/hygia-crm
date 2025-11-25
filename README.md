# Hygia CRM

A customer relationship management system built to track customers, visits, and invoices.

## 🚀 Tech Stack
- **Backend:** Spring Boot (Java 17)
- **Database:** H2 (dev), PostgreSQL (prod)
- **API Docs:** Swagger UI (`http://localhost:8090/swagger-ui/index.html`)

## 🧩 Modules
- Customers: Manage customer records, tiers, and follow-up logic.
- Regions: Group customers by geographic region.
- Visits: Log customer visits with next follow-up reminders.

## 🛠️ Run Locally
```bash
cd backend
./mvnw spring-boot:run
