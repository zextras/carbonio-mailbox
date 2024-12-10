getCalendarResource user@example.com
<AuthRequest xmlns="urn:zimbraAdmin"><name>zimbra</name><password>password</password></AuthRequest>
<GetCalendarResourceRequest xmlns="urn:zimbraAdmin"><calresource by="name">user@example.com</calresource></GetCalendarResourceRequest>