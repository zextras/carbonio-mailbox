group "default" {
  targets = ["mailbox", "ldap", "mariadb", "postfix"]
  output = [{ type = "cacheonly" }]
}

target "ldap" {
  context = "."
  dockerfile = "docker/standalone/openldap/Dockerfile"
}

target "mailbox" {
  context = "."
  dockerfile = "docker/standalone/mailbox/Dockerfile"
}

target "postfix" {
  context = "."
  dockerfile = "docker/standalone/postfix/Dockerfile"
}

target "mariadb" {
  context = "."
  dockerfile = "docker/standalone/mariadb/Dockerfile"
}