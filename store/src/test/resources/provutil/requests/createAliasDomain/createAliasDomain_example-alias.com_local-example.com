createAliasDomain example-alias.com local-example.com
<AuthRequest xmlns="urn:zimbraAdmin"><name>zimbra</name><password>password</password></AuthRequest>
<GetDomainRequest applyConfig="1" xmlns="urn:zimbraAdmin"><domain by="name">local-example.com</domain></GetDomainRequest>
<CreateDomainRequest name="example-alias.com" xmlns="urn:zimbraAdmin"><a n="zimbraDomainAliasTargetId"/><a n="zimbraDomainType">alias</a></CreateDomainRequest>