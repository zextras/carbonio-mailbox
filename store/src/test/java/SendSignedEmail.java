import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

import javax.mail.*;
import javax.mail.internet.*;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SendSignedEmail {
    public static void main(String[] args) throws Exception {
// Step 1: Set up mail session
        Properties props = new Properties();
        props.put("mail.smtp.host", "kc-dev5-u22-ce.demo.zextras.io");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("user@demo.zextras.io", "password");
            }
        });

        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) { }
                }
        };
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        SSLSocketFactory sslSocketFactory = sc.getSocketFactory();

        props.put("mail.smtp.ssl.socketFactory", sslSocketFactory);
        // Step 2: Load private key and certificate (from a .p12 file)
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        FileInputStream fis = new FileInputStream("/Users/soner/Downloads/user_certificate.p12");
        keystore.load(fis, "assext".toCharArray());

        String alias = "1";
        keystore.aliases().asIterator().forEachRemaining(System.out::println);
        PrivateKey privateKey = (PrivateKey) keystore.getKey(alias, "assext".toCharArray());
        X509Certificate cert = (X509Certificate) keystore.getCertificate(alias);

        // Step 3: Create the email message
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress("user@demo.zextras.io"));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress("user@demo.zextras.io"));
        message.setSubject("S/MIME Signed Email");

        // Step 4: Create the email content
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText("This is a signed message.");

        // Step 5: Generate S/MIME signature
        List<Certificate> certList = new ArrayList<>();
        certList.add(cert);
        JcaCertStore certs = new JcaCertStore(certList);

        CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(privateKey);
        generator.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(
                new JcaDigestCalculatorProviderBuilder().build())
                .build(signer, cert));
        generator.addCertificates(certs);

        CMSProcessableByteArray content = new CMSProcessableByteArray(textPart.getContent().toString().getBytes());
        CMSSignedData signedData = generator.generate(content, true);

        MimeBodyPart signaturePart = new MimeBodyPart();
        signaturePart.setContent(signedData.getEncoded(), "application/pkcs7-signature");
        signaturePart.addHeader("Content-Type", "application/pkcs7-signature; name=smime.p7s");
        signaturePart.addHeader("Content-Disposition", "attachment; filename=smime.p7s");
        signaturePart.addHeader("Content-Description", "S/MIME Cryptographic Signature");
        signaturePart.addHeader("Content-Transfer-Encoding", "base64");

        // Step 7: Create multipart for the signed message
        MimeMultipart multipart = new MimeMultipart("signed");
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(signaturePart);
        message.setContent(multipart);

        // Step 7: Send the email
        Transport.send(message);
        System.out.println("Email sent successfully with S/MIME signature.");
    }
}
