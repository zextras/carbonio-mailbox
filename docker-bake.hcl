group "default" {
  targets = ["mailbox", "ldap", "mariadb", "postfix"]
}

variable "MAVEN_OPTS" {
  default = ""
}
variable "BUILD_OPTS" {
  default = ""
}


target "builder" {
  context = "."
  dockerfile = "docker/standalone/builder/Dockerfile"
  tags = []
  args = {
      MAVEN_OPTS = MAVEN_OPTS
      BUILD_OPTS = BUILD_OPTS
    }
  output = ["type=local,dest=./build"]
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