services {
  check {
    http      = "http://127.78.0.7:10000/service/health/ready"
    timeout  = "1s"
    interval = "5s"
  }
  connect {
    sidecar_service {
      proxy {
        local_service_address = "127.78.0.7"
        upstreams = [
          {
            destination_name   = "carbonio-memcached"
            local_bind_port    = 20006
            local_bind_address = "127.78.0.7"
          },
        ]
      }
    }
  }
  name = "carbonio-mailbox-internal-api"
  port = 10000
}
