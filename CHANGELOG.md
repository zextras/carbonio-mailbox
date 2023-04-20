
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
