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
          {
            destination_name   = "carbonio-preview"
            local_bind_port    = 20001
            local_bind_address = "127.78.0.7"
          },
          {
            destination_name   = "carbonio-files"
            local_bind_port    = 20002
            local_bind_address = "127.78.0.7"
          },
          {
            destination_name   = "carbonio-mta"
            local_bind_port    = 20025
            local_bind_address = "127.78.0.7"
          },
          {
            destination_name   = "carbonio-mailbox-db"
            local_bind_port    = 20003
            local_bind_address = "127.78.0.7"
          },
          {
            destination_name   = "carbonio-clamav"
            local_bind_port    = 20004
            local_bind_address = "127.78.0.7"
          },
          {
            destination_name   = "carbonio-message-broker"
            local_bind_port    = 20005
            local_bind_address = "127.78.0.7"
          },
        ]
      }
    }
  }
  name = "carbonio-mailbox"
  port = 8080
}
