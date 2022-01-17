services {
  check {
    tcp      = "localhost:8080"
    timeout  = "1s"
    interval = "5s"
  }
  connect {
    sidecar_service {
      port = 10000
      proxy {
        upstreams = [
          {
            destination_name   = "carbonio-storages"
            local_bind_port    = 20000
            local_bind_address = "127.78.0.7"
          }
        ]
      }
    }
  }
  name = "carbonio-mailbox"
  port = 8080
}