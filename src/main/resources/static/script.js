
const componentCategories = [
    "cpu", "gpu", "motherboard", "ram", "storage"
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

        ["pricingStrategy", "assembly", "extendedWarranty"].forEach(id => {
            document.getElementById(id)
                .addEventListener("change", updateConfiguration);
        });

        await refreshSavedBuilds();
        updateConfiguration();

    } catch (error) {
        console.error(error);
        const result = document.getElementById("result");
        result.textContent = "Failed to load components!";
        result.style.color = "#ef4444";
    }
}

function getSelection() {
    const selection = {};

    componentCategories.forEach(category => {
        selection[category] = document.getElementById(category).value;
    });

    selection.pricingStrategy =
        document.getElementById("pricingStrategy").value;

    selection.assembly =
        document.getElementById("assembly").checked;

    selection.extendedWarranty =
        document.getElementById("extendedWarranty").checked;

    return selection;
}

function isComplete(selection) {
    return componentCategories.every(category => selection[category]);
}

function showNotifications(messages) {
    const list = document.getElementById("notifications");
    list.innerHTML = "";

    (messages || []).forEach(message => {
        const item = document.createElement("li");
        item.textContent = message;
        item.style.marginBottom = "8px";
        list.appendChild(item);
    });
}

async function updateConfiguration() {
    const requestId = ++latestRequest;
    const selection = getSelection();

    componentCategories.forEach(category => {
        const component = componentCatalog[category]?.find(
            item => item.id === selection[category]
        );

        document.getElementById(category + "-price").textContent =
            "$" + (component?.price ?? 0).toFixed(2);
    });

    const result = document.getElementById("result");
    const total = document.getElementById("total");
    const monthlyRow = document.getElementById("monthly-row");
    const servicesElement = document.getElementById("services-cost");

    if (!isComplete(selection)) {
        total.textContent = "$0.00";
        servicesElement.textContent = "$0.00";
        monthlyRow.style.display = "none";

        document.getElementById("strategy-name").textContent = "—";

        result.textContent = "Please select all components.";
        result.style.color = "#facc15";

        showNotifications(["Please select all components."]);
        return;
    }

    try {
        const response = await fetch("/api/build", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
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

function storageStatus(message, success = true) {
    const element = document.getElementById("storageMessage");
    element.textContent = message;
    element.style.color = success ? "#22c55e" : "#ef4444";
}

function buildPayload() {
    const selection = getSelection();
    const name = document.getElementById("buildName").value.trim();

    if (!name) {
        throw new Error("Please enter a build name.");
    }

    if (!isComplete(selection)) {
        throw new Error("Please select all components first.");
    }

    return {
        name,
        ...selection
    };
}

async function refreshSavedBuilds(selectedId = "") {
    const response = await fetch("/api/saved-builds");

    if (!response.ok) {
        throw new Error("Failed to retrieve saved builds.");
    }

    const builds = await response.json();
    const select = document.getElementById("savedBuilds");

    select.innerHTML =
        '<option value="">Select saved build</option>';

    builds.forEach(build => {
        const option = document.createElement("option");
        option.value = build.id;
        option.textContent = build.name;
        select.appendChild(option);
    });

    select.value = selectedId;
    return builds;
}

async function saveBuild() {
    try {
        const payload = buildPayload();

        const response = await fetch("/api/saved-builds", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            throw new Error("Failed to save build.");
        }

        const saved = await response.json();
        await refreshSavedBuilds(saved.id);

        storageStatus("Build saved successfully!");
    } catch (error) {
        storageStatus(error.message, false);
    }
}

async function loadSavedBuild() {
    const id = document.getElementById("savedBuilds").value;

    if (!id) {
        storageStatus("Please select a saved build.", false);
        return;
    }

    try {
        const builds = await refreshSavedBuilds(id);
        const build = builds.find(item => item.id === id);

        if (!build) {
            throw new Error("Saved build not found.");
        }

        document.getElementById("buildName").value = build.name;

        componentCategories.forEach(category => {
            document.getElementById(category).value = build[category];
        });

        document.getElementById("pricingStrategy").value =
            build.pricingStrategy;

        document.getElementById("assembly").checked = build.assembly;
        document.getElementById("extendedWarranty").checked =
            build.extendedWarranty;

        await updateConfiguration();
        storageStatus("Build loaded successfully!");
    } catch (error) {
        storageStatus(error.message, false);
    }
}

async function updateSavedBuild() {
    const id = document.getElementById("savedBuilds").value;

    if (!id) {
        storageStatus("Please select a saved build to update.", false);
        return;
    }

    try {
        const payload = buildPayload();

        const response = await fetch(
            `/api/saved-builds/${encodeURIComponent(id)}`,
            {
                method: "PUT",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(payload)
            }
        );

        if (!response.ok) {
            throw new Error("Failed to update build.");
        }

        await refreshSavedBuilds(id);
        storageStatus("Build updated successfully!");
    } catch (error) {
        storageStatus(error.message, false);
    }
}

async function deleteSavedBuild() {
    const id = document.getElementById("savedBuilds").value;

    if (!id) {
        storageStatus("Please select a saved build to delete.", false);
        return;
    }

    if (!confirm("Delete this saved build?")) return;

    try {
        const response = await fetch(
            `/api/saved-builds/${encodeURIComponent(id)}`,
            {method: "DELETE"}
        );

        if (!response.ok) {
            throw new Error("Failed to delete build.");
        }

        await refreshSavedBuilds();
        storageStatus("Build deleted successfully!");
    } catch (error) {
        storageStatus(error.message, false);
    }
}

function calculatePrice() {
    updateConfiguration();
}

document.addEventListener("DOMContentLoaded", loadComponents);

async function exportReport() {
    const selection = getSelection();

    if (!isComplete(selection)) {
        storageStatus("Please select all components first.", false);
        return;
    }

    const buildName = document.getElementById("buildName").value.trim();

    try {
        const response = await fetch("/api/export-report", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                name: buildName || "My Custom PC",
                ...selection
            })
        });

        if (!response.ok) {
            throw new Error("Failed to generate report.");
        }

        const blob = await response.blob();
        const url = URL.createObjectURL(blob);

        const link = document.createElement("a");
        link.href = url;
        link.download = "build-report.txt";

        document.body.appendChild(link);
        link.click();
        link.remove();

        URL.revokeObjectURL(url);

        storageStatus("Report exported successfully!");
    } catch (error) {
        storageStatus(error.message, false);
    }
}
