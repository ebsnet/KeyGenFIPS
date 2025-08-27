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

/** Command to generate FIPS compatible Brainpool keys. */
@Command(
    name = "EBSnet KeyGenFIPS",
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Generate Brainpool Key Pairs in FIPS Mode")
public final class KeyGen implements Callable<Void> {
  static {
    Security.addProvider(new BouncyCastleFipsProvider());
  }

  private static final SecureRandom PRNG = new SecureRandom();

  @Option(
      names = {"--out"},
      required = true,
      description = "Path to store the generated key")
  private Path out;

  private static final byte[] PERSONALIZATION_STRING =
      "dies ist unser custom marker".getBytes(StandardCharsets.UTF_8);

  /**
   * Entrypoint
   *
   * @param args CLI arguments
   */
  public static void main(final String[] args) {
    new CommandLine(new KeyGen()).execute(args);
  }

  @Override
  public Void call()
      throws InvalidAlgorithmParameterException,
          NoSuchAlgorithmException,
          NoSuchProviderException,
          IOException {
    final var keyPair = generateKeyPair();
    final var encoded = encodePEM(keyPair);
    // this will fail, if the file under `out` already exists to prevent overwriting existing keys.
    Files.writeString(this.out, encoded, StandardOpenOption.CREATE_NEW);
    return null;
  }

  /**
   * PEM encode a {@link KeyPair}
   *
   * @param kp
   * @return
   * @throws IOException
   */
  private static String encodePEM(final KeyPair keyPair) throws IOException {
    try (var stringWriter = new StringWriter()) {
      try (var pemWriter = new JcaPEMWriter(stringWriter)) {
        pemWriter.writeObject(keyPair);
      }
      return stringWriter.toString();
    }
  }

  /**
   * Create a FIPS compatible cryptographically secure pseudo-random number generator
   *
   * @return
   */
  private static SecureRandom csprng() {
    final var entSource = new BasicEntropySourceProvider(new SecureRandom(), true);
    final var nonce = new byte[128];
    PRNG.nextBytes(nonce);
    final var builder =
        FipsDRBG.SHA512_HMAC
            .fromEntropySource(entSource)
            .setSecurityStrength(256)
            .setEntropyBitsRequired(256)
            .setPersonalizationString(PERSONALIZATION_STRING);
    return builder.build(nonce, true);
  }

  /**
   * Generate a {@code BrainpoolP256r1} keypair.
   *
   * @return
   * @throws NoSuchAlgorithmException
   * @throws NoSuchProviderException
   * @throws InvalidAlgorithmParameterException
   */
  private static KeyPair generateKeyPair()
      throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
    final var gen = KeyPairGenerator.getInstance("EC", "BCFIPS");
    gen.initialize(new ECGenParameterSpec("brainpoolP256r1"), csprng());
    return gen.generateKeyPair();
  }
}
