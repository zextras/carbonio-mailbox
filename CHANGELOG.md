

# [4.23.0](https://github.com/zextras/carbonio-mailbox/compare/4.22.0...4.23.0) (2025-08-25)


### Bug Fixes

* [CO-2293] use user timezone when recovering password ([#786](https://github.com/zextras/carbonio-mailbox/issues/786)) ([e76fca8](https://github.com/zextras/carbonio-mailbox/commit/e76fca86891147bd66229d0088bfe19d2b215e7e))
* incorrect French grammar in email subject when user responds ([d2c1f5e](https://github.com/zextras/carbonio-mailbox/commit/d2c1f5e4488c8f94fd9347933a7e81aede77ee65))
* redis ephemeral store number of connections ([#755](https://github.com/zextras/carbonio-mailbox/issues/755)) ([9be15f8](https://github.com/zextras/carbonio-mailbox/commit/9be15f89a16ddf271ed4f07d7790cde1e06178d6))


### Features

* [CO-1066] add redis support for ephemeral data ([#750](https://github.com/zextras/carbonio-mailbox/issues/750)) ([6725a92](https://github.com/zextras/carbonio-mailbox/commit/6725a92dbff67e3e6365641d6b477e433d7c6c9e))

# [4.22.0](https://github.com/zextras/carbonio-mailbox/compare/4.21.0...4.22.0) (2025-05-29)


### Features

* [TSK-105] add carbonioFeatureTasksEnabled ([#740](https://github.com/zextras/carbonio-mailbox/issues/740)) ([6d2de81](https://github.com/zextras/carbonio-mailbox/commit/6d2de81729ae17ce815759140e3cfebf0054de22))

# [4.21.0](https://github.com/Zextras/carbonio-mailbox/compare/4.20.1...4.21.0) (2025-05-19)


### Bug Fixes

* [CO-1622] refresh milter service on sendToDistList DL permission changes ([#677](https://github.com/Zextras/carbonio-mailbox/issues/677)) ([199a130](https://github.com/Zextras/carbonio-mailbox/commit/199a13097d6827f6693fa53a60e4fa3751e483ca))
* [CO-1848] skip imap UI id range search ([#681](https://github.com/Zextras/carbonio-mailbox/issues/681)) ([72b52da](https://github.com/Zextras/carbonio-mailbox/commit/72b52dac215de207586a54ac41a4268840654621))
* add explicit async support on servet and like + other alignments ([#675](https://github.com/Zextras/carbonio-mailbox/issues/675)) ([9c8724c](https://github.com/Zextras/carbonio-mailbox/commit/9c8724cc13ef0da62eb60896a03c1bd667cd023e))
* add required filters and servlet names ([#676](https://github.com/Zextras/carbonio-mailbox/issues/676)) ([15f22e3](https://github.com/Zextras/carbonio-mailbox/commit/15f22e3d61848fde9ee8acf1efc19c0f26bdd4f8))
* CreateMessageTest inconsistency ([#717](https://github.com/Zextras/carbonio-mailbox/issues/717)) ([219b107](https://github.com/Zextras/carbonio-mailbox/commit/219b1076002b05015f92d1a11a229970dbc0e860))
* remove qupte from buildInfo.properties ([#715](https://github.com/Zextras/carbonio-mailbox/issues/715)) ([d0b938d](https://github.com/Zextras/carbonio-mailbox/commit/d0b938d2c0f90d2a3bbed9fbd1a77d49ab82205e))
* wrong methods resulting from merge ([1b398dc](https://github.com/Zextras/carbonio-mailbox/commit/1b398dca73fc0a264e25aa6d7ac2c541d1ceedee))


### Features

* [CO-1945] autoprovisioning filter time made dependent on attribute carbonioAutoProvTimestampAttribute ([#688](https://github.com/Zextras/carbonio-mailbox/issues/688)) ([527e802](https://github.com/Zextras/carbonio-mailbox/commit/527e8027f683c09d8e15b575cccfd28f34835f9c))
* [CO-1954] support lang_tag when calling preview service ([#687](https://github.com/Zextras/carbonio-mailbox/issues/687)) ([c87b9cf](https://github.com/Zextras/carbonio-mailbox/commit/c87b9cfc43082a671e0d73dbc0544f42d03c6c46)), closes [zextras/carbonio-mails-ui#849](https://github.com/zextras/carbonio-mails-ui/issues/849)
* [CO-2053] drop support for SPNEGO authentication ([#724](https://github.com/Zextras/carbonio-mailbox/issues/724)) ([9ca6464](https://github.com/Zextras/carbonio-mailbox/commit/9ca64648df2bcb57da524e442051efde6d599882))


### Reverts

* Revert "chore: close critical Apache MINA Deserialization RCE Vulnerability" (#694) ([01e63c4](https://github.com/Zextras/carbonio-mailbox/commit/01e63c47cf9725cc6a41075dd17977c81804a373)), closes [#694](https://github.com/Zextras/carbonio-mailbox/issues/694)

## [4.20.1](https://github.com/zextras/carbonio-mailbox/compare/4.20.0...4.20.1) (2025-02-17)


### Bug Fixes

* [CO-1817] refactor LikeXmlJettyServer to use localServer for configuration values where required ([#668](https://github.com/zextras/carbonio-mailbox/issues/668)) ([940bc0f](https://github.com/zextras/carbonio-mailbox/commit/940bc0f71f9a4fcc21db20f45084631c1b76f181))
* [CO-1818] add user https connector on mailSSLPort ([#669](https://github.com/zextras/carbonio-mailbox/issues/669)) ([e53aa16](https://github.com/zextras/carbonio-mailbox/commit/e53aa16b45afc5b98fd90bfd616d656638d72ebd))

# [4.20.0](https://github.com/zextras/carbonio-mailbox/compare/4.19.5...4.20.0) (2025-02-10)


### Bug Fixes

* [CO-1574] delete account with public share ([#655](https://github.com/zextras/carbonio-mailbox/issues/655)) ([446a0c6](https://github.com/zextras/carbonio-mailbox/commit/446a0c6edf2deb29b6db9086bd722eeb968c501f))


### Features

* [CO-1337] Email encryption s/mime (#646) ([af2807f](https://github.com/zextras/carbonio-mailbox/commit/af2807fd03494f1dba104cd9da7bf5b3e68f50a4))
* [CO-1661] Modularize ProvUtil ([59c85d1](https://github.com/zextras/carbonio-mailbox/commit/59c85d1b3ad59b5e19e74037cc3fdcc4829a62b9))
* [CO-1489] added pagination support to search users by feature [#638] ([59c85d1](https://github.com/zextras/carbonio-mailbox/commit/59c85d1b3ad59b5e19e74037cc3fdcc4829a62b9))
* [CO-1668] Do not notify the organizer when attendees forward an appointment ([#633](https://github.com/zextras/carbonio-mailbox/issues/633)) ([2e26480](https://github.com/zextras/carbonio-mailbox/commit/2e2648075ecea080a2cbc9b5ddd44c1cd763f22a))
* remove event driven account deletion from mailbox and replace it with http call ([#664](https://github.com/zextras/carbonio-mailbox/issues/664)) ([ae79492](https://github.com/zextras/carbonio-mailbox/commit/ae79492960c71198cd29dd2046714b1228d97fea))

## [4.19.5](https://github.com/zextras/carbonio-mailbox/compare/4.19.4...4.19.5) (2025-01-23)


### Bug Fixes

* [CO-1783] remove mailbox permission to access files healthcheck ([#658](https://github.com/zextras/carbonio-mailbox/issues/658)) ([5df3228](https://github.com/zextras/carbonio-mailbox/commit/5df3228bebfdc8fc9f384f10f5f7ebc98492bd00))

## [4.19.4](https://github.com/zextras/carbonio-mailbox/compare/4.19.3...4.19.4) (2025-01-07)


### Bug Fixes

* [CF-924] point to web.xml wqhen specifying webappcontext webapp parameter ([#651](https://github.com/zextras/carbonio-mailbox/issues/651)) ([10e40f9](https://github.com/zextras/carbonio-mailbox/commit/10e40f9817519194b7494d9f158e5ce117d601c8))

## [4.19.3](https://github.com/zextras/carbonio-mailbox/compare/4.19.2...4.19.3) (2024-12-16)


### Bug Fixes

* retry consumer init if failed and handle message broker factory exceptions ([#643](https://github.com/zextras/carbonio-mailbox/issues/643)) ([cfff2ec](https://github.com/zextras/carbonio-mailbox/commit/cfff2ec66bbb3dc324a2ec9218eb92cc4b580d55))

## [4.19.2](https://github.com/Zextras/carbonio-mailbox/compare/4.19.1...4.19.2) (2024-12-13)


### Bug Fixes

* [CO-1720] delete account from mailbox only after files finished deleting blobs from powerstore ([#637](https://github.com/Zextras/carbonio-mailbox/issues/637)) ([a7e029b](https://github.com/Zextras/carbonio-mailbox/commit/a7e029b827fba5510178be8d74f0195f99505294))

## [4.19.1](https://github.com/zextras/carbonio-mailbox/compare/4.19.0...4.19.1) (2024-12-04)


### Bug Fixes

* remove purgeImapDeleted. ([6444022](https://github.com/zextras/carbonio-mailbox/commit/6444022b78636e2d699e163df4d96000a91d5c12))

# [4.19.0](https://github.com/Zextras/carbonio-mailbox/compare/4.18.0...4.19.0) (2024-11-25)


### Features

* [CO-1668] Do not notify the organizer when attendees forward an appointment ([#633](https://github.com/Zextras/carbonio-mailbox/issues/633)) ([4d5cd86](https://github.com/Zextras/carbonio-mailbox/commit/4d5cd8638db0feb0e2a3db18a87124a21e935a9b))

# [](https://github.com/Zextras/carbonio-mailbox/compare/4.17.8...null) (2024-11-19)


### Bug Fixes

* [CO-1453] SearchAllDomainsByFeature sometime fails to complete accounts while adding to a group chat on wsc ([#599](https://github.com/Zextras/carbonio-mailbox/issues/599)) ([#600](https://github.com/Zextras/carbonio-mailbox/issues/600)) ([640921e](https://github.com/Zextras/carbonio-mailbox/commit/640921ec05dfdeee4bc43164736c926677d0cbdd))
* [CO-1462] Autoprovision zimbraAutoProvLastPolledTimestamp updated to account createdTimestamp ([#583](https://github.com/Zextras/carbonio-mailbox/issues/583)) ([446ba22](https://github.com/Zextras/carbonio-mailbox/commit/446ba2221c115ceeba015758d6dbfdd7f9e5b1bd))
* [CO-1463] Delete account log exception when DeleteUserUseCase throws exception ([#595](https://github.com/Zextras/carbonio-mailbox/issues/595)) ([d5be97f](https://github.com/Zextras/carbonio-mailbox/commit/d5be97f6d9d5f904c7fd27007c3c2db4d75f4b9f))
* [CO-1577] remove closure-compiler([#620](https://github.com/Zextras/carbonio-mailbox/issues/620)) ([816b798](https://github.com/Zextras/carbonio-mailbox/commit/816b798e0406ac0dbf02eb7ed71a182f64bdb50a))
* add DeleteCalendarResponse in JaxbUtil. ([1ac5efe](https://github.com/Zextras/carbonio-mailbox/commit/1ac5efec7e3e17124884bc22df064b6b626f3a7f))
* all calendars group name. ([5b2c0f1](https://github.com/Zextras/carbonio-mailbox/commit/5b2c0f1a7214d3823a026d877eb45eb7133aa3c8))
* bcutil dependency ([#613](https://github.com/Zextras/carbonio-mailbox/issues/613)) ([0c59766](https://github.com/Zextras/carbonio-mailbox/commit/0c59766f45643231229ad671244dd0f78078ef80))
* better calendar ids validation. ([f2e106b](https://github.com/Zextras/carbonio-mailbox/commit/f2e106b67518c695ed6cf2a57bb1daaedd8cacd0))
* better calendar ids validation. ([67f957f](https://github.com/Zextras/carbonio-mailbox/commit/67f957f0d02feb3939d7fdee3e1f3c3c507672d1))
* check that calendars exist before create a group ([485f0c6](https://github.com/Zextras/carbonio-mailbox/commit/485f0c65fe83d6c85c3b7878897e598fe5eb5b11))
* ci: upload rhel packages to devel ([a7076bf](https://github.com/Zextras/carbonio-mailbox/commit/a7076bf006d50e25d6146ac9521be2e23bc1a87f))
* CO-1475 MailSignature response contract changed ([#616](https://github.com/Zextras/carbonio-mailbox/issues/616)) ([09e5c3a](https://github.com/Zextras/carbonio-mailbox/commit/09e5c3a7baf3673324d7d1c7bdc6a2ee5815f7ec))
* conv query ([c3c7857](https://github.com/Zextras/carbonio-mailbox/commit/c3c7857021772d10478fb561ebb4ce44c74f2dec))
* DeleteAccount fails mailbox startup during bootstrap causing installation to be stuck ([#622](https://github.com/Zextras/carbonio-mailbox/issues/622)) ([573d15b](https://github.com/Zextras/carbonio-mailbox/commit/573d15b525e46a7066cdcd8cebc5c1cbcf5a512b))
* empty calendars list when group has no calendars ([1a8fc9f](https://github.com/Zextras/carbonio-mailbox/commit/1a8fc9f37a1e863655ac0cc324d030823309329e))
* fixed PreviewServlet package name in web.xml ([#570](https://github.com/Zextras/carbonio-mailbox/issues/570)) ([cbfa8ae](https://github.com/Zextras/carbonio-mailbox/commit/cbfa8ae6f58109c81f638300f71317f78c9734a7))
* fixed request and response for CreateCalendarGroup ([5228d7c](https://github.com/Zextras/carbonio-mailbox/commit/5228d7c7ab0b542c4a666523f876b3d125f5e4f1))
* handle empty list from request. ([575650c](https://github.com/Zextras/carbonio-mailbox/commit/575650cd5f8d7a0b87a3b65eb2ef63e7946e0fb6))
* handle list ids modification as optional ([4ea020b](https://github.com/Zextras/carbonio-mailbox/commit/4ea020b06dca24202c5709182401352bdf68c8e4))
* id as string ([1ab5318](https://github.com/Zextras/carbonio-mailbox/commit/1ab5318fa92cd0c5dad83137e9bd1bb08457539d))
* id as string ([3ef2ccb](https://github.com/Zextras/carbonio-mailbox/commit/3ef2ccb7c9f8f4e63443f24279aca1987301f4d1))
* make message broker optional ([#614](https://github.com/Zextras/carbonio-mailbox/issues/614)) ([06dab24](https://github.com/Zextras/carbonio-mailbox/commit/06dab24e1d99a0ef75d52460d86e789dedabafc5))
* separate unique/nonUnique element creation. ([fd6658d](https://github.com/Zextras/carbonio-mailbox/commit/fd6658de249cad048cece3206093f9dc1b16f11a))
* should update list if coming list has same size as existing one but different ids. ([939a92f](https://github.com/Zextras/carbonio-mailbox/commit/939a92f077fb18785cf174bb9876615edd3b31fe))
* **test:** correct size ([905f7c0](https://github.com/Zextras/carbonio-mailbox/commit/905f7c01b853ecafb52a1e47bc08feb16b0ee78c))
* use getFolderById instead of getCalendarItemById. ([312d782](https://github.com/Zextras/carbonio-mailbox/commit/312d7827176daca2e7abccdfa7a9f751af083c89))
* use MailItem.Type.UNKNOWN to set custom metadata. ([bd65576](https://github.com/Zextras/carbonio-mailbox/commit/bd65576006fc7e72dba5fe30b4b6b70a6afeee2b))


### Features

* [CO-1341] forward appointment ([#589](https://github.com/Zextras/carbonio-mailbox/issues/589)) ([4f36ac5](https://github.com/Zextras/carbonio-mailbox/commit/4f36ac5678bc5715bfd63c5945b784efc532ee9a))
* [CO-1357] emit user status changed event on message broker ([#558](https://github.com/Zextras/carbonio-mailbox/issues/558)) ([75b9b53](https://github.com/Zextras/carbonio-mailbox/commit/75b9b533e939413a744667798cd515ef5b0d4d14))
* [CO-1395] Reset the participation status of attendees on date time is change ([#588](https://github.com/Zextras/carbonio-mailbox/issues/588)) ([fb0677d](https://github.com/Zextras/carbonio-mailbox/commit/fb0677d39019fc33e6d20038abce188c4c861284))
* [CO-1430] emit user delete event on message broker when user is deleted ([#571](https://github.com/Zextras/carbonio-mailbox/issues/571)) ([9d8d919](https://github.com/Zextras/carbonio-mailbox/commit/9d8d919f037d189a654db6a72d74e1a1f14e0d30))
* [CO-1481] FileUploadServlet, Align maxSize calculation  ([#593](https://github.com/Zextras/carbonio-mailbox/issues/593)) ([e9cb0a9](https://github.com/Zextras/carbonio-mailbox/commit/e9cb0a993de7b9b32e1d4b7ed242fd9bb8a8d457))
* [CO-1528] PreviewServlet allow previewing attachments from Appointments ([#607](https://github.com/Zextras/carbonio-mailbox/issues/607)) ([ebaa78b](https://github.com/Zextras/carbonio-mailbox/commit/ebaa78b1bb6f88713bb87e0e350e45dc8ccd5763))
* add carbonioSearchSpecifiedDomainsByFeature ([#621](https://github.com/Zextras/carbonio-mailbox/issues/621)) ([9ac2f99](https://github.com/Zextras/carbonio-mailbox/commit/9ac2f9913b13592a080b23ab3ebd1bdec0b2ed95))
* add delete calendar group. ([ccb8b1e](https://github.com/Zextras/carbonio-mailbox/commit/ccb8b1e95011eac8f50d82b2e4df4acb6b8dde05))
* add empty calendar trash service. ([541d781](https://github.com/Zextras/carbonio-mailbox/commit/541d7811f925d5d8a3d92d279c4bf6f3d01f384f))
* add getCalendarGroups. ([3f67317](https://github.com/Zextras/carbonio-mailbox/commit/3f673176776f7b5b5de6ab373f5c5c1ffb6b8a79))
* add modify calendar group. ([5835e2f](https://github.com/Zextras/carbonio-mailbox/commit/5835e2fbc8d2e9e336981e7fe7d5877c326cff99))
* add name in ModifyCalendarGroupRequest to also set group name (optional). ([2a563e6](https://github.com/Zextras/carbonio-mailbox/commit/2a563e6776841f6635310b8685de74f12a594d54))
* allow to create group without calendars. ([5c47be0](https://github.com/Zextras/carbonio-mailbox/commit/5c47be0f73ae7c2cbd70b53ad095fc6509dceb92))
* check account rights to access calendar groups ([2d11120](https://github.com/Zextras/carbonio-mailbox/commit/2d1112007562aca6e8107473f8ae1d1b3e5f62cd))
* check if ids belong to calendars. ([60d8a27](https://github.com/Zextras/carbonio-mailbox/commit/60d8a27016c9c352e225fa91e0d503fb0f9f0c79))
* **CO-1264:** set attachment filename on attached emails ([#626](https://github.com/Zextras/carbonio-mailbox/issues/626)) ([fa6289b](https://github.com/Zextras/carbonio-mailbox/commit/fa6289b2a779f2c6ed3c031f54749cbad470eecc))
* declare GetCalendarGroups handler ([1615d74](https://github.com/Zextras/carbonio-mailbox/commit/1615d746d301065d06f351e3053bc43c399b9cd4))
* declare GetCalendarGroups request/response contants ([4a41ec4](https://github.com/Zextras/carbonio-mailbox/commit/4a41ec45cd66daf79b84bc977332255c913558de))
* declare GetCalendarGroups request/response DTO ([e4d13e2](https://github.com/Zextras/carbonio-mailbox/commit/e4d13e237f697b5d27643a2cadc702203ffaf795))
* exception trying to create a calendar group with already existing name. ([41e453a](https://github.com/Zextras/carbonio-mailbox/commit/41e453ad6b9ffb9daf81016639de57bf3774f9cd))
* extract encode/decode calendarIds in separate class. ([2afc301](https://github.com/Zextras/carbonio-mailbox/commit/2afc3012ee62dbea3979225608165c2e4a9e5ff3))
* handle rename + test ([a84d1bb](https://github.com/Zextras/carbonio-mailbox/commit/a84d1bb42bc84f26bf55f138a99635a0684a9d60))
* implement GetCalendarGroups walking skelethon ([2755f2d](https://github.com/Zextras/carbonio-mailbox/commit/2755f2da1e6b5dc2d1c59b40bad3863d45cc13df))
* introduce custom exception when group name already exists. ([5b2ade5](https://github.com/Zextras/carbonio-mailbox/commit/5b2ade539cf4b4a323f3f305c2868c8335a60d21))
* prevent duplicate calendars when creating a group. ([b607dcd](https://github.com/Zextras/carbonio-mailbox/commit/b607dcd7a8e9fe34b1fae98ea08c2f33ba28077a))
* put all account's calendar inside all calendars group ([9fee0f9](https://github.com/Zextras/carbonio-mailbox/commit/9fee0f990c13908d01212c23896a6b4fa4e65362))
* started implementing createCalendarGroup in Mailbox ([801972e](https://github.com/Zextras/carbonio-mailbox/commit/801972e70e97b172070a83db9983481ddb539a6b))
* started implementing CreateCalendarGroup soap endpoint ([395a39f](https://github.com/Zextras/carbonio-mailbox/commit/395a39fe9a4b959e72fb56ad47f9870b604417b3))
* store calendar group as folder ([26b232c](https://github.com/Zextras/carbonio-mailbox/commit/26b232c9802d2a5b127ec425965dcb30530a0fd0))
* store calendar group as Folder with Appointment view ([cdcfdbb](https://github.com/Zextras/carbonio-mailbox/commit/cdcfdbb2c033e445a02dd3d383b7c6ad298ba8f9))
* store calendar group as Folder with CALENDAR_GROUP view ([aeed771](https://github.com/Zextras/carbonio-mailbox/commit/aeed77190ee129b7cd1b5a07fdf9f4a15f303c54))
* **wip:** create delete calendar soap service. ([5338a24](https://github.com/Zextras/carbonio-mailbox/commit/5338a243d50d87d776197355f90df9e8fa37686f))
* **wip:** register delete calendar soap service in Mail Service. ([8b1f47e](https://github.com/Zextras/carbonio-mailbox/commit/8b1f47e03b8dd9e7c2182b135cad4596f7c0e4ae))

# [](https://github.com/Zextras/carbonio-mailbox/compare/4.17.7...null) (2024-10-18)


### Bug Fixes

* [CO-1570] Remove logic of smartlinkAwareSize calculation from ToXML class ([7c105bb](https://github.com/Zextras/carbonio-mailbox/commit/7c105bb47e47623e796c85652e50e129c1d597cd))

# [4.17.7](https://github.com/Zextras/carbonio-mailbox/compare/4.17.6...4.17.7) (2024-10-09)


### Bug Fixes

* [CO-1529] Add carbonio-charset runtime dependency in store module ([#606](https://github.com/Zextras/carbonio-mailbox/issues/606)) ([d738c32](https://github.com/Zextras/carbonio-mailbox/commit/d738c327d80866e3984f9f19079b76960dd5ef22))

# [4.17.6](https://github.com/zextras/carbonio-mailbox/compare/4.17.5...4.17.6) (2024-09-18)


### Bug Fixes

* [CO-1453] added filter for default cos in SearchUserByFeature ([f8f3927](https://github.com/zextras/carbonio-mailbox/commit/f8f392743b6b55a216d3a7766f9cd3b8ef2446a3))


### Features

* [CO-1453] added allDomains search to SearchUsersByFeature ([64a19fc](https://github.com/zextras/carbonio-mailbox/commit/64a19fc5f6dcd9cfa6d468fe9b7f87e2e7e61e1c))
* [CO-1453] added default cos support for multiple domains in SearchUsersByFeature ([5e78a06](https://github.com/zextras/carbonio-mailbox/commit/5e78a067b01459615341d6581c06e96a4a2f4269))
* [CO-1453] added global attribute to configure SearchUsersByFeature behaviour with domains ([b4b4dd7](https://github.com/zextras/carbonio-mailbox/commit/b4b4dd7604e97195f69fdf17e140cec9908cb293))

## [4.17.5](https://github.com/zextras/carbonio-mailbox/compare/4.17.4...4.17.5) (2024-09-10)


### Bug Fixes

* [CO-1446] Searching a mail in shared mailbox when in conversation view not working ([#590](https://github.com/zextras/carbonio-mailbox/issues/590)) ([c1f5788](https://github.com/zextras/carbonio-mailbox/commit/c1f5788934b0da89a1e557884ea00b0c2da3b0d1))


## [4.17.4](https://github.com/zextras/carbonio-mailbox/compare/4.17.3...4.17.4) (2024-09-09)


### Bug Fixes

* [CO-1462] AD zimbraAutoProvLastPolledTimestamp ([#584](https://github.com/zextras/carbonio-mailbox/issues/584)) ([0a39116](https://github.com/zextras/carbonio-mailbox/commit/0a391167c4497d5fe7c2e611da71be9d82e7afba))


## [4.17.3](https://github.com/zextras/carbonio-mailbox/compare/4.17.2...4.17.3) (2024-09-03)


### Bug Fixes

* versioning ([#577](https://github.com/zextras/carbonio-mailbox/issues/577)) ([c6982c1](https://github.com/zextras/carbonio-mailbox/commit/c6982c1108b9fa0c8d59a18f4c9115220275dd1c))

## [4.17.2](https://github.com/zextras/carbonio-mailbox/compare/4.17.1...4.17.2) (2024-09-02)


### Bug Fixes

* [CO-1442] remove Log4J2Plugins.dat from common ([#575](https://github.com/zextras/carbonio-mailbox/issues/575)) ([d3d5bab](https://github.com/zextras/carbonio-mailbox/commit/d3d5babda5a9e990c8afbc34f4c977f4895a74ca))

## [4.17.1](https://github.com/zextras/carbonio-mailbox/compare/4.17.0...4.17.1) (2024-08-30)


### Bug Fixes

* CO-1443 access log ([edfdcd8](https://github.com/zextras/carbonio-mailbox/commit/edfdcd84a921ecab2d860dbea811c78d34e5b8aa))
* rearrange handlers ([a63b0a6](https://github.com/zextras/carbonio-mailbox/commit/a63b0a629ef29d8a6ad525786d63ad8ff8e16b92))

# [4.17.0](https://github.com/zextras/carbonio-mailbox/compare/4.16.3...4.17.0) (2024-08-26)


### Bug Fixes

* [CO-1179] UserServlet to properly return XML and scriptable attachments without defanging ([#530](https://github.com/zextras/carbonio-mailbox/issues/530)) ([2708e4d](https://github.com/zextras/carbonio-mailbox/commit/2708e4dab90ae71d6c2afb4d3a82d43551a970e5))
* [CO-1180] Remove TNEF converter to prevent .dat attachment duplication ([#528](https://github.com/zextras/carbonio-mailbox/issues/528)) ([e36d1d9](https://github.com/zextras/carbonio-mailbox/commit/e36d1d960ee00ccb8b7fbd01e0f43ddb85597201))
* [CO-1348] PreviewServlet avoid HTTP call to get attachment when available locally ([#555](https://github.com/zextras/carbonio-mailbox/issues/555)) ([1e222df](https://github.com/zextras/carbonio-mailbox/commit/1e222df6c0862d4f46ae3b950e86740242d22bfe))
* carbonio-milter.service: class not found cause of classpath edits ([6b9a584](https://github.com/zextras/carbonio-mailbox/commit/6b9a58446a3ccf8cbf62b31848d5c08dfbf95f15))
* **CO-1133:** fix amavis ldap attrs definition ([#533](https://github.com/zextras/carbonio-mailbox/issues/533)) ([5f75ce3](https://github.com/zextras/carbonio-mailbox/commit/5f75ce3ed893cb78d97112051819d5d79a9266d7))


### Features

* [CO-1089] Add carbonioFeatureOTPMgmtEnabled Attribute ([#538](https://github.com/zextras/carbonio-mailbox/issues/538)) ([faad780](https://github.com/zextras/carbonio-mailbox/commit/faad7805ffb5bb20f40422a8e595c3aa6d1c69cd))
* [CO-1258] FileUploadServlet allow uploading huge attachments ([#563](https://github.com/zextras/carbonio-mailbox/issues/563)) ([d139f66](https://github.com/zextras/carbonio-mailbox/commit/d139f667ad5cd59ee0884671c1e5337e089b3dda))
* [CO-1343] Update LDAP Module Configuration on upgrade ([#548](https://github.com/zextras/carbonio-mailbox/issues/548)) ([44cc588](https://github.com/zextras/carbonio-mailbox/commit/44cc5887f152cfa447411ec86be52c7c5ca92c0a))
* [CO-1350] export http connection pool statistics to prometheus ([#551](https://github.com/zextras/carbonio-mailbox/issues/551)) ([ea87abb](https://github.com/zextras/carbonio-mailbox/commit/ea87abb3d7d0bd88627372441b0e6138e7280e2f))
* [CO-1425] Improve argon2 and PID file migration for LDAP ([#567](https://github.com/zextras/carbonio-mailbox/issues/567)) ([fbdea5f](https://github.com/zextras/carbonio-mailbox/commit/fbdea5f708e1b7371f835fa3057db71f938bca99))
* add ubuntu 24.04 (ubuntu-noble) support ([3f03e11](https://github.com/zextras/carbonio-mailbox/commit/3f03e11c4c48a0155eca806dfea3c0a8803bad83))
* **CO-1295:** define new carbonioFeatureWscEnabled attribute ([#564](https://github.com/zextras/carbonio-mailbox/issues/564)) ([8ae839a](https://github.com/zextras/carbonio-mailbox/commit/8ae839a9395f76c527add7d0b6edf2a33c687194))
* properly close soap client in tests ([#544](https://github.com/zextras/carbonio-mailbox/issues/544)) ([d6b5e66](https://github.com/zextras/carbonio-mailbox/commit/d6b5e66f16bd4d05e1752c7a0c78cafa0b4d2a60))


### Reverts

* Revert "refactor: remove mailboxd_directory (#549)" (#554) ([2b7d7ef](https://github.com/zextras/carbonio-mailbox/commit/2b7d7ef920ec9c36c57f0de00578a1eb795db8fb)), closes [#549](https://github.com/zextras/carbonio-mailbox/issues/549) [#554](https://github.com/zextras/carbonio-mailbox/issues/554) [#549](https://github.com/zextras/carbonio-mailbox/issues/549)

## [4.16.3](https://github.com/Zextras/carbonio-mailbox/compare/4.16.2...4.16.3) (2024-07-17)


### Bug Fixes

* [CO-1358] Migrate the slapd PID file location in LDAP config ([#552](https://github.com/Zextras/carbonio-mailbox/issues/552)) ([634f059](https://github.com/Zextras/carbonio-mailbox/commit/634f059830192d9c64b4a7b62bcd78e87aaf5766))

## [4.16.2](https://github.com/zextras/carbonio-mailbox/compare/4.16.1...4.16.2) (2024-07-08)


### Bug Fixes

* [CO-1239] always notify attendees when appointment is updated ([#543](https://github.com/zextras/carbonio-mailbox/issues/543)) ([9b4706e](https://github.com/zextras/carbonio-mailbox/commit/9b4706eb313df5880c07a73858b5f89be43b2808))

# [4.16.1](https://github.com/Zextras/carbonio-mailbox/compare/4.16.0...4.16.1) (2024-07-03)


### Bug Fixes

* [CO-1244] Return 500 for any error and replace it with 422 ([#531](https://github.com/Zextras/carbonio-mailbox/issues/531) ([7f47d06](https://github.com/zextras/carbonio-mailbox/commit/7f47d06aad850cb6d749c74e1f400cbd56922c2e))
* [CO-1249] Better handling of GalGroup type matches in FullAutoComplete API ([#532](https://github.com/Zextras/carbonio-mailbox/issues/532)) ([818b57d](https://github.com/Zextras/carbonio-mailbox/commit/818b57d5a4a11fe750dd4790523a5524b375bf28))

# [4.16.0](https://github.com/Zextras/carbonio-mailbox/compare/4.15.5...4.16.0) (2024-06-18)


### Bug Fixes

* [CO-1047] serialize matches also into JSON objects in FullAutoComplete ([#496](https://github.com/Zextras/carbonio-mailbox/issues/496)) ([cabd96c](https://github.com/Zextras/carbonio-mailbox/commit/cabd96c1ab5adc1adf1f779f7b8dea0672aef9e1))


### Features

* [CO-1023] add ability to empty folder by item type ([#519](https://github.com/Zextras/carbonio-mailbox/issues/519)) ([dee564f](https://github.com/Zextras/carbonio-mailbox/commit/dee564f5974bddad5bb7c1f151bc6d753ef86076))
* [CO-1040] make account status accessible via GetAccountInfo SOAP API ([#503](https://github.com/Zextras/carbonio-mailbox/issues/503)) ([9fbd316](https://github.com/Zextras/carbonio-mailbox/commit/9fbd316b437e8eb4e3c02837de6de2c4165e19a9))
* [CO-1045] SetAllowonCurrentSocketFactoryUse to true ([#506](https://github.com/Zextras/carbonio-mailbox/issues/506)) ([6168e4e](https://github.com/Zextras/carbonio-mailbox/commit/6168e4ef803a31ad6e827d1909f59632acec340e))
* [CO-1061] Support Contact groups ordering in FullAutoComplete ([#504](https://github.com/Zextras/carbonio-mailbox/issues/504)) ([57fd67e](https://github.com/Zextras/carbonio-mailbox/commit/57fd67e7c06d6697f94477b1e1e986e1b83c10d0))
* [CO-1074] avoid SOAP request to perform AutoComplete if requested account is local ([#521](https://github.com/Zextras/carbonio-mailbox/issues/521)) ([691d0b4](https://github.com/Zextras/carbonio-mailbox/commit/691d0b4d4054d76dbc5ace77dc63240cb101ee69))
* [CO-959] Filter out read-receipt notification mails from BulkTest Sieve filter ([#509](https://github.com/Zextras/carbonio-mailbox/issues/509)) ([a0ae2a9](https://github.com/Zextras/carbonio-mailbox/commit/a0ae2a97ccce9c99e399fcedb6a0c7dff8ac4e5c))
* [CO-985] Enhance recovery code based Auth handling when carbonio auth is registered ([#499](https://github.com/Zextras/carbonio-mailbox/issues/499)) ([56b6c29](https://github.com/Zextras/carbonio-mailbox/commit/56b6c299e6d7ffd7ed078c3972f99b8d2d4f94b1))
* [COR-916] Use proxy config to make outbound connections through internet ([#517](https://github.com/Zextras/carbonio-mailbox/issues/517)) ([fa4be90](https://github.com/Zextras/carbonio-mailbox/commit/fa4be9068e86718597f528630255f900feedda72))
* [ZIF-1022] Convert the smart-link metrics to send info to posthog (instead of matomo) ([#501](https://github.com/Zextras/carbonio-mailbox/issues/501)) ([fa51599](https://github.com/Zextras/carbonio-mailbox/commit/fa515994e17c1da47cd47765947c7ee35b6a2186))
* **CO-1121:** export account zimbraIsExternalVirtualAccount attribute in GetAccountInfo request ([2a7cc7d](https://github.com/Zextras/carbonio-mailbox/commit/2a7cc7db544966591ed6fad410b7aa3b4f50db6e))
* remove sendRecoveryLink operation from RecoverAccountOperation ([#522](https://github.com/Zextras/carbonio-mailbox/issues/522)) ([567adb2](https://github.com/Zextras/carbonio-mailbox/commit/567adb20f944912461f08003a3bcfb25533b53ef))


### Reverts

* "revert: [CO-982] Enhance ordering of matches in FullAutoComplete ([#495](https://github.com/Zextras/carbonio-mailbox/issues/495))" ([9f4056b](https://github.com/Zextras/carbonio-mailbox/commit/9f4056b303b81e2f88dd0cd42c356de250f2f8bc))

# [4.15.1](https://github.com/Zextras/carbonio-mailbox/compare/4.15.0...4.15.1) (2024-04-12)


### Bug Fixes

* FullAutoComplete API response in JSON instead of wrapping xml in JSON ([c602118](https://github.com/Zextras/carbonio-mailbox/commit/c602118e840c8363551edcbac93605fada3df13a))


### Features

* introduce ContactEntryType enum replacing type string literal ([cd5e26c](https://github.com/Zextras/carbonio-mailbox/commit/cd5e26cb73dab2a1d11c63382d7334f8799b3b52))

# [4.15.0](https://github.com/Zextras/carbonio-mailbox/compare/4.14.0...4.15.0) (2024-04-11)


### Features

* [CO-1014] add carbonioPrefSendAnalytics attribute ([#468](https://github.com/Zextras/carbonio-mailbox/issues/468)) ([0eddb70](https://github.com/Zextras/carbonio-mailbox/commit/0eddb709aa10a257f834c9f69414d97d9c288f22))
* [CO-982] Enhance ordering of matches in FullAutoComplete ([#473](https://github.com/Zextras/carbonio-mailbox/issues/473)) ([f0481ba](https://github.com/Zextras/carbonio-mailbox/commit/f0481badaab31656ac0d72adee2041cce1808c62))
* [ZIF-1008] add carbonio send analytics check ([#478](https://github.com/Zextras/carbonio-mailbox/issues/478)) ([896d1b6](https://github.com/Zextras/carbonio-mailbox/commit/896d1b66019c6831351033194f6226a1eceaeb07))
* [ZIF-1008] add Matomo feature usage tracking for smart link ([#474](https://github.com/Zextras/carbonio-mailbox/issues/474)) ([f352b31](https://github.com/Zextras/carbonio-mailbox/commit/f352b3104ba7524c0bb8ef22f31461ce3d6e8455))
* [ZIF-970] Convert an attachment into a smart-link ([#466](https://github.com/Zextras/carbonio-mailbox/issues/466)) ([fa650f2](https://github.com/Zextras/carbonio-mailbox/commit/fa650f2bc071d7b2266511444c8b0312996d8e8a)), closes [#454](https://github.com/Zextras/carbonio-mailbox/issues/454)
* [ZIF-982] remove message size check on save draft ([#471](https://github.com/Zextras/carbonio-mailbox/issues/471)) ([d9114e5](https://github.com/Zextras/carbonio-mailbox/commit/d9114e5c0bb334680003cc358c70c15e84c30462))
* add systemd units ([#477](https://github.com/Zextras/carbonio-mailbox/issues/477)) ([f210224](https://github.com/Zextras/carbonio-mailbox/commit/f210224f3f76727490d38b0d3858e1c0f21d7132))
* **CO-954:** define Mailbox Admin Sidecar ([#450](https://github.com/Zextras/carbonio-mailbox/issues/450)) ([23ea121](https://github.com/Zextras/carbonio-mailbox/commit/23ea121066cc79fd409a50985bc52a809d5bf760))
* **CO-954:** define nslookup sidecar ([#452](https://github.com/Zextras/carbonio-mailbox/issues/452)) ([7adde50](https://github.com/Zextras/carbonio-mailbox/commit/7adde5004f55e4d8382edb8485037a0383845196))

# [4.14.0](https://github.com/Zextras/carbonio-mailbox/compare/4.12.0...4.14.0) (2024-02-15)


### Bug Fixes

* [CO-856] rename account reports error ([#415](https://github.com/Zextras/carbonio-mailbox/issues/415)) ([21b614c](https://github.com/Zextras/carbonio-mailbox/commit/21b614cc81b89d8a426dbb00af7fe0f0b04a1be4))
* [CO-862] Autoprovisioning zimbraAutoProvAccountNameMap account create ([#435](https://github.com/Zextras/carbonio-mailbox/issues/435)) ([f1fa2d5](https://github.com/Zextras/carbonio-mailbox/commit/f1fa2d5c93e7e58ca0d551abe9568249f0f421c1))
* [CO-863] Missing log4j class in zmproxypurge ([#423](https://github.com/Zextras/carbonio-mailbox/issues/423)) ([ed44ba0](https://github.com/Zextras/carbonio-mailbox/commit/ed44ba0df37c27209441e191fe8f8fd92cc1c8b5))
* AutoProvisionTest ([#436](https://github.com/Zextras/carbonio-mailbox/issues/436)) ([4d81588](https://github.com/Zextras/carbonio-mailbox/commit/4d815881d9aa9afa1ca92c6fb00b6a3ca00be6f5))


### Features

* [COR-1022] add zimbra mail transport edit right to combo right "domainAdminRights" ([#424](https://github.com/Zextras/carbonio-mailbox/issues/424)) ([36ffe3e](https://github.com/Zextras/carbonio-mailbox/commit/36ffe3e4d26500091cadc4a0fdccce34be3b17b9))
* [COR-936] Create account with given zimbraId ([#438](https://github.com/Zextras/carbonio-mailbox/issues/438)) ([2f5e8c5](https://github.com/Zextras/carbonio-mailbox/commit/2f5e8c5906a554d5d5f96601bbd5167399cff2bd))
* [HIG-112] delete account without store ([#417](https://github.com/Zextras/carbonio-mailbox/issues/417)) ([f895f0b](https://github.com/Zextras/carbonio-mailbox/commit/f895f0bb65e38ae8060d133acdd632c35b0c6d12))

# [4.12.0](https://github.com/Zextras/carbonio-mailbox/compare/4.11.1...4.12.0) (2024-01-04)


### Bug Fixes

* [CO-929] update milter log4j config ([#410](https://github.com/Zextras/carbonio-mailbox/issues/410)) ([28809de](https://github.com/Zextras/carbonio-mailbox/commit/28809deb4cad8b66b9543a530d50f76f0e01da9e))
* generateversions-init.sql and attr-schema ([#398](https://github.com/Zextras/carbonio-mailbox/issues/398)) ([b737938](https://github.com/Zextras/carbonio-mailbox/commit/b73793848b8aa0a7da228cd715c411d5982461e0))


### Features

* [CO-917] generate regular auth token for recover password requests ([#397](https://github.com/Zextras/carbonio-mailbox/issues/397)) ([e35458d](https://github.com/Zextras/carbonio-mailbox/commit/e35458d32daf1577fe7c756aa2d552d11120ce90))
* healthcheck ([#401](https://github.com/Zextras/carbonio-mailbox/issues/401)) ([237ee66](https://github.com/Zextras/carbonio-mailbox/commit/237ee66280db20fc7c408c646f23b0fa6da6723e))


## [4.11.1](https://github.com/Zextras/carbonio-mailbox/compare/4.11.0...4.11.1) (2023-11-28)


### Bug Fixes

* carbonio-appserver-db conflicts common-appserver-db ([c2c445f](https://github.com/Zextras/carbonio-mailbox/commit/c2c445f465e1336d7b7cd27bb7042a1144676c39))

# [4.11.0](https://github.com/Zextras/carbonio-mailbox/compare/4.10.1...4.11.0) (2023-11-23)


### Bug Fixes

* [CO-851] search contacts also in shared accounts ([#384](https://github.com/Zextras/carbonio-mailbox/issues/384)) ([fa228b2](https://github.com/Zextras/carbonio-mailbox/commit/fa228b263bfbff320f6372e4d616c0494aaa1574))
* [CO-851] use USER_ROOT for Autocomplete by default ([#385](https://github.com/Zextras/carbonio-mailbox/issues/385)) ([5080f5d](https://github.com/Zextras/carbonio-mailbox/commit/5080f5d72e0b8ed48ab35cf524de6820720c4a2c))
* [CO-860] handle missing header in FB request made with iCalendar ([#376](https://github.com/Zextras/carbonio-mailbox/issues/376)) ([e9a53bb](https://github.com/Zextras/carbonio-mailbox/commit/e9a53bbae57d94f8dd5460c89c777260395e8ed6))
* [CO-862] Autoprovisioning try to create account with domain ([#381](https://github.com/Zextras/carbonio-mailbox/issues/381)) ([140765a](https://github.com/Zextras/carbonio-mailbox/commit/140765ad354ba6d7f2e758f58f1ff365245816f3))
* [CO-869] [MariaDB] Upgrade to 10.4.31 - max index length 3072 bytes ([#380](https://github.com/Zextras/carbonio-mailbox/issues/380)) ([a7bce3a](https://github.com/Zextras/carbonio-mailbox/commit/a7bce3a9c721c13c4efd92377784694a23926520))
* [COR-1012] handle zero domain account quota ([#365](https://github.com/Zextras/carbonio-mailbox/issues/365)) ([de25ade](https://github.com/Zextras/carbonio-mailbox/commit/de25aded93ef1e704c29994294e48d62905ec023))


### Features

* [CO-851] add contact to shared account contact list ([#368](https://github.com/Zextras/carbonio-mailbox/issues/368)) ([23f7bd3](https://github.com/Zextras/carbonio-mailbox/commit/23f7bd3b78903315fd14f567b060d952ca9fbe6b))
* [CO-867]  Custom timestamp format support for Autoprovisioning using external Directory server ([#383](https://github.com/Zextras/carbonio-mailbox/issues/383)) ([774192e](https://github.com/Zextras/carbonio-mailbox/commit/774192e251ac1411d8dc58bbe39a6f49dfe61a63))

## [4.10.1](https://github.com/Zextras/carbonio-mailbox/compare/4.10.0...4.10.1) (2023-11-09)

# [4.10.0](https://github.com/Zextras/carbonio-mailbox/compare/4.9.1...4.10.0) (2023-10-20)


### Bug Fixes

*  CO-823 FreeBusy missing uid parameter in CalDav request ([#343](https://github.com/Zextras/carbonio-mailbox/issues/343)) ([bc11447](https://github.com/Zextras/carbonio-mailbox/commit/bc11447882a485cd10d999141b4abdcc7e400ab1))
* [CO-817] ProxyConfGen is not able to show debug output to stdout ([#353](https://github.com/Zextras/carbonio-mailbox/issues/353)) ([6158d24](https://github.com/Zextras/carbonio-mailbox/commit/6158d2452cf2ad91cd39f399f1254b1916e5a1f2))
* [CO-861] mailbox wsdl generator uses soap 12 soap protocol wrong content type ([#356](https://github.com/Zextras/carbonio-mailbox/issues/356)) ([98ac447](https://github.com/Zextras/carbonio-mailbox/commit/98ac447bb32c36670f8c78af4b1667906c0c9e1d))
* add more tests ([1eb54d6](https://github.com/Zextras/carbonio-mailbox/commit/1eb54d6c2836907f0a4e8f4a3a1ebc8ad6b02042))
* log category does not exist ([#346](https://github.com/Zextras/carbonio-mailbox/issues/346)) ([50a6d18](https://github.com/Zextras/carbonio-mailbox/commit/50a6d18e2ed20712f59e69c373eed12f7cdb6fe6))
* put back addService ([#359](https://github.com/Zextras/carbonio-mailbox/issues/359)) ([4f5e8e5](https://github.com/Zextras/carbonio-mailbox/commit/4f5e8e578eb0c842038bafb25dedfb69647ab71b))


### Features

* [CO-825] add attribute to manage ClamAv ReadTimeout ([#355](https://github.com/Zextras/carbonio-mailbox/issues/355)) ([157600a](https://github.com/Zextras/carbonio-mailbox/commit/157600ad7bafe0e1e37c93d4c0eba28cf5546414))
* [CO-844] delete user should revoke all grants ([#348](https://github.com/Zextras/carbonio-mailbox/issues/348)) ([32537c9](https://github.com/Zextras/carbonio-mailbox/commit/32537c9d0eb4ce4c064267a3a371c2e4a51ae6ef))
* [CO-850] show Disposition-Notification on GetMsg delegated requests ([#354](https://github.com/Zextras/carbonio-mailbox/issues/354)) ([f8cbb88](https://github.com/Zextras/carbonio-mailbox/commit/f8cbb88dc094701de0806eb31a69ebfb9907f5f6))
* [COR-975] define domain admin rights ([#357](https://github.com/Zextras/carbonio-mailbox/issues/357)) ([5b68cd2](https://github.com/Zextras/carbonio-mailbox/commit/5b68cd2223c66dfce6d4e8596dc9c6f1ac33dc87))
* add ubuntu 22.04 (jammy jellyfish) support ([#347](https://github.com/Zextras/carbonio-mailbox/issues/347)) ([a75e8d7](https://github.com/Zextras/carbonio-mailbox/commit/a75e8d717372ec6c5a5cf270a4bbd7ae1c84131e))
* bump carbonio-preview-sdk for gif support ([96e0f7f](https://github.com/Zextras/carbonio-mailbox/commit/96e0f7f7d09dd2a8e7b91bbc04420236725a6c96))
* deprecate CSRF-related LDAP-attributes ([cde0515](https://github.com/Zextras/carbonio-mailbox/commit/cde0515fc681031287df3e39ace61c8bdb1c3cf6))
* follow RFC-6266 to parse extended filename from uploads ([#335](https://github.com/Zextras/carbonio-mailbox/issues/335)) ([4b8b114](https://github.com/Zextras/carbonio-mailbox/commit/4b8b114be32fcc74346c2b4908bd94e0dbe31a4b))
* move to yap agent and add rhel9 support ([#361](https://github.com/Zextras/carbonio-mailbox/issues/361)) ([4098739](https://github.com/Zextras/carbonio-mailbox/commit/4098739e07acadf180cc30fb668e242a9b29b3c2))
* **test:** add shellcheck first complete test ([792f08f](https://github.com/Zextras/carbonio-mailbox/commit/792f08f5437b8f3694b19a5f4deafac9b4c95e65))
* **tests:** add Characterization tests for user deletion ([0d0e6fd](https://github.com/Zextras/carbonio-mailbox/commit/0d0e6fddefa298a871f72de34c51bfd611cfa0fa))
* **tests:** add shellspec for BDD testing ([49516ca](https://github.com/Zextras/carbonio-mailbox/commit/49516ca7e49ef70c09c42e54e305b82be9135724))
* updated sdk version in javadoc ([c70aa6d](https://github.com/Zextras/carbonio-mailbox/commit/c70aa6df329573428ecf487594509ff42506bb5c))

## [4.9.1](https://github.com/Zextras/carbonio-mailbox/compare/4.9.0...4.9.1) (2023-09-28)


### Reverts

* "feat: zimlet code cleanup ([#310](https://github.com/Zextras/carbonio-mailbox/issues/310))" ([#344](https://github.com/Zextras/carbonio-mailbox/issues/344)) ([93cead1](https://github.com/Zextras/carbonio-mailbox/commit/93cead1978c9c5bbc6dcbf097bd2ac7831a3e114))

# [4.9.0](https://github.com/Zextras/carbonio-mailbox/compare/4.8.3...4.9.0) (2023-09-26)

### Bug Fixes

* API documentation ([#318](https://github.com/Zextras/carbonio-mailbox/issues/318)) ([b5bc120](https://github.com/Zextras/carbonio-mailbox/commit/b5bc1208e6ff2b7fea874c73a1ca52db205ca704))

### Features

* add 127.0.0.1 to MailTrustedIP to prevent loss of OIP ([#328](https://github.com/Zextras/carbonio-mailbox/issues/328)) ([9a4fc82](https://github.com/Zextras/carbonio-mailbox/commit/9a4fc82b42cdbf2633670c12cb0e225a647c1537))
* allow admins to specify custom domain login and logout URL ([#301](https://github.com/Zextras/carbonio-mailbox/issues/301)) ([811fd87](https://github.com/Zextras/carbonio-mailbox/commit/811fd87fd6ce4659346f71c3602e07b5685c199e))
* zimlet code cleanup ([#310](https://github.com/Zextras/carbonio-mailbox/issues/310)) ([8d8da00](https://github.com/Zextras/carbonio-mailbox/commit/8d8da00da1e08f1a02d677295eed1f1d1fba45db))

## [4.8.3](https://github.com/Zextras/carbonio-mailbox/compare/4.8.2...4.8.3) (2023-09-21)

### Bug Fixes

* delete CalDav appointment ([#332](https://github.com/Zextras/carbonio-mailbox/issues/332)) ([8324af6](https://github.com/Zextras/carbonio-mailbox/commit/8324af6e51977bac8960a2905fcc745ffb5ecffa))

## [4.8.2](https://github.com/Zextras/carbonio-mailbox/compare/4.8.1...4.8.2) (2023-09-20)

### Bug Fixes

* allow Appointment creation from CalDAV ([#329](https://github.com/Zextras/carbonio-mailbox/issues/329)) ([27d6578](https://github.com/Zextras/carbonio-mailbox/commit/27d65789e34225c953f8fd628f9d566ddbbe218d))

# [4.8.1](https://github.com/Zextras/carbonio-mailbox/compare/4.8.0...4.8.1) (2023-09-08)

### Bug Fixes

* empty calendar.json and upload of cal in zip format  ([#321](https://github.com/Zextras/carbonio-mailbox/issues/321)) ([e6dc2b6](https://github.com/zextras/carbonio-mailbox/commit/e6dc2b60a0488a533f7fab1a030e7767ec8f0dc2))
* ridZ field missing in SearchRequest of Calendars ([#319](https://github.com/Zextras/carbonio-mailbox/issues/319)) ([2485376](https://github.com/zextras/carbonio-mailbox/commit/248537649d18c5e6ca97ee03e809e271b1db01a0))

# [4.8.0](https://github.com/Zextras/carbonio-mailbox/compare/4.6.4...4.8.0) (2023-08-28)

### Bug Fixes

* jython downgrade to 2.5.2 in order to fix configd ([#299](https://github.com/Zextras/carbonio-mailbox/issues/299)) ([e72a01e](https://github.com/Zextras/carbonio-mailbox/commit/e72a01ee8fe1ba32ff39893e147964ec98b4611d))
* Log4j properties in Logger class ([#285](https://github.com/Zextras/carbonio-mailbox/issues/285)) ([97b8972](https://github.com/Zextras/carbonio-mailbox/commit/97b89722e7a46904d1cd7816892aa6471a1923da))
* skin related code cleanup ([#304](https://github.com/Zextras/carbonio-mailbox/issues/304)) ([c057969](https://github.com/Zextras/carbonio-mailbox/commit/c05796953c590922cd532f9d68b01f164166a07f))

### Features

* [CO-767] Remove stale config from Carbonio SOAP Service  ([#266](https://github.com/Zextras/carbonio-mailbox/issues/266)) ([b692cfe](https://github.com/Zextras/carbonio-mailbox/commit/b692cfe03c8b083ef0f599f57d81a483bacf24c0))
* [CO-809] remove curator service discover ([#297](https://github.com/Zextras/carbonio-mailbox/issues/297)) ([0c4cf47](https://github.com/Zextras/carbonio-mailbox/commit/0c4cf47dabd875099f2b206b38677a300506dfcb))
* add json update configuration ([1df1a50](https://github.com/Zextras/carbonio-mailbox/commit/1df1a5045b5abd1f5f3256b16f65da399843c7f9))
* add new information to ReadMe.md ([67ce5d7](https://github.com/Zextras/carbonio-mailbox/commit/67ce5d7cddf05df92b06b52a718c97882ebb5e8e))
* add new whitelabel attributes ([38df556](https://github.com/Zextras/carbonio-mailbox/commit/38df556b94f89610cac2120f570ac82e999e81c2))
* added default values for attributes ([d858c1d](https://github.com/Zextras/carbonio-mailbox/commit/d858c1d479c3093abf01ed9c7bb4df14ee254b68))
* **CO-770:** remove UC Service and Voice features ([#260](https://github.com/Zextras/carbonio-mailbox/issues/260)) ([1fd0809](https://github.com/Zextras/carbonio-mailbox/commit/1fd0809e5b5348d9bb133b8dea594318c05edd26))
* ReadMe clean up, build migration ([18249a6](https://github.com/Zextras/carbonio-mailbox/commit/18249a6dcd5c2b414f745119fc5dd30299802b61))
* remove Domain and Global ACL Manager ([#296](https://github.com/Zextras/carbonio-mailbox/issues/296)) ([2558a04](https://github.com/Zextras/carbonio-mailbox/commit/2558a04f9195a7720c528570a1b083fcc0fb34f7))
* remove Zimbra Mobile Gateway feature ([#271](https://github.com/Zextras/carbonio-mailbox/issues/271)) ([3a63a78](https://github.com/Zextras/carbonio-mailbox/commit/3a63a78a9b5f2a7992cb35ddc15750d3232f7dd3))
* update mailboxd_java_options for JDK17 ([#307](https://github.com/Zextras/carbonio-mailbox/issues/307)) ([ea3fdbe](https://github.com/Zextras/carbonio-mailbox/commit/ea3fdbe0b348864c272967799f8bd159a21ec0d4))

## [4.6.4](https://github.com/Zextras/carbonio-mailbox/compare/4.6.3...4.6.4) (2023-07-26)

### Bug Fixes

* [CO-803] Fix NPE while exporting user data in tgz format ([#293](https://github.com/Zextras/carbonio-mailbox/issues/293)) ([3a037ca](https://github.com/Zextras/carbonio-mailbox/commit/3a037ca514143facc9c1c51574fd84beaf2e91f6))

## [4.6.3](https://github.com/Zextras/carbonio-mailbox/compare/4.6.2...4.6.3) (2023-07-17)

### Bug Fixes

* [CO-792] fix ProxyConfGen ([#283](https://github.com/Zextras/carbonio-mailbox/issues/283)) ([9bebfae](https://github.com/Zextras/carbonio-mailbox/commit/9bebfaecd2c145b3337b17f21e4bbd1b4cb068aa))

## [4.6.2](https://github.com/Zextras/carbonio-mailbox/compare/4.6.1...4.6.2) (2023-07-14)

### Bug Fixes

* proxyconfgen failing to delete cerbot domain conf ([#281](https://github.com/Zextras/carbonio-mailbox/issues/281)) ([dcb5e3d](https://github.com/Zextras/carbonio-mailbox/commit/dcb5e3de5a8a7a74519658f0d18f9cc5961902cd))

## [4.6.1](https://github.com/Zextras/carbonio-mailbox/compare/4.6.0...4.6.1) (2023-07-11)

### Bug Fixes

* **CO-782:** Delete Draft from authed user's mailbox in SendMsg ([#267](https://github.com/Zextras/carbonio-mailbox/issues/267)) ([228639e](https://github.com/Zextras/carbonio-mailbox/commit/228639eda9dec757f297d11287e02b5b1e77fb29))

# [4.6.0](https://github.com/Zextras/carbonio-mailbox/compare/4.5.0...4.6.0) (2023-07-05)

### Bug Fixes

* log4j.properties: remove undefined references to EWS logger ([#256](https://github.com/Zextras/carbonio-mailbox/issues/256)) ([62b89d4](https://github.com/Zextras/carbonio-mailbox/commit/62b89d44567b2bcf5661a27fafe36723116645a4))
* use commons-io 2.11.0 ([#246](https://github.com/Zextras/carbonio-mailbox/issues/246)) ([c59f263](https://github.com/Zextras/carbonio-mailbox/commit/c59f26316834647c3769bf50e4183c5306818383))

### Features

* [CO-563] Deprecate versionCheck related attributes & clean  related code ([#236](https://github.com/Zextras/carbonio-mailbox/issues/236)) ([6991b0b](https://github.com/Zextras/carbonio-mailbox/commit/6991b0b7b7f0856a08a6c9e08a97302bc426190e))
* [CO-721] delete Certbot domain config ([#249](https://github.com/Zextras/carbonio-mailbox/issues/249)) ([1e9c6b2](https://github.com/Zextras/carbonio-mailbox/commit/1e9c6b232fa7de0481e07baed1a83f32c2c88611))
* [CO-729] Redirect as default globalConf value for reverseProxyMailMode ([#238](https://github.com/Zextras/carbonio-mailbox/issues/238)) ([b9aa30d](https://github.com/Zextras/carbonio-mailbox/commit/b9aa30d0bf9068595fec7bab054c498fd2a73a66))
* [CO-756] Update defaults for proxy CSP header ([#259](https://github.com/Zextras/carbonio-mailbox/issues/259)) ([4b654c8](https://github.com/Zextras/carbonio-mailbox/commit/4b654c8e31e529f418ab7e2b1719dc9dc7168393))
* [CO-762] Add carbonio-clamav upstream ([#257](https://github.com/Zextras/carbonio-mailbox/issues/257)) ([387ea3d](https://github.com/Zextras/carbonio-mailbox/commit/387ea3d167b1341cf6d17f688c3cac44e67696bc))
* **CO-727:** allow Save To Files from a shared mailbox ([#255](https://github.com/Zextras/carbonio-mailbox/issues/255)) ([052d460](https://github.com/Zextras/carbonio-mailbox/commit/052d4609e5f56fcf772f12c465f24642fb18b8aa))

# [4.5.0](https://github.com/Zextras/carbonio-mailbox/compare/4.4.0...4.5.0) (2023-05-18)

### Bug Fixes

* [CO-690] delegated account sent mail always read ([#209](https://github.com/Zextras/carbonio-mailbox/issues/209)) ([3b0efc9](https://github.com/Zextras/carbonio-mailbox/commit/3b0efc9454b00d84eec48c70645b9696463996cb))
* [CO-695] enable async support in guice servlet filter ([#211](https://github.com/Zextras/carbonio-mailbox/issues/211)) ([87c5f88](https://github.com/Zextras/carbonio-mailbox/commit/87c5f88667214e6ce2f25b47b8fed719a1edde5b))

### Features

* [CO-658] Reduce messageCacheSize ([#214](https://github.com/Zextras/carbonio-mailbox/issues/214)) ([15359a6](https://github.com/Zextras/carbonio-mailbox/commit/15359a6c8047a389c5cde3b36928bec63a66205e))
* [CO-675] Let admin set arbitrary PublicService & Virtual hostname ([#220](https://github.com/Zextras/carbonio-mailbox/issues/220)) ([3d0d765](https://github.com/Zextras/carbonio-mailbox/commit/3d0d765574c3c9b8a219b576dd68e830fb35637e))
* **CO-625:** briefcase removal ([#221](https://github.com/Zextras/carbonio-mailbox/issues/221)) ([e475b7a](https://github.com/Zextras/carbonio-mailbox/commit/e475b7a547a8e3b15756fba14e48a66bba6897b1)), closes [#188](https://github.com/Zextras/carbonio-mailbox/issues/188) [#219](https://github.com/Zextras/carbonio-mailbox/issues/219)

# [4.4.0](https://github.com/Zextras/carbonio-mailbox/compare/4.3.0...4.4.0) (2023-04-20)

### Features

* [CO 621] Allow LE certificates to be generated from Mailbox endpoint ([#175](https://github.com/Zextras/carbonio-mailbox/issues/175)) ([6f797aa](https://github.com/Zextras/carbonio-mailbox/commit/6f797aa4a852a4187b56c944800744c9bf3258c3))
* [CO-584] remove graphql ([#197](https://github.com/Zextras/carbonio-mailbox/issues/197)) ([4810aee](https://github.com/Zextras/carbonio-mailbox/commit/4810aee1aa8a3caf26ab828e35f12964b3171f17))
* [CO-592] set smtpRestrictEnvelopeFrom default to FALSE ([#190](https://github.com/Zextras/carbonio-mailbox/issues/190)) ([e777dcd](https://github.com/Zextras/carbonio-mailbox/commit/e777dcd374a77dac9d6174f065d0d3b9b011c171))
* [CO-640] chats, meeting, team feature enabled attributes ([#191](https://github.com/Zextras/carbonio-mailbox/issues/191)) ([e9843b1](https://github.com/Zextras/carbonio-mailbox/commit/e9843b18f4fcb268b271f817349759eebdf4d791))
* [CO-659] add attribute to disable the amavis antivirus scan ([#189](https://github.com/Zextras/carbonio-mailbox/issues/189)) ([9fdacae](https://github.com/Zextras/carbonio-mailbox/commit/9fdacae88077bb903d8dcd966c4758bebdd10a19))
* [CO-665] add web.clamav.signature.provider template ([#201](https://github.com/Zextras/carbonio-mailbox/issues/201)) ([c28bb11](https://github.com/Zextras/carbonio-mailbox/commit/c28bb11666d58a5ef2880f5b97e64461071b2531))

# [4.3.0](https://github.com/Zextras/carbonio-mailbox/compare/4.2.0...4.3.0) (2023-03-23)

### Bug Fixes

* [CO-672] Use carbonio-preview-sdk 1.0.1 ([#193](https://github.com/Zextras/carbonio-mailbox/issues/193)) ([28f4385](https://github.com/Zextras/carbonio-mailbox/commit/28f4385868edbb99e0fc3cd35fa01e11e227f8fd))
* [CO-553] Calendar invites parsing in Outlook ([#170](https://github.com/Zextras/carbonio-mailbox/issues/170)) ([ebc3802](https://github.com/Zextras/carbonio-mailbox/commit/ebc3802ca306e1123ff7db024070e9ad03fd4860))

### Features

* [CO-536] Add/update CSP headers in ReverseProxyResponseHeaders   ([#168](https://github.com/Zextras/carbonio-mailbox/issues/168)) ([e3191fb](https://github.com/Zextras/carbonio-mailbox/commit/e3191fb8a6bd9238d8759e38b2fc963d14a53e60))
* [CO-573] expose soap prometheus metrics ([#167](https://github.com/Zextras/carbonio-mailbox/issues/167)) ([c21d5ea](https://github.com/Zextras/carbonio-mailbox/commit/c21d5ea03b5f4eac9c1a30b9135451b8ee202ace))
* [CO-611] prom mailbox JVM stats ([#172](https://github.com/Zextras/carbonio-mailbox/issues/172)) ([7b07213](https://github.com/Zextras/carbonio-mailbox/commit/7b0721332584aa3bfb5a9a457ec92bd7b1fdc59c))
* [CO-619] store Let's encrypt certs to LDAP before proxy conf generation  ([#169](https://github.com/Zextras/carbonio-mailbox/issues/169)) ([f2fcf5b](https://github.com/Zextras/carbonio-mailbox/commit/f2fcf5b08829d7378a1cf9751890b4d47c9a7744))
* [CO-620] Enable Gzip and Brotli compression with ProxyConfGen ([#176](https://github.com/Zextras/carbonio-mailbox/issues/176)) ([2ae8f3d](https://github.com/Zextras/carbonio-mailbox/commit/2ae8f3db9c2d1bc23d4637a13cf62b23cd07c3bb))
* [CO-643] Make Carbonio Notification Recipients/Sender domainInherited ([#178](https://github.com/Zextras/carbonio-mailbox/issues/178)) ([7249cc7](https://github.com/Zextras/carbonio-mailbox/commit/7249cc75629c2fd7eed9b75d69bc164d70ff5153))
* [CO-539] add attributes to manage infrastructure notifications ([#156](https://github.com/Zextras/carbonio-mailbox/issues/156)) ([10f5c3c](https://github.com/Zextras/carbonio-mailbox/commit/10f5c3c5bfe375feaec43f5d4352b05f87e13e9f))
* [CO-546] rename producer id in calendar to CarbonioProdId for events ([#166](https://github.com/Zextras/carbonio-mailbox/issues/166)) ([9b155f2](https://github.com/Zextras/carbonio-mailbox/commit/9b155f2295bfdd90fe5763f44d341ac044b83bb9))

# [4.2.0](https://github.com/Zextras/carbonio-mailbox/compare/4.1.1...4.2.0) (2023-02-23)

### Bug Fixes

* [CO-491] add support for caldav schedule agent 'client', 'server' and 'none' ([#153](https://github.com/Zextras/carbonio-mailbox/issues/153)) ([01758e1](https://github.com/Zextras/carbonio-mailbox/commit/01758e197019f2023794e079a5e37af7c4cbe797))
* [CO-544] remove domain virtual hostnames if input empty string ([#150](https://github.com/Zextras/carbonio-mailbox/issues/150)) ([03c13f3](https://github.com/Zextras/carbonio-mailbox/commit/03c13f38d2b4c49f6c7c2539aef7ebc52466c6f5))

### Features

* [CO-335] Add issue LetsEncrypt certificate SOAP API Endpoint and related handler ([#154](https://github.com/Zextras/carbonio-mailbox/issues/154)) ([70653a5](https://github.com/Zextras/carbonio-mailbox/commit/70653a53cd4dee4933343d985beb601cc2028ca5))
* [CO-499] Remove both and mixed mode form web proxy config ([#155](https://github.com/Zextras/carbonio-mailbox/issues/155)) ([c7efafc](https://github.com/Zextras/carbonio-mailbox/commit/c7efafc86717e4e2bff46bdd8ad45f413eec3a06))
* [CO-513] Set sane defaults for postfix smtp server ([#151](https://github.com/Zextras/carbonio-mailbox/issues/151)) ([26d9e72](https://github.com/Zextras/carbonio-mailbox/commit/26d9e72cf7173f13507dc562ac050f6177177f97))
* [CO-559] remove docs common template expansion ([#149](https://github.com/Zextras/carbonio-mailbox/issues/149)) ([ea3bb3e](https://github.com/Zextras/carbonio-mailbox/commit/ea3bb3ea3f84aee2bf8337538e1b232c55c8c255))
* [CO-594] Add support for deeply nested attachments preview ([#157](https://github.com/Zextras/carbonio-mailbox/issues/157)) ([06ec40c](https://github.com/Zextras/carbonio-mailbox/commit/06ec40c4f8ac8997902505ba1ab29be69b2c8819))

# [4.1.1](https://github.com/Zextras/carbonio-mailbox/compare/4.1.0...4.1.1) (2023-01-31)

### Features

* [CO-477] Set default for 'catch-all messages' address in alias domains ([#138](https://github.com/Zextras/carbonio-mailbox/issues/138)) ([6ab1d69](https://github.com/Zextras/carbonio-mailbox/commit/6ab1d692dccd038d8987ebaf7351cd7cadea145e))
* [CO-486] Add Domain SSL certificate info SOAP API Endpoint ([#136](https://github.com/Zextras/carbonio-mailbox/issues/136)) ([fb303c5](https://github.com/Zextras/carbonio-mailbox/commit/fb303c5d40e00774c76d59cb4fe757d3ea238afb))
* [CO-495] add carbonioLogoUrl attribute ([#142](https://github.com/Zextras/carbonio-mailbox/issues/142)) ([ccf484e](https://github.com/Zextras/carbonio-mailbox/commit/ccf484e188279bfd8e5666a01cdbcee7012cb689))
* [CO-526] Prevent delegated admins creating DDLs ([#145](https://github.com/Zextras/carbonio-mailbox/issues/145)) ([9090306](https://github.com/Zextras/carbonio-mailbox/commit/9090306cd8714d3d2db59388379b520d2f3c76af))

# [4.1.0](https://github.com/Zextras/carbonio-mailbox/compare/4.0.18...4.1.0) (2022-12-15)

### Bug Fixes

* **generate-rights:** use custom localconfig ([#129](https://github.com/Zextras/carbonio-mailbox/issues/129)) ([30f13eb](https://github.com/Zextras/carbonio-mailbox/commit/30f13eb1dfeae7a2736dee09734dfdd46a1c1011))
* [CO-443] zimbraReverseProxyDnsLookupInServerEnabled false ([#118](https://github.com/Zextras/carbonio-mailbox/issues/118)) ([06ad272](https://github.com/Zextras/carbonio-mailbox/commit/06ad272a1b16cf0e3b58729a132000afd0cfe7a5))

### Features

* [CO-459] add new white-label management attributes ([#128](https://github.com/Zextras/carbonio-mailbox/issues/128)) ([ad5b67e](https://github.com/Zextras/carbonio-mailbox/commit/ad5b67eaa7e49ca94b3ab934a35dfad89e8681a3))
* [CO-467] add carbonioCalAVDatabaseCustomURL global multi-attr ([#132](https://github.com/Zextras/carbonio-mailbox/issues/132)) ([16f0dd0](https://github.com/Zextras/carbonio-mailbox/commit/16f0dd06f60dc5aaf14f403588303c262df79c6e))
* [CO-458] **attrs.xml:** zimbraClamAVDatabaseMirror multiAttr ([#130](https://github.com/Zextras/carbonio-mailbox/issues/130)) ([03e579c](https://github.com/Zextras/carbonio-mailbox/commit/03e579c2fec43b2097caf09f8f4997928a5b535f))
