Split Domain Setup
==================

We are often asked how to configure Zimbra Collaboration Suite so that
some accounts are moved/migrated to Zimbra but other accounts continue
to live on existing mail system.  This is possible IF your mail system
offers some of the mail routing features that the Zimbra offers.

Consider this example:

   - Your domain is example.com. 

   - Your existing mail infrastructure lives on the host
     "mail.example.com" (assuming single node install for simplicity -
     what we are discussing should also work on multi-node).

   - Your domain example.com has MX records that to point to host
     "mail.example.com".

   - Users "foo@example.com" and "bar@example.com" existed in the old
     system.

   - You have installed Zimbra on the host "zimbra.example.com" and
     created the domain "example.com" on it also.

   - You have migrated the mailbox of the user "foo@example.com" to
     the Zimbra system, using one of our migration tools documented
     elsewhere.

Some issues to consider as a result of such splitting your domain:

   - The end user "bar@example.com", who is not being migrated, will
     be unaware of the change insofar as regular email is concerned.
     Global address list (GAL) experience should also not change for
     this user.  There maybe some issues around group calendaring
     between the two systems (assuming your existing infrastructure
     supports group calendaring); we are working on these issues.

   - The end user "foo@example.com" will need to be educated and
     user's desktop software might need some minor configuration
     changes.  For user uses IMAP, s/he must be told to change IMAP
     server setting to where Zimbra is installed.  If s/he uses
     Outlook/MAPI, Zimbra MAPI plugin should be deployed.  If using
     webmail, the address of the new Zimbra Web Client address needs
     to be communicated to the user.

   - If "foo@example.com" is using a mail client that has outgoing
     SMTP configured with SMTP AUTH enabled, and SMTP AUTH
     functionality is required by your network topology or roaming
     users, then based on where authentication information lives (a
     decision you will make below), user may foo@example.com or may
     not require changing their outgoing SMTP setting.

   - You will have to pick either the Zimbra system or your existing
     system to be the "authoritative" system for your domain.  Let's
     call the authoritative system the master, and the other one the
     slave.  The master MTA must be aware of all accounts on the
     domain, and must reject on RCPT TO: those accounts that do not
     exist in either system.  The slave MTA must accept mail for
     accounts hosted in the slave system, and must forward mail for
     all other accounts on the domain to the master MTA.

   - Note that because of the above master/slave mail routing
     requirement, you will have to perform some type of provisioning
     step on both the slave and the master for an account that lives
     on the slave system.

   - For ease of administration you should keep any GAL on only one of
     these systems - avoids duplication.  However this requires one
     these systems be able to proxy to the other for GAL (Zimbra can
     use a non-Zimbra LDAP server).  If proxy is not possible some
     sync method needs to be written.
     
   - Zimbra is also able to authenticate users on a remote LDAP
     server, in case you have a portal or user page backed by LDAP
     where passwords are changed.  Note that, out of the box, Zimbra
     user's password will live in the Zimbra system.  If passwords
     live remotely, it is possible the old system can continue to SMTP
     authenticate users who live on the Zimbra system.  If that is the
     case, then a Zimbra user with SMTP AUTH can continue to use the
     old system's MTA as their MTA, assuming it is visible on the
     network.

Please read the following discussion of how to provision and configure
Zimbra as either a master or as a slave.

If you decide to use Zimbra as the master system, then you should also
carefully read the section on how Zimbra is configured as the slave -
this will show you what functionality is required of your existing
mail system so it can be a slave.  Vice versa if you are going to use
Zimbra as the slave system.

Configuring Zimbra as the Master
================================

Create corresponding accounts on the Zimbra system for all the
accounts that will live on the slave system.  Note that
bar@example.com is not migrating, and lives on the slave.

    $ zmprov ca bar@example.com <some_random_password>

Configure mail routing for this account so email flows to the slave
system for this account:

    $ zmprov ma bar@example.com zimbraMailTransport smtp:mail.example.com

Note that we are not using any catch all (akin to "luser_relay")
tricks here.  Master needs to be authoritative, and if it forwards
unknown accounts, then we would have a mail loop.

(Note to Zimbra: TBD: do we need to set any other attributes to make
this forwarding account on the Zimbra side more of a dummy?  Instead
of random password, it would better to have login disabled, but
zimbraMailStatus still has to read enabled.)

Change your MX record so mail from the cloud flows into the Zimbra MTA
first.  (This is the last step!  You will bounce mail if you make this
change before configuring the entire system and testing that mail flow
is working as desired.)

Configuring Zimbra as the Slave
===============================

The slave system needs to accept mail for accounts that live on the
slave, but must forward all other mail for accounts on this domain to
the master.

You can accomplish this piece of mail routing by saying:

    $ zmprov md example.com zimbraMailCatchAllAddress @zimbra.com
    $ zmprov md example.com zimbraMailCatchAllForwardingAddress @zimbra.com
    $ zmprov md example.com zimbraMailTransport smtp:mail.example.com

The first two commands (in combination) tell Zimbra postfix to accept
all addresses in the @zimbra.com domain as valid addresses.

The third command establishes default mail routing for the domain.

We also highly recommend that, in a slave Zimbra system, you turn off
DNS lookups and internet wide message routing from the slave host and
route all mail through the master.  You can accomplish this by:

    $ zmprov mcf zimbraMtaRelayHost mail.example.com
    $ zmprov mcf zimbraMtaDnsLookupsEnabled FALSE

Make sure to configure mail.example.com to accept mail relayed by
zimbra.example.com.

All these commands require that after running the whole set of them,
you do:

    $ postfix stop
    $ postfix start

Examples of Mail Flow
=====================

- Zimbra is master, mail sent from the internet, account lives on
  slave.

  - internet -> 
  - zimbra.example.com postfix server ->
  - account transport says to use smtp:mail.example.com ->
  - mail.example.com existing infrastructure MTA ->
  - mail.example.com existing infrastructure mail store

- Zimbra is slave and mail sent from foo@example.com (zimbra) ->
  bar@example.com (old system).  Assume foo is using Zimbra AJAX
  client.

  - foo's web browser ->
  - zimbra.example.com tomcat server ->
  - zimbra.example.com postfix server ->
  - account not local, finds domain transport setting ->
  - mail.example.com existing infrastructure MTA ->
  - mail.example.com existing infrastructure mail store
