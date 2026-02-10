"""
Vault Injector Admission Webhook

This webhook checks if a Vault secret exists for a DAG and conditionally injects
Vault Agent annotations into Airflow pods. It adds annotations only if the secret
path exists in Vault, preventing pods from hanging on missing secrets.

Key Features:
- Checks Vault secret existence before injection
- Handles empty secrets gracefully
- Adds Vault annotations to existing pod annotations (non-destructive)
- Fail-open policy to prevent blocking pod creation

Documentation Links:
- Kubernetes Admission Controllers: https://kubernetes.io/docs/reference/access-authn-authz/admission-controllers/
- Vault API: https://www.vaultproject.io/api-docs
- hvac Python Client: https://hvac.readthedocs.io/
"""

import base64
import json
import logging
import os
import ssl
from typing import Dict, Any, Optional, List, Tuple
from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field
import uvicorn
import hvac  # Vault Python client

# Initialize FastAPI application
app = FastAPI(
    title="Vault Injector Admission Webhook",
    description="""
    🔐 **Vault Injector Webhook for Airflow**
    
    This webhook intelligently injects Vault Agent annotations into Airflow pods
    based on secret availability in Vault.
    
    ## Features
    - ✅ **Pre-flight Secret Validation** - Checks if Vault secret exists before injection
    - ✅ **Empty Secret Handling** - Gracefully handles secrets with no data
    - ✅ **Non-destructive Annotation Addition** - Preserves existing annotations
    - ✅ **Fail-open Policy** - Never blocks pod creation
    
    ## How It Works
    1. Extracts DAG ID from Airflow pod metadata
    2. Checks if `{vault_kv_mount}/{vault_env}/{airflow_env}_{dag_id}` exists in Vault
    3. If secret exists (even if empty), injects Vault Agent annotations
    4. If secret doesn't exist, skips Vault injection (pod runs without Vault)
    """,
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc"
)

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Vault configuration from environment variables
VAULT_ADDR = os.getenv("VAULT_ADDR", "http://vault.vault.svc.cluster.local:8200")
VAULT_NAMESPACE = os.getenv("VAULT_NAMESPACE", "/")
VAULT_AUTH_PATH = os.getenv("VAULT_AUTH_PATH", "auth/k8_auth_dna_aws_dev")
VAULT_ROLE = os.getenv("VAULT_ROLE", "dna_aws_dev_k8_role")
VAULT_TLS_SKIP_VERIFY = os.getenv("VAULT_TLS_SKIP_VERIFY", "true")

# Vault secret path configuration
VAULT_KV_MOUNT = os.getenv("VAULT_KV_MOUNT", "airflow_kv")  # KV mount point
VAULT_ENV = os.getenv("VAULT_ENV", "staging")  # Vault environment (staging/production)
AIRFLOW_ENV = os.getenv("AIRFLOW_ENV", "DEV")  # Airflow environment prefix (DEV/PROD)
# TLS configuration
TLS_CERT_FILE = os.getenv("TLS_CERT_FILE", "/etc/webhook/certs/tls.crt")
TLS_KEY_FILE = os.getenv("TLS_KEY_FILE", "/etc/webhook/certs/tls.key")

# Pydantic models for request/response
class ObjectMeta(BaseModel):
    """Kubernetes object metadata"""
    name: Optional[str] = None
    namespace: Optional[str] = None
    labels: Optional[Dict[str, str]] = {}
    annotations: Optional[Dict[str, str]] = {}

class KubernetesObject(BaseModel):
    """Kubernetes object model"""
    apiVersion: str
    kind: str
    metadata: ObjectMeta
    spec: Optional[Dict[str, Any]] = None

class AdmissionRequest(BaseModel):
    """Kubernetes AdmissionRequest"""
    uid: str
    kind: Optional[Dict[str, str]] = None
    resource: Optional[Dict[str, str]] = None
    namespace: Optional[str] = None
    operation: Optional[str] = None
    object: KubernetesObject

