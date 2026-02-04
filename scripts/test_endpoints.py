#!/usr/bin/env python3
import base64
import json
import time
import urllib.error
import urllib.parse
import urllib.request

BASE_URL = "http://localhost:8080"
USERNAME = "fenix"
PASSWORD = "fenix123"


def request(method, path, body=None, params=None):
    url = BASE_URL + path
    if params:
        query = urllib.parse.urlencode(params, doseq=True)
        url = f"{url}?{query}"
    data = None
    headers = {
        "Authorization": "Basic " + base64.b64encode(f"{USERNAME}:{PASSWORD}".encode()).decode(),
        "Accept": "application/json",
    }
    if body is not None:
        data = json.dumps(body).encode("utf-8")
        headers["Content-Type"] = "application/json"
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req) as resp:
            raw = resp.read()
            status = resp.status
    except urllib.error.HTTPError as e:
        raw = e.read()
        status = e.code
    text = raw.decode("utf-8") if raw else ""
    parsed = None
    if text:
        try:
            parsed = json.loads(text)
        except json.JSONDecodeError:
            parsed = text
    return status, parsed, url


def wait_for_ready(timeout_sec=60):
    start = time.time()
    while time.time() - start < timeout_sec:
        status, _, _ = request("GET", "/v3/api-docs")
        if status == 200:
            return True
        time.sleep(1)
    return False


def log_call(report, name, method, path, body=None, params=None):
    status, response, url = request(method, path, body=body, params=params)
    entry = {
        "name": name,
        "method": method,
        "url": url,
        "request": body,
        "status": status,
        "response": response,
    }
    report.append(entry)
    return status, response


