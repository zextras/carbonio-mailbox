group "build" {
  targets = ["builder"]
}

group "images" {
  targets = ["mailbox", "ldap", "mariadb", "postfix"]
  output = [{ type = "cacheonly" }]
}

variable "MAVEN_OPTS" {
  default = ""
}

target "builder" {
  context = "."
  dockerfile = "docker/standalone/builder/Dockerfile"
  tags = ["mailbox-build:local"]
  args = {
      MAVEN_OPTS = MAVEN_OPTS
    }
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