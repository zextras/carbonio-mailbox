createDomain example.com
<AuthRequest xmlns="urn:zimbraAdmin"><name>zimbra</name><password>password</password></AuthRequest>
<CreateDomainRequest name="example.com" xmlns="urn:zimbraAdmin"/>