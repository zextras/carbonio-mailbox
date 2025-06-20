services {
  check {
    http     = "https://localhost:7072/service/extension/nginx-lookup"
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
  name = "carbonio-mailbox-nslookup"
  port = 7072
}
