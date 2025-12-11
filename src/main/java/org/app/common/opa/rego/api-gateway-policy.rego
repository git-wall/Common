package api.gateway

# Default deny
default allow = false

# Allow if request has valid API key
allow {
    # Check if API key header exists
    input.request.headers["x-api-key"]
    
    # Validate API key
    valid_api_keys[input.request.headers["x-api-key"]]
}

# Allow if user is authenticated and has access to the path
allow {
    # User is authenticated
    input.user.authenticated
    
    # Check path permissions
    path_parts := split(trim_prefix(input.request.path, "/"), "/")
    resource_type := path_parts[0]
    
    # User has permission for this resource type
    user_permissions[input.user.roles[_]][resource_type][_] == input.request.method
}

# Valid API keys
valid_api_keys = {
    "api-key-1": true,
    "api-key-2": true
}

# User role permissions
user_permissions = {
    "admin": {
        "documents": ["GET", "POST", "PUT", "DELETE"],
        "users": ["GET", "POST", "PUT", "DELETE"]
    },
    "editor": {
        "documents": ["GET", "POST", "PUT"],
        "users": ["GET"]
    },
    "viewer": {
        "documents": ["GET"],
        "users": ["GET"]
    }
}