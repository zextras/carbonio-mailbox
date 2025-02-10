getAllDistributionLists -v test.com
<AuthRequest xmlns="urn:zimbraAdmin"><name>zimbra</name><password>password</password></AuthRequest>
<GetDomainRequest applyConfig="1" xmlns="urn:zimbraAdmin"><domain by="name">test.com</domain></GetDomainRequest>
<GetAllDistributionListsRequest xmlns="urn:zimbraAdmin"><domain by="id"/></GetAllDistributionListsRequest>