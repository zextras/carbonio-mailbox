addDistributionListAlias 8a64a712-cceb-4e03-b5ce-c131481bb455 alias@example.com
<AuthRequest xmlns="urn:zimbraAdmin"><name>zimbra</name><password>password</password></AuthRequest>
<GetDistributionListRequest xmlns="urn:zimbraAdmin"><dl by="id">8a64a712-cceb-4e03-b5ce-c131481bb455</dl></GetDistributionListRequest>
<AddDistributionListAliasRequest alias="alias@example.com" xmlns="urn:zimbraAdmin"/>
<GetDistributionListRequest xmlns="urn:zimbraAdmin"><dl by="id"/></GetDistributionListRequest>