class AdmissionReview(BaseModel):
    """Kubernetes AdmissionReview request"""
    apiVersion: str = Field(default="admission.k8s.io/v1")
    kind: str = Field(default="AdmissionReview")
    request: AdmissionRequest

class AdmissionResponse(BaseModel):
    """Kubernetes AdmissionResponse"""
    uid: str
    allowed: bool = True
    patchType: Optional[str] = None
    patch: Optional[str] = None

class AdmissionReviewResponse(BaseModel):
    """Kubernetes AdmissionReview response"""
    apiVersion: str = "admission.k8s.io/v1"
    kind: str = "AdmissionReview"
    response: AdmissionResponse


def get_vault_client() -> Optional[hvac.Client]:
    """
    Create and authenticate Vault client using Kubernetes auth.
    
    Returns:
        hvac.Client: Authenticated Vault client or None if authentication fails
        
    References:
        - hvac Documentation: https://hvac.readthedocs.io/
        - Vault Kubernetes Auth: https://www.vaultproject.io/docs/auth/kubernetes
    """
    try:
        # Initialize Vault client
        client = hvac.Client(
            url=VAULT_ADDR,
            namespace=VAULT_NAMESPACE if VAULT_NAMESPACE != "/" else None,
            verify=False
        )
        
        # Skip TLS verification if configured (for internal cluster communication)
        if VAULT_TLS_SKIP_VERIFY.lower() == "true":
            client.session.verify = False
        
        # Read Kubernetes service account token
        with open('/var/run/secrets/kubernetes.io/serviceaccount/token', 'r') as f:
            jwt = f.read()
        
        # Authenticate using Kubernetes auth method
        client.auth.kubernetes.login(
            role=VAULT_ROLE,
            jwt=jwt,
            mount_point=VAULT_AUTH_PATH.replace('auth/', '')
        )
        
        logger.info(f"Successfully authenticated to Vault at {VAULT_ADDR}")
        return client
        
    except FileNotFoundError:
        logger.warning("Kubernetes service account token not found - running outside cluster?")
        # Try to use VAULT_TOKEN environment variable as fallback
        vault_token = os.getenv("VAULT_TOKEN")
        if vault_token:
            client = hvac.Client(url=VAULT_ADDR, token=vault_token, namespace=VAULT_NAMESPACE if VAULT_NAMESPACE != "/" else None)
            if VAULT_TLS_SKIP_VERIFY.lower() == "true":
                client.session.verify = False
            logger.info("Using VAULT_TOKEN from environment")
            return client
        return None
    except Exception as e:
        logger.error(f"Failed to authenticate to Vault: {e}")
        return None

def check_vault_secret_exists(vault_client: hvac.Client, secret_path: str) -> Tuple[bool, bool, Dict]:
    """
    Check if a Vault secret exists and whether it has data.
    
    Args:
        vault_client: Authenticated Vault client
        secret_path: Path to secret (e.g., "{VAULT_KV_MOUNT}/{VAULT_ENV}/{AIRFLOW_ENV}_P005609_DAG_1")
        
    Returns:
        Tuple[bool, bool, dict]: (secret_exists, has_data, secret_data)
            - secret_exists: True if secret path exists
            - has_data: True if secret has key-value pairs
            - secret_data: The secret data dictionary (empty if no data)
            
    References:
        - Vault KV v2 API: https://www.vaultproject.io/api-docs/secret/kv/kv-v2
    """
    try:
        # Split path into mount and actual path
        # Example: "airflow_kv/staging/DEV_P005609_DAG_1" 
        # -> mount: "airflow_kv", path: "staging/DEV_P005609_DAG_1"
        # Path format: {VAULT_KV_MOUNT}/{VAULT_ENV}/{AIRFLOW_ENV}_{dag_id}
        path_parts = secret_path.split('/', 1)
        mount_point = path_parts[0]
        secret_key = path_parts[1] if len(path_parts) > 1 else ""
        
        logger.info(f"Checking Vault secret: mount={mount_point}, path={secret_key}")
        
        # Read secret using KV v2 API
        secret_response = vault_client.secrets.kv.v2.read_secret_version(
            mount_point=mount_point,
            path=secret_key
        )
        
        # Extract data from response
        secret_data = secret_response.get('data', {}).get('data', {})
        
        if secret_data:
            logger.info(f"✓ Vault secret exists and has {len(secret_data)} key(s): {secret_path}")
            return True, True, secret_data
        else:
            logger.info(f"✓ Vault secret exists but is EMPTY: {secret_path}")
            return True, False, {}
            
    except hvac.exceptions.InvalidPath:
        logger.info(f"✗ Vault secret does NOT exist: {secret_path}")
        return False, False, {}
    except Exception as e:
        logger.error(f"Error checking Vault secret {secret_path}: {e}")
        return False, False, {}

