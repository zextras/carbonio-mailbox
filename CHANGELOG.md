

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