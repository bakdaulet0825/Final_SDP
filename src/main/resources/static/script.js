const componentCategories = [
    "cpu",
    "gpu",
    "motherboard",
    "ram",
    "storage"
];

let componentCatalog = {};
let latestRequest = 0;

async function checkBackend() {
    const result = document.getElementById("result");

    try {
        const response = await fetch("/api/status");

        if (!response.ok) {
            throw new Error("Server error");
        }

        const data = await response.json();

        result.textContent = data.message;
        result.style.color = "#22c55e";

    } catch (error) {
        result.textContent = "Backend connection failed!";
        result.style.color = "#ef4444";
    }
}

async function loadComponents() {
    try {
        const response = await fetch("/api/components");

        if (!response.ok) {
            throw new Error("Failed to load components");
        }

        componentCatalog = await response.json();

        componentCategories.forEach(category => {
            const select = document.getElementById(category);

            if (!select) return;

            select.innerHTML =
                '<option value="">Select component</option>';

            (componentCatalog[category] || []).forEach(component => {
                const option = document.createElement("option");

                option.value = component.id;
                option.textContent =
                    `${component.name} - $${component.price.toFixed(2)}`;

                select.appendChild(option);
            });

            select.addEventListener("change", updateConfiguration);
        });

        document.getElementById("pricingStrategy")
            .addEventListener("change", updateConfiguration);

        document.getElementById("assembly")
            .addEventListener("change", updateConfiguration);

        document.getElementById("extendedWarranty")
            .addEventListener("change", updateConfiguration);

        updateConfiguration();

    } catch (error) {
        const result = document.getElementById("result");
        result.textContent = "Failed to load components!";
        result.style.color = "#ef4444";
        console.error(error);
    }
}

function showNotifications(messages) {
    const list = document.getElementById("notifications");
    list.innerHTML = "";

    if (!messages || messages.length === 0) {
        const item = document.createElement("li");
        item.textContent = "No notifications.";
        list.appendChild(item);
        return;
    }

    messages.forEach(message => {
        const item = document.createElement("li");
        item.textContent = message;
        item.style.marginBottom = "8px";
        list.appendChild(item);
    });
}

async function updateConfiguration() {
    const requestId = ++latestRequest;
    const selection = {};

    componentCategories.forEach(category => {
        selection[category] = document.getElementById(category).value;

        const component = componentCatalog[category]?.find(
            item => item.id === selection[category]
        );

        document.getElementById(category + "-price").textContent =
            "$" + (component?.price ?? 0).toFixed(2);
    });

    selection.pricingStrategy =
        document.getElementById("pricingStrategy").value;

    selection.assembly =
        document.getElementById("assembly").checked;

    selection.extendedWarranty =
        document.getElementById("extendedWarranty").checked;

    const result = document.getElementById("result");
    const total = document.getElementById("total");
    const monthlyRow = document.getElementById("monthly-row");
    const servicesElement = document.getElementById("services-cost");

    if (componentCategories.some(category => !selection[category])) {
        total.textContent = "$0.00";
        servicesElement.textContent = "$0.00";
        monthlyRow.style.display = "none";

        document.getElementById("strategy-name").textContent = "—";

        result.textContent = "Please select all components.";
        result.style.color = "#facc15";

        showNotifications([
            "Please select all components to create your build."
        ]);
        return;
    }

    try {
        const response = await fetch("/api/build", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(selection)
        });

        if (!response.ok) {
            throw new Error("Build calculation failed");
        }

        const data = await response.json();

        if (requestId !== latestRequest) return;

        total.textContent = "$" + data.total.toFixed(2);

        servicesElement.textContent =
            "$" + data.servicesCost.toFixed(2);

        document.getElementById("strategy-name").textContent =
            data.strategyName;

        if (data.monthlyPayment > 0) {
            monthlyRow.style.display = "flex";

            document.getElementById("monthly-payment").textContent =
                "$" + data.monthlyPayment.toFixed(2);
        } else {
            monthlyRow.style.display = "none";
        }

        result.textContent = data.message;
        result.style.color = data.compatible ? "#22c55e" : "#ef4444";

        showNotifications(data.notifications);

    } catch (error) {
        if (requestId !== latestRequest) return;

        total.textContent = "$0.00";
        servicesElement.textContent = "$0.00";
        monthlyRow.style.display = "none";

        result.textContent = "Backend calculation failed!";
        result.style.color = "#ef4444";

        showNotifications(["Unable to update configuration."]);
        console.error(error);
    }
}

function calculatePrice() {
    updateConfiguration();
}

document.addEventListener("DOMContentLoaded", loadComponents);
