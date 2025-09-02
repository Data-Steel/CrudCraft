#!/bin/bash
# Script to exercise CRUD endpoints for sample API using JWT authentication
BASE_URL=${BASE_URL:-http://localhost:8080}
USER=${USER:-alice}
PASS=${PASS:-password}

login() {
  local u=$1 p=$2
  curl -s -H "Content-Type: application/json" -d "{\"username\":\"$u\",\"password\":\"$p\"}" "$BASE_URL/auth/login" | jq -r '.token'
}

TOKEN=$(login "$USER" "$PASS")

# collect ids for request bodies
TENANT_ID=$(curl -s -H "Authorization: Bearer $TOKEN" "$BASE_URL/tenants" | jq -r '.[0].id')
BRANCH_ID=$(curl -s -H "Authorization: Bearer $TOKEN" "$BASE_URL/branches" | jq -r '.[0].id')
ACCOUNT_ID=$(curl -s -H "Authorization: Bearer $TOKEN" "$BASE_URL/accounts" | jq -r '.[0].id')
CUSTOMER_ID=$(curl -s -H "Authorization: Bearer $TOKEN" "$BASE_URL/customers" | jq -r '.[0].id')

declare -A bodies
bodies[users]='{"tenantId":"'$TENANT_ID'","branchId":"'$BRANCH_ID'","username":"user'$(date +%s)'","passwordHash":"password","roles":["ADMIN"]}'
bodies[transactions]='{"tenantId":"'$TENANT_ID'","accountId":"'$ACCOUNT_ID'","timestamp":"2024-01-01T00:00:00Z","direction":"DEBIT","amount":50.00,"currency":"EUR","clientReference":"Ref-1","counterpartyIban":"NL00EXAMPLE00000000","counterpartyName":"Example Co","status":"BOOKED"}'

# helper using main token
auth_curl() {
  curl -s -o /dev/null -w "%{http_code}\n" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" "$@"
}

readonly_eps=(accounts account-limits account-holders audit-logs branches cards card-pins customers kyc-profiles tenants)
no_delete_eps=(transactions)

for ep in "${!bodies[@]}"; do :; done # ensure array declared

for ep in accounts account-limits account-holders audit-logs branches cards card-pins customers kyc-profiles tenants users transactions; do
  echo "Testing $ep"
  echo -n "GET list: "; auth_curl "$BASE_URL/$ep"
  id=$(curl -s -H "Authorization: Bearer $TOKEN" "$BASE_URL/$ep" | jq -r '.[0].id')
  if [[ -n $id ]]; then
    echo -n "GET by id: "; auth_curl "$BASE_URL/$ep/$id"
  fi

  if [[ " ${readonly_eps[*]} " != *" $ep "* ]]; then
    body=${bodies[$ep]}
    echo -n "POST create: "
    create_resp=$(curl -s -w "\n%{http_code}" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "$body" "$BASE_URL/$ep")
    create_code=$(echo "$create_resp" | tail -n1)
    create_body=$(echo "$create_resp" | head -n -1)
    echo "$create_code"
    new_id=$(echo "$create_body" | jq -r '.id // empty')

    echo -n "PUT update: "; auth_curl -X PUT -d "$body" "$BASE_URL/$ep/$new_id"
    echo -n "PATCH patch: "; auth_curl -X PATCH -d "$body" "$BASE_URL/$ep/$new_id"

    echo -n "POST bulk create: "
    bulk_resp=$(curl -s -w "\n%{http_code}" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "[$body]" "$BASE_URL/$ep/batch")
    bulk_code=$(echo "$bulk_resp" | tail -n1)
    bulk_body=$(echo "$bulk_resp" | head -n -1)
    echo "$bulk_code"
    bulk_id=$(echo "$bulk_body" | jq -r '.[0].id // empty')
    body_with_id=$(jq -c --arg id "$bulk_id" '. + {id:$id}' <<<"$body")
    identified_body=$(jq -c --arg id "$bulk_id" '{id:$id,data:.}' <<<"$body")

    echo -n "PUT bulk update: "; auth_curl -X PUT -d "[$identified_body]" "$BASE_URL/$ep/batch"
    echo -n "PATCH bulk patch: "; auth_curl -X PATCH -d "[$identified_body]" "$BASE_URL/$ep/batch"
    echo -n "POST bulk upsert: "; auth_curl -d "[$body_with_id]" "$BASE_URL/$ep/batch"
    echo -n "POST bulk ids: "; auth_curl -d "[\"$bulk_id\"]" "$BASE_URL/$ep/batch/ids"

    if [[ " ${no_delete_eps[*]} " != *" $ep "* ]]; then
      echo -n "DELETE bulk delete: "; auth_curl -X DELETE -d "[\"$bulk_id\"]" "$BASE_URL/$ep/batch"
      echo -n "DELETE: "; auth_curl -X DELETE "$BASE_URL/$ep/$new_id"
    fi
  fi
  echo
done

# Row security test: attempt cross-tenant access
TOKEN_CAROL=$(login carol password)
blue_account=$(curl -s -H "Authorization: Bearer $TOKEN_CAROL" "$BASE_URL/accounts" | jq -r '.[0].id')
code=$(curl -s -o /dev/null -w "%{http_code}\n" -H "Authorization: Bearer $TOKEN" "$BASE_URL/accounts/$blue_account")
echo "Row security cross-tenant account access (expect 403): $code"

# Field security tests
user_id=$(curl -s -H "Authorization: Bearer $TOKEN" "$BASE_URL/users" | jq -r '.[0].id')
pass_field=$(curl -s -H "Authorization: Bearer $TOKEN" "$BASE_URL/users/$user_id" | jq -r '.passwordHash')
echo "passwordHash visible to admin: $pass_field (expected null)"

txn_id=$(curl -s -H "Authorization: Bearer $TOKEN" "$BASE_URL/transactions" | jq -r '.[0].id')
admin_iban=$(curl -s -H "Authorization: Bearer $TOKEN" "$BASE_URL/transactions/$txn_id" | jq -r '.counterpartyIban')
TOKEN_FRANK=$(login frank password)
customer_iban=$(curl -s -H "Authorization: Bearer $TOKEN_FRANK" "$BASE_URL/transactions/$txn_id" | jq -r '.counterpartyIban')
echo "counterpartyIban for admin: $admin_iban"
echo "counterpartyIban for customer: $customer_iban (expected null)"
