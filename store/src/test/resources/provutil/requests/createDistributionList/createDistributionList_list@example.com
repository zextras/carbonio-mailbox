createDistributionList list@example.com
<AuthRequest xmlns="urn:zimbraAdmin"><name>zimbra</name><password>password</password></AuthRequest>
<CreateDistributionListRequest name="list@example.com" dynamic="0" xmlns="urn:zimbraAdmin"/>