def extract_dag_id(pod: Dict[str, Any]) -> Optional[str]:
    """
    Extract the DAG ID from Airflow-generated pod metadata.
    
    This is the same logic as in the original webhook - reused for consistency.
    
    Args:
        pod: Kubernetes pod specification
        
    Returns:
        str: DAG ID if found, None otherwise
    """
    metadata = pod.get("metadata", {})
    annotations = metadata.get("annotations", {})
    labels = metadata.get("labels", {})
    
    # Try annotations first (more reliable)
    dag_id = annotations.get("dag_id")
    
    # Fallback to labels
    if not dag_id:
        dag_id = labels.get("dag_id")
    
    if dag_id:
        logger.info(f"Extracted DAG ID: {dag_id}")
    else:
        logger.warning("Could not extract DAG ID from pod metadata")
    
    return dag_id

def should_inject_vault(pod: Dict[str, Any]) -> Tuple[bool, str]:
    """
    Determine if this pod should have Vault annotations injected.
    
    Criteria:
    1. Must be an Airflow pod (tier=airflow, kubernetes_executor=True)
    2. Must have DAG ID in metadata
    3. Must NOT already have Vault injection enabled
    
    Args:
        pod: Kubernetes pod specification
        
    Returns:
        Tuple[bool, str]: (should_inject, reason)
    """
    metadata = pod.get("metadata", {})
    labels = metadata.get("labels", {})
    annotations = metadata.get("annotations", {})
    
    # Check if it's an Airflow pod
    if labels.get("tier") != "airflow":
        return False, "Not an Airflow pod (tier != airflow)"
    
    if labels.get("kubernetes_executor") != "True":
        return False, "Not a KubernetesExecutor pod"
    
    # Check if DAG ID exists
    if "dag_id" not in annotations and "dag_id" not in labels:
        return False, "No DAG ID found in pod metadata"
    
    # Check if Vault injection is already enabled
    if annotations.get("vault.hashicorp.com/agent-inject") == "true":
        return False, "Vault injection already enabled (annotation exists)"
    
    logger.info("Pod qualifies for Vault injection evaluation")
    return True, "Pod is eligible for Vault injection"

