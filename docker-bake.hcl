group "default" {
  targets = ["mailbox", "mariadb"]
  output = [{ type = "cacheonly" }]
}

target "mailbox" {
  context = "."
  dockerfile = "docker/mailbox/Dockerfile"
}

target "postfix" {
  context = "."
  dockerfile = "docker/postfix/Dockerfile"
}

target "mariadb" {
  context = "."
  dockerfile = "docker/mariadb/Dockerfile"
}