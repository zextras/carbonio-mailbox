syncGal example.com
<AuthRequest xmlns="urn:zimbraAdmin"><name>zimbra</name><password>password</password></AuthRequest>
<GetDomainRequest applyConfig="1" xmlns="urn:zimbraAdmin"><domain by="name">example.com</domain></GetDomainRequest>
<SearchGalRequest domain="test.com" type="all" token="" xmlns="urn:zimbraAdmin"><name/></SearchGalRequest>