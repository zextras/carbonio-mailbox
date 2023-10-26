# Carbonio Mailbox SDK

A Java client for Carbonio Mailbox SOAP APIs.

## How to build

Build using maven:

```bash
mvn clean install
```

## Dependency installation

Add the following dependency to your `pom.xml` file.

```xml
<dependency>
    <groupId>zextras</groupId>
    <artifactId>carbonio-mailbox-sdk</artifactId>
    <version>23.11</version>
</dependency>
```

Make sure to use the right version for you or the latest one.

## Usage

First of all you need to create an instance of `MailboxClient` that points to the server that exposes the WSDL.

```java
  MailboxClient client =
      new MailboxClient.Builder()
          .withServer("http://localhost:8080")
          .build();
```

With the same builder you can specify not to check the certificate, useful in a test environment

```java
  MailboxClient client =
      new MailboxClient.Builder()
      // ...
      .trustAllCertificates()
      .build();
```

Now with the `MailboxClient` we can create a client for the `Service` SOAP endpoint.

```java
  ServiceClient serviceClient = client
    .newServiceClientBuilder()
    .withPool(5)
    .build();
```

As you can see it is possible to create a pool of instances. In a similar way it is possible to create a client for the `AdminService` SOAP endpoint.

```java
  AdminServiceClient adminServiceClient = client
    .newAdminServiceClientBuilder()
    .withPool(5)
    .build();
```

All that remains is to create a request, send it and receive the relative answer.

```java
final var request = ServiceRequests.AccountInfo.byId(id).withAuthToken(token);
final var accountInfo = serviceClient.send(request);
final var name = accountInfo.getName();
```

`ServiceRequests` offers a fluent API to easily build requests. Again, in a similar way there is `AdminServiceRequests` for admin requests.
<br />
The API does everything possible to use the Java Type System in order to avoid errors such as, for example, sending a `Service` request with the `AdminServiceClient` or sending requests without an authentication token if required.
<br />
Under the hood the all clients uses standard Java API for XML Web Services (JAX-WS) to create the SOAP clients and to send the requests.

## Examples

For more examples see `ExampleUsage` class in the [test](src/test/java/com/zextras/mailbox) folder or take a look to the test suite.