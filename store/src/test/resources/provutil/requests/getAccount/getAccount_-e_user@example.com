getAccount -e user@example.com
<AuthRequest xmlns="urn:zimbraAdmin"><name>zimbra</name><password>password</password></AuthRequest>
<GetAccountRequest applyCos="0" xmlns="urn:zimbraAdmin"><account by="name">user@example.com</account></GetAccountRequest>