def generate_vault_injection_patches(pod: Dict[str, Any], dag_id: str) -> List[Dict[str, Any]]:
    """
    Generate JSONPatch operations to add Vault Agent annotations.
    
    This function adds Vault annotations to the pod WITHOUT overwriting existing annotations.
    
    Args:
        pod: Kubernetes pod specification
        dag_id: DAG ID extracted from pod metadata
        
    Returns:
        List of JSONPatch operations
        
    References:
        - JSONPatch RFC 6902: https://tools.ietf.org/html/rfc6902
        - Vault Annotations: https://www.vaultproject.io/docs/platform/k8s/injector/annotations
    """
    patches = []
    
    vault_template = (
        f"{{{{- with secret \"{VAULT_KV_MOUNT}/{VAULT_ENV}/{AIRFLOW_ENV}_{dag_id}\" -}}}}\n"
        f"{{{{- range $k, $v := .Data.data }}}}\n"
        f"{{{{ $k }}}}={{{{ $v }}}}\n"
        f"{{{{- end }}}}\n"
        f"{{{{- end }}}}"
    )
    # Vault annotations to add
    vault_annotations = {
        "vault.hashicorp.com/agent-inject": "true",
        "vault.hashicorp.com/agent-inject-secret-envvar": f"{VAULT_KV_MOUNT}/{VAULT_ENV}/{AIRFLOW_ENV}_{dag_id}",
        "vault.hashicorp.com/agent-inject-template-envvar": vault_template,
        "vault.hashicorp.com/agent-pre-populate-only": "true",
        "vault.hashicorp.com/auth-path": VAULT_AUTH_PATH,
        "vault.hashicorp.com/namespace": VAULT_NAMESPACE,
        "vault.hashicorp.com/role": VAULT_ROLE,
        "vault.hashicorp.com/tls-skip-verify": VAULT_TLS_SKIP_VERIFY
    }
    
    # Add each annotation as a separate patch operation
    for key, value in vault_annotations.items():
        # Escape forward slashes in annotation key for JSONPatch
        escaped_key = key.replace('/', '~1')
        
        patches.append({
            "op": "add",
            "path": f"/metadata/annotations/{escaped_key}",
            "value": value
        })
        
        logger.info(f"Adding Vault annotation: {key}")
    
    return patches

@app.post("/mutate", response_model=AdmissionReviewResponse)
async def mutate(admission_review: AdmissionReview):
    """
    Main mutating admission webhook endpoint.
    
    Flow:
    1. Extract pod from admission request
    2. Check if it's an Airflow pod eligible for Vault injection
    3. Extract DAG ID from pod metadata
    4. Check if Vault secret exists at {VAULT_KV_MOUNT}/{VAULT_ENV}/{AIRFLOW_ENV}_{dag_id}
    5. If secret exists (even empty), add Vault Agent annotations
    6. If secret doesn't exist, skip injection
    
    Args:
        admission_review: AdmissionReview object from Kubernetes
        
    Returns:
        AdmissionReviewResponse: Response with JSONPatch operations
    """
    try:
        # Extract pod and request UID
        pod = admission_review.request.object.dict()
        uid = admission_review.request.uid
        logger.info(f"pod raw data: {json.dumps(pod)}")
        
        pod_name = pod.get("metadata", {}).get("name", "<unknown>")
        pod_namespace = pod.get("metadata", {}).get("namespace", "<unknown>")
        
        logger.info(f"Received AdmissionReview for pod: {pod_namespace}/{pod_name}")
        
        patches = []
        
        # Step 1: Check if this pod should have Vault injection
        should_inject, inject_reason = should_inject_vault(pod)
        
        if not should_inject:
            logger.info(f"Skipping Vault injection for {pod_namespace}/{pod_name}: {inject_reason}")
            return AdmissionReviewResponse(
                response=AdmissionResponse(uid=uid, allowed=True)
            )
        
        # Step 2: Extract DAG ID
        dag_id = extract_dag_id(pod)
        
        if not dag_id:
            logger.warning(f"No DAG ID found for pod {pod_namespace}/{pod_name}, skipping Vault injection")
            return AdmissionReviewResponse(
                response=AdmissionResponse(uid=uid, allowed=True)
            )
        
        # Step 3: Check if Vault secret exists
        vault_client = get_vault_client()
        
        if not vault_client:
            logger.error("Failed to connect to Vault, allowing pod creation without Vault injection")
            return AdmissionReviewResponse(
                response=AdmissionResponse(uid=uid, allowed=True)
            )
        
        # Build Vault secret path using environment variables
        secret_path = f"{VAULT_KV_MOUNT}/{VAULT_ENV}/{AIRFLOW_ENV}_{dag_id}"
        secret_exists, has_data, secret_data = check_vault_secret_exists(vault_client, secret_path)
        
        # Step 4: Decide whether to inject Vault annotations
        if secret_exists:
            if has_data:
                logger.info(f"✓ Vault secret exists with data for DAG {dag_id}, injecting Vault annotations")
                logger.info(f"  Secret contains {len(secret_data)} keys: {list(secret_data.keys())}")
            else:
                logger.info(f"✓ Vault secret exists but is EMPTY for DAG {dag_id}, injecting Vault annotations anyway")
            
            # Generate patches to add Vault annotations
            patches = generate_vault_injection_patches(pod, dag_id)
            logger.info(f"Generated {len(patches)} annotation patches for pod {pod_namespace}/{pod_name}")
        else:
            logger.info(f"✗ Vault secret does NOT exist for DAG {dag_id}, skipping Vault injection")
            logger.info(f"  Expected secret path: {secret_path}")
            logger.info(f"  Pod will run WITHOUT Vault secrets")
        
        # Step 5: Return admission response
        if patches:
            return AdmissionReviewResponse(
                response=AdmissionResponse(
                    uid=uid,
                    allowed=True,
                    patchType="JSONPatch",
                    patch=base64.b64encode(json.dumps(patches).encode()).decode()
                )
            )
        else:
            return AdmissionReviewResponse(
                response=AdmissionResponse(uid=uid, allowed=True)
            )
        
    except Exception as e:
        logger.error(f"Error processing admission review: {e}", exc_info=True)
        
        # Fail-open: always allow pod creation even on webhook errors
        return AdmissionReviewResponse(
            response=AdmissionResponse(
                uid=admission_review.request.uid,
                allowed=True
            )
        )

