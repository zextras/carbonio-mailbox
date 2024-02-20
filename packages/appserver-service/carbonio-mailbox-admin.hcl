services {
  check {
    http     = "https://localhost:7071/service/health/ready"
    tls_skip_verify = true
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
