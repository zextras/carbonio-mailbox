pushFreebusyDomain example.com
<AuthRequest xmlns="urn:zimbraAdmin"><name>zimbra</name><password>password</password></AuthRequest>
<GetDomainRequest applyConfig="1" xmlns="urn:zimbraAdmin"><domain by="name">example.com</domain></GetDomainRequest>
<GetAllServersRequest service="mailbox" applyConfig="1" xmlns="urn:zimbraAdmin"/>
<GetAllServersRequest service="service" applyConfig="1" xmlns="urn:zimbraAdmin"/>
<AuthRequest xmlns="urn:zimbraAdmin"><name>zimbra</name><password>password</password></AuthRequest>
<PushFreeBusyRequest xmlns="urn:zimbraAdmin"><domain name="example.com"/></PushFreeBusyRequest>
<AuthRequest xmlns="urn:zimbraAdmin"><name>zimbra</name><password>password</password></AuthRequest>
<PushFreeBusyRequest xmlns="urn:zimbraAdmin"><domain name="example.com"/></PushFreeBusyRequest>