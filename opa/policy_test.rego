package example

test_allow_admin {
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

test_allow_authenticated_public_path {
    allow with input as {
        "user": {
            "roles": ["user"],
            "authenticated": true
        },
        "request": {
            "path": "/api/public/resource"
        }
    }
}

test_deny_unauthenticated {
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

test_deny_wrong_role {
    not allow with input as {
        "user": {
            "roles": ["user"],
            "authenticated": true
        },
        "request": {
            "path": "/api/admin/settings"
        }
    }
}