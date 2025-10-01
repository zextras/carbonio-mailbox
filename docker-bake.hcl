group "default" {
  targets = ["mailbox", "ldap", "mariadb", "postfix"]
}

target "ldap" {
  context = "."
  dockerfile = "docker/standalone/openldap/Dockerfile"
  tags = []
}

target "mailbox" {
  context = "."
  dockerfile = "docker/standalone/mailbox/Dockerfile"
  tags = []
}

target "postfix" {
  context = "."
  dockerfile = "docker/standalone/postfix/Dockerfile"
  tags = []
}

target "mariadb" {
  context = "."
  dockerfile = "docker/standalone/mariadb/Dockerfile"
  tags = []
}