group "default" {
  targets = ["mailbox", "mariadb"]
  output = [{ type = "cacheonly" }]
}

target "mailbox" {
  context = "."
  dockerfile = "docker/mailbox/Dockerfile"
  platforms = ["linux/amd64", "linux/arm64"]
}

target "postfix" {
  context = "."
  dockerfile = "docker/postfix/Dockerfile"
  platforms = ["linux/amd64", "linux/arm64"]
}

target "mariadb" {
  context = "."
  dockerfile = "docker/mariadb/Dockerfile"
  platforms = ["linux/amd64", "linux/arm64"]
}