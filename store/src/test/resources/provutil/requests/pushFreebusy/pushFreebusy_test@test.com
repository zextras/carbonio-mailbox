pushFreebusy test@test.com
<AuthRequest xmlns="urn:zimbraAdmin"><name>zimbra</name><password>password</password></AuthRequest>
<GetAccountRequest applyCos="1" xmlns="urn:zimbraAdmin"><account by="id">test@test.com</account></GetAccountRequest>
<AuthRequest xmlns="urn:zimbraAdmin"><name>zimbra</name><password>password</password></AuthRequest>
<PushFreeBusyRequest xmlns="urn:zimbraAdmin"><account id="test@test.com"/></PushFreeBusyRequest>