services {
  check {
    http      = "http://127.78.0.7:10001/service/health/ready"
    timeout  = "1s"
    interval = "5s"
  }
  connect {
    sidecar_service {
      proxy {
        local_service_address = "127.78.0.7"
      }
    }
  }
  name = "carbonio-authz"
  port = 10001
}
