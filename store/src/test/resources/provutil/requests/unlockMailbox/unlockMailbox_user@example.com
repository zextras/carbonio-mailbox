unlockMailbox user@example.com
<AuthRequest xmlns="urn:zimbraAdmin"><name>zimbra</name><password>password</password></AuthRequest>
<GetAccountRequest applyCos="1" xmlns="urn:zimbraAdmin"><account by="name">user@example.com</account></GetAccountRequest>
<LockoutMailboxRequest op="end" xmlns="urn:zimbraAdmin"><account by="name" name="test@test.com">test@test.com</account></LockoutMailboxRequest>