# Code Review Assistant

A simple **web-based code review assistant** built with **Spring Boot (Java)** for the backend and a lightweight **HTML/CSS/JavaScript frontend**.
The app allows users to paste code, choose a language, and generate automated review feedback such as complexity, best practices, and improvement suggestions.

---

##  Features

*  Backend: Java Spring Boot REST API
*  Frontend: Static HTML, CSS, and Vanilla JS
*  JSON-based communication (`POST /api/review`)
*  Basic rule engine for code feedback
*  Clean UI with dynamic result rendering
*  Export review results as `.json` report
*  CORS-friendly for local testing

---

## Tech Stack

| Component             | Technology                      |
| --------------------- | ------------------------------- |
| **Backend**           | Spring Boot 3 (Java 17)         |
| **Frontend**          | HTML5, CSS3, Vanilla JavaScript |
| **Build Tool**        | Maven                           |
| **IDE (Recommended)** | IntelliJ IDEA                   |
| **Version Control**   | Git + GitHub                    |

---

##  Project Structure

```
code-review-assistant/
│
├── src/
│   ├── main/
│   │   ├── java/com/codereview/backend/
│   │   │   ├── controller/ReviewController.java
│   │   │   ├── model/ReviewRequest.java
│   │   │   ├── model/ReviewResponse.java
│   │   │   └── BackendApplication.java
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── index.html
│   │       │   ├── styles.css
│   │       │   └── app.js
│   │       └── application.properties
│   └── test/
│
├── pom.xml
├── .gitignore
└── README.md
```

---

##  Backend Setup (Spring Boot)

### 1 Prerequisites

* **Java 17+**
* **IntelliJ IDEA** (Community or Ultimate)
* **Maven** (comes with IntelliJ or install separately)
* **Git**

### 2️ Run the backend

1. Open the project in **IntelliJ IDEA**
2. Make sure dependencies are installed (`Maven → Reload Project`)
3. Run the main class:

   ```
   src/main/java/com/codereview/backend/BackendApplication.java
   ```
4. The backend will start at:
    `http://localhost:8080`

---

##  Frontend Setup (HTML + JS)

### Option 1: Served by Spring Boot

The easiest way — files are already in `src/main/resources/static/`.
When the backend runs, open in your browser:
 **[http://localhost:8080/](http://localhost:8080/)**

### Option 2: Run locally as standalone

1. Navigate to the `frontend` folder.
2. Open `index.html` in your browser.
3. Ensure the backend is running at `http://localhost:8080` (update `API_URL` in `app.js` if needed).

---

##  API Endpoints

### `POST /api/review`

**Request Body (JSON):**

```json
{
  "language": "java",
  "code": "public class Hello { void hi() { System.out.println(\"Hi\"); } }",
  "filename": "Hello.java"
}
```

**Response Example:**

```json
{
  "meta": {
    "lines": 3,
    "complexityScore": 2
  },
  "issues": [
    {
      "ruleId": "CONSOLE_USAGE",
      "message": "Avoid using System.out.println in production code.",
      "severity": "warning",
      "line": 2,
      "suggestion": "Use a logger instead."
    }
  ]
}
```

---

##  How It Works

1. The user pastes code into the web UI.
2. The frontend sends a JSON payload to the Spring Boot API.
3. The backend performs static checks (line count, simple rules, suggestions).
4. The response is displayed dynamically on the webpage.
5. Users can export the report as a `.json` file.

---

##  Example Use

1. Open `http://localhost:8080`
2. Paste this code:

   ```java
   public class Example {
       public void greet() {
           System.out.println("Hello");
       }
   }
   ```
3. Click **Run Review**
4. The report will show a warning about `System.out.println` and suggest using a logger.

---

##  CORS Notes

If you open the frontend separately (not served by Spring Boot), enable CORS temporarily in your backend:

```java
@CrossOrigin(origins = "*")
```

on your controller class or method.

---

##  .gitignore (recommended)

```
target/
.idea/
*.iml
*.log
*.class
build/
out/
.DS_Store
```

---

##  Contributing

Pull requests are welcome!
To contribute:

1. Fork this repo
2. Create a new branch (`feature/improvement-name`)
3. Commit changes and push
4. Open a Pull Request

---

##  License

This project is open-source and available under the **MIT License**.

---

##  Author

**Nithish Kumar Reddy Chintakunta**
Graduate Student — Software Engineering
GitHub: [@nithishreddy262](https://github.com/nithishreddy262)
