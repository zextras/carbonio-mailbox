addDistributionListMember list@example.com member@example.com member@example.com member@example.com
<AuthRequest xmlns="urn:zimbraAdmin"><name>zimbra</name><password>password</password></AuthRequest>
<GetDistributionListRequest xmlns="urn:zimbraAdmin"><dl by="name">list@example.com</dl></GetDistributionListRequest>
<AddDistributionListMemberRequest xmlns="urn:zimbraAdmin"><dlm>member@example.com</dlm><dlm>member@example.com</dlm><dlm>member@example.com</dlm></AddDistributionListMemberRequest>
<GetDistributionListRequest xmlns="urn:zimbraAdmin"><dl by="id"/></GetDistributionListRequest>