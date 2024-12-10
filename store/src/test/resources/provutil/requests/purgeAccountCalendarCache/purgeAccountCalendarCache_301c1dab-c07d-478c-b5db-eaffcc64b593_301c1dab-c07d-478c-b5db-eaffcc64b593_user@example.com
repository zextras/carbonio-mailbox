purgeAccountCalendarCache 301c1dab-c07d-478c-b5db-eaffcc64b593 301c1dab-c07d-478c-b5db-eaffcc64b593 user@example.com
<AuthRequest xmlns="urn:zimbraAdmin"><name>zimbra</name><password>password</password></AuthRequest>
<GetAccountRequest applyCos="1" xmlns="urn:zimbraAdmin"><account by="id">301c1dab-c07d-478c-b5db-eaffcc64b593</account></GetAccountRequest>
<PurgeAccountCalendarCacheRequest id="186c1c23-d2ad-46b4-9efd-ddd890b1a4a2" xmlns="urn:zimbraAdmin"/>
<GetAccountRequest applyCos="1" xmlns="urn:zimbraAdmin"><account by="id">301c1dab-c07d-478c-b5db-eaffcc64b593</account></GetAccountRequest>
<PurgeAccountCalendarCacheRequest id="186c1c23-d2ad-46b4-9efd-ddd890b1a4a2" xmlns="urn:zimbraAdmin"/>
<GetAccountRequest applyCos="1" xmlns="urn:zimbraAdmin"><account by="name">user@example.com</account></GetAccountRequest>
<PurgeAccountCalendarCacheRequest id="186c1c23-d2ad-46b4-9efd-ddd890b1a4a2" xmlns="urn:zimbraAdmin"/>