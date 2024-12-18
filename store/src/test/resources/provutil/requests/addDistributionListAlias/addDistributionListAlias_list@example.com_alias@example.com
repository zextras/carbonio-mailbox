addDistributionListAlias list@example.com alias@example.com
<AuthRequest xmlns="urn:zimbraAdmin"><name>zimbra</name><password>password</password></AuthRequest>
<GetDistributionListRequest xmlns="urn:zimbraAdmin"><dl by="name">list@example.com</dl></GetDistributionListRequest>
<AddDistributionListAliasRequest alias="alias@example.com" xmlns="urn:zimbraAdmin"/>
<GetDistributionListRequest xmlns="urn:zimbraAdmin"><dl by="id"/></GetDistributionListRequest>