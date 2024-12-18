addDistributionListMember 8a64a712-cceb-4e03-b5ce-c131481bb455 member@example.com member@example.com member@example.com member@example.com
<AuthRequest xmlns="urn:zimbraAdmin"><name>zimbra</name><password>password</password></AuthRequest>
<GetDistributionListRequest xmlns="urn:zimbraAdmin"><dl by="id">8a64a712-cceb-4e03-b5ce-c131481bb455</dl></GetDistributionListRequest>
<AddDistributionListMemberRequest xmlns="urn:zimbraAdmin"><dlm>member@example.com</dlm><dlm>member@example.com</dlm><dlm>member@example.com</dlm><dlm>member@example.com</dlm></AddDistributionListMemberRequest>
<GetDistributionListRequest xmlns="urn:zimbraAdmin"><dl by="id"/></GetDistributionListRequest>