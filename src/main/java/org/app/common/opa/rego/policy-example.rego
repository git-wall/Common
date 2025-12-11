package example

# Default deny
default allow = false

# Allow if user has required role
allow {
    # Check if user has admin role
    input.user.roles[_] == "admin"
}

# Allow specific paths for authenticated users
allow {
    # User is authenticated
    input.user.authenticated
    
    # Request is to an allowed path
    allowed_paths := ["/api/public", "/api/user"]
    startswith(input.request.path, allowed_paths[_])
}