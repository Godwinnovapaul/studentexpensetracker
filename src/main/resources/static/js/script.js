const API_BASE = "/api";
let trendChart = null;

// Initialize defaults on script load
document.addEventListener("DOMContentLoaded", () => {
  const expDateInput = document.getElementById("exp-date");
  if (expDateInput) {
    expDateInput.valueAsDate = new Date();
  }

  const badgeMonth = document.getElementById("current-month-badge");
  if (badgeMonth) {
    badgeMonth.innerText = new Date().toLocaleString("default", {
      month: "long",
      year: "numeric",
    });
  }

  loadData();
});

// Sidebar Navigation Switcher
function switchView(viewId, element) {
  document
    .querySelectorAll(".view-section")
    .forEach((view) => view.classList.remove("active"));
  document
    .querySelectorAll(".nav-item")
    .forEach((item) => item.classList.remove("active"));

  const targetView = document.getElementById(`view-${viewId}`);
  if (targetView) targetView.classList.add("active");
  if (element) element.classList.add("active");

  const titles = {
    overview: "Overview Dashboard",
    "add-expense": "Add Expense",
    budget: "Monthly Budgets",
    "category-spend": "Category Spend & Alerts",
    trends: "Spending Trends",
    history: "Expense History",
  };
  document.getElementById("view-title").innerText =
    titles[viewId] || "Dashboard";

  if (viewId === "trends" && trendChart) {
    setTimeout(() => trendChart.resize(), 100);
  }
}

async function loadData() {
  await Promise.all([loadSummaries(), loadTrends(), loadExpenses()]);
}

async function loadSummaries() {
  try {
    const res = await fetch(`${API_BASE}/summary`);
    const summaries = await res.json();

    const container = document.getElementById("category-summary-list");
    const ovContainer = document.getElementById("overview-progress-list");
    const alertsWrapper = document.getElementById("alerts-wrapper");

    container.innerHTML = "";
    ovContainer.innerHTML = "";
    alertsWrapper.innerHTML = "";

    let totalSpent = 0;
    let totalBudget = 0;
    let alertCount = 0;

    if (!summaries || summaries.length === 0) {
      const emptyMsg =
        '<p style="color:#64748b;font-size:0.9rem;">No category data recorded for this month.</p>';
      container.innerHTML = emptyMsg;
      ovContainer.innerHTML = emptyMsg;
    }

    summaries.forEach((item) => {
      totalSpent += item.spent;
      totalBudget += item.budget;

      let fillClass = "";
      if (item.percentage >= 100) {
        fillClass = "progress-danger";
      } else if (item.percentage >= 90) {
        fillClass = "progress-warning";
      }

      if (item.alert) {
        alertCount++;
        const alertDiv = document.createElement("div");
        alertDiv.className = "alert-card";
        alertDiv.innerText = `🚨 ${item.alertMessage}`;
        alertsWrapper.appendChild(alertDiv);
      }

      const progressHTML = `
                <div class="progress-header">
                    <span>${item.category}</span>
                    <span>₹${item.spent.toLocaleString("en-IN", { minimumFractionDigits: 2 })} ${item.budget > 0 ? `/ ₹${item.budget.toLocaleString("en-IN", { minimumFractionDigits: 2 })} (${item.percentage}%)` : "(No Budget)"}</span>
                </div>
                <div class="progress-bg">
                    <div class="progress-fill ${fillClass}" style="width: ${Math.min(item.percentage, 100)}%;"></div>
                </div>
            `;

      const div1 = document.createElement("div");
      div1.className = "progress-item";
      div1.innerHTML = progressHTML;
      container.appendChild(div1);

      const div2 = document.createElement("div");
      div2.className = "progress-item";
      div2.innerHTML = progressHTML;
      ovContainer.appendChild(div2);
    });

    document.getElementById("ov-total-spent").innerText =
      `₹${totalSpent.toLocaleString("en-IN", { minimumFractionDigits: 2 })}`;
    document.getElementById("ov-total-budget").innerText =
      `₹${totalBudget.toLocaleString("en-IN", { minimumFractionDigits: 2 })}`;
    document.getElementById("ov-alert-count").innerText = alertCount;
  } catch (err) {
    console.error("Error loading summaries:", err);
  }
}

async function loadTrends() {
  try {
    const res = await fetch(`${API_BASE}/trends`);
    const trends = await res.json();

    const labels = trends.map((t) => t.month);
    const data = trends.map((t) => t.total);

    const ctx = document.getElementById("trendChart").getContext("2d");
    if (trendChart) trendChart.destroy();

    trendChart = new Chart(ctx, {
      type: "bar",
      data: {
        labels: labels,
        datasets: [
          {
            label: "Monthly Spend (₹)",
            data: data,
            backgroundColor: "#4f46e5",
            borderRadius: 6,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: { y: { beginAtZero: true } },
      },
    });
  } catch (err) {
    console.error("Error loading trends:", err);
  }
}

async function loadExpenses() {
  try {
    const res = await fetch(`${API_BASE}/expenses`);
    const expenses = await res.json();
    const tbody = document.getElementById("expense-table-body");
    tbody.innerHTML = "";

    if (!expenses || expenses.length === 0) {
      tbody.innerHTML =
        '<tr><td colspan="5" style="text-align:center;color:#94a3b8;padding:1.5rem;">No expenses recorded yet.</td></tr>';
      return;
    }

    expenses.sort((a, b) => new Date(b.date) - new Date(a.date));
    expenses.forEach((e) => {
      const row = document.createElement("tr");
      row.innerHTML = `
                <td>${e.date}</td>
                <td style="font-weight:600;">${e.title}</td>
                <td><span class="category-tag">${e.category}</span></td>
                <td><strong>₹${Number(e.amount).toLocaleString("en-IN", { minimumFractionDigits: 2 })}</strong></td>
                <td style="text-align: center;"><button class="btn-del" onclick="deleteExpenseItem(${e.id})">Delete</button></td>
            `;
      tbody.appendChild(row);
    });
  } catch (err) {
    console.error("Error loading expenses:", err);
  }
}

// Form Handlers
document
  .getElementById("expense-form")
  .addEventListener("submit", async (e) => {
    e.preventDefault();
    const payload = {
      title: document.getElementById("exp-title").value,
      amount: parseFloat(document.getElementById("exp-amount").value),
      category: document.getElementById("exp-category").value,
      date: document.getElementById("exp-date").value,
    };

    await fetch(`${API_BASE}/expenses`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });

    document.getElementById("exp-title").value = "";
    document.getElementById("exp-amount").value = "";
    alert("Expense saved successfully!");
    loadData();
  });

document.getElementById("budget-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const payload = {
    category: document.getElementById("bgt-category").value,
    amount: parseFloat(document.getElementById("bgt-amount").value),
  };

  await fetch(`${API_BASE}/budgets`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });

  document.getElementById("bgt-amount").value = "";
  alert("Budget limit updated successfully!");
  loadData();
});

async function deleteExpenseItem(id) {
  if (confirm("Are you sure you want to delete this expense?")) {
    await fetch(`${API_BASE}/expenses/${id}`, { method: "DELETE" });
    loadData();
  }
}
