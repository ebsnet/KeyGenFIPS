package de.ebsnet.keygen;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.util.concurrent.Callable;
import org.bouncycastle.crypto.fips.FipsDRBG;
import org.bouncycastle.crypto.util.BasicEntropySourceProvider;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "KeyGenFIPS",
    mixinStandardHelpOptions = true,
    version = "1.0",
    description = "Generate Brainpool Keypairs in FIPS Mode")
public class KeyGen implements Callable<Void> {
  static {
    Security.addProvider(new BouncyCastleFipsProvider());
  }

  @Option(
      names = {"--out"},
      required = true,
      description = "Path to write the CSR to")
  private Path out;

  private static final byte[] PERSONALIZATION_STRING =
      "dies ist unser custom marker".getBytes(StandardCharsets.UTF_8);
  private static final byte[] NONCE = "TEAG/TEN/TMZ Key Gen Nonce".getBytes(StandardCharsets.UTF_8);

  public static void main(final String[] args) {
    new CommandLine(new KeyGen()).execute(args);
  }

  public Void call()
      throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException,
          IOException {
    final var kp = generateKeyPair();
    final var encoded = encodePEM(kp);
    Files.writeString(this.out, encoded, StandardOpenOption.CREATE_NEW);
    return null;
  }

  private static String encodePEM(final KeyPair kp) throws IOException {
    try (var sw = new StringWriter()) {
      try (var pw = new JcaPEMWriter(sw)) {
        pw.writeObject(kp);
      }
      final var pem = sw.toString();
      return pem;
    }
  }

  private static SecureRandom csprng() {
    final var entSource = new BasicEntropySourceProvider(new SecureRandom(), true);
    final var builder =
        FipsDRBG.SHA512_HMAC
            .fromEntropySource(entSource)
            .setSecurityStrength(256)
            .setEntropyBitsRequired(256)
            .setPersonalizationString(PERSONALIZATION_STRING);
    return builder.build(NONCE, true);
  }

  private static KeyPair generateKeyPair()
      throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
    final var gen = KeyPairGenerator.getInstance("EC", "BCFIPS");
    gen.initialize(new ECGenParameterSpec("brainpoolP256r1"), csprng());
    return gen.generateKeyPair();
  }
}
