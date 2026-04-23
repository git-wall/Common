package abac

# Default deny
default allow = false

# Allow if user is the owner of the resource
allow {
    input.user.id == input.resource.owner
}

# Allow if user is in the same department and resource is internal
allow {
    input.user.department == input.resource.department
    input.resource.visibility == "internal"
}

# Allow if resource is public
allow {
    input.resource.visibility == "public"
}

# Allow admins to access everything
allow {
    input.user.roles[_] == "admin"
}