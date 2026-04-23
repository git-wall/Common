package rbac

# Default deny
default allow = false

# Allow if user has required role for the operation
allow {
    # Get required roles for operation
    required_roles := role_permissions[input.request.method][input.resource.type]
    
    # Check if user has one of the required roles
    user_roles := {role | role := input.user.roles[_]}
    required_role := required_roles[_]
    user_roles[required_role]
}

# Role permissions mapping
role_permissions = {
    "GET": {
        "document": ["admin", "editor", "viewer"],
        "user": ["admin"]
    },
    "POST": {
        "document": ["admin", "editor"],
        "user": ["admin"]
    },
    "PUT": {
        "document": ["admin", "editor"],
        "user": ["admin"]
    },
    "DELETE": {
        "document": ["admin"],
        "user": ["admin"]
    }
}