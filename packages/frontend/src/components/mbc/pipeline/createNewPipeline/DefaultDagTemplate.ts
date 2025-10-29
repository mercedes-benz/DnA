export function getDefaultDagTemplate(pid: string, name: string) {
return `from airflow import DAG
import os
from dotenv import load_dotenv

# ==========================================
# IMPORTANT: Variable Access Instructions
# ==========================================
#
# Traditional Airflow Variable access is NOT available for new variables. 
# ❌ DON'T USE: Variable.get("your_variable_name")
#
# Instead, use the vault-based approach:
# ✅ USE THIS METHOD:
#   1. Load environment variables from vault
#   2. Access variables using os.getenv()
#
# Example:
# load_dotenv("/vault/secrets/envvar")
# your_variable = os.getenv("your_variable_name")
#
# Note: # Existing DAGs are unaffected, though migrating to the vault-based approach is recommended.
#
# ==========================================

# Load vault environment variables (REQUIRED for accessing variables)
load_dotenv("/vault/secrets/envvar") # Do not remove this line. Required for vault environment variables.

dag = DAG(
    dag_id='${pid}_${name}'
)`;
}
