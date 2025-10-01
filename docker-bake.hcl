group "default" {
  targets = ["mailbox", "ldap"]
}

target "ldap" {
  context = "."
  dockerfile = "docker/standalone/openldap/Dockerfile"
  tags = ["carbonio-openldap:local"]
}

target "mailbox" {
  context = "."
  dockerfile = "docker/standalone/openldap/Dockerfile"
  tags = ["carbonio-mailbox:local"]
}