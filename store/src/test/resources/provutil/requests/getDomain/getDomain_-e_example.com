getDomain -e example.com
<AuthRequest xmlns="urn:zimbraAdmin"><name>zimbra</name><password>password</password></AuthRequest>
<GetDomainRequest applyConfig="0" xmlns="urn:zimbraAdmin"><domain by="name">example.com</domain></GetDomainRequest>