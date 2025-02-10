getGrants -g key grantee.name@example.com
<AuthRequest xmlns="urn:zimbraAdmin"><name>zimbra</name><password>password</password></AuthRequest>
<GetGrantsRequest xmlns="urn:zimbraAdmin"><grantee all="1" by="name" type="key">grantee.name@example.com</grantee></GetGrantsRequest>