def main():
    if not wait_for_ready():
        raise SystemExit("App did not become ready within timeout.")

    report = []

    # Organizations
    status, org = log_call(
        report,
        "Create organization",
        "POST",
        "/organizations",
        body={"name": f"E2E Org {int(time.time())}"},
    )
    org_id = org.get("id") if isinstance(org, dict) else None

    log_call(report, "List organizations", "GET", "/organizations", params={"page": 0, "size": 5})
    log_call(report, "Get organization", "GET", f"/organizations/{org_id}")
    log_call(
        report,
        "Update organization",
        "PUT",
        f"/organizations/{org_id}",
        body={"name": "E2E Org Updated", "status": "ACTIVE"},
    )
    log_call(
        report,
        "Patch organization",
        "PATCH",
        f"/organizations/{org_id}",
        body={"status": "INACTIVE"},
    )

    # Websites
    status, website = log_call(
        report,
        "Create website",
        "POST",
        f"/organizations/{org_id}/websites",
        body={"code": "E2E-STORE", "name": "E2E Store", "platform": "SHOPIFY"},
    )
    website_id = website.get("id") if isinstance(website, dict) else None

    log_call(
        report,
        "List websites",
        "GET",
        f"/organizations/{org_id}/websites",
        params={"page": 0, "size": 5},
    )
    log_call(
        report,
        "Search websites",
        "GET",
        f"/organizations/{org_id}/websites/search",
        params={"code": "E2E-STORE"},
    )
    log_call(
        report,
        "Get website",
        "GET",
        f"/organizations/{org_id}/websites/{website_id}",
    )
    log_call(
        report,
        "Update website",
        "PUT",
        f"/organizations/{org_id}/websites/{website_id}",
        body={"code": "E2E-STORE", "name": "E2E Store Updated", "platform": "SHOPIFY", "status": "ACTIVE"},
    )
    log_call(
        report,
        "Patch website",
        "PATCH",
        f"/organizations/{org_id}/websites/{website_id}",
        body={"status": "INACTIVE"},
    )

    # Orders
    status, order = log_call(
        report,
        "Create order",
        "POST",
        "/orders",
        body={
            "orgId": org_id,
            "websiteId": website_id,
            "externalOrderId": "E2E-ORDER-1",
            "orderTotal": 12.34,
            "currency": "USD",
        },
    )
    order_id = order.get("id") if isinstance(order, dict) else None

    log_call(
        report,
        "List orders",
        "GET",
        "/orders",
        params={"orgId": org_id, "websiteId": website_id, "page": 0, "size": 5},
    )
    log_call(
        report,
        "Search orders",
        "GET",
        "/orders/search",
        params={"orgId": org_id, "websiteId": website_id, "externalOrderId": "E2E-ORDER-1"},
    )
    log_call(report, "Get order", "GET", f"/orders/{order_id}")
    log_call(
        report,
        "Update order",
        "PUT",
        f"/orders/{order_id}",
        body={
            "orgId": org_id,
            "websiteId": website_id,
            "externalOrderId": "E2E-ORDER-1",
            "orderTotal": 20.00,
            "currency": "USD",
        },
    )
    log_call(
        report,
        "Patch order",
        "PATCH",
        f"/orders/{order_id}",
        body={"status": "CLOSED", "financialStatus": "PAID"},
    )

    # Fulfillments
    status, fulfillment = log_call(
        report,
        "Create fulfillment",
        "POST",
        f"/orders/{order_id}/fulfillments",
        body={"externalFulfillmentId": "E2E-FUL-1", "status": "CREATED"},
    )
    fulfillment_id = fulfillment.get("id") if isinstance(fulfillment, dict) else None

    log_call(
        report,
        "List fulfillments",
        "GET",
        f"/orders/{order_id}/fulfillments",
        params={"page": 0, "size": 5},
    )
    log_call(
        report,
        "Search fulfillments",
        "GET",
        f"/orders/{order_id}/fulfillments/search",
        params={"externalFulfillmentId": "E2E-FUL-1"},
    )
    log_call(
        report,
        "Get fulfillment",
        "GET",
        f"/orders/{order_id}/fulfillments/{fulfillment_id}",
    )
    log_call(
        report,
        "Update fulfillment",
        "PUT",
        f"/orders/{order_id}/fulfillments/{fulfillment_id}",
        body={"externalFulfillmentId": "E2E-FUL-1", "status": "SHIPPED"},
    )
    log_call(
        report,
        "Patch fulfillment",
        "PATCH",
        f"/orders/{order_id}/fulfillments/{fulfillment_id}",
        body={"status": "DELIVERED"},
    )

    # Tracking
    status, tracking = log_call(
        report,
        "Create tracking",
        "POST",
        f"/fulfillments/{fulfillment_id}/tracking",
        body={"trackingNumber": "1Z999", "status": "IN_TRANSIT", "isPrimary": True},
    )
    tracking_id = tracking.get("id") if isinstance(tracking, dict) else None

    log_call(
        report,
        "List tracking",
        "GET",
        f"/fulfillments/{fulfillment_id}/tracking",
        params={"page": 0, "size": 5},
    )
    log_call(
        report,
        "Search tracking",
        "GET",
        f"/fulfillments/{fulfillment_id}/tracking/search",
        params={"trackingNumber": "1Z999"},
    )
    log_call(
        report,
        "Get tracking",
        "GET",
        f"/fulfillments/{fulfillment_id}/tracking/{tracking_id}",
    )
    log_call(
        report,
        "Update tracking",
        "PUT",
        f"/fulfillments/{fulfillment_id}/tracking/{tracking_id}",
        body={"trackingNumber": "1Z999", "status": "DELIVERED", "isPrimary": True},
    )
    log_call(
        report,
        "Patch tracking",
        "PATCH",
        f"/fulfillments/{fulfillment_id}/tracking/{tracking_id}",
        body={"status": "DELIVERED"},
    )

    # Deletes (reverse order)
    log_call(
        report,
        "Delete tracking",
        "DELETE",
        f"/fulfillments/{fulfillment_id}/tracking/{tracking_id}",
    )
    log_call(
        report,
        "Delete fulfillment",
        "DELETE",
        f"/orders/{order_id}/fulfillments/{fulfillment_id}",
    )
    log_call(report, "Delete order", "DELETE", f"/orders/{order_id}")
    log_call(
        report,
        "Delete website",
        "DELETE",
        f"/organizations/{org_id}/websites/{website_id}",
    )
    log_call(report, "Delete organization", "DELETE", f"/organizations/{org_id}")

    with open("reports/endpoint-test-report.json", "w", encoding="utf-8") as handle:
        json.dump(report, handle, indent=2)

    # Print a short summary
    failures = [entry for entry in report if entry["status"] >= 400]
    print(f"Executed {len(report)} calls. Failures: {len(failures)}")
    if failures:
        for entry in failures:
            print(f"- {entry['name']} -> {entry['status']} {entry['url']}")


if __name__ == "__main__":
    main()
