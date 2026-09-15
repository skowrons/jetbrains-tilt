# Tiltfile syntax highlighting showcase
# Open this file in GoLand to preview the Tiltfile color scheme.
# NOTE: Local resources run only when you trigger them manually.

# Constants, numbers, booleans, and the empty value.
APP_NAME = "highlighting-demo"
BASE_PORT = 8000
MAX_RETRIES = 3
TIMEOUT_SECONDS = 2.5
FILE_MODE = 0o755
COLOR_MASK = 0xFF
FEATURE_FLAGS = 0b1010
ENABLE_KUBERNETES = False
optional_value = None

# Single quotes, double quotes, escape sequences, and raw strings.
greeting = 'Hello, Tilt!'
escaped_message = "Status:\tready\n\"Quotes\" and \\backslashes"
source_pattern = r"src/.*\.(go|py)$"
description = """A small development environment.
This multiline string demonstrates highlighting across several lines.
Select a resource in the Tilt UI to print its status.
"""

# Lists, dictionaries, indexing, arithmetic, and comprehensions.
services = [
    {"name": "api", "port": BASE_PORT, "enabled": True},
    {"name": "worker", "port": BASE_PORT + 1, "enabled": False},
]
enabled_names = [service["name"] for service in services if service["enabled"]]
ports = {service["name"]: service["port"] for service in services}
retry_delays = [(attempt + 1) * 2 for attempt in range(MAX_RETRIES)]


def status_label(enabled, retries=MAX_RETRIES):
    """Return a readable status for the selected service."""
    if enabled and retries > 0:
        return "ready"
    elif not enabled:
        return "disabled"
    else:
        return "waiting"


def configure_local_services():
    """Register a manual command for each enabled service."""
    for service in services:
        if not service["enabled"]:
            continue

        name = "{}-{}".format(APP_NAME, service["name"])
        message = "{}: {} on port {}".format(
            name,
            status_label(service["enabled"]),
            service["port"],
        )

        local_resource(
            name=name,
            cmd=["printf", "%s\n", message],
            labels=["demo", "local"],
            trigger_mode=TRIGGER_MODE_MANUAL,
            auto_init=False,
        )

k8s

def configure_kubernetes_example():
    """Show Docker, live update, Kubernetes, and port forwarding calls."""
    # TODO: Add a Dockerfile, src/, and k8s/ manifests before enabling this.
    docker_build(
        ref=APP_NAME,
        context=".",
        dockerfile="Dockerfile",
        ignore=[".git", "**/__pycache__"],
        live_update=[
            sync("./src", "/app/src"),
        ],
    )

    k8s_yaml(["k8s/deployment.yaml", "k8s/service.yaml"])
    k8s_resource(
        workload=APP_NAME,
        port_forwards=[port_forward(BASE_PORT, 8080)],
        labels=["demo", "backend"],
    )


# Function calls, keyword arguments, built-ins, and conditional execution.
configure_local_services()

if ENABLE_KUBERNETES:
    configure_kubernetes_example()

print("{}: {} active service(s)".format(greeting, len(enabled_names)))
print("Enabled services: {}".format(", ".join(sorted(enabled_names))))
