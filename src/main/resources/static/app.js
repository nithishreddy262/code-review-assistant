// Simple frontend that posts to your Spring Boot backend
const API_URL = "http://localhost:8080/api/review";

const codeEl = document.getElementById("code");
const langEl = document.getElementById("language");
const filenameEl = document.getElementById("filename");
const reviewBtn = document.getElementById("reviewBtn");
const exportBtn = document.getElementById("exportBtn");
const clearBtn = document.getElementById("clearBtn");
const outputEl = document.getElementById("output");
const issuesEl = document.getElementById("issues");
const metaEl = document.getElementById("meta");
const statusEl = document.getElementById("status");

let lastReport = null;

reviewBtn.addEventListener("click", runReview);
exportBtn.addEventListener("click", exportReport);
clearBtn.addEventListener("click", () => {
    codeEl.value = "";
    outputEl.classList.add("hidden");
    statusEl.textContent = "";
    lastReport = null;
    exportBtn.disabled = true;
});

async function runReview() {
    const code = codeEl.value.trim();
    const language = langEl.value;
    const filename = filenameEl.value.trim() || "file.txt";

    if (!code) {
        alert("Please paste some code to review.");
        return;
    }

    statusEl.textContent = "Reviewing...";
    reviewBtn.disabled = true;
    exportBtn.disabled = true;
    outputEl.classList.add("hidden");
    issuesEl.innerHTML = "";
    metaEl.textContent = "";

    try {
        const resp = await fetch(API_URL, {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({ language, code, filename })
        });

        if (!resp.ok) {
            const err = await resp.json().catch(()=>({error: resp.statusText}));
            statusEl.textContent = "Error: " + (err?.error || resp.statusText);
            return;
        }

        const report = await resp.json();
        lastReport = report;
        renderReport(report);
        exportBtn.disabled = false;
        statusEl.textContent = "Done.";
    } catch (e) {
        statusEl.textContent = "Network error: " + e.message;
    } finally {
        reviewBtn.disabled = false;
    }
}

function renderReport(report) {
    outputEl.classList.remove("hidden");
    const lines = report.meta?.lines ?? "—";
    const score = report.meta?.complexityScore ?? report.meta?.complexity ?? "—";
    const issues = report.issues || [];

    metaEl.textContent = `Lines: ${lines} · ComplexityScore: ${score} · Issues: ${issues.length}`;

    issuesEl.innerHTML = "";
    if (issues.length === 0) {
        const li = document.createElement("li");
        li.textContent = "No issues found.";
        issuesEl.appendChild(li);
        return;
    }

    issues.forEach((it, idx) => {
        const li = document.createElement("li");

        const header = document.createElement("div");
        const sev = document.createElement("span");
        sev.className = "severity";
        sev.textContent = (it.severity || "info").toUpperCase();

        const msg = document.createElement("span");
        msg.textContent = " " + (it.message || "(no message)");

        header.appendChild(sev);
        header.appendChild(msg);
        li.appendChild(header);

        const meta = document.createElement("div");
        meta.className = "meta";
        meta.textContent = `${it.ruleId ? "rule: " + it.ruleId : ""} ${it.line ? "line: " + it.line : ""} ${it.column ? "col: " + it.column : ""}`;
        li.appendChild(meta);

        if (it.suggestion) {
            const s = document.createElement("div");
            s.className = "suggestion";
            s.textContent = "Suggestion: " + it.suggestion;
            li.appendChild(s);
        }

        issuesEl.appendChild(li);
    });
}

function exportReport() {
    if (!lastReport) return;
    const blob = new Blob([JSON.stringify(lastReport, null, 2)], { type: "application/json" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    const name = (lastReport.filename ? lastReport.filename + ".review.json" : "review.json");
    a.download = name;
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
}
