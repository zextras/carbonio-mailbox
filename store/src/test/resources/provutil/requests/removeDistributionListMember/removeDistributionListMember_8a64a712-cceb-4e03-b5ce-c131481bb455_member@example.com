removeDistributionListMember 8a64a712-cceb-4e03-b5ce-c131481bb455 member@example.com
<AuthRequest xmlns="urn:zimbraAdmin"><name>zimbra</name><password>password</password></AuthRequest>
<GetDistributionListRequest xmlns="urn:zimbraAdmin"><dl by="id">8a64a712-cceb-4e03-b5ce-c131481bb455</dl></GetDistributionListRequest>
<RemoveDistributionListMemberRequest xmlns="urn:zimbraAdmin"><dlm>member@example.com</dlm></RemoveDistributionListMemberRequest>
<GetDistributionListRequest xmlns="urn:zimbraAdmin"><dl by="id"/></GetDistributionListRequest>