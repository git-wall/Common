package policy

allow_admin {
    allow with input as {
        "user": {
            "roles": ["admin"],
            "authenticated": true
        },
        "request": {
            "path": "/api/restricted"
        }
    }
}

unauthenticated {
    not allow with input as {
        "user": {
            "roles": ["user"],
            "authenticated": false
        },
        "request": {
            "path": "/api/user/profile"
        }
    }
}
