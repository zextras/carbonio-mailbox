removeAccountAlias 8a64a712-cceb-4e03-b5ce-c131481bb455 alias@example.com
<AuthRequest xmlns="urn:zimbraAdmin"><name>zimbra</name><password>password</password></AuthRequest>
<GetAccountRequest applyCos="1" xmlns="urn:zimbraAdmin"><account by="id">8a64a712-cceb-4e03-b5ce-c131481bb455</account></GetAccountRequest>
<RemoveAccountAliasRequest alias="alias@example.com" id="186c1c23-d2ad-46b4-9efd-ddd890b1a4a2" xmlns="urn:zimbraAdmin"/>
<GetAccountRequest xmlns="urn:zimbraAdmin"><account by="id">186c1c23-d2ad-46b4-9efd-ddd890b1a4a2</account></GetAccountRequest>