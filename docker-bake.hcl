group "default" {
  targets = ["mailbox", "ldap", "mariadb", "postfix"]
}

target "builder" {
  context = "."
  dockerfile = "docker/standalone/builder/Dockerfile"
  tags = []
  output = ["type=local,dest=./staging"]
}

target "ldap" {
  context = "."
  dockerfile = "docker/standalone/openldap/Dockerfile"
  tags = []
  depends_on = "builder"
}

target "mailbox" {
  context = "."
  dockerfile = "docker/standalone/mailbox/Dockerfile"
  tags = []
  depends_on = "builder"
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