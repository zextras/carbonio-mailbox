#!/bin/bash

MAX_RETRIES=7
RETRY_INTERVAL=5

function mailboxIsHealthy() {
    local result
    result=$(curl -s -k -X POST https://localhost:7071/service/admin/soap -d '
    {
        "Body": {
            "CheckHealthRequest": {
                "_jsns": "urn:zimbraAdmin"
            }
        }
    }' | jq -r ".Body.CheckHealthResponse.healthy")
    echo "$result"
}

function waitUntilMailboxIsHealthy() {
    local retries=0
    local isHealthy="false"

    while [ "$isHealthy" != "true" ] && [ "$retries" -lt "$MAX_RETRIES" ]; do
        isHealthy=$(mailboxIsHealthy)
        if [ "$isHealthy" != "true" ]; then
            echo "Mailbox is not healthy (Attempt $((retries + 1)) of $MAX_RETRIES)"
            sleep "$RETRY_INTERVAL"
        fi
        retries=$((retries + 1))
    done

    if [ "$isHealthy" == "true" ]; then
        echo "Mailbox is healthy"
    else
        echo "Mailbox is still not healthy after $MAX_RETRIES retries"
        echo "[NOTE] Proceeding without ensuring mailbox readiness..."
    fi
}

waitUntilMailboxIsHealthy
