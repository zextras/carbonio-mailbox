deleteDistributionList list@example.com
<AuthRequest xmlns="urn:zimbraAdmin"><name>zimbra</name><password>password</password></AuthRequest>
<GetDistributionListRequest xmlns="urn:zimbraAdmin"><dl by="name">list@example.com</dl></GetDistributionListRequest>
<DeleteDistributionListRequest cascadeDelete="false" xmlns="urn:zimbraAdmin"/>