@app.get("/health")
async def health():
    """
    Health check endpoint for Kubernetes probes.
    
    Returns:
        dict: Health status
    """
    return {"status": "healthy", "service": "vault-injector-webhook"}

@app.get("/ready")
async def ready():
    """
    Readiness check endpoint - validates Vault connectivity.
    
    Returns:
        dict: Readiness status with Vault connectivity check
    """
    try:
        vault_client = get_vault_client()
        if vault_client and vault_client.is_authenticated():
            return {
                "status": "ready",
                "vault_connected": True,
                "vault_addr": VAULT_ADDR
            }
        else:
            return {
                "status": "degraded",
                "vault_connected": False,
                "vault_addr": VAULT_ADDR,
                "message": "Vault authentication failed, but webhook will operate in fail-open mode"
            }
    except Exception as e:
        return {
            "status": "degraded",
            "vault_connected": False,
            "error": str(e),
            "message": "Webhook will operate in fail-open mode"
        }

if __name__ == "__main__":
    logger.info(f"Starting Vault Injector Webhook on port 8080")
    logger.info(f"Vault Address: {VAULT_ADDR}")
    logger.info(f"Vault Namespace: {VAULT_NAMESPACE}")
    logger.info(f"Vault Auth Path: {VAULT_AUTH_PATH}")
    logger.info(f"Vault Role: {VAULT_ROLE}")
    logger.info(f"Vault KV Mount: {VAULT_KV_MOUNT}")
    logger.info(f"Vault Environment: {VAULT_ENV}")
    logger.info(f"Airflow Environment: {AIRFLOW_ENV}")
    logger.info(f"Secret Path Format: {VAULT_KV_MOUNT}/{VAULT_ENV}/{AIRFLOW_ENV}_{{dag_id}}")
    
    uvicorn.run(
        app,
        host="0.0.0.0",
        port=8443,
        ssl_certfile=TLS_CERT_FILE,
        ssl_keyfile=TLS_KEY_FILE,
        log_level="info"
    )