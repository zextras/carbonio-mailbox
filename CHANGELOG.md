
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
