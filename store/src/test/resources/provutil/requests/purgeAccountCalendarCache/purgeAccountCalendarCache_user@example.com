purgeAccountCalendarCache user@example.com
<AuthRequest xmlns="urn:zimbraAdmin"><name>zimbra</name><password>password</password></AuthRequest>
<GetAccountRequest applyCos="1" xmlns="urn:zimbraAdmin"><account by="name">user@example.com</account></GetAccountRequest>
<PurgeAccountCalendarCacheRequest id="186c1c23-d2ad-46b4-9efd-ddd890b1a4a2" xmlns="urn:zimbraAdmin"/>