services {
  check {
    http      = "http://localhost:8080/service/health/ready"
    timeout  = "1s"
    interval = "5s"
  }
  connect {
    sidecar_service {
      proxy {
        upstreams = [
        ]
      }
    }
  }
  name = "carbonio-mailbox-admin"
  port = 7071
}
